package org.example.rspcm.service;

import org.example.rspcm.dto.common.UserSummary;
import org.example.rspcm.dto.group.AdminGroupResponse;
import org.example.rspcm.dto.group.GroupRequest;
import org.example.rspcm.dto.group.GroupResponse;
import org.example.rspcm.dto.group.StudentGroupResponse;
import org.example.rspcm.dto.group.TeacherGroupResponse;
import org.example.rspcm.exception.ErrorCodes;
import org.example.rspcm.exception.ErrorMessageException;
import org.example.rspcm.exception.NotFoundException;
import org.example.rspcm.mapper.GroupMapper;
import org.example.rspcm.model.entity.Subject;
import org.example.rspcm.model.entity.User;
import org.example.rspcm.model.entity.StudyGroup;
import org.example.rspcm.repository.UserRepository;
import org.example.rspcm.repository.SubjectRepository;
import org.example.rspcm.repository.StudyGroupRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StudyGroupService {

    private final StudyGroupRepository groupRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final GroupMapper groupMapper;
    private final GroupChatSyncService groupChatSyncService;

    public Page<AdminGroupResponse> findAllForAdmin(Pageable pageable) {
        return groupRepository.findAllBy(pageable).map(groupMapper::toAdminResponse);
    }

    public StudyGroup findById(Long id) {
        return groupRepository.findById(id).orElseThrow(() -> new NotFoundException("Группа не найдена: " + id));
    }

    public AdminGroupResponse findAdminResponseById(Long id) {
        return groupMapper.toAdminResponse(groupRepository.findById(id).orElseThrow(() -> new NotFoundException("Группа не найдена: " + id)));
    }

    @Transactional
    public StudyGroup create(GroupRequest request) {
        StudyGroup group = groupMapper.toEntity(
                request,
                resolveSubjects(request.subjectIds()),
                resolveUsers(request.teacherIds()),
                resolveUsers(request.studentIds())
        );
        StudyGroup saved = groupRepository.save(group);
        groupChatSyncService.syncForGroup(saved);
        return saved;
    }

    public GroupResponse createResponse(GroupRequest request) {
        StudyGroup group = groupMapper.toEntity(
                request,
                resolveSubjects(request.subjectIds()),
                resolveUsers(request.teacherIds()),
                resolveUsers(request.studentIds())
        );
        StudyGroup saved = groupRepository.save(group);
        groupChatSyncService.syncForGroup(saved);
        return groupMapper.toResponse(saved);
    }

    @Transactional
    public GroupResponse update(Long id, GroupRequest request) {
        StudyGroup group = findById(id);
        groupMapper.updateEntity(
                group,
                request,
                resolveSubjects(request.subjectIds()),
                resolveUsers(request.teacherIds()),
                resolveUsers(request.studentIds())
        );
        StudyGroup saved = groupRepository.save(group);
        groupChatSyncService.syncForGroup(saved);
        return groupMapper.toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        StudyGroup group = findById(id);
        groupRepository.delete(group);
    }

    @Transactional
    public Map<String, Integer> importStudentsFromExcel(Long groupId, MultipartFile file) {
        StudyGroup group = findById(groupId);
        int imported = 0;
        int skipped = 0;
        Set<User> students = new HashSet<>(group.getStudents());
        DataFormatter formatter = new DataFormatter();

        try (XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            
            for (Row row : sheet) {
                Cell cell = row.getCell(0);
                if (cell == null || cell.getCellType() == CellType.BLANK) {
                    continue;
                }
                String value = formatter.formatCellValue(cell).trim();
                if (value.isBlank() || "email".equalsIgnoreCase(value) || "student_number".equalsIgnoreCase(value)) {
                    continue;
                }
                User user = resolveStudentFromCell(value);
                if (user == null) {
                    skipped++;
                    continue;
                }
                if (students.add(user)) {
                    imported++;
                }
            }
        } catch (IOException e) {
            throw new ErrorMessageException("Ошибка чтения Excel файла: " + e.getMessage(), ErrorCodes.InvalidParams);
        }

        group.setStudents(students);
        StudyGroup saved = groupRepository.save(group);
        groupChatSyncService.syncForGroup(saved);
        return Map.of("imported", imported, "skipped", skipped);
    }

    private Set<User> resolveUsers(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashSet<>();
        }
        List<User> users = userRepository.findAllById(ids);
        if (users.size() != ids.size()) {
            throw new NotFoundException("Некоторые пользователи не найдены");
        }
        return new HashSet<>(users);
    }

    private Set<Subject> resolveSubjects(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashSet<>();
        }
        List<Subject> subjects = subjectRepository.findAllById(ids);
        if (subjects.size() != ids.size()) {
            throw new NotFoundException("Некоторые предметы не найдены");
        }
        return new HashSet<>(subjects);
    }

    private User resolveStudentFromCell(String value) {
        if (value.contains("@")) {
            return userRepository.findByEmail(value).orElse(null);
        }
        return userRepository.findByUniversityIdAndEnabledTrueAndDeletedFalse(value)
                .orElse(null);
    }

    public List<TeacherGroupResponse> findOwnTeacherGroups(User user) {
        return groupRepository.findByTeachersId(user.getId()).stream()
                .map(groupMapper::toTeacherResponse).toList();
    }

    public TeacherGroupResponse findOwnTeacherGroupById(Long groupId, User user) {
        StudyGroup group = groupRepository.findByIdAndTeacherId(groupId, user.getId())
                .orElseThrow(() -> new NotFoundException("Группа не найдена: " + groupId));
        return groupMapper.toTeacherResponse(group);
    }

    public List<StudentGroupResponse> findOwnStudentGroups(User user) {
        return groupRepository.findByStudentsId(user.getId()).stream()
                .map(groupMapper::toStudentResponse).toList();
    }

    public List<UserSummary> findGroupMembersForStudent(Long groupId, User requestingStudent) {
        StudyGroup group = groupRepository.findByIdAndStudentId(groupId, requestingStudent.getId())
                .orElseThrow(() -> new NotFoundException("Группа не найдена: " + groupId));
        return group.getStudents().stream()
                .map(s -> new UserSummary(s.getId(), s.getFirstName(), s.getLastName(), s.getEmail()))
                .toList();
    }
}
