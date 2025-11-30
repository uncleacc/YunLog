package io.github.uncleacc.yunlog.repository;

import io.github.uncleacc.yunlog.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户仓库接口
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 根据微信OpenID查找用户
     */
    Optional<User> findByWechatOpenid(String wechatOpenid);
    
    /**
     * 根据手机号查找用户
     */
    Optional<User> findByPhone(String phone);
    
    /**
     * 检查用户名是否已存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 检查手机号是否已存在
     */
    boolean existsByPhone(String phone);
}
