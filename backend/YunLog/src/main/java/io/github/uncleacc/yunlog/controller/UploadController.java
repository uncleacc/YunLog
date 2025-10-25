package io.github.uncleacc.yunlog.controller;

import io.github.uncleacc.yunlog.common.ApiResponse;
import io.github.uncleacc.yunlog.dto.request.CreateAttachmentRequest;
import io.github.uncleacc.yunlog.entity.Attachment;
import io.github.uncleacc.yunlog.service.AttachmentService;
import io.github.uncleacc.yunlog.service.OssService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件上传控制器
 */
@Slf4j
@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadController {
    
    private final OssService ossService;
    private final AttachmentService attachmentService;
    
    /**
     * 上传单个图片
     * 
     * @param file 图片文件
     * @param diaryId 日记ID（可选，如果提供则自动创建附件记录）
     * @return 图片 URL
     */
    @PostMapping("/image")
    public ApiResponse<Map<String, Object>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "diaryId", required = false) Long diaryId) {
        
        log.info("上传图片: 文件名={}, 大小={}KB, 日记ID={}", 
            file.getOriginalFilename(), 
            file.getSize() / 1024, 
            diaryId);
        
        // 上传到 OSS
        String url = ossService.uploadFile(file);
        
        // 如果提供了日记ID，自动创建附件记录
        Attachment attachment = null;
        if (diaryId != null) {
            CreateAttachmentRequest request = new CreateAttachmentRequest();
            request.setDiaryId(diaryId);
            request.setUrl(url);
            attachment = attachmentService.createAttachment(request);
        }
        
        // 构建响应
        Map<String, Object> result = new HashMap<>();
        result.put("url", url);
        if (attachment != null) {
            result.put("attachmentId", attachment.getId());
            result.put("diaryId", attachment.getDiaryId());
        }
        
        return ApiResponse.success("图片上传成功", result);
    }
    
    /**
     * 批量上传图片
     * 
     * @param files 图片文件数组
     * @param diaryId 日记ID（可选，如果提供则自动创建附件记录）
     * @return 图片 URL 列表
     */
    @PostMapping("/images")
    public ApiResponse<Map<String, Object>> uploadImages(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "diaryId", required = false) Long diaryId) {
        
        log.info("批量上传图片: 数量={}, 日记ID={}", files.length, diaryId);
        
        // 上传到 OSS
        List<String> urls = ossService.uploadFiles(files);
        
        // 如果提供了日记ID，自动创建附件记录
        List<Attachment> attachments = null;
        if (diaryId != null) {
            attachments = attachmentService.batchCreateAttachments(diaryId, urls);
        }
        
        // 构建响应
        Map<String, Object> result = new HashMap<>();
        result.put("urls", urls);
        result.put("count", urls.size());
        if (attachments != null) {
            List<Long> attachmentIds = new ArrayList<>();
            for (Attachment attachment : attachments) {
                attachmentIds.add(attachment.getId());
            }
            result.put("attachmentIds", attachmentIds);
            result.put("diaryId", diaryId);
        }
        
        return ApiResponse.success("批量上传成功", result);
    }
    
    /**
     * 仅上传图片到 OSS（不创建附件记录）
     * 用于在编辑器中临时上传图片
     * 
     * @param file 图片文件
     * @return 图片 URL
     */
    @PostMapping("/temp-image")
    public ApiResponse<Map<String, String>> uploadTempImage(
            @RequestParam("file") MultipartFile file) {
        
        log.info("临时上传图片: 文件名={}, 大小={}KB", 
            file.getOriginalFilename(), 
            file.getSize() / 1024);
        
        // 仅上传到 OSS，不创建附件记录
        String url = ossService.uploadFile(file);
        
        Map<String, String> result = new HashMap<>();
        result.put("url", url);
        
        return ApiResponse.success("临时图片上传成功", result);
    }
    
    /**
     * 删除 OSS 文件
     * 
     * @param url 文件 URL
     */
    @DeleteMapping("/file")
    public ApiResponse<Void> deleteFile(@RequestParam("url") String url) {
        log.info("删除文件: {}", url);
        ossService.deleteFile(url);
        return ApiResponse.success("文件删除成功", null);
    }
}
