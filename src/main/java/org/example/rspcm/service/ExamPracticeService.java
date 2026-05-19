package org.example.rspcm.service;

import lombok.RequiredArgsConstructor;
import org.example.rspcm.dto.exam.ExamPracticeRequest;
import org.example.rspcm.dto.exam.ExamPracticeResponse;
import org.example.rspcm.exception.ErrorCodes;
import org.example.rspcm.exception.ErrorMessageException;
import org.example.rspcm.exception.NotFoundException;
import org.example.rspcm.mapper.SummaryMapper;
import org.example.rspcm.model.entity.Exam;
import org.example.rspcm.model.entity.ExamPractice;
import org.example.rspcm.model.entity.Practice;
import org.example.rspcm.model.entity.User;
import org.example.rspcm.model.enums.ExamType;
import org.example.rspcm.model.enums.RoleName;
import org.example.rspcm.repository.ExamPracticeRepository;
import org.example.rspcm.repository.ExamRepository;
import org.example.rspcm.repository.PracticeRepository;
import org.example.rspcm.repository.TeacherProfileRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExamPracticeService {

    private final ExamPracticeRepository examPracticeRepository;
    private final ExamRepository examRepository;
    private final PracticeRepository practiceRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final SummaryMapper summaryMapper;

    public Page<ExamPracticeResponse> findAll(Long examId, User user, Pageable pageable) {
        if (examId == null) {
            throw new ErrorMessageException("examId kiritilishi shart", ErrorCodes.BadRequest);
        }

        Exam exam = resolveExam(examId);
        validateTeacherAccess(user, exam);
        return examPracticeRepository.findByExamId(examId, pageable).map(this::toResponse);
    }

    public ExamPracticeResponse findById(Long id, User user) {
        ExamPractice examPractice = findEntityById(id);
        validateTeacherAccess(user, examPractice.getExam());
        return toResponse(examPractice);
    }

    @Transactional
    public ExamPracticeResponse create(ExamPracticeRequest request, User user) {
        Exam exam = resolveExam(request.examId());
        validateTeacherAccess(user, exam);
        validateExamType(exam);

        Practice practice = practiceRepository.findById(request.practiceId())
                .orElseThrow(() -> new NotFoundException("Practice topilmadi: " + request.practiceId()));

        validateExamScore(exam);

        if (examPracticeRepository.existsByExamIdAndPracticeId(exam.getId(), practice.getId())) {
            throw new ErrorMessageException("Bu practice allaqachon examga biriktirilgan", ErrorCodes.AlreadyExists);
        }

        if (examPracticeRepository.existsByExamIdAndOrderIndex(exam.getId(), request.orderIndex())) {
            throw new ErrorMessageException("Bu examda orderIndex band", ErrorCodes.AlreadyExists);
        }

        ExamPractice link = ExamPractice.builder()
                .exam(exam)
                .practice(practice)
                .orderIndex(request.orderIndex())
                .build();

        return toResponse(examPracticeRepository.save(link));
    }

    private void validateExamScore(Exam exam) {
        Long currentCount = examPracticeRepository.countByExamId(exam.getId());

        if (exam.getTaskLimit() == null || currentCount > exam.getTaskLimit()) {
            throw new ErrorMessageException("Bu exam uchun praktikalar soni yetarli", ErrorCodes.BadRequest);
        }

    /*    Integer currentScore = examPracticeRepository.sumScoreByExamId(exam.getId());

        if (exam.getMaxScore() == null || currentScore > exam.getMaxScore()) {
            throw new ErrorMessageException("Bu exam uchun maksimal ball yetarli emas", ErrorCodes.BadRequest);
        }*/
    }

    @Transactional
    public ExamPracticeResponse update(Long id, ExamPracticeRequest request, User user) {
        ExamPractice existing = findEntityById(id);
        Exam exam = resolveExam(request.examId());
        validateTeacherAccess(user, exam);
        validateExamType(exam);

        Practice practice = practiceRepository.findById(request.practiceId())
                .orElseThrow(() -> new NotFoundException("Practice topilmadi: " + request.practiceId()));

        if (examPracticeRepository.existsByExamIdAndOrderIndexAndIdNot(exam.getId(), request.orderIndex(), id)) {
            throw new ErrorMessageException("Bu examda orderIndex band", ErrorCodes.AlreadyExists);
        }

        existing.setExam(exam);
        existing.setPractice(practice);
        existing.setOrderIndex(request.orderIndex());
        return toResponse(examPracticeRepository.save(existing));
    }

    @Transactional
    public void delete(Long id, User user) {
        ExamPractice existing = findEntityById(id);
        validateTeacherAccess(user, existing.getExam());
        examPracticeRepository.delete(existing);
    }

    private ExamPractice findEntityById(Long id) {
        return examPracticeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("ExamPractice topilmadi: " + id));
    }

    private Exam resolveExam(Long examId) {
        return examRepository.findById(examId)
                .orElseThrow(() -> new NotFoundException("Exam topilmadi: " + examId));
    }

    private void validateExamType(Exam exam) {
        if (exam.getType() != ExamType.PRACTICE) {
            throw new ErrorMessageException("Faqat PRACTICE turidagi examga practice biriktirish mumkin", ErrorCodes.BadRequest);
        }
    }

    private void validateTeacherAccess(User user, Exam exam) {
        if (isAdmin(user)) {
            return;
        }
        if (!isTeacher(user)) {
            throw new ErrorMessageException("Ruxsat etilmagan amal", ErrorCodes.Forbidden);
        }

        if (exam.getSubject() == null) {
            throw new ErrorMessageException("Examga subject biriktirilmagan", ErrorCodes.BadRequest);
        }

        boolean teaches = teacherProfileRepository.existsByUserIdAndTeachingSubjectsId(user.getId(), exam.getSubject().getId());
        if (!teaches) {
            throw new ErrorMessageException("Faqat o'zingizga biriktirilgan fan examlarini boshqara olasiz", ErrorCodes.Forbidden);
        }
    }

    private boolean isAdmin(User user) {
        return user.getRoles().stream().anyMatch(role -> role.getRoleName() == RoleName.ROLE_ADMIN);
    }

    private boolean isTeacher(User user) {
        return user.getRoles().stream().anyMatch(role -> role.getRoleName() == RoleName.ROLE_TEACHER);
    }

    private ExamPracticeResponse toResponse(ExamPractice link) {
        return new ExamPracticeResponse(
                link.getId(),
                link.getExam().getId(),
                summaryMapper.toPracticeSummary(link.getPractice()),
                link.getOrderIndex()
        );
    }
}
