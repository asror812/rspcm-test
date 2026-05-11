package org.example.rspcm.service;

import org.example.rspcm.dto.subject.SubjectRequest;
import org.example.rspcm.dto.subject.SubjectResponse;
import org.example.rspcm.exception.NotFoundException;
import org.example.rspcm.mapper.SubjectMapper;
import org.example.rspcm.model.entity.User;
import org.example.rspcm.model.entity.Subject;
import org.example.rspcm.repository.UserRepository;
import org.example.rspcm.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SubjectService {

    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;

    public List<Subject> findAll() {
        return subjectRepository.findAll();
    }

    public List<SubjectResponse> findAllResponse() {
        return findAll().stream().map(SubjectMapper::toResponse).toList();
    }

    public Subject findById(Long id) {
        return subjectRepository.findById(id).orElseThrow(() -> new NotFoundException("Subject topilmadi: " + id));
    }

    @Transactional
    public Subject create(SubjectRequest request) {
        Subject subject = Subject.builder()
                .name(request.name())
                .description(request.description())
                .teachers(resolveUsers(request.teacherIds()))
                .build();
        return subjectRepository.save(subject);
    }

    public SubjectResponse createResponse(SubjectRequest request) {
        return SubjectMapper.toResponse(create(request));
    }

    @Transactional
    public Subject update(Long id, SubjectRequest request) {
        Subject subject = findById(id);
        subject.setName(request.name());
        subject.setDescription(request.description());
        subject.setTeachers(resolveUsers(request.teacherIds()));
        return subjectRepository.save(subject);
    }

    public SubjectResponse updateResponse(Long id, SubjectRequest request) {
        return SubjectMapper.toResponse(update(id, request));
    }

    @Transactional
    public void delete(Long id) {
        subjectRepository.delete(findById(id));
    }

    private Set<User> resolveUsers(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashSet<>();
        }
        return new HashSet<>(userRepository.findAllById(ids));
    }
}
