package io.github.uncleacc.yunlog.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 创建附件请求DTO
 */
@Data
public class CreateAttachmentRequest {
    
    @NotNull(message = "日记ID不能为空")
    private Long diaryId;
    
    @NotBlank(message = "图片URL不能为空")
    @Size(max = 500, message = "URL长度不能超过500个字符")
    private String url;
}
