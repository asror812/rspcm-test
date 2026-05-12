package org.example.rspcm.service;

import org.example.rspcm.dto.practice.PracticeTeamRequest;
import org.example.rspcm.dto.practice.PracticeTeamResponse;
import org.example.rspcm.exception.ErrorCodes;
import org.example.rspcm.exception.ErrorMessageException;
import org.example.rspcm.exception.NotFoundException;
import org.example.rspcm.model.entity.User;
import org.example.rspcm.model.entity.PracticalTask;
import org.example.rspcm.model.entity.PracticeTeam;
import org.example.rspcm.model.enums.WorkMode;
import org.example.rspcm.repository.UserRepository;
import org.example.rspcm.repository.PracticeRepository;
import org.example.rspcm.repository.PracticeTeamRepository;
import org.example.rspcm.mapper.PracticeTeamMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PracticeTeamService {

    private final PracticeTeamRepository teamRepository;
    private final PracticeRepository practiceRepository;
    private final UserRepository userRepository;

    public List<PracticeTeam> getByPracticeId(Long practiceId) {
        return teamRepository.findByPracticalTaskId(practiceId);
    }

    public List<PracticeTeamResponse> getByPracticeIdResponse(Long practiceId) {
        return teamRepository.findByPracticalTaskId(practiceId).stream().map(PracticeTeamMapper::toResponse).toList();
    }

    @Transactional
    public PracticeTeam create(PracticeTeamRequest request) {
        PracticalTask practicalTask = practiceRepository.findById(request.practiceId())
                .orElseThrow(() -> new NotFoundException("PracticalTask topilmadi: " + request.practiceId()));
        if (practicalTask.getWorkMode() != WorkMode.TEAM) {
            throw new ErrorMessageException("Bu practicalTask individual rejimda", ErrorCodes.BadRequest);
        }
        Set<User> members = new HashSet<>(userRepository.findAllById(request.memberIds()));
        Integer teamSize = practicalTask.getTeamSize();
        if (teamSize != null && members.size() > teamSize) {
            throw new ErrorMessageException("Jamoa a'zolari soni teamSize dan oshib ketdi", ErrorCodes.BadRequest);
        }

        PracticeTeam team = PracticeTeamMapper.toEntity(request, practicalTask, members);
        return teamRepository.save(team);
    }

    public PracticeTeamResponse createResponse(PracticeTeamRequest request) {
        PracticalTask practicalTask = practiceRepository.findById(request.practiceId())
                .orElseThrow(() -> new NotFoundException("PracticalTask topilmadi: " + request.practiceId()));
        if (practicalTask.getWorkMode() != WorkMode.TEAM) {
            throw new ErrorMessageException("Bu practicalTask individual rejimda", ErrorCodes.BadRequest);
        }
        Set<User> members = new HashSet<>(userRepository.findAllById(request.memberIds()));
        Integer teamSize = practicalTask.getTeamSize();
        if (teamSize != null && members.size() > teamSize) {
            throw new ErrorMessageException("Jamoa a'zolari soni teamSize dan oshib ketdi", ErrorCodes.BadRequest);
        }
        PracticeTeam team = PracticeTeamMapper.toEntity(request, practicalTask, members);
        return PracticeTeamMapper.toResponse(teamRepository.save(team));
    }
}
