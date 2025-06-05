package point.zzicback.challenge.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import point.zzicback.member.domain.Member;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChallengeParticipationRepository extends JpaRepository<ChallengeParticipation, Long> {
    Optional<ChallengeParticipation> findByMemberAndChallengeId(Member member, Long challengeId);
    List<ChallengeParticipation> findByMember(Member member);
    List<ChallengeParticipation> findByChallengeId(Long challengeId); // 특정 챌린지에 참여한 목록 조회
}
