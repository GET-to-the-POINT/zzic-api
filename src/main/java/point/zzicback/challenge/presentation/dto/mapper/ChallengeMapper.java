package point.zzicback.challenge.presentation.dto.mapper;

import org.springframework.stereotype.Component;
import point.zzicback.challenge.domain.Challenge;
import point.zzicback.challenge.presentation.dto.response.ChallengeResponse;

@Component
public class ChallengeMapper {
    public ChallengeResponse toResponse(Challenge challenge) {
        return new ChallengeResponse(
                challenge.getId(),
                challenge.getTitle(),
                challenge.getDescription()
        );
    }
}

