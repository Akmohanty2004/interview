package com.interviewprep.repository;

import com.interviewprep.entity.MockTest;
import com.interviewprep.entity.MockTestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MockTestRepository extends JpaRepository<MockTest, Long> {

    List<MockTest> findAllByOrderByCreatedAtDesc();

    List<MockTest> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<MockTest> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, MockTestStatus status);

    long countByUserIdAndStatus(Long userId, MockTestStatus status);

    long countByStatus(MockTestStatus status);

    @Query("SELECT AVG(m.percentage) FROM MockTest m WHERE m.user.id = :userId AND m.status = com.interviewprep.entity.MockTestStatus.COMPLETED")
    Double getAveragePercentageByUserId(@Param("userId") Long userId);

    @Query("SELECT MAX(m.percentage) FROM MockTest m WHERE m.user.id = :userId AND m.status = com.interviewprep.entity.MockTestStatus.COMPLETED")
    Double getMaxPercentageByUserId(@Param("userId") Long userId);

    @Query("SELECT AVG(m.accuracy) FROM MockTest m WHERE m.user.id = :userId AND m.status = com.interviewprep.entity.MockTestStatus.COMPLETED")
    Double getAverageAccuracyByUserId(@Param("userId") Long userId);

    @Query("SELECT AVG(m.percentage) FROM MockTest m WHERE m.status = com.interviewprep.entity.MockTestStatus.COMPLETED")
    Double getOverallAveragePercentage();

    @Query("SELECT m FROM MockTest m WHERE m.status = com.interviewprep.entity.MockTestStatus.COMPLETED ORDER BY m.createdAt DESC")
    List<MockTest> findRecentCompletedTests();

    List<MockTest> findTop10ByStatusOrderByCreatedAtDesc(MockTestStatus status);
}
