package point.zzicback.challenge.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.zzicback.challenge.application.dto.command.CreateChallengeCommand;
import point.zzicback.challenge.application.dto.command.UpdateChallengeCommand;
import point.zzicback.challenge.presentation.dto.response.ChallengeParticipantsResponse;
import point.zzicback.challenge.presentation.dto.response.ChallengeResponse;
import point.zzicback.challenge.domain.Challenge;
import point.zzicback.challenge.domain.ChallengeParticipationRepository;
import point.zzicback.challenge.domain.ChallengeRepository;
import point.zzicback.challenge.presentation.dto.mapper.ChallengeMapper;
import point.zzicback.common.error.EntityNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final ChallengeParticipationRepository challengeParticipationRepository;
    private final ChallengeMapper challengeMapper;

    //챌린지 생성
    public Long createChallenge(CreateChallengeCommand command) {
        Challenge challenge = Challenge.builder()
                .title(command.title())
                .description(command.description())
                .build();
        return challengeRepository.save(challenge).getId();
    }

    //챌린지 목록 조회
    @Transactional(readOnly = true)
    public List<ChallengeResponse> getChallenges() {
        return challengeRepository.findAll().stream()
                .map(challengeMapper::toResponse)
                .toList();
    }

    //챌린지 업데이트
    public void updateChallenge(Long challengeId, UpdateChallengeCommand command) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("Challenge not found"));
        challenge.update(command.title(), command.description());
        challengeRepository.save(challenge);
    }

    //챌린지 삭제
    public void deleteChallenge(Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("Challenge not found"));
        challengeRepository.delete(challenge);
    }

    /**
     * 특정 챌린지의 참여자 목록을 조회합니다.
     * @param challengeId 조회할 챌린지 ID
     * @return 챌린지 정보와 참여자 목록이 포함된 응답 객체
     * @throws EntityNotFoundException 챌린지가 존재하지 않을 경우
     */
    @Transactional(readOnly = true)
    public List<ChallengeParticipantsResponse> getChallengeParticipants(Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new EntityNotFoundException("Challenge", challengeId));

        return challenge.getParticipations()
                .stream()
                .map(participation ->
                        new ChallengeParticipantsResponse(participation.getChallenge().getId(), participation.getMember())
                )
                .collect(Collectors.toList());
    }
}
