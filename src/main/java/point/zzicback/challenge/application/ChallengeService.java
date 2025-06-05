package point.zzicback.challenge.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.zzicback.challenge.application.dto.request.CreateChallengeCommand;
import point.zzicback.challenge.application.dto.request.UpdateChallengeCommand;
import point.zzicback.challenge.application.dto.response.ChallengeResponse;
import point.zzicback.challenge.domain.Challenge;
import point.zzicback.challenge.domain.ChallengeRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ChallengeService {

    private final ChallengeRepository challengeRepository;

    //챌린지 생성
    public void createChallenge(CreateChallengeCommand command) {
        Challenge challenge = Challenge.builder()
                .title(command.title())
                .description(command.description())
                .build();
        challengeRepository.save(challenge);
    }

    //챌린지 목록 조회
    @Transactional(readOnly = true)
    public List<ChallengeResponse> getChallenges() {
        return challengeRepository.findAll().stream()
                .map(ChallengeResponse::from)
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
    }
}
