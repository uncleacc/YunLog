package io.github.uncleacc.yunlog.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 认证响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    /**
     * JWT token
     */
    private String token;
    
    /**
     * 用户信息
     */
    private UserInfoDTO userInfo;
}
