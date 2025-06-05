package point.zzicback.challenge.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import point.zzicback.challenge.domain.ChallengeParticipation;
import java.time.LocalDateTime;

@Schema(description = "챌린지 참여 현황 응답")
public record ChallengeParticipationResponse(
        @Schema(description = "참여 ID")
        Long id,
        @Schema(description = "챌린지 제목")
        String challengeTitle,
        @Schema(description = "인증 사진 URL")
        String proofImageUrl,
        @Schema(description = "완료 여부")
        boolean completed,
        @Schema(description = "참여 시각")
        LocalDateTime participatedAt,
        @Schema(description = "성공 시각")
        LocalDateTime successAt
) {
    public static ChallengeParticipationResponse from(ChallengeParticipation participation) {
        return new ChallengeParticipationResponse(
                participation.getId(),
                participation.getChallenge().getTitle(),
                participation.getProofImageUrl(),
                participation.isCompleted(),
                participation.getParticipatedAt(),
                participation.getSuccessAt()
        );
    }
}
