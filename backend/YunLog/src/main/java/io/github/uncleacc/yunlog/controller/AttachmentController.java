package io.github.uncleacc.yunlog.controller;

import io.github.uncleacc.yunlog.common.ApiResponse;
import io.github.uncleacc.yunlog.dto.request.CreateAttachmentRequest;
import io.github.uncleacc.yunlog.entity.Attachment;
import io.github.uncleacc.yunlog.service.AttachmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 附件控制器
 */
@Slf4j
@RestController
@RequestMapping("/attachments")
@RequiredArgsConstructor
public class AttachmentController {
    
    private final AttachmentService attachmentService;
    
    /**
     * 根据日记ID获取附件列表
     */
    @GetMapping("/diary/{diaryId}")
    public ApiResponse<List<Attachment>> getAttachmentsByDiaryId(@PathVariable Long diaryId) {
        List<Attachment> attachments = attachmentService.getAttachmentsByDiaryId(diaryId);
        return ApiResponse.success(attachments);
    }
    
    /**
     * 根据ID获取附件详情
     */
    @GetMapping("/{id}")
    public ApiResponse<Attachment> getAttachmentById(@PathVariable Long id) {
        Attachment attachment = attachmentService.getAttachmentById(id);
        return ApiResponse.success(attachment);
    }
    
    /**
     * 创建附件
     */
    @PostMapping
    public ApiResponse<Attachment> createAttachment(@Valid @RequestBody CreateAttachmentRequest request) {
        Attachment attachment = attachmentService.createAttachment(request);
        return ApiResponse.success("附件添加成功", attachment);
    }
    
    /**
     * 批量创建附件
     */
    @PostMapping("/batch")
    public ApiResponse<List<Attachment>> batchCreateAttachments(@RequestBody BatchCreateRequest request) {
        List<Attachment> attachments = attachmentService.batchCreateAttachments(
            request.getDiaryId(), 
            request.getUrls()
        );
        return ApiResponse.success("批量添加成功", attachments);
    }
    
    /**
     * 删除附件
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteAttachment(@PathVariable Long id) {
        attachmentService.deleteAttachment(id);
        return ApiResponse.success("附件删除成功", null);
    }
    
    /**
     * 根据日记ID删除所有附件
     */
    @DeleteMapping("/diary/{diaryId}")
    public ApiResponse<Void> deleteAttachmentsByDiaryId(@PathVariable Long diaryId) {
        attachmentService.deleteAttachmentsByDiaryId(diaryId);
        return ApiResponse.success("该日记的所有附件已删除", null);
    }
    
    /**
     * 批量删除附件
     */
    @DeleteMapping("/batch")
    public ApiResponse<Void> batchDeleteAttachments(@RequestBody BatchDeleteRequest request) {
        attachmentService.batchDeleteAttachments(request.getIds());
        return ApiResponse.success("批量删除成功", null);
    }
    
    /**
     * 批量创建请求类
     */
    public static class BatchCreateRequest {
        private Long diaryId;
        private List<String> urls;
        
        public Long getDiaryId() {
            return diaryId;
        }
        
        public void setDiaryId(Long diaryId) {
            this.diaryId = diaryId;
        }
        
        public List<String> getUrls() {
            return urls;
        }
        
        public void setUrls(List<String> urls) {
            this.urls = urls;
        }
    }
    
    /**
     * 批量删除请求类
     */
    public static class BatchDeleteRequest {
        private List<Long> ids;
        
        public List<Long> getIds() {
            return ids;
        }
        
        public void setIds(List<Long> ids) {
            this.ids = ids;
        }
    }
}
