package io.github.uncleacc.yunlog.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 创建日记请求DTO
 */
@Data
public class CreateDiaryRequest {
    
    @Size(max = 200, message = "标题长度不能超过200个字符")
    private String title = "无标题";
    
    @NotBlank(message = "日记内容不能为空")
    private String content;
    
    private String contentHtml;
    
    @NotNull(message = "分类ID不能为空")
    private Long categoryId;
}