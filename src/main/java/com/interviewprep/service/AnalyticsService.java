package com.interviewprep.service;

import com.interviewprep.dto.*;
import com.interviewprep.entity.*;
import com.interviewprep.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    private final MockTestRepository mockTestRepository;
    private final MockQuestionRepository mockQuestionRepository;
    private final SubjectRepository subjectRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final AuthService authService;

    public AnalyticsService(MockTestRepository mockTestRepository,
                            MockQuestionRepository mockQuestionRepository,
                            SubjectRepository subjectRepository,
                            QuestionRepository questionRepository,
                            UserRepository userRepository,
                            UserBadgeRepository userBadgeRepository,
                            AuthService authService) {
        this.mockTestRepository = mockTestRepository;
        this.mockQuestionRepository = mockQuestionRepository;
        this.subjectRepository = subjectRepository;
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
        this.userBadgeRepository = userBadgeRepository;
        this.authService = authService;
    }

    @Transactional(readOnly = true)
    public StudentDashboardDto getStudentDashboardData() {
        User user = authService.getCurrentAuthenticatedUser();
        StudentDashboardDto dto = new StudentDashboardDto();

        long totalTests = mockTestRepository.countByUserIdAndStatus(user.getId(), MockTestStatus.COMPLETED);
        Double avgScore = mockTestRepository.getAveragePercentageByUserId(user.getId());
        Double maxScore = mockTestRepository.getMaxPercentageByUserId(user.getId());
        Double avgAccuracy = mockTestRepository.getAverageAccuracyByUserId(user.getId());
        Long attemptedQuestions = mockQuestionRepository.countAttemptedQuestionsByUserId(user.getId());

        dto.setTotalTests(totalTests);
        dto.setAverageScore(avgScore != null ? Math.round(avgScore * 10.0) / 10.0 : 0.0);
        dto.setBestScore(maxScore != null ? Math.round(maxScore * 10.0) / 10.0 : 0.0);
        dto.setAccuracy(avgAccuracy != null ? Math.round(avgAccuracy * 10.0) / 10.0 : 0.0);
        dto.setQuestionsSolved(attemptedQuestions != null ? attemptedQuestions : 0L);
        dto.setStreakDays(user.getStreakDays());

        List<UserBadge> badges = userBadgeRepository.findByUserIdOrderByEarnedAtDesc(user.getId());
        dto.setBadgesCount(badges.size());

        List<MockTest> recentTests = mockTestRepository.findByUserIdAndStatusOrderByCreatedAtDesc(user.getId(), MockTestStatus.COMPLETED);
        dto.setRecentTests(recentTests.stream()
                .limit(5)
                .map(MockTestSummaryDto::fromEntity)
                .collect(Collectors.toList()));

        dto.setWeakTopics(detectWeakTopics(user.getId()));
        dto.setSubjectPerformances(getSubjectPerformance(user.getId()));

        return dto;
    }

    @Transactional(readOnly = true)
    public List<WeakTopicDto> detectWeakTopics(Long userId) {
        List<MockQuestion> attemptedQuestions = mockQuestionRepository.findAllByUserId(userId);
        if (attemptedQuestions.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, List<MockQuestion>> topicMap = new HashMap<>();
        for (MockQuestion mq : attemptedQuestions) {
            if (!Boolean.TRUE.equals(mq.getIsSkipped()) && mq.getQuestion().getTopic() != null) {
                topicMap.computeIfAbsent(mq.getQuestion().getTopic().getId(), k -> new ArrayList<>()).add(mq);
            }
        }

        List<WeakTopicDto> weakTopics = new ArrayList<>();
        for (Map.Entry<Long, List<MockQuestion>> entry : topicMap.entrySet()) {
            List<MockQuestion> list = entry.getValue();
            if (list.isEmpty()) continue;

            int total = list.size();
            long correct = list.stream().filter(mq -> Boolean.TRUE.equals(mq.getIsCorrect())).count();
            double acc = ((double) correct / total) * 100.0;
            acc = Math.round(acc * 10.0) / 10.0;

            if (acc < 70.0 || total >= 3) {
                Topic topic = list.get(0).getQuestion().getTopic();
                String rec = generateTopicRecommendation(topic.getName(), acc);
                weakTopics.add(new WeakTopicDto(
                        topic.getId(),
                        topic.getName(),
                        topic.getSubject().getId(),
                        topic.getSubject().getName(),
                        total,
                        (int) correct,
                        acc,
                        rec
                ));
            }
        }

        // Sort ascending by accuracy (weakest first)
        weakTopics.sort(Comparator.comparingDouble(WeakTopicDto::getAccuracyPercentage));
        return weakTopics.stream().limit(5).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SubjectPerformanceDto> getSubjectPerformance(Long userId) {
        List<Subject> subjects = subjectRepository.findByActiveTrue();
        List<MockTest> userTests = mockTestRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, MockTestStatus.COMPLETED);

        Map<Long, List<MockTest>> testsBySubject = userTests.stream()
                .collect(Collectors.groupingBy(t -> t.getSubject().getId()));

        List<SubjectPerformanceDto> list = new ArrayList<>();
        for (Subject sub : subjects) {
            List<MockTest> subTests = testsBySubject.getOrDefault(sub.getId(), Collections.emptyList());
            int testsTaken = subTests.size();
            int totalAttempted = 0;
            int totalCorrect = 0;

            for (MockTest t : subTests) {
                totalCorrect += t.getCorrectCount();
                totalAttempted += (t.getCorrectCount() + t.getWrongCount());
            }

            double acc = totalAttempted > 0 ? ((double) totalCorrect / totalAttempted) * 100.0 : 0.0;
            acc = Math.round(acc * 10.0) / 10.0;

            list.add(new SubjectPerformanceDto(
                    sub.getId(),
                    sub.getName(),
                    sub.getIcon(),
                    sub.getColor(),
                    testsTaken,
                    totalAttempted,
                    totalCorrect,
                    acc
            ));
        }

        return list;
    }

    @Transactional(readOnly = true)
    public AdminDashboardDto getAdminDashboardData() {
        AdminDashboardDto dto = new AdminDashboardDto();
        dto.setTotalStudents(userRepository.countByRole(Role.ROLE_STUDENT));
        dto.setTotalQuestions(questionRepository.countByActiveTrue());
        dto.setTotalMockTests(mockTestRepository.countByStatus(MockTestStatus.COMPLETED));
        dto.setTotalSubjects((long) subjectRepository.findByActiveTrue().size());

        Double avgScore = mockTestRepository.getOverallAveragePercentage();
        dto.setAveragePlatformScore(avgScore != null ? Math.round(avgScore * 10.0) / 10.0 : 0.0);

        List<MockTest> recent = mockTestRepository.findTop10ByStatusOrderByCreatedAtDesc(MockTestStatus.COMPLETED);
        dto.setRecentTests(recent.stream()
                .map(MockTestSummaryDto::fromEntity)
                .collect(Collectors.toList()));

        List<Subject> subjects = subjectRepository.findAll();
        List<SubjectPerformanceDto> stats = subjects.stream().map(s -> {
            long qCount = questionRepository.countBySubjectId(s.getId());
            return new SubjectPerformanceDto(s.getId(), s.getName(), s.getIcon(), s.getColor(), 0, (int) qCount, 0, 0.0);
        }).collect(Collectors.toList());

        dto.setSubjectStats(stats);
        return dto;
    }

    private String generateTopicRecommendation(String topicName, double accuracy) {
        if (accuracy < 40.0) {
            return "Critical review needed. Re-study foundational concepts for " + topicName + " and take a focused practice quiz.";
        } else if (accuracy < 65.0) {
            return "Needs reinforcement. Review common edge cases and pitfall questions in " + topicName + ".";
        } else {
            return "Good progress! Practice advanced scenario-based questions in " + topicName + " to achieve mastery.";
        }
    }
}
