package io.github.uncleacc.yunlog.repository;

import io.github.uncleacc.yunlog.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 分类数据访问层
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    /**
     * 查找所有分类列表（按排序字段）- 按用户ID过滤
     */
    List<Category> findByUserIdOrderBySortOrderAscCreateTimeAsc(Long userId);
    
    /**
     * 查找默认分类 - 按用户ID过滤
     */
    Optional<Category> findByUserIdAndIsDefaultTrue(Long userId);
    
    /**
     * 检查是否存在指定名称的分类 - 按用户ID过滤
     */
    boolean existsByUserIdAndName(Long userId, String name);
    
    /**
     * 统计分类数量 - 按用户ID过滤
     */
    long countByUserId(Long userId);
}