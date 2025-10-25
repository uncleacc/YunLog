package io.github.uncleacc.yunlog.controller;

import io.github.uncleacc.yunlog.common.ApiResponse;
import io.github.uncleacc.yunlog.common.PageResponse;
import io.github.uncleacc.yunlog.dto.request.CreateDiaryRequest;
import io.github.uncleacc.yunlog.dto.request.UpdateDiaryTimeRequest;
import io.github.uncleacc.yunlog.entity.Diary;
import io.github.uncleacc.yunlog.service.DiaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 日记控制器
 */
@Slf4j
@RestController
@RequestMapping("/diaries")
@RequiredArgsConstructor
public class DiaryController {
    
    private final DiaryService diaryService;
    
    /**
     * 获取日记列表
     */
    @GetMapping
    public ApiResponse<PageResponse<Diary>> getDiaryList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword) {
        
        PageResponse<Diary> diaries;
        if (keyword != null && !keyword.trim().isEmpty()) {
            // 搜索日记
            diaries = diaryService.searchDiaries(keyword, page, limit);
        } else {
            // 普通查询
            diaries = diaryService.getDiaryList(page, limit, categoryId);
        }
        
        return ApiResponse.success(diaries);
    }
    
    /**
     * 获取日记详情
     */
    @GetMapping("/{id}")
    public ApiResponse<Diary> getDiaryById(@PathVariable Long id) {
        Diary diary = diaryService.getDiaryById(id);
        return ApiResponse.success(diary);
    }
    
    /**
     * 创建日记
     */
    @PostMapping
    public ApiResponse<Diary> createDiary(@Valid @RequestBody CreateDiaryRequest diaryRequest) {
        Diary diary = diaryService.createDiary(diaryRequest);
        return ApiResponse.created(diary);
    }
    
    /**
     * 更新日记
     */
    @PutMapping("/{id}")
    public ApiResponse<Diary> updateDiary(@PathVariable Long id,
                                          @Valid @RequestBody CreateDiaryRequest diaryRequest) {
        Diary diary = diaryService.updateDiary(id, diaryRequest);
        return ApiResponse.success(diary);
    }
    
    /**
     * 更新日记时间
     */
    @PutMapping("/{id}/time")
    public ApiResponse<Diary> updateDiaryTime(@PathVariable Long id,
                                              @Valid @RequestBody UpdateDiaryTimeRequest timeRequest) {
        Diary diary = diaryService.updateDiaryTime(id, timeRequest);
        return ApiResponse.success(diary);
    }
    
    /**
     * 删除日记（移到垃圾桶）
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteDiary(@PathVariable Long id) {
        diaryService.deleteDiary(id);
        return ApiResponse.success("已移到垃圾桶", null);
    }
    
    /**
     * 批量删除日记
     */
    @DeleteMapping("/batch")
    public ApiResponse<Void> batchDeleteDiaries(@RequestBody BatchDeleteRequest deleteRequest) {
        diaryService.batchDeleteDiaries(deleteRequest.getIds());
        return ApiResponse.success("已批量移到垃圾桶", null);
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