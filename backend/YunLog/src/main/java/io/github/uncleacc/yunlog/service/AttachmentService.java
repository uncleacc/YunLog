package io.github.uncleacc.yunlog.service;

import io.github.uncleacc.yunlog.dto.request.CreateAttachmentRequest;
import io.github.uncleacc.yunlog.entity.Attachment;
import io.github.uncleacc.yunlog.exception.BusinessException;
import io.github.uncleacc.yunlog.repository.AttachmentRepository;
import io.github.uncleacc.yunlog.repository.DiaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 附件服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AttachmentService {
    
    private final AttachmentRepository attachmentRepository;
    private final DiaryRepository diaryRepository;
    private final OssService ossService;
    
    /**
     * 根据日记ID获取附件列表
     */
    public List<Attachment> getAttachmentsByDiaryId(Long diaryId) {
        // 验证日记是否存在
        if (!diaryRepository.existsById(diaryId)) {
            throw new BusinessException(404, "日记不存在");
        }
        
        return attachmentRepository.findByDiaryIdOrderByCreateTimeAsc(diaryId);
    }
    
    /**
     * 根据ID获取附件详情
     */
    public Attachment getAttachmentById(Long id) {
        return attachmentRepository.findById(id)
            .orElseThrow(() -> new BusinessException(404, "附件不存在"));
    }
    
    /**
     * 创建附件
     */
    @Transactional
    public Attachment createAttachment(CreateAttachmentRequest request) {
        // 验证日记是否存在
        if (!diaryRepository.existsById(request.getDiaryId())) {
            throw new BusinessException(404, "日记不存在");
        }
        
        Attachment attachment = new Attachment();
        attachment.setDiaryId(request.getDiaryId());
        attachment.setUrl(request.getUrl());
        
        return attachmentRepository.save(attachment);
    }
    
    /**
     * 批量创建附件
     */
    @Transactional
    public List<Attachment> batchCreateAttachments(Long diaryId, List<String> urls) {
        // 验证日记是否存在
        if (!diaryRepository.existsById(diaryId)) {
            throw new BusinessException(404, "日记不存在");
        }
        
        List<Attachment> attachments = new java.util.ArrayList<>();
        for (String url : urls) {
            Attachment attachment = new Attachment();
            attachment.setDiaryId(diaryId);
            attachment.setUrl(url);
            attachments.add(attachment);
        }
        
        return attachmentRepository.saveAll(attachments);
    }
    
    /**
     * 删除附件
     */
    @Transactional
    public void deleteAttachment(Long id) {
        Attachment attachment = getAttachmentById(id);
        
        // 先从 OSS 删除文件
        try {
            ossService.deleteFile(attachment.getUrl());
        } catch (Exception e) {
            log.error("删除 OSS 文件失败: {}", attachment.getUrl(), e);
            // 继续删除数据库记录
        }
        
        // 再删除数据库记录
        attachmentRepository.delete(attachment);
    }
    
    /**
     * 根据日记ID删除所有附件
     */
    @Transactional
    public void deleteAttachmentsByDiaryId(Long diaryId) {
        // 先查询所有附件
        List<Attachment> attachments = attachmentRepository.findByDiaryIdOrderByCreateTimeAsc(diaryId);
        
        // 从 OSS 删除所有文件
        for (Attachment attachment : attachments) {
            try {
                ossService.deleteFile(attachment.getUrl());
            } catch (Exception e) {
                log.error("删除 OSS 文件失败: {}", attachment.getUrl(), e);
            }
        }
        
        // 再删除数据库记录
        attachmentRepository.deleteByDiaryId(diaryId);
    }
    
    /**
     * 批量删除附件
     */
    @Transactional
    public void batchDeleteAttachments(List<Long> ids) {
        for (Long id : ids) {
            deleteAttachment(id);
        }
    }
}
