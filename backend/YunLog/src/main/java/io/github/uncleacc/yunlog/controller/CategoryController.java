package io.github.uncleacc.yunlog.controller;

import io.github.uncleacc.yunlog.common.ApiResponse;
import io.github.uncleacc.yunlog.dto.request.CreateCategoryRequest;
import io.github.uncleacc.yunlog.dto.request.UpdateCategorySortRequest;
import io.github.uncleacc.yunlog.entity.Category;
import io.github.uncleacc.yunlog.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 分类控制器 - 无用户认证版本
 */
@Slf4j
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {
    
    private final CategoryService categoryService;
    
    /**
     * 获取分类列表
     */
    @GetMapping
    public ApiResponse<List<Category>> getCategoryList() {
        List<Category> categories = categoryService.getCategoryList();
        return ApiResponse.success(categories);
    }
    
    /**
     * 获取分类详情
     */
    @GetMapping("/{id}")
    public ApiResponse<Category> getCategoryById(@PathVariable Long id) {
        Category category = categoryService.getCategoryById(id);
        return ApiResponse.success(category);
    }
    
    /**
     * 创建分类
     */
    @PostMapping
    public ApiResponse<Category> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        Category category = categoryService.createCategory(request);
        return ApiResponse.created(category);
    }
    
    /**
     * 更新分类
     */
    @PutMapping("/{id}")
    public ApiResponse<Category> updateCategory(@PathVariable Long id,
                                                @Valid @RequestBody CreateCategoryRequest request) {
        Category category = categoryService.updateCategory(id, request);
        return ApiResponse.success(category);
    }
    
    /**
     * 删除分类
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ApiResponse.success();
    }
    
    /**
     * 获取分类统计
     */
    @GetMapping("/{id}/stats")
    public ApiResponse<CategoryService.CategoryStatsResponse> getCategoryStats(@PathVariable Long id) {
        CategoryService.CategoryStatsResponse stats = categoryService.getCategoryStats(id);
        return ApiResponse.success(stats);
    }
    
    /**
     * 批量更新分类排序
     */
    @PutMapping("/sort")
    public ApiResponse<Void> updateCategorySort(@Valid @RequestBody UpdateCategorySortRequest request) {
        categoryService.updateCategorySort(request.getCategorySortList());
        return ApiResponse.success();
    }
}