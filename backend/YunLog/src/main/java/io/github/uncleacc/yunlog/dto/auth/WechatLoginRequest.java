package io.github.uncleacc.yunlog.dto.auth;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 微信登录请求DTO
 */
@Data
public class WechatLoginRequest {
    
    @NotBlank(message = "微信授权码不能为空")
    private String code;
}
