package point.zzicback.challenge.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

public class ChallengeVerificationRequest {

    @Schema(description = "인증 이미지", type = "string", format = "binary")
    private MultipartFile proofImage;

    public MultipartFile getProofImage() {
        return proofImage;
    }

    public void setProofImage(MultipartFile proofImage) {
        this.proofImage = proofImage;
    }
}