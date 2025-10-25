package io.github.uncleacc.yunlog.service;

import io.github.uncleacc.yunlog.dto.request.CreateCategoryRequest;
import io.github.uncleacc.yunlog.entity.Category;
import io.github.uncleacc.yunlog.entity.Diary;
import io.github.uncleacc.yunlog.exception.BusinessException;
import io.github.uncleacc.yunlog.repository.CategoryRepository;
import io.github.uncleacc.yunlog.repository.DiaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 分类服务 - 无用户认证版本
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {
    
    private final CategoryRepository categoryRepository;
    private final DiaryRepository diaryRepository;
    
    /**
     * 获取分类列表
     */
    public List<Category> getCategoryList() {
        return categoryRepository.findAllByOrderByCreateTimeAsc();
    }
    
    /**
     * 根据ID获取分类详情
     */
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
            .orElseThrow(() -> new BusinessException(404, "分类不存在"));
    }
    
    /**
     * 创建分类
     */
    @Transactional
    public Category createCategory(CreateCategoryRequest request) {
        // 检查分类名称是否已存在
        if (categoryRepository.existsByName(request.getName())) {
            throw new BusinessException(400, "分类名称已存在");
        }
        
        Category category = new Category();
        category.setName(request.getName());
        category.setIcon(request.getIcon());
        category.setColor(request.getColor());
        category.setIsDefault(false);
        
        return categoryRepository.save(category);
    }
    
    /**
     * 更新分类
     */
    @Transactional
    public Category updateCategory(Long id, CreateCategoryRequest request) {
        Category category = getCategoryById(id);
        
        // 如果是默认分类，不允许修改名称
        if (category.getIsDefault() && !category.getName().equals(request.getName())) {
            throw new BusinessException(400, "默认分类名称不可修改");
        }
        
        // 如果修改名称且不是默认分类，检查是否重名
        if (!category.getName().equals(request.getName())) {
            if (categoryRepository.existsByName(request.getName())) {
                throw new BusinessException(400, "分类名称已存在");
            }
            // 只有非默认分类才允许修改名称
            category.setName(request.getName());
        }
        
        // 图标和颜色所有分类都可以修改
        category.setIcon(request.getIcon());
        category.setColor(request.getColor());
        
        return categoryRepository.save(category);
    }
    
    /**
     * 删除分类
     */
    @Transactional
    public void deleteCategory(Long id) {
        Category category = getCategoryById(id);
        
        // 不能删除默认分类
        if (category.getIsDefault()) {
            throw new BusinessException(400, "默认分类不能删除");
        }
        
        // 获取默认分类
        Category defaultCategory = categoryRepository.findByIsDefaultTrue()
            .orElseThrow(() -> new BusinessException(500, "默认分类不存在"));
        
        // 查找该分类下的所有日记（包括已删除和未删除）
        List<Diary> diaries = diaryRepository.findByCategoryId(id);
        
        log.info("删除分类 [{}]，需要处理 {} 条日记", category.getName(), diaries.size());
        
        // 更新所有日记的分类为默认分类
        for (Diary diary : diaries) {
            diary.setCategoryId(defaultCategory.getId());
            
            // 如果日记未被删除，将其移到垃圾桶
            if (!diary.getIsDeleted()) {
                diary.setIsDeleted(true);
                diary.setDeletedTime(LocalDateTime.now());
            }
            // 如果日记已在垃圾桶，只更新分类ID
        }
        
        // 批量保存更新后的日记
        diaryRepository.saveAll(diaries);
        
        // 删除分类
        categoryRepository.delete(category);
        
        log.info("分类 [{}] 删除成功，共处理 {} 条日记", category.getName(), diaries.size());
    }
    
    /**
     * 创建默认分类
     */
    @Transactional
    public void createDefaultCategory() {
        Category defaultCategory = new Category();
        defaultCategory.setName("默认分类");
        defaultCategory.setIcon("📝");
        defaultCategory.setColor("#FF9A76");
        defaultCategory.setIsDefault(true);
        
        categoryRepository.save(defaultCategory);
    }
    
    /**
     * 获取分类统计信息
     */
    public CategoryStatsResponse getCategoryStats(Long id) {
        // 验证分类是否存在
        getCategoryById(id);
        
        long totalCount = diaryRepository.countByCategoryIdAndIsDeletedFalse(id);
        
        CategoryStatsResponse stats = new CategoryStatsResponse();
        stats.setTotalCount(totalCount);
        stats.setThisMonth(0L); // TODO: 实现本月统计
        stats.setThisWeek(0L);  // TODO: 实现本周统计
        
        // 获取最近的日记
        diaryRepository.findFirstByCategoryIdAndIsDeletedFalseOrderByCreateTimeDesc(id)
            .ifPresent(diary -> {
                CategoryStatsResponse.RecentDiary recentDiary = new CategoryStatsResponse.RecentDiary();
                recentDiary.setId(diary.getId());
                recentDiary.setTitle(diary.getTitle());
                recentDiary.setCreateTime(diary.getCreateTime());
                stats.setRecentDiary(recentDiary);
            });
        
        return stats;
    }
    
    /**
     * 分类统计响应类
     */
    public static class CategoryStatsResponse {
        private Long totalCount;
        private Long thisMonth;
        private Long thisWeek;
        private RecentDiary recentDiary;
        
        // getters and setters
        public Long getTotalCount() { return totalCount; }
        public void setTotalCount(Long totalCount) { this.totalCount = totalCount; }
        
        public Long getThisMonth() { return thisMonth; }
        public void setThisMonth(Long thisMonth) { this.thisMonth = thisMonth; }
        
        public Long getThisWeek() { return thisWeek; }
        public void setThisWeek(Long thisWeek) { this.thisWeek = thisWeek; }
        
        public RecentDiary getRecentDiary() { return recentDiary; }
        public void setRecentDiary(RecentDiary recentDiary) { this.recentDiary = recentDiary; }
        
        public static class RecentDiary {
            private Long id;
            private String title;
            private java.time.LocalDateTime createTime;
            
            // getters and setters
            public Long getId() { return id; }
            public void setId(Long id) { this.id = id; }
            
            public String getTitle() { return title; }
            public void setTitle(String title) { this.title = title; }
            
            public java.time.LocalDateTime getCreateTime() { return createTime; }
            public void setCreateTime(java.time.LocalDateTime createTime) { this.createTime = createTime; }
        }
    }
}