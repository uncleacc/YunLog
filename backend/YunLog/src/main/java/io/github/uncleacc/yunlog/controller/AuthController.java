package io.github.uncleacc.yunlog.controller;

import io.github.uncleacc.yunlog.common.ApiResponse;
import io.github.uncleacc.yunlog.dto.auth.*;
import io.github.uncleacc.yunlog.service.AuthService;
import io.github.uncleacc.yunlog.service.SmsService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {
    
    private final AuthService authService;
    private final SmsService smsService;
    
    /**
     * 发送短信验证码
     */
    @PostMapping("/sms/send")
    public ApiResponse<String> sendSmsCode(@Valid @RequestBody SendSmsRequest request) {
        String code = smsService.generateCode();
        boolean success = smsService.sendVerifyCode(request.getPhone(), code);
        
        if (success) {
            return ApiResponse.success("验证码已发送");
        } else {
            return ApiResponse.error("验证码发送失败，请稍后重试");
        }
    }
    
    /**
     * 手机验证码登录
     */
    @PostMapping("/phone/login")
    public ApiResponse<AuthResponse> phoneLogin(@Valid @RequestBody PhoneLoginRequest request) {
        AuthResponse response = authService.phoneLogin(request);
        return ApiResponse.success("登录成功", response);
    }
    
    /**
     * 微信登录
     */
    @PostMapping("/wechat/login")
    public ApiResponse<AuthResponse> wechatLogin(@Valid @RequestBody WechatLoginRequest request) {
        AuthResponse response = authService.wechatLogin(request);
        return ApiResponse.success("登录成功", response);
    }
    
    /**
     * 登出（客户端删除token即可，服务端无需处理）
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        return ApiResponse.success();
    }
}
