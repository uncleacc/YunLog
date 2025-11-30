package io.github.uncleacc.yunlog.interceptor;

import io.github.uncleacc.yunlog.context.UserContext;
import io.github.uncleacc.yunlog.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JWT拦截器 - 验证token并设置用户上下文
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {
    
    private final JwtUtil jwtUtil;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // OPTIONS请求直接放行
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }
        
        // 获取token
        String token = extractToken(request);
        
        // 验证token
        if (token == null || !jwtUtil.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"未授权，请先登录\"}");
            return false;
        }
        
        // 从token中获取用户ID并设置到上下文
        try {
            Long userId = jwtUtil.getUserIdFromToken(token);
            UserContext.setUserId(userId);
            log.debug("用户ID {} 通过JWT验证", userId);
            return true;
        } catch (Exception e) {
            log.error("解析token失败", e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"token无效\"}");
            return false;
        }
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清除用户上下文
        UserContext.clear();
    }
    
    /**
     * 从请求头中提取token
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
