package org.example.rspcm.service;

import lombok.RequiredArgsConstructor;
import org.example.rspcm.model.entity.Chat;
import org.example.rspcm.model.entity.ChatMember;
import org.example.rspcm.model.entity.Subject;
import org.example.rspcm.model.entity.StudyGroup;
import org.example.rspcm.model.entity.User;
import org.example.rspcm.model.enums.ChatMemberRole;
import org.example.rspcm.model.enums.ChatType;
import org.example.rspcm.repository.ChatMemberRepository;
import org.example.rspcm.repository.ChatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupChatSyncService {

    private final ChatRepository chatRepository;
    private final ChatMemberRepository chatMemberRepository;

    @Transactional
    public void syncForGroup(StudyGroup group) {
        Chat studentGroupChat = getOrCreateGroupChat(group, ChatType.STUDENT_GROUP);

        syncStudentGroupChatMembers(studentGroupChat, group.getStudents());
        syncTeacherSubjectChats(group);
        syncSubjectGroupChats(group);
    }

    @Transactional
    public void syncForAllGroups(List<StudyGroup> groups) {
        for (StudyGroup group : groups) {
            syncForGroup(group);
        }
    }

    private Chat getOrCreateGroupChat(StudyGroup group, ChatType type) {
        String title = buildChatTitle(group, type);
        return chatRepository.findByStudyGroupIdAndTypeAndTitle(group.getId(), type, title)
                .orElseGet(() -> {
                    List<Chat> existing = chatRepository.findAllByStudyGroupIdAndTypeOrderByIdAsc(group.getId(), type);
                    if (!existing.isEmpty()) {
                        Chat chat = existing.get(0);
                        chat.setTitle(title);
                        return chatRepository.save(chat);
                    }
                    Chat created = new Chat();
                    created.setTitle(title);
                    created.setStudyGroup(group);
                    created.setType(type);
                    return chatRepository.save(created);
                });
    }

    private void syncStudentGroupChatMembers(Chat chat, Set<User> students) {
        chatMemberRepository.deleteByChatIdAndRole(chat.getId(), ChatMemberRole.TEACHER);
        syncRoleMembers(chat, ChatMemberRole.STUDENT, students);
    }

    private void syncSubjectGroupChats(StudyGroup group) {
        Map<String, Subject> subjectsByTitle = group.getSubjects().stream()
                .collect(Collectors.toMap(subject -> buildSubjectChatTitle(group, subject), Function.identity()));

        List<Chat> existingSubjectChats = chatRepository.findAllByStudyGroupIdAndTypeOrderByIdAsc(group.getId(), ChatType.SUBJECT_GROUP);
        Set<String> expectedTitles = subjectsByTitle.keySet();

        for (Chat existing : existingSubjectChats) {
            if (expectedTitles.contains(existing.getTitle())) {
                continue;
            }
            chatMemberRepository.deleteByChatId(existing.getId());
            chatRepository.delete(existing);
        }

        for (Map.Entry<String, Subject> entry : subjectsByTitle.entrySet()) {
            String title = entry.getKey();
            Subject subject = entry.getValue();
            Chat chat = chatRepository.findByStudyGroupIdAndTypeAndTitle(group.getId(), ChatType.SUBJECT_GROUP, title)
                    .orElseGet(() -> {
                        Chat created = new Chat();
                        created.setTitle(title);
                        created.setStudyGroup(group);
                        created.setType(ChatType.SUBJECT_GROUP);
                        return chatRepository.save(created);
                    });

            Set<User> subjectTeachersForGroup = subject.getTeachers().stream()
                    .filter(group.getTeachers()::contains)
                    .collect(Collectors.toSet());

            syncRoleMembers(chat, ChatMemberRole.TEACHER, subjectTeachersForGroup);
            syncRoleMembers(chat, ChatMemberRole.STUDENT, group.getStudents());
        }
    }

    private void syncTeacherSubjectChats(StudyGroup group) {
        Map<String, Subject> subjectsByTitle = group.getSubjects().stream()
                .collect(Collectors.toMap(subject -> buildTeacherSubjectChatTitle(group, subject), Function.identity()));

        List<Chat> existingTeacherChats = chatRepository.findAllByStudyGroupIdAndTypeOrderByIdAsc(group.getId(), ChatType.TEACHER_GROUP);
        Set<String> expectedTitles = subjectsByTitle.keySet();

        for (Chat existing : existingTeacherChats) {
            if (expectedTitles.contains(existing.getTitle())) {
                continue;
            }
            chatMemberRepository.deleteByChatId(existing.getId());
            chatRepository.delete(existing);
        }

        for (Map.Entry<String, Subject> entry : subjectsByTitle.entrySet()) {
            String title = entry.getKey();
            Subject subject = entry.getValue();
            Chat chat = chatRepository.findByStudyGroupIdAndTypeAndTitle(group.getId(), ChatType.TEACHER_GROUP, title)
                    .orElseGet(() -> {
                        Chat created = new Chat();
                        created.setTitle(title);
                        created.setStudyGroup(group);
                        created.setType(ChatType.TEACHER_GROUP);
                        return chatRepository.save(created);
                    });

            Set<User> subjectTeachersForGroup = subject.getTeachers().stream()
                    .filter(group.getTeachers()::contains)
                    .collect(Collectors.toSet());

            syncRoleMembers(chat, ChatMemberRole.TEACHER, subjectTeachersForGroup);
            chatMemberRepository.deleteByChatIdAndRole(chat.getId(), ChatMemberRole.STUDENT);
        }
    }

    private void syncRoleMembers(Chat chat, ChatMemberRole role, Set<User> users) {
        Set<Long> expectedUserIds = users.stream().map(User::getId).collect(Collectors.toSet());

        if (expectedUserIds.isEmpty()) {
            chatMemberRepository.deleteByChatIdAndRole(chat.getId(), role);
            return;
        }

        chatMemberRepository.deleteByChatIdAndRoleAndUserIdNotIn(chat.getId(), role, expectedUserIds);
        for (User user : users) {
            if (chatMemberRepository.existsByChatIdAndUserId(chat.getId(), user.getId())) {
                continue;
            }
            ChatMember member = new ChatMember();
            member.setChat(chat);
            member.setUser(user);
            member.setRole(role);
            chatMemberRepository.save(member);
        }
    }

    private String buildChatTitle(StudyGroup group, ChatType type) {
        return switch (type) {
            case STUDENT_GROUP -> "Group " + group.getName();
            case TEACHER_GROUP -> throw new IllegalArgumentException("Use buildTeacherSubjectChatTitle for TEACHER_GROUP");
            case SUBJECT_GROUP -> throw new IllegalArgumentException("Use buildSubjectChatTitle for SUBJECT_GROUP");
            case DIRECT -> throw new IllegalArgumentException("DIRECT chat type is not supported for group sync");
        };
    }

    private String buildSubjectChatTitle(StudyGroup group, Subject subject) {
        return group.getName() + " - " + subject.getName();
    }

    private String buildTeacherSubjectChatTitle(StudyGroup group, Subject subject) {
        return "Teachers " + group.getName() + " - " + subject.getName();
    }
}
