package io.github.uncleacc.yunlog.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Entity
@Table(name = "user")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    /**
     * 用户ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 用户名
     */
    @Column(name = "username", length = 50)
    private String username;
    
    /**
     * 手机号
     */
    @Column(name = "phone", unique = true, length = 20)
    private String phone;
    
    /**
     * 微信OpenID
     */
    @Column(name = "wechat_openid", unique = true, length = 100)
    private String wechatOpenid;
    
    /**
     * 微信UnionID（预留）
     */
    @Column(name = "wechat_union_id", unique = true, length = 100)
    private String wechatUnionId;
    
    /**
     * 昵称
     */
    @Column(name = "nickname", length = 50)
    private String nickname;
    
    /**
     * 头像URL
     */
    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;
    
    /**
     * 状态：1-正常 0-禁用
     */
    @Column(name = "status", columnDefinition = "TINYINT DEFAULT 1")
    private Integer status;
    
    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
