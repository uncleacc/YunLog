package io.github.uncleacc.yunlog.service;

import io.github.uncleacc.yunlog.dto.auth.*;

/**
 * 认证服务接口
 */
public interface AuthService {
    
    /**
     * 手机验证码登录
     * 
     * @param request 手机登录请求
     * @return 认证响应（包含token和用户信息）
     */
    AuthResponse phoneLogin(PhoneLoginRequest request);
    
    /**
     * 微信登录
     * 
     * @param request 微信登录请求
     * @return 认证响应（包含token和用户信息）
     */
    AuthResponse wechatLogin(WechatLoginRequest request);
}
