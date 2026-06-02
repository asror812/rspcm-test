package org.example.rspcm.service;

import lombok.RequiredArgsConstructor;
import org.example.rspcm.dto.AdminRecentReportResponse;
import org.example.rspcm.dto.AdminDashboardGeneralStatsResponse;
import org.example.rspcm.dto.group.GroupResponse;
import org.example.rspcm.mapper.SummaryMapper;
import org.example.rspcm.mapper.GroupMapper;
import org.example.rspcm.model.entity.PracticeSubmission;
import org.example.rspcm.model.entity.StudyGroup;
import org.example.rspcm.model.entity.User;
import org.example.rspcm.model.enums.PracticeSubmissionStatus;
import org.example.rspcm.model.enums.RoleName;
import org.example.rspcm.repository.PracticeSubmissionRepository;
import org.example.rspcm.repository.StudentProfileRepository;
import org.example.rspcm.repository.StudyGroupRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final StudyGroupRepository studyGroupRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final PracticeSubmissionRepository practiceSubmissionRepository;
    private final GroupMapper groupMapper;
    private final SummaryMapper summaryMapper;


    @Transactional(readOnly = true)
    public AdminDashboardGeneralStatsResponse getGeneralStats() {
        Long studentCount = studentProfileRepository.count();
        Long groupCount = studyGroupRepository.count();
        long pendingReports = practiceSubmissionRepository.countByStatus(PracticeSubmissionStatus.SUBMITTED);
        long approvedReports = practiceSubmissionRepository.countByStatus(PracticeSubmissionStatus.GRADED);

        return AdminDashboardGeneralStatsResponse.builder()
                .totalStudents(studentCount)
                .totalStudyGroups(groupCount)
                .totalGroups(groupCount)
                .pendingReports(pendingReports)
                .approvedReports(approvedReports)
                .build();
    }


    @Transactional(readOnly = true)
    public Page<AdminRecentReportResponse> getRecentReports(User user, Pageable pageable) {
        return practiceSubmissionRepository.findBySubmittedAtIsNotNullOrderBySubmittedAtDesc(pageable)
                .map(this::toRecentReportResponse);
    }

    @Transactional(readOnly = true)
    public List<GroupResponse> getOwnStudyGroups(User user) {
        if (hasRole(user, RoleName.ROLE_ADMIN)) {
            return studyGroupRepository.findAll().stream().map(groupMapper::toResponse).toList();
        }
        if (hasRole(user, RoleName.ROLE_TEACHER)) {
            return studyGroupRepository.findByTeachersId(user.getId()).stream().map(groupMapper::toResponse).toList();
        }
        if (hasRole(user, RoleName.ROLE_STUDENT)) {
            return studyGroupRepository.findByStudentsId(user.getId()).stream().map(groupMapper::toResponse).toList();
        }
        return List.of();
    }

    private boolean hasRole(User user, RoleName roleName) {
        return user.getRoles().stream().anyMatch(role -> role.getRoleName() == roleName);
    }

    private AdminRecentReportResponse toRecentReportResponse(PracticeSubmission submission) {
        var exam = submission.getExamParticipation().getExam();
        List<String> groupNames = exam.getGroups().stream()
                .map(StudyGroup::getName)
                .sorted()
                .collect(Collectors.toList());

        return new AdminRecentReportResponse(
                submission.getId(),
                exam.getId(),
                exam.getTitle(),
                groupNames,
                submission.getStudent() == null ? null : summaryMapper.toUserSummary(submission.getStudent()),
                submission.getStatus(),
                submission.getSubmittedAt()
        );
    }
}
