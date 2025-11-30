package io.github.uncleacc.yunlog.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.uncleacc.yunlog.exception.BusinessException;
import io.github.uncleacc.yunlog.service.WechatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * 微信服务实现类
 */
@Slf4j
@Service
public class WechatServiceImpl implements WechatService {
    
    @Value("${wechat.appid}")
    private String appid;
    
    @Value("${wechat.secret}")
    private String secret;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private static final String WECHAT_API_URL = "https://api.weixin.qq.com/sns/jscode2session?appid={appid}&secret={secret}&js_code={code}&grant_type=authorization_code";
    
    @Override
    public String getOpenidByCode(String code) {
        try {
            // 检查配置
            if ("your-wechat-appid".equals(appid) || appid == null || appid.isEmpty()) {
                log.warn("微信AppID未配置，使用模拟登录");
                // 开发环境返回模拟openid
                return "mock_openid_" + System.currentTimeMillis();
            }
            
            // 调用微信API
            String url = WECHAT_API_URL
                    .replace("{appid}", appid)
                    .replace("{secret}", secret)
                    .replace("{code}", code);
            
            String response = restTemplate.getForObject(url, String.class);
            log.info("微信登录响应: {}", response);
            
            // 解析响应
            JsonNode jsonNode = objectMapper.readTree(response);
            
            // 检查是否有错误
            if (jsonNode.has("errcode")) {
                int errcode = jsonNode.get("errcode").asInt();
                if (errcode != 0) {
                    String errmsg = jsonNode.get("errmsg").asText();
                    log.error("微信登录失败: errcode={}, errmsg={}", errcode, errmsg);
                    throw new BusinessException(500, "微信登录失败: " + errmsg);
                }
            }
            
            // 获取openid
            if (jsonNode.has("openid")) {
                return jsonNode.get("openid").asText();
            } else {
                throw new BusinessException(500, "获取openid失败");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("微信登录异常", e);
            throw new BusinessException(500, "微信登录失败: " + e.getMessage());
        }
    }
}
