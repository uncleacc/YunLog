package io.github.uncleacc.yunlog.dto.request;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 批量更新分类排序请求
 */
@Data
public class UpdateCategorySortRequest {
    
    @NotNull(message = "排序列表不能为空")
    @Valid
    private List<CategorySortItem> categorySortList;
    
    /**
     * 分类排序项
     */
    @Data
    public static class CategorySortItem {
        
        @NotNull(message = "分类ID不能为空")
        private Long id;
        
        @NotNull(message = "排序值不能为空")
        @Min(value = 0, message = "排序值不能小于0")
        private Integer sortOrder;
    }
}
