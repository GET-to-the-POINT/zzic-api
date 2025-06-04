package point.zzicback.challenge.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChallengeParticipationRepository extends JpaRepository<ChallengeParticipation, Long> {
    Optional<ChallengeParticipation> findByMemberIdAndChallengeId(UUID memberId, Long challengeId);
    List<ChallengeParticipation> findByMemberId(UUID memberId);
}
