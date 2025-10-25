package io.github.uncleacc.yunlog.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云 OSS 配置
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class OssConfig {
    
    private final OssProperties ossProperties;
    
    /**
     * 创建 OSS 客户端
     */
    @Bean
    public OSS ossClient() {
        log.info("初始化阿里云 OSS 客户端: endpoint={}, bucket={}", 
            ossProperties.getEndpoint(), 
            ossProperties.getBucketName());
        
        return new OSSClientBuilder().build(
            ossProperties.getEndpoint(),
            ossProperties.getAccessKeyId(),
            ossProperties.getAccessKeySecret()
        );
    }
}
