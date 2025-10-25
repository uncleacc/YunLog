package io.github.uncleacc.yunlog.repository;

import io.github.uncleacc.yunlog.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 分类数据访问层 - 无用户认证版本
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    /**
     * 查找所有分类列表
     */
    List<Category> findAllByOrderByCreateTimeAsc();
    
    /**
     * 查找默认分类
     */
    Optional<Category> findByIsDefaultTrue();
    
    /**
     * 检查是否存在指定名称的分类
     */
    boolean existsByName(String name);
    
    /**
     * 统计分类数量
     */
    long count();
}