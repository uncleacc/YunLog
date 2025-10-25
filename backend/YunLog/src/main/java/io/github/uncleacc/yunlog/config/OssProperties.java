package io.github.uncleacc.yunlog.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云 OSS 配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "aliyun.oss")
public class OssProperties {
    
    /**
     * OSS 端点
     */
    private String endpoint;
    
    /**
     * Access Key ID
     */
    private String accessKeyId;
    
    /**
     * Access Key Secret
     */
    private String accessKeySecret;
    
    /**
     * Bucket 名称
     */
    private String bucketName;
    
    /**
     * 文件访问 URL 前缀
     */
    private String urlPrefix;
    
    /**
     * 上传文件夹路径
     */
    private String folder;
}
