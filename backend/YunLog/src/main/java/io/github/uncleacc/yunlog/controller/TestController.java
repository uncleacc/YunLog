package io.github.uncleacc.yunlog.controller;

import io.github.uncleacc.yunlog.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试控制器
 */
@RestController
public class TestController {
    
    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("云日记API服务运行正常");
    }
}