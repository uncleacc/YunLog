package io.github.uncleacc.yunlog.repository;

import io.github.uncleacc.yunlog.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 附件数据访问层
 */
@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    
    /**
     * 根据日记ID查找所有附件
     */
    List<Attachment> findByDiaryIdOrderByCreateTimeAsc(Long diaryId);
    
    /**
     * 根据日记ID删除所有附件
     */
    void deleteByDiaryId(Long diaryId);
    
    /**
     * 统计指定日记的附件数量
     */
    long countByDiaryId(Long diaryId);
}
