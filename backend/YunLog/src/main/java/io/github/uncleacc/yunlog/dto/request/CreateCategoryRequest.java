package io.github.uncleacc.yunlog.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 创建分类请求DTO
 */
@Data
public class CreateCategoryRequest {
    
    @NotBlank(message = "分类名称不能为空")
    @Size(min = 1, max = 10, message = "分类名称长度为1-10个字符")
    private String name;
    
    @Size(max = 50, message = "图标长度不能超过50个字符")
    private String icon;
    
    @Size(max = 20, message = "颜色值长度不能超过20个字符")
    private String color = "#FF9A76";
}