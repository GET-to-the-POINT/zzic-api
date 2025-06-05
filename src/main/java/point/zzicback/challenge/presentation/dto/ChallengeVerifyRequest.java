package point.zzicback.challenge.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

@Schema(description = "챌린지 인증 요청")
public record ChallengeVerifyRequest(
        @Schema(description = "인증 사진", type = "string", format = "binary")
        MultipartFile image
) {}
