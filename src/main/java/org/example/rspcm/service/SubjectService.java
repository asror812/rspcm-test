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

    public List<SubjectResponse> findAll() {
        return subjectRepository.findAll().stream().map(SubjectMapper::toResponse).toList();
    }

    public Subject findById(Long id) {
        return subjectRepository.findById(id).orElseThrow(() -> new NotFoundException("Subject topilmadi: " + id));
    }

    @Transactional
    public Subject create(SubjectRequest request) {
        Subject subject = SubjectMapper.toEntity(request, resolveUsers(request.teacherIds()));
        return subjectRepository.save(subject);
    }

    public SubjectResponse createResponse(SubjectRequest request) {
        Subject subject = SubjectMapper.toEntity(request, resolveUsers(request.teacherIds()));
        return SubjectMapper.toResponse(subjectRepository.save(subject));
    }

    @Transactional
    public SubjectResponse update(Long id, SubjectRequest request) {
        Subject subject = findById(id);
        SubjectMapper.updateEntity(subject, request, resolveUsers(request.teacherIds()));
        return SubjectMapper.toResponse(subjectRepository.save(subject));
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
