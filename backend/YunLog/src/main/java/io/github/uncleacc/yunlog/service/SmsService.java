package io.github.uncleacc.yunlog.service;

/**
 * 短信服务接口
 * 定义短信发送的通用接口，方便切换不同的短信服务商
 */
public interface SmsService {
    
    /**
     * 发送验证码短信
     * @param phone 手机号
     * @param code 验证码
     * @return 是否发送成功
     */
    boolean sendVerifyCode(String phone, String code);
    
    /**
     * 验证验证码
     * @param phone 手机号
     * @param code 验证码
     * @return 是否验证成功
     */
    boolean verifyCode(String phone, String code);
    
    /**
     * 生成验证码
     * @return 6位数字验证码
     */
    String generateCode();
}
