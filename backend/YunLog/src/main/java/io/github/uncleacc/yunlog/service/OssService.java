package io.github.uncleacc.yunlog.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.CreateBucketRequest;
import com.aliyun.oss.model.PutObjectRequest;
import io.github.uncleacc.yunlog.config.OssProperties;
import io.github.uncleacc.yunlog.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * 阿里云 OSS 文件上传服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OssService {
    
    private final OSS ossClient;
    private final OssProperties ossProperties;
    
    /**
     * 初始化时检查并创建 Bucket
     */
    @PostConstruct
    public void init() {
        String bucketName = ossProperties.getBucketName();
        try {
            // 检查 Bucket 是否存在
            if (!ossClient.doesBucketExist(bucketName)) {
                log.warn("Bucket [{}] 不存在，正在创建...", bucketName);
                
                // 创建 Bucket
                CreateBucketRequest createBucketRequest = new CreateBucketRequest(bucketName);
                // 设置公共读权限（图片可以直接通过 URL 访问）
                createBucketRequest.setCannedACL(CannedAccessControlList.PublicRead);
                
                ossClient.createBucket(createBucketRequest);
                log.info("Bucket [{}] 创建成功", bucketName);
            } else {
                log.info("Bucket [{}] 已存在", bucketName);
            }
        } catch (Exception e) {
            log.error("初始化 OSS Bucket 失败: {}", e.getMessage());
            log.warn("请手动在阿里云控制台创建 Bucket: {}", bucketName);
        }
    }
    
    /**
     * 上传文件到 OSS
     * 
     * @param file 上传的文件
     * @return 文件访问 URL
     */
    public String uploadFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "上传文件不能为空");
        }
        
        // 获取原始文件名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new BusinessException(400, "文件名不能为空");
        }
        
        // 验证文件类型（只允许图片）
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException(400, "只支持上传图片文件");
        }
        
        // 验证文件大小（最大 10MB）
        long maxSize = 10 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new BusinessException(400, "文件大小不能超过 10MB");
        }
        
        try {
            // 生成唯一文件名
            String fileName = generateFileName(originalFilename);
            
            // 构建完整的文件路径
            String objectName = ossProperties.getFolder() + fileName;
            
            // 上传文件到 OSS
            InputStream inputStream = file.getInputStream();
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                ossProperties.getBucketName(), 
                objectName, 
                inputStream
            );
            
            ossClient.putObject(putObjectRequest);
            
            // 构建文件访问 URL
            String fileUrl = ossProperties.getUrlPrefix() + objectName;
            
            log.info("文件上传成功: {} -> {}", originalFilename, fileUrl);
            
            return fileUrl;
            
        } catch (IOException e) {
            log.error("文件上传失败: {}", originalFilename, e);
            throw new BusinessException(500, "文件上传失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量上传文件
     * 
     * @param files 上传的文件数组
     * @return 文件访问 URL 列表
     */
    public java.util.List<String> uploadFiles(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            throw new BusinessException(400, "上传文件不能为空");
        }
        
        java.util.List<String> urls = new java.util.ArrayList<>();
        for (MultipartFile file : files) {
            String url = uploadFile(file);
            urls.add(url);
        }
        
        return urls;
    }
    
    /**
     * 删除 OSS 文件
     * 
     * @param fileUrl 文件 URL
     */
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }
        
        try {
            // 从 URL 中提取对象名称
            String objectName = extractObjectName(fileUrl);
            
            // 删除文件
            ossClient.deleteObject(ossProperties.getBucketName(), objectName);
            
            log.info("文件删除成功: {}", fileUrl);
            
        } catch (Exception e) {
            log.error("文件删除失败: {}", fileUrl, e);
            // 删除失败不抛异常，只记录日志
        }
    }
    
    /**
     * 批量删除文件
     * 
     * @param fileUrls 文件 URL 列表
     */
    public void deleteFiles(java.util.List<String> fileUrls) {
        if (fileUrls == null || fileUrls.isEmpty()) {
            return;
        }
        
        for (String fileUrl : fileUrls) {
            deleteFile(fileUrl);
        }
    }
    
    /**
     * 生成唯一文件名
     * 格式: yyyyMMdd/UUID.扩展名
     */
    private String generateFileName(String originalFilename) {
        // 获取文件扩展名
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
        }
        
        // 生成日期路径
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String datePath = sdf.format(new Date());
        
        // 生成唯一文件名
        String uuid = UUID.randomUUID().toString().replace("-", "");
        
        return datePath + "/" + uuid + extension;
    }
    
    /**
     * 从 URL 中提取对象名称
     */
    private String extractObjectName(String fileUrl) {
        String urlPrefix = ossProperties.getUrlPrefix();
        if (fileUrl.startsWith(urlPrefix)) {
            return fileUrl.substring(urlPrefix.length());
        }
        
        // 如果不是标准 URL，尝试从最后一个斜杠后提取
        int lastSlashIndex = fileUrl.lastIndexOf("/");
        if (lastSlashIndex > 0) {
            return ossProperties.getFolder() + fileUrl.substring(lastSlashIndex + 1);
        }
        
        return fileUrl;
    }
}
