package io.github.uncleacc.yunlog.repository;

import io.github.uncleacc.yunlog.entity.Diary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 日记数据访问层
 */
@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> {
    
    /**
     * 分页查询日记列表（未删除）- 按用户ID过滤
     */
    Page<Diary> findByUserIdAndIsDeletedFalseOrderByCreateTimeDesc(Long userId, Pageable pageable);
    
    /**
     * 根据分类ID分页查询日记列表（未删除）- 按用户ID过滤
     */
    Page<Diary> findByUserIdAndCategoryIdAndIsDeletedFalseOrderByCreateTimeDesc(Long userId, Long categoryId, Pageable pageable);
    
    /**
     * 根据日记ID查找日记（未删除）
     */
    Optional<Diary> findByIdAndIsDeletedFalse(Long id);
    
    /**
     * 查找垃圾桶中的日记列表 - 按用户ID过滤
     */
    Page<Diary> findByUserIdAndIsDeletedTrueOrderByDeletedTimeDesc(Long userId, Pageable pageable);
    
    /**
     * 根据日记ID查找垃圾桶中的日记
     */
    Optional<Diary> findByIdAndIsDeletedTrue(Long id);
    
    /**
     * 统计日记总数（未删除）- 按用户ID过滤
     */
    long countByUserIdAndIsDeletedFalse(Long userId);
    
    /**
     * 统计分类下的日记数量（未删除）- 按用户ID过滤
     */
    long countByUserIdAndCategoryIdAndIsDeletedFalse(Long userId, Long categoryId);
    
    /**
     * 根据分类ID查找最近的日记
     */
    Optional<Diary> findFirstByCategoryIdAndIsDeletedFalseOrderByCreateTimeDesc(Long categoryId);
    
    /**
     * 根据分类ID查找所有日记（包括已删除和未删除）
     */
    List<Diary> findByCategoryId(Long categoryId);
    
    /**
     * 搜索日记（内容）- 按用户ID过滤
     */
    @Query("SELECT d FROM Diary d WHERE d.userId = :userId AND d.isDeleted = false AND " +
           "d.content LIKE %:keyword% " +
           "ORDER BY d.createTime DESC")
    Page<Diary> searchDiaries(@Param("userId") Long userId, @Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 根据时间范围查询日记
     */
    @Query("SELECT d FROM Diary d WHERE d.isDeleted = false AND " +
           "d.createTime BETWEEN :startTime AND :endTime ORDER BY d.createTime DESC")
    Page<Diary> findByCreateTimeBetween(@Param("startTime") LocalDateTime startTime, 
                                       @Param("endTime") LocalDateTime endTime, 
                                       Pageable pageable);
    
    /**
     * 清理过期的垃圾桶日记
     */
    @Query("DELETE FROM Diary d WHERE d.isDeleted = true AND d.deletedTime < :expireTime")
    void deleteExpiredTrashDiaries(@Param("expireTime") LocalDateTime expireTime);
}