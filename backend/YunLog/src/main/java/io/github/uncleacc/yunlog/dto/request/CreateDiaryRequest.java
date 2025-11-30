package io.github.uncleacc.yunlog.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 创建日记请求DTO
 */
@Data
public class CreateDiaryRequest {
    
    @NotBlank(message = "日记内容不能为空")
    private String content;
    
    private String contentHtml;
    
    @NotNull(message = "分类ID不能为空")
    private Long categoryId;
}