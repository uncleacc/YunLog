package io.github.uncleacc.yunlog.common;

import lombok.Data;

import java.util.List;

/**
 * 分页响应数据
 */
@Data
public class PageResponse<T> {
    
    /**
     * 数据列表
     */
    private List<T> list;
    
    /**
     * 总数量
     */
    private Long total;
    
    /**
     * 当前页码
     */
    private Integer page;
    
    /**
     * 每页条数
     */
    private Integer limit;
    
    /**
     * 总页数
     */
    private Long totalPages;
    
    public PageResponse() {}
    
    public PageResponse(List<T> list, Long total, Integer page, Integer limit) {
        this.list = list;
        this.total = total;
        this.page = page;
        this.limit = limit;
        this.totalPages = (total + limit - 1) / limit;
    }
    
    public static <T> PageResponse<T> of(List<T> list, Long total, Integer page, Integer limit) {
        return new PageResponse<>(list, total, page, limit);
    }
}