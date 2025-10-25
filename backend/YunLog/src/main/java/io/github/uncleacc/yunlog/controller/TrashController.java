package io.github.uncleacc.yunlog.controller;

import io.github.uncleacc.yunlog.common.ApiResponse;
import io.github.uncleacc.yunlog.common.PageResponse;
import io.github.uncleacc.yunlog.entity.Diary;
import io.github.uncleacc.yunlog.service.DiaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 垃圾桶控制器 - 无用户认证版本
 */
@Slf4j
@RestController
@RequestMapping("/trash")
@RequiredArgsConstructor
public class TrashController {
    
    private final DiaryService diaryService;
    
    /**
     * 获取垃圾桶列表
     */
    @GetMapping
    public ApiResponse<PageResponse<Diary>> getTrashList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer limit) {
        
        PageResponse<Diary> trashDiaries = diaryService.getTrashList(page, limit);
        return ApiResponse.success(trashDiaries);
    }
    
    /**
     * 恢复日记
     */
    @PostMapping("/{id}/restore")
    public ApiResponse<Void> restoreFromTrash(@PathVariable Long id) {
        diaryService.restoreFromTrash(id);
        return ApiResponse.success("恢复成功", null);
    }
    
    /**
     * 永久删除日记
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> permanentDeleteDiary(@PathVariable Long id) {
        diaryService.permanentDeleteDiary(id);
        return ApiResponse.success("永久删除成功", null);
    }
    
    /**
     * 清空垃圾桶
     */
    @DeleteMapping("/clear")
    public ApiResponse<Void> clearTrash() {
        diaryService.clearTrash();
        return ApiResponse.success("垃圾桶已清空", null);
    }
    
    /**
     * 批量恢复
     */
    @PostMapping("/batch-restore")
    public ApiResponse<Void> batchRestore(@RequestBody BatchRestoreRequest restoreRequest) {
        
        for (Long id : restoreRequest.getIds()) {
            diaryService.restoreFromTrash(id);
        }
        
        return ApiResponse.success("批量恢复成功", null);
    }
    
    /**
     * 批量恢复请求类
     */
    public static class BatchRestoreRequest {
        private List<Long> ids;
        
        public List<Long> getIds() {
            return ids;
        }
        
        public void setIds(List<Long> ids) {
            this.ids = ids;
        }
    }
}