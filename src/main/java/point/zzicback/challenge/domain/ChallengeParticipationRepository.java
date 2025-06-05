package point.zzicback.challenge.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import point.zzicback.member.domain.Member;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChallengeParticipationRepository extends JpaRepository<ChallengeParticipation, Long> {
    Optional<ChallengeParticipation> findByMemberAndChallengeId(Member member, Long challengeId);
    List<ChallengeParticipation> findByMember(Member member);
}


