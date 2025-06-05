package point.zzicback.challenge.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.zzicback.challenge.domain.ChallengeParticipation;
import point.zzicback.challenge.domain.ChallengeParticipationRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeParticipationService {
    private final ChallengeParticipationRepository participationRepository;

    public List<ChallengeParticipation> findByMemberId(UUID memberId) {
        return participationRepository.findByMemberId(memberId);
    }
}

