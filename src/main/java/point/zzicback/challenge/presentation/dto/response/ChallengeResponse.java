package point.zzicback.challenge.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "챌린지 응답")
public record ChallengeResponse(
        @Schema(description = "챌린지 ID")
        Long id,
        @Schema(description = "챌린지 제목")
        String title,
        @Schema(description = "챌린지 설명")
        String description
) {}
