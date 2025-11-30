package io.github.uncleacc.yunlog.service.impl;

import io.github.uncleacc.yunlog.dto.auth.*;
import io.github.uncleacc.yunlog.entity.User;
import io.github.uncleacc.yunlog.exception.BusinessException;
import io.github.uncleacc.yunlog.repository.UserRepository;
import io.github.uncleacc.yunlog.service.AuthService;
import io.github.uncleacc.yunlog.service.CategoryService;
import io.github.uncleacc.yunlog.service.SmsService;
import io.github.uncleacc.yunlog.service.WechatService;
import io.github.uncleacc.yunlog.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 认证服务实现类
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final WechatService wechatService;
    private final CategoryService categoryService;
    private final SmsService smsService;
    
    @Override
    @Transactional
    public AuthResponse phoneLogin(PhoneLoginRequest request) {
        // 验证验证码
        if (!smsService.verifyCode(request.getPhone(), request.getCode())) {
            throw new BusinessException(401, "验证码错误或已过期");
        }
        
        // 查找用户，如果不存在则创建
        User user = userRepository.findByPhone(request.getPhone())
                .orElseGet(() -> {
                    // 创建新用户
                    User newUser = new User();
                    newUser.setPhone(request.getPhone());
                    newUser.setUsername("user_" + request.getPhone().substring(7)); // 使用手机号后4位生成用户名
                    newUser.setStatus(1); // 默认状态为正常
                    User savedUser = userRepository.save(newUser);
                    
                    // 为新用户创建默认分类
                    categoryService.createDefaultCategory(savedUser.getId());
                    
                    return savedUser;
                });
        
        // 生成token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        
        // 返回认证响应
        return AuthResponse.builder()
                .token(token)
                .userInfo(convertToUserInfoDTO(user))
                .build();
    }
    
    @Override
    @Transactional
    public AuthResponse wechatLogin(WechatLoginRequest request) {
        // 通过微信code获取openid
        String openid = wechatService.getOpenidByCode(request.getCode());
        
        // 查找是否已有用户绑定该openid
        User user = userRepository.findByWechatOpenid(openid)
                .orElseGet(() -> {
                    // 创建新用户
                    User newUser = new User();
                    newUser.setWechatOpenid(openid);
                    // 生成默认用户名，避免substring越界
                    String usernamePrefix = openid.length() >= 8 ? openid.substring(0, 8) : openid;
                    newUser.setUsername("wx_" + usernamePrefix);
                    newUser.setStatus(1); // 默认状态为正常
                    User savedUser = userRepository.save(newUser);
                    
                    // 为新用户创建默认分类
                    categoryService.createDefaultCategory(savedUser.getId());
                    
                    return savedUser;
                });
        
        // 生成token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        
        // 返回认证响应
        return AuthResponse.builder()
                .token(token)
                .userInfo(convertToUserInfoDTO(user))
                .build();
    }
    
    /**
     * 将User实体转换为UserInfoDTO
     */
    private UserInfoDTO convertToUserInfoDTO(User user) {
        return UserInfoDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .phone(user.getPhone())
                .wechatOpenid(user.getWechatOpenid())
                .build();
    }
}
