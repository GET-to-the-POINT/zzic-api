package point.zzicback.challenge.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import point.zzicback.challenge.application.dto.response.ChallengeParticipationResponse;
import point.zzicback.challenge.domain.Challenge;
import point.zzicback.challenge.domain.ChallengeParticipation;
import point.zzicback.challenge.domain.ChallengeParticipationRepository;
import point.zzicback.challenge.domain.ChallengeRepository;
import point.zzicback.challenge.infrastructure.LocalFileStorageRepository;
import point.zzicback.member.domain.Member;
import point.zzicback.member.domain.MemberRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ChallengeParticipationService {
    private final ChallengeParticipationRepository participationRepository;
    private final ChallengeRepository challengeRepository;
    private final LocalFileStorageRepository localFileStorageRepository;
    private final MemberRepository memberRepository;

    // 챌린지 참여
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

    // 챌린지 인증
    public void verifyChallenge(UUID memberId, Long challengeId, MultipartFile proofImage) {
        ChallengeParticipation participation = participationRepository
                .findByMemberIdAndChallengeId(memberId, challengeId)
                .orElseThrow(() -> new IllegalArgumentException("Not participating in this challenge"));

        if (participation.isCompleted()) {
            throw new IllegalStateException("Challenge already completed");
        }

        String imagePath = localFileStorageRepository.store(proofImage);
        String fileName = java.nio.file.Paths.get(imagePath).getFileName().toString();
        participation.setProofImageUrl("/uploads/" + fileName);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
        participation.complete(member);
    }

    // 내 챌린지 참여 목록 조회
    @Transactional(readOnly = true)
    public List<ChallengeParticipationResponse> getMyChallenges(UUID memberId) {
        return participationRepository.findByMemberId(memberId).stream()
                .map(ChallengeParticipationResponse::from)
                .toList();
    }

    public List<ChallengeParticipation> findByMemberId(UUID memberId) {
        return participationRepository.findByMemberId(memberId);
    }
}


