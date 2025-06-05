package point.zzicback.challenge.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import point.zzicback.challenge.domain.Challenge;

@Schema(description = "챌린지 응답")
public record ChallengeResponse(
        @Schema(description = "챌린지 ID")
        Long id,
        @Schema(description = "챌린지 제목")
        String title,
        @Schema(description = "챌린지 설명")
        String description
) {
    public static ChallengeResponse from(Challenge challenge) {
        return new ChallengeResponse(
                challenge.getId(),
                challenge.getTitle(),
                challenge.getDescription()
        );
    }
}
