package point.zzicback.challenge.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import point.zzicback.auth.domain.MemberPrincipal;
import point.zzicback.challenge.application.ChallengeService;
import point.zzicback.challenge.application.dto.request.ChallengeCreateRequest;
import point.zzicback.challenge.application.dto.response.ChallengeParticipationResponse;
import point.zzicback.challenge.application.dto.response.ChallengeResponse;
import point.zzicback.challenge.application.dto.request.ChallengeVerificationRequest;

import java.util.List;

@Tag(name = "챌린지", description = "챌린지 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/challenges")
public class ChallengeController {
    private final ChallengeService challengeService;

    @Operation(summary = "챌린지 등록", description = "관리자가 챌린지를 등록합니다.")
    @PostMapping
    public void createChallenge(@RequestBody ChallengeCreateRequest request) {
        challengeService.createChallenge(request);
    }

    @Operation(summary = "챌린지 목록 조회", description = "모든 챌린지 목록을 조회합니다.")
    @GetMapping
    public List<ChallengeResponse> getChallenges() {
        return challengeService.getChallenges();
    }

    @Operation(summary = "챌린지 참여", description = "사용자가 챌린지에 참여합니다.")
    @PostMapping("/{challengeId}/join")
    public void joinChallenge(@PathVariable Long challengeId, @AuthenticationPrincipal MemberPrincipal principal) {
        challengeService.joinChallenge(principal.id(), challengeId);
    }

    @Operation(
            summary = "챌린지 인증",
            description = "사용자가 챌린지 인증(사진 업로드)을 합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = ChallengeVerificationRequest.class)
                    )
            )
    )
    @PostMapping(value = "/{challengeId}/verify", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void verifyChallenge(
            @PathVariable Long challengeId,
            @RequestPart MultipartFile proofImage,
            @AuthenticationPrincipal MemberPrincipal principal
    ) {
        challengeService.verifyChallenge(principal.id(), challengeId, proofImage);
    }

    @Operation(summary = "내 챌린지 현황 조회", description = "내가 참여한 챌린지 현황을 조회합니다.")
    @GetMapping("/my")
    public List<ChallengeParticipationResponse> getMyChallenges(@AuthenticationPrincipal MemberPrincipal principal) {
        return challengeService.getMyChallenges(principal.id());
    }
}

