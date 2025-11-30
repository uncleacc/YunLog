package io.github.uncleacc.yunlog.context;

/**
 * 用户上下文 - 使用ThreadLocal存储当前请求的用户信息
 */
public class UserContext {
    
    private static final ThreadLocal<Long> userIdHolder = new ThreadLocal<>();
    
    /**
     * 设置当前用户ID
     * 
     * @param userId 用户ID
     */
    public static void setUserId(Long userId) {
        userIdHolder.set(userId);
    }
    
    /**
     * 获取当前用户ID
     * 
     * @return 用户ID
     */
    public static Long getUserId() {
        return userIdHolder.get();
    }
    
    /**
     * 清除用户上下文
     */
    public static void clear() {
        userIdHolder.remove();
    }
}
