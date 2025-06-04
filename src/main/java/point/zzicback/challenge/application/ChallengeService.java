package point.zzicback.challenge.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import point.zzicback.challenge.domain.*;
import point.zzicback.challenge.application.dto.request.ChallengeCreateRequest;
import point.zzicback.challenge.application.dto.response.ChallengeParticipationResponse;
import point.zzicback.challenge.application.dto.response.ChallengeResponse;
import point.zzicback.challenge.infrastructure.FileStorageService;
import point.zzicback.member.domain.Member;
import point.zzicback.member.domain.MemberRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ChallengeService {
    private final ChallengeRepository challengeRepository;
    private final ChallengeParticipationRepository participationRepository;
    private final FileStorageService fileStorageService;
    private final MemberRepository memberRepository;

    public void createChallenge(ChallengeCreateRequest request) {
        Challenge challenge = Challenge.builder()
                .title(request.title())
                .description(request.description())
                .build();
        challengeRepository.save(challenge);
    }

    @Transactional(readOnly = true)
    public List<ChallengeResponse> getChallenges() {
        return challengeRepository.findAll().stream()
                .map(ChallengeResponse::from)
                .toList();
    }

    public void joinChallenge(UUID memberId, Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("Challenge not found"));

        Optional<ChallengeParticipation> existingParticipation = participationRepository
                .findByMemberIdAndChallengeId(memberId, challengeId);
        
        if (existingParticipation.isPresent()) {
            throw new IllegalStateException("Already participating in this challenge");
        }

        ChallengeParticipation participation = new ChallengeParticipation(memberId, challenge);
        participationRepository.save(participation);
    }

    public void verifyChallenge(UUID memberId, Long challengeId, MultipartFile proofImage) {
        ChallengeParticipation participation = participationRepository
                .findByMemberIdAndChallengeId(memberId, challengeId)
                .orElseThrow(() -> new IllegalArgumentException("Not participating in this challenge"));

        if (participation.isCompleted()) {
            throw new IllegalStateException("Challenge already completed");
        }

        String imageUrl = fileStorageService.store(proofImage);
        participation.setProofImageUrl("/uploads/" + imageUrl);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
        participation.complete(member);
    }

    @Transactional(readOnly = true)
    public List<ChallengeParticipationResponse> getMyChallenges(UUID memberId) {
        return participationRepository.findByMemberId(memberId).stream()
                .map(ChallengeParticipationResponse::from)
                .toList();
    }
}
