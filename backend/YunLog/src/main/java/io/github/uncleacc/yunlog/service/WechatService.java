package io.github.uncleacc.yunlog.service;

/**
 * 微信服务接口
 */
public interface WechatService {
    
    /**
     * 通过code获取微信openid
     * 
     * @param code 微信授权码
     * @return openid
     */
    String getOpenidByCode(String code);
}
