package io.github.uncleacc.yunlog.service.impl;

import io.github.uncleacc.yunlog.service.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 阿里云短信服务实现
 * 使用阿里云短信服务发送验证码
 */
@Service
public class AliyunSmsServiceImpl implements SmsService {
    
    private static final Logger log = LoggerFactory.getLogger(AliyunSmsServiceImpl.class);
    
    // 验证码在Redis中的key前缀
    private static final String SMS_CODE_PREFIX = "sms:code:";
    
    // 验证码有效期（分钟）
    private static final int CODE_EXPIRE_MINUTES = 5;
    
    // 阿里云短信配置
    @Value("${aliyun.sms.access-key-id:}")
    private String accessKeyId;
    
    @Value("${aliyun.sms.access-key-secret:}")
    private String accessKeySecret;
    
    @Value("${aliyun.sms.sign-name:}")
    private String signName;
    
    @Value("${aliyun.sms.template-code:}")
    private String templateCode;
    
    @Value("${aliyun.sms.enabled:false}")
    private boolean smsEnabled;
    
    private final StringRedisTemplate redisTemplate;
    
    public AliyunSmsServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    @Override
    public boolean sendVerifyCode(String phone, String code) {
        try {
            // 如果短信服务未启用，则使用模拟模式（仅存储验证码）
            if (!smsEnabled) {
                log.info("短信服务未启用，使用模拟模式。手机号: {}, 验证码: {}", phone, code);
                // 将验证码存入Redis
                String key = SMS_CODE_PREFIX + phone;
                redisTemplate.opsForValue().set(key, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
                return true;
            }
            
            // TODO: 集成阿里云短信SDK
            // 1. 引入阿里云短信SDK依赖
            // 2. 使用以下代码发送短信：
            /*
            DefaultProfile profile = DefaultProfile.getProfile(
                "cn-hangzhou",  // 地域ID
                accessKeyId,
                accessKeySecret
            );
            IAcsClient client = new DefaultAcsClient(profile);
            
            SendSmsRequest request = new SendSmsRequest();
            request.setPhoneNumbers(phone);
            request.setSignName(signName);
            request.setTemplateCode(templateCode);
            request.setTemplateParam("{\"code\":\"" + code + "\"}");
            
            SendSmsResponse response = client.getAcsResponse(request);
            
            if ("OK".equals(response.getCode())) {
                // 发送成功，将验证码存入Redis
                String key = SMS_CODE_PREFIX + phone;
                redisTemplate.opsForValue().set(key, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
                log.info("短信发送成功，手机号: {}", phone);
                return true;
            } else {
                log.error("短信发送失败，手机号: {}, 错误: {}", phone, response.getMessage());
                return false;
            }
            */
            
            // 临时实现：仅存储验证码到Redis
            String key = SMS_CODE_PREFIX + phone;
            redisTemplate.opsForValue().set(key, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
            log.info("验证码已生成（模拟发送），手机号: {}, 验证码: {}", phone, code);
            return true;
            
        } catch (Exception e) {
            log.error("发送短信验证码失败，手机号: {}", phone, e);
            return false;
        }
    }
    
    @Override
    public boolean verifyCode(String phone, String code) {
        try {
            String key = SMS_CODE_PREFIX + phone;
            String storedCode = redisTemplate.opsForValue().get(key);
            
            if (storedCode == null) {
                log.warn("验证码不存在或已过期，手机号: {}", phone);
                return false;
            }
            
            boolean isValid = storedCode.equals(code);
            
            if (isValid) {
                // 验证成功后删除验证码
                redisTemplate.delete(key);
                log.info("验证码验证成功，手机号: {}", phone);
            } else {
                log.warn("验证码错误，手机号: {}", phone);
            }
            
            return isValid;
            
        } catch (Exception e) {
            log.error("验证验证码失败，手机号: {}", phone, e);
            return false;
        }
    }
    
    @Override
    public String generateCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}
