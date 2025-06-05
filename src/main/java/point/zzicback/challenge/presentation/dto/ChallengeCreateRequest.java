package point.zzicback.challenge.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "챌린지 등록 요청")
public record ChallengeCreateRequest(
        @Schema(description = "챌린지 제목", example = "하루 만보 걷기")
        String title,
        @Schema(description = "챌린지 설명", example = "매일 만보를 걸으면 인증!")
        String description
) {}
