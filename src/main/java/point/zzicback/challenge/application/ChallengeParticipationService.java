package point.zzicback.challenge.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.zzicback.challenge.domain.*;
import point.zzicback.challenge.infrastructure.ChallengeParticipationRepository;
import point.zzicback.challenge.infrastructure.ChallengeTodoRepository;
import point.zzicback.member.domain.Member;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.time.temporal.TemporalAdjusters.previousOrSame;

@Service
@RequiredArgsConstructor
@Transactional
public class ChallengeParticipationService {
    private final ChallengeParticipationRepository participationRepository;
    private final ChallengeTodoRepository challengeTodoRepository;
    private final ChallengeService challengeService;

    // 참여
    public ChallengeParticipation joinChallenge(Long challengeId, Member member) {
        Challenge challenge = challengeService.findById(challengeId);

        if (participationRepository.existsByMemberAndChallenge_Id(member, challengeId)) {
            throw new IllegalStateException("이미 참여중인 챌린지입니다.");
        }

        ChallengeParticipation participation = ChallengeParticipation.builder()
                .challenge(challenge)
                .member(member)
                .build();

        participation = participationRepository.save(participation);
        
        // ChallengeTodo 직접 생성 - PeriodType에 따라 적절한 targetDate 계산
        PeriodType periodType = challenge.getPeriodType();
        if (periodType == null) {
            throw new IllegalStateException("챌린지 주기 타입이 설정되지 않았습니다");
        }
        
        LocalDate targetDate = calculateTargetDate(periodType);
        ChallengeTodo challengeTodo = ChallengeTodo.builder()
                .challengeParticipation(participation)
                .targetDate(targetDate)
                .build();
        challengeTodoRepository.save(challengeTodo);

        return participation;
    }

    // delete 탈퇴
    public void leaveChallenge(Long challengeId, Member member) {
        ChallengeParticipation participation = participationRepository
                .findByMemberAndChallenge_Id(member, challengeId)
                .orElseThrow(() -> new IllegalArgumentException("참여하지 않은 챌린지입니다."));

        // ChallengeTodo 먼저 삭제
        ChallengeTodo todo = challengeTodoRepository.findByChallengeParticipation(participation)
                .orElseThrow(() -> new IllegalStateException("챌린지 Todo를 찾을 수 없습니다."));
        challengeTodoRepository.delete(todo);

        // ChallengeParticipation 삭제
        participationRepository.delete(participation);
    }

    private LocalDate calculateTargetDate(PeriodType periodType) {
        if (periodType == null) {
            throw new IllegalStateException("주기 타입이 설정되지 않았습니다");
        }
        
        LocalDate today = LocalDate.now();
        return switch (periodType) {
            case DAILY -> today;
            case WEEKLY -> {
                LocalDate monday = today.with(previousOrSame(java.time.DayOfWeek.MONDAY));
                yield monday;
            }
            case MONTHLY -> today.withDayOfMonth(1);
        };
    }

    // 참여자가 챌린지 간격에 의해 해야할 챌린지 투두를 출력
    public List<ChallengeTodo> getChallengeTodos(Member member) {
        return participationRepository.findByMember(member)
                .stream()
                .map(participation -> challengeTodoRepository.findByChallengeParticipation(participation))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}

