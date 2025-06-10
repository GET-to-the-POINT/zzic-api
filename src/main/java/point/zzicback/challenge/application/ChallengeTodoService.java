package point.zzicback.challenge.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.zzicback.challenge.domain.Challenge;
import point.zzicback.challenge.domain.ChallengeParticipation;
import point.zzicback.challenge.domain.ChallengeTodo;
import point.zzicback.challenge.infrastructure.ChallengeParticipationRepository;
import point.zzicback.challenge.infrastructure.ChallengeTodoRepository;
import point.zzicback.member.domain.Member;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class ChallengeTodoService {
    private final ChallengeTodoRepository challengeTodoRepository;
    private final ChallengeParticipationRepository participationRepository;
    private final ChallengeService challengeService;

    // 챌린지 Todo 생성
    public void createChallengeTodo(ChallengeParticipation challengeParticipation, Challenge.PeriodType periodType) {
        createChallengeTodo(challengeParticipation, periodType, LocalDate.now());
    }

    // 특정 날짜 기준으로 챌린지 Todo 생성
    public void createChallengeTodo(ChallengeParticipation challengeParticipation, Challenge.PeriodType periodType, LocalDate targetDate) {
        ChallengeTodo challengeTodo = ChallengeTodo.builder()
                .challengeParticipation(challengeParticipation)
                .targetDate(targetDate)
                .build();
        challengeTodoRepository.save(challengeTodo);
    }

    //챌린지 성공
    public void completeChallenge(ChallengeParticipation cp, LocalDate currentDate) {
        ChallengeTodo challengeTodo = challengeTodoRepository
                .findByChallengeParticipation(cp)
                .orElseThrow(() -> new IllegalArgumentException("Challenge todo not found"));

        challengeTodo.complete(currentDate);
        challengeTodoRepository.save(challengeTodo);
    }

    //챌린지 완료 취소
    public void cancelCompleteChallenge(Long challengeId, Member member, LocalDate currentDate) {
        // 챌린지 존재 여부 확인
        challengeService.findById(challengeId);

        // 참여 정보 조회
        ChallengeParticipation participation = participationRepository
                .findByMemberAndChallenge_Id(member, challengeId)
                .orElseThrow(() -> new IllegalArgumentException("해당 챌린지에 참여하지 않았습니다."));

        // Todo 조회
        ChallengeTodo challengeTodo = challengeTodoRepository
                .findByChallengeParticipation(participation)
                .orElseThrow(() -> new IllegalArgumentException("챌린지 Todo를 찾을 수 없습니다."));

        challengeTodo.cancel(currentDate);
        challengeTodoRepository.save(challengeTodo);
    }

    public boolean isCompletedInPeriod(ChallengeParticipation cp, LocalDate date) {
        return challengeTodoRepository
                .findByChallengeParticipation(cp)
                .map(todo -> todo.isInPeriod(cp.getChallenge().getPeriodType(), date) && todo.isCompleted())
                .orElse(false);
    }
}
