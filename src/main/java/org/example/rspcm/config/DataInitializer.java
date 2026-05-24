package org.example.rspcm.config;

import lombok.RequiredArgsConstructor;
import org.example.rspcm.model.entity.Role;
import org.example.rspcm.model.entity.Question;
import org.example.rspcm.model.entity.QuestionOption;
import org.example.rspcm.model.entity.User;
import org.example.rspcm.model.entity.StudentProfile;
import org.example.rspcm.model.entity.StudyGroup;
import org.example.rspcm.model.entity.Subject;
import org.example.rspcm.model.entity.TeacherProfile;
import org.example.rspcm.model.entity.Exam;
import org.example.rspcm.model.entity.ExamPractice;
import org.example.rspcm.model.entity.ExamQuestion;
import org.example.rspcm.model.entity.Practice;
import org.example.rspcm.model.entity.PracticeParticipation;
import org.example.rspcm.model.entity.PracticeParticipationMember;
import org.example.rspcm.model.entity.PracticeSubmission;
import org.example.rspcm.model.enums.GroupLanguage;
import org.example.rspcm.model.enums.QuestionType;
import org.example.rspcm.model.enums.RoleName;
import org.example.rspcm.model.enums.ExamType;
import org.example.rspcm.model.enums.ExamStatus;
import org.example.rspcm.model.enums.PracticeMemberRole;
import org.example.rspcm.model.enums.PracticeParticipationMemberStatus;
import org.example.rspcm.model.enums.PracticeParticipationStatus;
import org.example.rspcm.model.enums.PracticeSubmissionStatus;
import org.example.rspcm.model.enums.WorkMode;
import org.example.rspcm.model.enums.SubmissionType;
import org.example.rspcm.repository.UserRepository;
import org.example.rspcm.repository.AppRoleRepository;
import org.example.rspcm.repository.StudentProfileRepository;
import org.example.rspcm.repository.StudyGroupRepository;
import org.example.rspcm.repository.SubjectRepository;
import org.example.rspcm.repository.TeacherProfileRepository;
import org.example.rspcm.repository.QuestionRepository;
import org.example.rspcm.repository.ExamRepository;
import org.example.rspcm.repository.ExamQuestionRepository;
import org.example.rspcm.repository.PracticeRepository;
import org.example.rspcm.repository.PracticeParticipationRepository;
import org.example.rspcm.repository.PracticeParticipationMemberRepository;
import org.example.rspcm.repository.PracticeSubmissionRepository;
import org.example.rspcm.service.UserProfileSyncService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AppRoleRepository roleRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final QuestionRepository questionRepository;
    private final ExamRepository examRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final PracticeRepository practiceRepository;
    private final PracticeParticipationRepository practiceParticipationRepository;
    private final PracticeParticipationMemberRepository practiceParticipationMemberRepository;
    private final PracticeSubmissionRepository practiceSubmissionRepository;
    private final UserProfileSyncService userProfileSyncService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String ... args) {
        Map<RoleName, Role> roles = Arrays.stream(RoleName.values())
                .map(roleName -> 
                    roleRepository.findByName(roleName)
                            .orElseGet(() -> roleRepository.save(
                                    Role.builder()
                                            .name(roleName)
                                            .build())))

                .collect(Collectors.toMap(Role::getRoleName, role -> role));

        seedUsers(roles);
        seedAcademicRelations();
    }

    private void seedUsers(Map<RoleName, Role> roles) {
        createOrUpdateUser("admin@rspcm.local", "202400001", "System Admin", "123", Set.of(RoleName.ROLE_ADMIN), roles);

        // K1 group students (first five)
        createOrUpdateUser("k1.anvar.rasulov@rspcm.local", "202400101", "Anvar Rasulov", "123", Set.of(RoleName.ROLE_STUDENT), roles);
        createOrUpdateUser("k1.alisher.nazarov@rspcm.local", "202400102", "Alisher Nazarov", "123", Set.of(RoleName.ROLE_STUDENT), roles);
        createOrUpdateUser("k1.axror.karimov@rspcm.local", "202400103", "Axror Karimov", "123", Set.of(RoleName.ROLE_STUDENT), roles);
        createOrUpdateUser("k1.asror.abdullayeva@rspcm.local", "202400104", "Asror Abdullayeva", "123", Set.of(RoleName.ROLE_STUDENT), roles);
        createOrUpdateUser("k1.abror.rahimov@rspcm.local", "202400105", "Abror Rahimov", "123", Set.of(RoleName.ROLE_STUDENT), roles);

        // L1 group students (second five)
        createOrUpdateUser("l1.bahrom.rasulov@rspcm.local", "202400201", "Bahrom Rasulov", "123", Set.of(RoleName.ROLE_STUDENT), roles);
        createOrUpdateUser("l1.bahodir.nazarov@rspcm.local", "202400202", "Bahodir Nazarov", "123", Set.of(RoleName.ROLE_STUDENT), roles);
        createOrUpdateUser("l1.bobur.karimov@rspcm.local", "202400203", "Bobur Karimov", "123", Set.of(RoleName.ROLE_STUDENT), roles);
        createOrUpdateUser("l1.botir.abdullayeva@rspcm.local", "202400204", "Botir Abdullayeva", "123", Set.of(RoleName.ROLE_STUDENT), roles);
        createOrUpdateUser("l1.bekzod.rahimov@rspcm.local", "202400205", "Bekzod Rahimov", "123", Set.of(RoleName.ROLE_STUDENT), roles);

        // Subject teachers (three users)
        createOrUpdateUser("math.teacher@rspcm.local", "202400301", "Math Teacher", "123", Set.of(RoleName.ROLE_TEACHER), roles);
        createOrUpdateUser("physics.teacher@rspcm.local", "202400302", "Physics Teacher", "123", Set.of(RoleName.ROLE_TEACHER), roles);
        createOrUpdateUser("programming.teacher@rspcm.local", "202400303", "Programming Teacher", "123", Set.of(RoleName.ROLE_TEACHER), roles);
    }

    private void seedAcademicRelations() {
        User k1Student1 = getUser("k1.anvar.rasulov@rspcm.local");
        User k1Student2 = getUser("k1.alisher.nazarov@rspcm.local");
        User k1Student3 = getUser("k1.axror.karimov@rspcm.local");
        User k1Student4 = getUser("k1.asror.abdullayeva@rspcm.local");
        User k1Student5 = getUser("k1.abror.rahimov@rspcm.local");

        User l1Student1 = getUser("l1.bahrom.rasulov@rspcm.local");
        User l1Student2 = getUser("l1.bahodir.nazarov@rspcm.local");
        User l1Student3 = getUser("l1.bobur.karimov@rspcm.local");
        User l1Student4 = getUser("l1.botir.abdullayeva@rspcm.local");
        User l1Student5 = getUser("l1.bekzod.rahimov@rspcm.local");

        User teacherMath = getUser("math.teacher@rspcm.local");
        User teacherPhysics = getUser("physics.teacher@rspcm.local");
        User teacherProgramming = getUser("programming.teacher@rspcm.local");


        Subject math = createOrUpdateSubject("Mathematics", "Algebra va Calculus asoslari.");
        Subject physics = createOrUpdateSubject("Physics", "Mexanika va elektr bo'limlari.");
        Subject programming = createOrUpdateSubject("Programming", "Java va backend dasturlash.");

        assignTeacherProfile(teacherMath, "PhD", Set.of(math));
        assignTeacherProfile(teacherPhysics, "MSc", Set.of(physics));
        assignTeacherProfile(teacherProgramming, "MSc", Set.of(programming));

        assignStudentProfile(k1Student1, 1);
        assignStudentProfile(k1Student2, 1);
        assignStudentProfile(k1Student3, 1);
        assignStudentProfile(k1Student4, 1);
        assignStudentProfile(k1Student5, 1);
        assignStudentProfile(l1Student1, 1);
        assignStudentProfile(l1Student2, 1);
        assignStudentProfile(l1Student3, 1);
        assignStudentProfile(l1Student4, 1);
        assignStudentProfile(l1Student5, 1);

        math.setTeachers(Set.of(teacherMath));
        subjectRepository.save(math);

        physics.setTeachers(Set.of(teacherPhysics));
        subjectRepository.save(physics);

        programming.setTeachers(Set.of(teacherProgramming));
        subjectRepository.save(programming);

        createOrUpdateGroup(
                "K1",
                "K1 guruhi",
                GroupLanguage.UZ,
                Set.of(math, programming),
                Set.of(teacherMath, teacherProgramming),
                Set.of(k1Student1, k1Student2, k1Student3, k1Student4, k1Student5)
        );
        createOrUpdateGroup(
                "L1",
                "L1 guruhi",
                GroupLanguage.RU,
                Set.of(physics, programming),
                Set.of(teacherPhysics, teacherProgramming),
                Set.of(l1Student1, l1Student2, l1Student3, l1Student4, l1Student5)
        );

        ensureMinimumQuestions(math, teacherMath, 10);
        ensureMinimumQuestions(physics, teacherPhysics, 10);
        ensureMinimumQuestions(programming, teacherProgramming, 10);

        StudyGroup k1Group = getGroup("K1");
        StudyGroup l1Group = getGroup("L1");

        Exam questionExam = createOrUpdateExam(
                "Umumiy savol imtihoni",
                "Savollar asosidagi auto-seeded imtihon.",
                programming,
                teacherProgramming,
                Set.of(k1Group, l1Group),
                ExamType.QUESTION
        );
        attachSubjectQuestionsToExam(questionExam, programming);

        Exam mathQuestionExam = createOrUpdateExam(
                "Matematika savol imtihoni",
                "Matematika fanidan savollar asosidagi auto-seeded imtihon.",
                math,
                teacherMath,
                Set.of(k1Group),
                ExamType.QUESTION
        );
        attachSubjectQuestionsToExam(mathQuestionExam, math);

        List<Practice> practices = ensureMinimumPractices(
                List.of(
                        "Math practical task 1",
                        "Physics practical task 1",
                        "Programming practical task 1",
                        "Programming practical task 2",
                        "Cross-subject practical task 1"
                ),
                List.of(
                        math,
                        physics,
                        programming,
                        programming,
                        programming
                ),
                teacherProgramming
        );

        Exam practicalExam = createOrUpdateExam(
                "Umumiy amaliy imtihon",
                "Amaliy topshiriqlar asosidagi auto-seeded imtihon.",
                programming,
                teacherProgramming,
                Set.of(k1Group, l1Group),
                ExamType.PRACTICE
        );

        attachPracticesToExam(practicalExam, List.of(practices.get(2), practices.get(3), practices.get(4)));
        seedPracticeParticipations(practicalExam);
        backfillExamAndExamQuestionAuditData(getUser("admin@rspcm.local"));
    }

    private void seedPracticeParticipations(Exam practicalExam) {
        if (!practiceParticipationRepository.findByExamId(practicalExam.getId(), Pageable.unpaged()).isEmpty()) {
            return;
        }

        User k1Student1 = getUser("k1.anvar.rasulov@rspcm.local");
        User k1Student2 = getUser("k1.alisher.nazarov@rspcm.local");
        User k1Student3 = getUser("k1.axror.karimov@rspcm.local");
        User k1Student4 = getUser("k1.asror.abdullayeva@rspcm.local");
        User k1Student5 = getUser("k1.abror.rahimov@rspcm.local");
        User l1Student1 = getUser("l1.bahrom.rasulov@rspcm.local");
        User l1Student2 = getUser("l1.bahodir.nazarov@rspcm.local");
        User l1Student3 = getUser("l1.bobur.karimov@rspcm.local");
        User l1Student4 = getUser("l1.botir.abdullayeva@rspcm.local");
        User l1Student5 = getUser("l1.bekzod.rahimov@rspcm.local");

        ExamPractice teamExamPractice = practicalExam.getPractices().stream()
                .filter(link -> link.getPractice().getWorkMode() == WorkMode.TEAM)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("TEAM exam practice topilmadi"));

        ExamPractice individualExamPractice = practicalExam.getPractices().stream()
                .filter(link -> link.getPractice().getWorkMode() == WorkMode.INDIVIDUAL)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("INDIVIDUAL exam practice topilmadi"));

        // WAITING_MEMBERS: leader + invited + accepted
        PracticeParticipation waitingMembers = createParticipation(
                practicalExam,
                teamExamPractice,
                PracticeParticipationStatus.WAITING_MEMBERS,
                LocalDateTime.now().minusDays(2),
                null,
                null
        );
        addMember(waitingMembers, k1Student1, PracticeMemberRole.LEADER, PracticeParticipationMemberStatus.ACCEPTED);
        addMember(waitingMembers, k1Student2, PracticeMemberRole.MEMBER, PracticeParticipationMemberStatus.INVITED);
        addMember(waitingMembers, k1Student3, PracticeMemberRole.MEMBER, PracticeParticipationMemberStatus.ACCEPTED);

        // READY_TO_CHOOSE: full accepted team
        PracticeParticipation readyToChoose = createParticipation(
                practicalExam,
                teamExamPractice,
                PracticeParticipationStatus.READY_TO_CHOOSE,
                LocalDateTime.now().minusDays(3),
                LocalDateTime.now().minusDays(2),
                null
        );
        addMember(readyToChoose, l1Student1, PracticeMemberRole.LEADER, PracticeParticipationMemberStatus.ACCEPTED);
        addMember(readyToChoose, l1Student2, PracticeMemberRole.MEMBER, PracticeParticipationMemberStatus.ACCEPTED);
        addMember(readyToChoose, l1Student3, PracticeMemberRole.MEMBER, PracticeParticipationMemberStatus.ACCEPTED);

        // PRACTICE_CHOSEN + submission
        PracticeParticipation chosen = createParticipation(
                practicalExam,
                individualExamPractice,
                PracticeParticipationStatus.PRACTICE_CHOSEN,
                LocalDateTime.now().minusDays(4),
                LocalDateTime.now().minusDays(4),
                LocalDateTime.now().minusDays(3)
        );
        addMember(chosen, k1Student4, PracticeMemberRole.LEADER, PracticeParticipationMemberStatus.ACCEPTED);

        PracticeSubmission submission = PracticeSubmission.builder()
                .examParticipation(chosen)
                .student(k1Student4)
                .textAnswer("Auto-seeded submission content")
                .fileUrl("https://example.com/submissions/auto-seeded-1")
                .submittedAt(LocalDateTime.now().minusDays(2))
                .status(PracticeSubmissionStatus.GRADED)
                .teacherComment("Auto-seeded: looks good")
                .build();
        practiceSubmissionRepository.save(submission);

        // Additional member states for realism
        addMember(waitingMembers, l1Student4, PracticeMemberRole.MEMBER, PracticeParticipationMemberStatus.DECLINED);
        addMember(waitingMembers, l1Student5, PracticeMemberRole.MEMBER, PracticeParticipationMemberStatus.REMOVED);
    }

    private PracticeParticipation createParticipation(
            Exam exam,
            ExamPractice examPractice,
            PracticeParticipationStatus status,
            LocalDateTime createdAt,
            LocalDateTime readyAt,
            LocalDateTime chosenAt
    ) {
        PracticeParticipation participation = new PracticeParticipation();
        participation.setExam(exam);
        participation.setExamPractice(examPractice);
        participation.setStatus(status);
        participation.setCreatedAt(createdAt);
        participation.setReadyAt(readyAt);
        participation.setChosenAt(chosenAt);
        return practiceParticipationRepository.save(participation);
    }

    private void addMember(
            PracticeParticipation participation,
            User user,
            PracticeMemberRole role,
            PracticeParticipationMemberStatus status
    ) {
        PracticeParticipationMember member = new PracticeParticipationMember();
        member.setPracticeParticipation(participation);
        member.setUser(user);
        member.setRole(role);
        member.setStatus(status);
        practiceParticipationMemberRepository.save(member);
    }

    private void createOrUpdateUser(
            String email,
            String universityId,
            String fullName,
            String passwordRaw,
            Set<RoleName> roleNames,
            Map<RoleName, Role> roles
    ) {
        Set<Role> resolvedRoles = roleNames.stream()
                .map(roles::get)
                .collect(Collectors.toSet());

        if (userRepository.findByEmail(email).isPresent()) {
            return;
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(passwordRaw))
                .firstName(extractFirstName(fullName))
                .lastName(extractLastName(fullName))
                .universityId(universityId)
                .enabled(true)
                .roles(new HashSet<>(resolvedRoles))
                .build();

        User saved = userRepository.save(user);
        userProfileSyncService.sync(saved);
    }

    private Subject createOrUpdateSubject(String name, String description) {
        return subjectRepository.findByName(name)
                .orElseGet(() -> subjectRepository.save(
                        Subject.builder()
                                .name(name)
                                .description(description)
                                .build()
                ));
    }

    private void assignTeacherProfile(User teacher, String degree, Set<Subject> subjects) {
        if (teacherProfileRepository.findByUserId(teacher.getId()).isPresent()) {
            return;
        }
        TeacherProfile profile = TeacherProfile.builder()
                .user(teacher)
                .academicDegree(degree)
                .teachingSubjects(new HashSet<>(subjects))
                .build();
        teacherProfileRepository.save(profile);
    }

    private void assignStudentProfile(User student, Integer course) {
        if (studentProfileRepository.findByUserId(student.getId()).isPresent()) {
            return;
        }
        StudentProfile profile = StudentProfile.builder()
                .user(student)
                .course(course)
                .build();
        studentProfileRepository.save(profile);
    }

    private void createOrUpdateGroup(
            String name,
            String description,
            GroupLanguage language,
            Set<Subject> subjects,
            Set<User> teachers,
            Set<User> students
    ) {
        if (studyGroupRepository.findByName(name).isPresent()) {
            return;
        }
        StudyGroup group = StudyGroup.builder()
                .name(name)
                .description(description)
                .language(language)
                .subjects(new HashSet<>(subjects))
                .teachers(new HashSet<>(teachers))
                .students(new HashSet<>(students))
                .build();
        studyGroupRepository.save(group);
    }

    private void ensureMinimumQuestions(Subject subject, User teacher, int minimumCount) {
        for (int index = 1; index <= minimumCount; index++) {
            String text = subject.getName() + " question " + index;
            Question question = questionRepository.findBySubjectIdAndText(subject.getId(), text)
                    .orElse(null);

            if (question != null) {
                continue;
            }
            question = Question.builder()
                    .text(text)
                    .subject(subject)
                    .createdBy(teacher)
                    .options(new ArrayList<>())
                    .build();

            if (index == 1) {
                question.setType(QuestionType.OPEN);
                question.setOptions(new ArrayList<>());
            } else if (index % 2 == 0) {
                question.setType(QuestionType.CLOSED);
                question.setOptions(buildClosedOptions(question));
            } else {
                question.setType(QuestionType.MULTIPLE_CHOICE);
                question.setOptions(buildMultipleChoiceOptions(question));
            }
            questionRepository.save(question);
        }
    }

    private Exam createOrUpdateExam(
            String title,
            String description,
            Subject subject,
            User teacher,
            Set<StudyGroup> groups,
            ExamType examType
    ) {
        Exam exam = examRepository.findAll().stream()
                .filter(existing -> title.equals(existing.getTitle()))
                .findFirst()
                .orElseGet(() -> Exam.builder()
                        .title(title)
                        .questions(new ArrayList<>())
                        .practices(new ArrayList<>())
                        .groups(new HashSet<>())
                        .targetStudents(new HashSet<>())
                        .build());
        boolean isNew = exam.getId() == null;

        if (!isNew) {
            return exam;
        }

        if (isNew) {
            exam.setDescription(description);
            exam.setSubject(subject);
            exam.setType(examType);
            exam.setStatus(ExamStatus.PUBLISHED);
            exam.setMaxScore(100);
            exam.setTaskLimit(10);
            exam.setStartAt(LocalDateTime.now().minusDays(1));
            exam.setEndAt(LocalDateTime.now().plusDays(8));
            exam.setGroups(new HashSet<>(groups));
            exam.setCreatedBy(teacher);

            if (exam.getCreatedAt() == null) exam.setCreatedAt(LocalDateTime.now());

            if (exam.getQuestions() == null) exam.setQuestions(new ArrayList<>());
            if (exam.getPractices() == null) exam.setPractices(new ArrayList<>());

            if (examType == ExamType.QUESTION) {
                exam.setPractices(new ArrayList<>());
            } else if (examType == ExamType.PRACTICE) {
                List<ExamQuestion> existingQuestions = examQuestionRepository.findByExamId(exam.getId());
                if (!existingQuestions.isEmpty()) {
                    examQuestionRepository.deleteAll(existingQuestions);
                }
                exam.setQuestions(new ArrayList<>());
            }
        }

        return examRepository.save(exam);
    }

    private void attachSubjectQuestionsToExam(Exam exam, Subject subject) {
        if (!examQuestionRepository.findByExamId(exam.getId()).isEmpty()) {
            return;
        }
        List<Question> subjectQuestions = questionRepository.findBySubjectIdAndDeletedFalse(subject.getId());
        Map<Long, ExamQuestion> existingByQuestionId = examQuestionRepository.findByExamId(exam.getId()).stream()
                .collect(Collectors.toMap(eq -> eq.getQuestion().getId(), eq -> eq));

        Set<Long> desiredQuestionIds = subjectQuestions.stream()
                .map(Question::getId)
                .collect(Collectors.toSet());

        List<ExamQuestion> staleLinks = existingByQuestionId.values().stream()
                .filter(eq -> !desiredQuestionIds.contains(eq.getQuestion().getId()))
                .toList();

        if (!staleLinks.isEmpty()) {
            examQuestionRepository.deleteAll(staleLinks);
        }

        List<ExamQuestion> examQuestions = new ArrayList<>();
        int order = 1;

        for (Question question : subjectQuestions) {
            ExamQuestion examQuestion = existingByQuestionId.getOrDefault(question.getId(), new ExamQuestion());
            examQuestion.setExam(exam);
            examQuestion.setQuestion(question);
            examQuestion.setScore(10);
            examQuestion.setOrderIndex(order++);
            examQuestion.setCreatedBy(exam.getCreatedBy());
            examQuestions.add(examQuestion);
        }
        exam.setQuestions(examQuestions);
        examRepository.save(exam);
    }

    private List<Practice> ensureMinimumPractices(List<String> taskNames, List<Subject> subjects, User createdBy) {
        List<Practice> tasks = new ArrayList<>();
        for (int index = 0; index < taskNames.size(); index++) {
            String taskName = taskNames.get(index);
            Subject subject = subjects.get(index);
            Practice task = practiceRepository.findAll().stream()
                    .filter(existing -> taskName.equals(existing.getName()))
                    .findFirst()
                    .orElse(null);

            if (task == null) {
                task = Practice.builder()
                        .name(taskName)
                        .description("Auto-seeded practical task: " + taskName)
                        .subject(subject)
                        .resourceUrl("https://example.com/tasks/" + (index + 1))
                        .requirements("Task requirements for " + taskName)
                        .workMode(index % 2 == 0 ? WorkMode.INDIVIDUAL : WorkMode.TEAM)
                        .teamSize(index % 2 == 0 ? null : 3)
                        .schedulingRequired(false)
                        .allowedSubmissionTypes(Set.of(SubmissionType.TEXT, SubmissionType.FILE, SubmissionType.CODE))
                        .createdBy(createdBy)
                        .build();
                task = practiceRepository.save(task);
            }
            tasks.add(task);
        }
        return tasks;
    }

    private void attachPracticesToExam(Exam exam, List<Practice> practices) {
        if (exam.getPractices() != null && !exam.getPractices().isEmpty()) {
            return;
        }
        List<ExamPractice> existingLinks = new ArrayList<>(exam.getPractices() == null ? List.of() : exam.getPractices());
        Map<Long, ExamPractice> existingByPracticeId = existingLinks.stream()
                .collect(Collectors.toMap(link -> link.getPractice().getId(), link -> link, (left, right) -> left));

        Set<Long> desiredPracticeIds = practices.stream()
                .map(Practice::getId)
                .collect(Collectors.toSet());

        List<ExamPractice> staleLinks = existingLinks.stream()
                .filter(link -> !desiredPracticeIds.contains(link.getPractice().getId()))
                .toList();

        if (!staleLinks.isEmpty()) {
            exam.getPractices().removeAll(staleLinks);
        }

        List<ExamPractice> normalized = new ArrayList<>();
        for (Practice practice : practices) {
            ExamPractice link = existingByPracticeId.getOrDefault(practice.getId(), new ExamPractice());
            link.setExam(exam);
            link.setPractice(practice);
            normalized.add(link);
        }

        exam.setPractices(normalized);
        examRepository.save(exam);
    }

    private void backfillExamAndExamQuestionAuditData(User fallbackUser) {
        // create-only mode: do not mutate existing records
    }

    private List<QuestionOption> buildClosedOptions(Question question) {
        List<QuestionOption> options = new ArrayList<>();
        options.add(option(question, "True", true, 1));
        options.add(option(question, "False", false, 2));
        return options;
    }

    private List<QuestionOption> buildMultipleChoiceOptions(Question question) {
        List<QuestionOption> options = new ArrayList<>();
        options.add(option(question, "Option A", true, 1));
        options.add(option(question, "Option B", false, 2));
        options.add(option(question, "Option C", true, 3));
        options.add(option(question, "Option D", false, 4));
        return options;
    }

    private QuestionOption option(Question question, String text, boolean correct, int orderIndex) {
        QuestionOption option = new QuestionOption();
        option.setQuestion(question);
        option.setText(text);
        option.setCorrect(correct);
        option.setOrderIndex(orderIndex);
        return option;
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Seed user topilmadi: " + email));
    }

    private StudyGroup getGroup(String name) {
        return studyGroupRepository.findByName(name)
                .orElseThrow(() -> new IllegalStateException("Seed group topilmadi: " + name));
    }

    private String extractFirstName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "";
        }

        String normalized = fullName.trim().replaceAll("\\s+", " ");
        int firstSpace = normalized.indexOf(' ');

        if (firstSpace < 0) return normalized;

        return normalized.substring(0, firstSpace);
    }

    private String extractLastName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "";
        }
        String normalized = fullName.trim().replaceAll("\\s+", " ");
        int firstSpace = normalized.indexOf(' ');
        if (firstSpace < 0) {
            return "";
        }
        return normalized.substring(firstSpace + 1);
    }
}
