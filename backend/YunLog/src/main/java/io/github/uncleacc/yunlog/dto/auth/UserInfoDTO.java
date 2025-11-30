package io.github.uncleacc.yunlog.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户信息DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDTO {
    
    private Long id;
    
    private String username;
    
    private String phone;
    
    private String wechatOpenid;
}
