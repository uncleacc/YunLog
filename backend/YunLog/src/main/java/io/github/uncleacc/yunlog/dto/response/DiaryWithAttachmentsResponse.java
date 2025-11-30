package io.github.uncleacc.yunlog.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.github.uncleacc.yunlog.entity.Attachment;
import io.github.uncleacc.yunlog.entity.Diary;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 包含附件信息的日记响应DTO
 */
@Data
public class DiaryWithAttachmentsResponse {
    
    private Long id;
    private String content;
    private String contentHtml;
    private Long categoryId;
    private Boolean isDeleted;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deletedTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    
    /**
     * 附件列表
     */
    private List<Attachment> attachments;
    
    /**
     * 从 Diary 实体创建响应对象
     */
    public static DiaryWithAttachmentsResponse from(Diary diary, List<Attachment> attachments) {
        DiaryWithAttachmentsResponse response = new DiaryWithAttachmentsResponse();
        response.setId(diary.getId());
        response.setContent(diary.getContent());
        response.setContentHtml(diary.getContentHtml());
        response.setCategoryId(diary.getCategoryId());
        response.setIsDeleted(diary.getIsDeleted());
        response.setDeletedTime(diary.getDeletedTime());
        response.setCreateTime(diary.getCreateTime());
        response.setUpdateTime(diary.getUpdateTime());
        response.setAttachments(attachments);
        return response;
    }
}
