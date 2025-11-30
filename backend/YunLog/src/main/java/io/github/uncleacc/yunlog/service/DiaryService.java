package io.github.uncleacc.yunlog.service;

import io.github.uncleacc.yunlog.common.PageResponse;
import io.github.uncleacc.yunlog.context.UserContext;
import io.github.uncleacc.yunlog.dto.request.CreateDiaryRequest;
import io.github.uncleacc.yunlog.dto.request.UpdateDiaryTimeRequest;
import io.github.uncleacc.yunlog.dto.response.DiaryWithAttachmentsResponse;
import io.github.uncleacc.yunlog.entity.Attachment;
import io.github.uncleacc.yunlog.entity.Diary;
import io.github.uncleacc.yunlog.exception.BusinessException;
import io.github.uncleacc.yunlog.repository.AttachmentRepository;
import io.github.uncleacc.yunlog.repository.CategoryRepository;
import io.github.uncleacc.yunlog.repository.DiaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 日记服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DiaryService {
    
    private final DiaryRepository diaryRepository;
    private final CategoryRepository categoryRepository;
    private final AttachmentRepository attachmentRepository;
    
    /**
     * 分页获取日记列表（包含附件）
     */
    public PageResponse<DiaryWithAttachmentsResponse> getDiaryList(Integer page, Integer limit, Long categoryId) {
        Long userId = UserContext.getUserId();
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Diary> diaryPage;
        
        if (categoryId != null) {
            // 验证分类是否存在并属于当前用户
            categoryRepository.findById(categoryId)
                .filter(category -> category.getUserId().equals(userId))
                .orElseThrow(() -> new BusinessException(404, "分类不存在"));
            
            diaryPage = diaryRepository.findByUserIdAndCategoryIdAndIsDeletedFalseOrderByCreateTimeDesc(
                userId, categoryId, pageable);
        } else {
            diaryPage = diaryRepository.findByUserIdAndIsDeletedFalseOrderByCreateTimeDesc(
                userId, pageable);
        }
        
        // 为每个日记加载附件
        List<DiaryWithAttachmentsResponse> diariesWithAttachments = diaryPage.getContent().stream()
            .map(diary -> {
                List<Attachment> attachments = attachmentRepository.findByDiaryIdOrderByCreateTimeAsc(diary.getId());
                return DiaryWithAttachmentsResponse.from(diary, attachments);
            })
            .collect(Collectors.toList());
        
        return PageResponse.of(
            diariesWithAttachments,
            diaryPage.getTotalElements(),
            page,
            limit
        );
    }
    
    /**
     * 根据ID获取日记详情
     */
    public Diary getDiaryById(Long id) {
        Long userId = UserContext.getUserId();
        return diaryRepository.findByIdAndIsDeletedFalse(id)
            .filter(diary -> diary.getUserId().equals(userId))
            .orElseThrow(() -> new BusinessException(404, "日记不存在"));
    }
    
    /**
     * 创建日记
     */
    @Transactional
    public Diary createDiary(CreateDiaryRequest request) {
        Long userId = UserContext.getUserId();
        
        // 验证分类是否存在并属于当前用户
        categoryRepository.findById(request.getCategoryId())
            .filter(category -> category.getUserId().equals(userId))
            .orElseThrow(() -> new BusinessException(404, "分类不存在"));
        
        Diary diary = new Diary();
        diary.setContent(request.getContent());
        diary.setContentHtml(request.getContentHtml());
        diary.setCategoryId(request.getCategoryId());
        diary.setUserId(userId);
        diary.setIsDeleted(false);
        
        return diaryRepository.save(diary);
    }
    
    /**
     * 更新日记
     */
    @Transactional
    public Diary updateDiary(Long id, CreateDiaryRequest request) {
        Diary diary = getDiaryById(id);
        
        // 如果要更换分类,验证新分类是否存在
        if (!diary.getCategoryId().equals(request.getCategoryId())) {
            categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BusinessException(404, "分类不存在"));
        }
        
        diary.setContent(request.getContent());
        diary.setContentHtml(request.getContentHtml());
        diary.setCategoryId(request.getCategoryId());
        
        return diaryRepository.save(diary);
    }
    
    /**
     * 更新日记时间
     */
    @Transactional
    public Diary updateDiaryTime(Long id, UpdateDiaryTimeRequest request) {
        Diary diary = getDiaryById(id);
        
        // 直接设置createTime字段，忽略@CreationTimestamp注解
        diary.setCreateTime(request.getCreateTime());
        
        return diaryRepository.save(diary);
    }
    
    /**
     * 删除日记（移到垃圾桶）
     */
    @Transactional
    public void deleteDiary(Long id) {
        Diary diary = getDiaryById(id);
        
        diary.setIsDeleted(true);
        diary.setDeletedTime(LocalDateTime.now());
        
        diaryRepository.save(diary);
    }
    
    /**
     * 批量删除日记
     */
    @Transactional
    public void batchDeleteDiaries(List<Long> ids) {
        for (Long id : ids) {
            deleteDiary(id);
        }
    }
    
    /**
     * 获取垃圾桶列表
     */
    public PageResponse<Diary> getTrashList(Integer page, Integer limit) {
        Long userId = UserContext.getUserId();
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Diary> diaryPage = diaryRepository.findByUserIdAndIsDeletedTrueOrderByDeletedTimeDesc(userId, pageable);
        
        return PageResponse.of(
            diaryPage.getContent(),
            diaryPage.getTotalElements(),
            page,
            limit
        );
    }
    
    /**
     * 从垃圾桶恢复日记
     */
    @Transactional
    public void restoreFromTrash(Long id) {
        Diary diary = diaryRepository.findByIdAndIsDeletedTrue(id)
            .orElseThrow(() -> new BusinessException(404, "垃圾桶中不存在该日记"));
        
        diary.setIsDeleted(false);
        diary.setDeletedTime(null);
        
        diaryRepository.save(diary);
    }
    
    /**
     * 永久删除日记
     */
    @Transactional
    public void permanentDeleteDiary(Long id) {
        Diary diary = diaryRepository.findByIdAndIsDeletedTrue(id)
            .orElseThrow(() -> new BusinessException(404, "垃圾桶中不存在该日记"));
        
        // 删除日记的所有附件
        attachmentRepository.deleteByDiaryId(id);
        
        // 删除日记
        diaryRepository.delete(diary);
    }
    
    /**
     * 清空垃圾桶
     */
    @Transactional
    public void clearTrash() {
        Long userId = UserContext.getUserId();
        Page<Diary> trashDiaries = diaryRepository.findByUserIdAndIsDeletedTrueOrderByDeletedTimeDesc(
            userId, PageRequest.of(0, Integer.MAX_VALUE));
        
        // 删除所有垃圾桶中日记的附件
        for (Diary diary : trashDiaries.getContent()) {
            attachmentRepository.deleteByDiaryId(diary.getId());
        }
        
        // 删除所有垃圾桶中的日记
        diaryRepository.deleteAll(trashDiaries.getContent());
    }
    
    /**
     * 搜索日记（包含附件）
     */
    public PageResponse<DiaryWithAttachmentsResponse> searchDiaries(String keyword, Integer page, Integer limit) {
        Long userId = UserContext.getUserId();
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Diary> diaryPage = diaryRepository.searchDiaries(userId, keyword, pageable);
        
        // 为每个日记加载附件
        List<DiaryWithAttachmentsResponse> diariesWithAttachments = diaryPage.getContent().stream()
            .map(diary -> {
                List<Attachment> attachments = attachmentRepository.findByDiaryIdOrderByCreateTimeAsc(diary.getId());
                return DiaryWithAttachmentsResponse.from(diary, attachments);
            })
            .collect(Collectors.toList());
        
        return PageResponse.of(
            diariesWithAttachments,
            diaryPage.getTotalElements(),
            page,
            limit
        );
    }
}