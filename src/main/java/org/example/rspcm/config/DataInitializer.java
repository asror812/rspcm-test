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
import org.example.rspcm.model.enums.GroupLanguage;
import org.example.rspcm.model.enums.QuestionType;
import org.example.rspcm.model.enums.RoleName;
import org.example.rspcm.repository.UserRepository;
import org.example.rspcm.repository.AppRoleRepository;
import org.example.rspcm.repository.StudentProfileRepository;
import org.example.rspcm.repository.StudyGroupRepository;
import org.example.rspcm.repository.SubjectRepository;
import org.example.rspcm.repository.TeacherProfileRepository;
import org.example.rspcm.repository.QuestionRepository;
import org.example.rspcm.service.UserProfileSyncService;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final UserProfileSyncService userProfileSyncService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String @NonNull ... args) {
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
        createOrUpdateUser("admin@rspcm.local", "System Admin", "123", Set.of(RoleName.ROLE_ADMIN), roles);

        // K1 group students (first five)
        createOrUpdateUser("k1.anvar.rasulov@rspcm.local", "Anvar Rasulov", "123", Set.of(RoleName.ROLE_STUDENT), roles);
        createOrUpdateUser("k1.alisher.nazarov@rspcm.local", "Alisher Nazarov", "123", Set.of(RoleName.ROLE_STUDENT), roles);
        createOrUpdateUser("k1.axror.karimov@rspcm.local", "Axror Karimov", "123", Set.of(RoleName.ROLE_STUDENT), roles);
        createOrUpdateUser("k1.asror.abdullayeva@rspcm.local", "Asror Abdullayeva", "123", Set.of(RoleName.ROLE_STUDENT), roles);
        createOrUpdateUser("k1.abror.rahimov@rspcm.local", "Abror Rahimov", "123", Set.of(RoleName.ROLE_STUDENT), roles);

        // L1 group students (second five)
        createOrUpdateUser("l1.bahrom.rasulov@rspcm.local", "Bahrom Rasulov", "123", Set.of(RoleName.ROLE_STUDENT), roles);
        createOrUpdateUser("l1.bahodir.nazarov@rspcm.local", "Bahodir Nazarov", "123", Set.of(RoleName.ROLE_STUDENT), roles);
        createOrUpdateUser("l1.bobur.karimov@rspcm.local", "Bobur Karimov", "123", Set.of(RoleName.ROLE_STUDENT), roles);
        createOrUpdateUser("l1.botir.abdullayeva@rspcm.local", "Botir Abdullayeva", "123", Set.of(RoleName.ROLE_STUDENT), roles);
        createOrUpdateUser("l1.bekzod.rahimov@rspcm.local", "Bekzod Rahimov", "123", Set.of(RoleName.ROLE_STUDENT), roles);

        // Subject teachers (three users)
        createOrUpdateUser("math.teacher@rspcm.local", "Math Teacher", "123", Set.of(RoleName.ROLE_TEACHER), roles);
        createOrUpdateUser("physics.teacher@rspcm.local", "Physics Teacher", "123", Set.of(RoleName.ROLE_TEACHER), roles);
        createOrUpdateUser("programming.teacher@rspcm.local", "Programming Teacher", "123", Set.of(RoleName.ROLE_TEACHER), roles);
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

        assignTeacherProfile(teacherMath, "PhD", 8, Set.of(math));
        assignTeacherProfile(teacherPhysics, "MSc", 6, Set.of(physics));
        assignTeacherProfile(teacherProgramming, "MSc", 5, Set.of(programming));

        assignStudentProfile(k1Student1, 1, "K1-1001");
        assignStudentProfile(k1Student2, 1, "K1-1002");
        assignStudentProfile(k1Student3, 1, "K1-1003");
        assignStudentProfile(k1Student4, 1, "K1-1004");
        assignStudentProfile(k1Student5, 1, "K1-1005");
        assignStudentProfile(l1Student1, 1, "L1-1001");
        assignStudentProfile(l1Student2, 1, "L1-1002");
        assignStudentProfile(l1Student3, 1, "L1-1003");
        assignStudentProfile(l1Student4, 1, "L1-1004");
        assignStudentProfile(l1Student5, 1, "L1-1005");

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
    }

    private void createOrUpdateUser(
            String email,
            String fullName,
            String passwordRaw,
            Set<RoleName> roleNames,
            Map<RoleName, Role> roles
    ) {
        Set<Role> resolvedRoles = roleNames.stream()
                .map(roles::get)
                .collect(Collectors.toSet());

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> User.builder()
                        .email(email)
                        .password(passwordEncoder.encode(passwordRaw))
                        .enabled(true)
                        .build());

        user.setFirstName(extractFirstName(fullName));
        user.setLastName(extractLastName(fullName));
        user.setEnabled(true);
        user.setRoles(new HashSet<>(resolvedRoles));
        User saved = userRepository.save(user);
        userProfileSyncService.sync(saved);
    }

    private Subject createOrUpdateSubject(String name, String description) {
        Subject subject = subjectRepository.findByName(name)
                .orElseGet(() -> Subject.builder().name(name).build());
        subject.setDescription(description);
        return subjectRepository.save(subject);
    }

    private void assignTeacherProfile(User teacher, String degree, Integer experienceYears, Set<Subject> subjects) {
        TeacherProfile profile = teacherProfileRepository.findByUserId(teacher.getId())
                .orElseGet(() -> TeacherProfile.builder().user(teacher).build());
        profile.setAcademicDegree(degree);
        profile.setExperienceYears(experienceYears);
        profile.setTeachingSubjects(new HashSet<>(subjects));
        teacherProfileRepository.save(profile);
    }

    private void assignStudentProfile(User student, Integer course, String studentNumber) {
        StudentProfile profile = studentProfileRepository.findByUserId(student.getId())
                .orElseGet(() -> StudentProfile.builder().user(student).build());
        profile.setCourse(course);
        profile.setStudentNumber(studentNumber);
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
        StudyGroup group = studyGroupRepository.findByName(name)
                .orElseGet(() -> StudyGroup.builder().name(name).build());
        group.setDescription(description);
        group.setLanguage(language);
        group.setSubjects(new HashSet<>(subjects));
        group.setTeachers(new HashSet<>(teachers));
        group.setStudents(new HashSet<>(students));
        studyGroupRepository.save(group);
    }

    private void ensureMinimumQuestions(Subject subject, User teacher, int minimumCount) {
        for (int index = 1; index <= minimumCount; index++) {
            String text = subject.getName() + " question " + index;
            Question question = questionRepository.findBySubjectIdAndText(subject.getId(), text)
                    .orElseGet(() -> Question.builder()
                            .text(text)
                            .subject(subject)
                            .createdBy(teacher)
                            .options(new ArrayList<>())
                            .build());

            question.setText(text);
            question.setSubject(subject);
            question.setCreatedBy(teacher);

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

    private String extractFirstName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "";
        }
        String normalized = fullName.trim().replaceAll("\\s+", " ");
        int firstSpace = normalized.indexOf(' ');
        if (firstSpace < 0) {
            return normalized;
        }
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
