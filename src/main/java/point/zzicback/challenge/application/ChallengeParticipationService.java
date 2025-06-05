package point.zzicback.challenge.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.zzicback.challenge.domain.Challenge;
import point.zzicback.challenge.domain.ChallengeParticipation;
import point.zzicback.challenge.domain.ChallengeParticipationRepository;
import point.zzicback.challenge.domain.ChallengeRepository;
import point.zzicback.member.domain.Member;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ChallengeParticipationService {
    private final ChallengeParticipationRepository participationRepository;
    private final ChallengeRepository challengeRepository;

    // create
    public void joinChallenge(Long challengeId, Member member) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("Challenge not found"));

        Optional<ChallengeParticipation> existingParticipation = participationRepository
                .findByMemberAndChallengeId(member, challengeId);

        if (existingParticipation.isPresent()) {
            throw new IllegalStateException("Already participating in this challenge");
        }

        ChallengeParticipation participation = new ChallengeParticipation(challenge, member);
        participationRepository.save(participation);
    }

    // delete 탈퇴
    public void leaveChallenge(Long challengeId, Member member) {
        ChallengeParticipation participation = participationRepository
                .findByMemberAndChallengeId(member, challengeId)
                .orElseThrow(() -> new IllegalArgumentException("Not participating in this challenge"));

        participationRepository.delete(participation);
    }

    //챌린지 성공
    public void completeChallenge(Long challengeId, Member member) {
        ChallengeParticipation participation = participationRepository
                .findByMemberAndChallengeId(member, challengeId)
                .orElseThrow(() -> new IllegalArgumentException("Not participating in this challenge"));

        if (participation.isCompleted()) {
            throw new IllegalStateException("Challenge already completed");
        }

        participation.complete(member);
    }

    @Transactional(readOnly = true)
    public List<ChallengeParticipation> findByMember(Member member) {
        return participationRepository.findByMember(member);
    }

//    // 챌린지 인증
//    public void verifyChallenge(UUID memberId, Long challengeId, MultipartFile proofImage) {
//        ChallengeParticipation participation = participationRepository
//                .findByMemberIdAndChallengeId(memberId, challengeId)
//                .orElseThrow(() -> new IllegalArgumentException("Not participating in this challenge"));
//
//        if (participation.isCompleted()) {
//            throw new IllegalStateException("Challenge already completed");
//        }
//
//        String imagePath = localFileStorageRepository.store(proofImage);
//        String fileName = java.nio.file.Paths.get(imagePath).getFileName().toString();
//        participation.setProofImageUrl("/uploads/" + fileName);
//
//        Member member = memberRepository.findById(memberId)
//                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
//        participation.complete(member);
//    }
//
//    // 내 챌린지 참여 목록 조회
//    @Transactional(readOnly = true)
//    public List<ChallengeParticipationResponse> getMyChallenges(UUID memberId) {
//        return participationRepository.findByMemberId(memberId).stream()
//                .map(ChallengeParticipationResponse::from)
//                .toList();
//    }
//
}
