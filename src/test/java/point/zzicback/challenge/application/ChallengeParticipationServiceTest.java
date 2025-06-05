package point.zzicback.challenge.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import point.zzicback.challenge.domain.Challenge;
import point.zzicback.challenge.domain.ChallengeParticipation;
import point.zzicback.challenge.domain.ChallengeParticipationRepository;
import point.zzicback.challenge.domain.ChallengeRepository;
import point.zzicback.member.domain.Member;
import point.zzicback.member.domain.MemberRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(ChallengeParticipationService.class)
class ChallengeParticipationServiceTest {

    @Autowired
    private ChallengeParticipationService challengeParticipationService;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private ChallengeParticipationRepository participationRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Challenge challenge;
    private Member member;

    @BeforeEach
    void setUp() {
        challenge = challengeRepository.save(
            Challenge.builder()
                .title("테스트 챌린지")
                .description("테스트 설명")
                .build()
        );

        member = memberRepository.save(Member.builder()
                .email("test@example.com")
                .nickname("테스터")
                .password("Test@1234")  // password 필드 추가
                .build());
    }

    @Test
    @DisplayName("챌린지 참여 성공")
    void joinChallenge_Success() {
        // when
        challengeParticipationService.joinChallenge(challenge.getId(), member);

        // then
        assertThat(participationRepository.findByMemberAndChallengeId(member, challenge.getId()))
                .isPresent();
    }

    @Test
    @DisplayName("챌린지 중복 참여시 예외 발생")
    void joinChallenge_DuplicateParticipation() {
        // given
        challengeParticipationService.joinChallenge(challenge.getId(), member);

        // when & then
        assertThatThrownBy(() -> challengeParticipationService.joinChallenge(challenge.getId(), member))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Already participating in this challenge");
    }

    @Test
    @DisplayName("챌린지 참여 취소 성공")
    void leaveChallenge_Success() {
        // given
        challengeParticipationService.joinChallenge(challenge.getId(), member);

        // when
        challengeParticipationService.leaveChallenge(challenge.getId(), member);

        // then
        assertThat(participationRepository.findByMemberAndChallengeId(member, challenge.getId()))
                .isEmpty();
    }

    @Test
    @DisplayName("참여하지 않은 챌린지 취소시 예외 발생")
    void leaveChallenge_NotParticipating() {
        // when & then
        assertThatThrownBy(() -> challengeParticipationService.leaveChallenge(challenge.getId(), member))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Not participating in this challenge");
    }

    //챌린지 성공
    @Test
    @DisplayName("챌린지 성공 처리")
    void completeChallenge_Success() {
        // given
        challengeParticipationService.joinChallenge(challenge.getId(), member);

        // when
        challengeParticipationService.completeChallenge(challenge.getId(), member);

        // then
        ChallengeParticipation participation = participationRepository
                .findByMemberAndChallengeId(member, challenge.getId())
                .orElseThrow();
        assertThat(participation.getDone()).isTrue();
        assertThat(participation.isCompleted()).isTrue();
    }

    @Test
    @DisplayName("참여하지 않은 챌린지 성공 처리시 예외 발생")
    void completeChallenge_NotParticipating() {
        // when & then
        assertThatThrownBy(() -> challengeParticipationService.completeChallenge(challenge.getId(), member))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Not participating in this challenge");
    }

    @Test
    @DisplayName("이미 완료된 챌린지 중복 성공 처리시 예외 발생")
    void completeChallenge_AlreadyCompleted() {
        // given
        challengeParticipationService.joinChallenge(challenge.getId(), member);
        challengeParticipationService.completeChallenge(challenge.getId(), member);

        // when & then
        assertThatThrownBy(() -> challengeParticipationService.completeChallenge(challenge.getId(), member))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Challenge already completed");
    }

    @Test
    @DisplayName("멤버의 챌린지 참여 목록 조회 성공")
    void findByMember_Success() {
        // given
        // 첫 번째 챌린지 생성 및 참여 (setUp에서 이미 생성됨)
        Challenge challenge1 = challenge;
        challengeParticipationService.joinChallenge(challenge1.getId(), member);

        // 두 번째 챌린지 생성 및 참여
        Challenge challenge2 = challengeRepository.save(
            Challenge.builder()
                .title("두 번째 챌린지")
                .description("두 번째 설명")
                .build()
        );
        challengeParticipationService.joinChallenge(challenge2.getId(), member);

        // when
        List<ChallengeParticipation> participations = challengeParticipationService.findByMember(member);

        // then
        assertThat(participations).hasSize(2);
        assertThat(participations)
            .extracting(participation -> participation.getChallenge().getTitle())
            .containsExactlyInAnyOrder("테스트 챌린지", "두 번째 챌린지");

        // 참여 정보의 초기 상태 확인
        assertThat(participations)
            .allSatisfy(participation -> {
                assertThat(participation.getDone()).isFalse(); // 모든 참여가 처음에는 미완료 상태
                assertThat(participation.getMember()).isEqualTo(member); // 모든 참여의 멤버가 동일한지 확인
            });
    }

    @Test
    @DisplayName("멤버의 챌린지 참여 목록 - 참여한 챌린지가 없을 경우 빈 리스트 반환")
    void findByMember_EmptyList() {
        // given
        Member newMember = memberRepository.save(Member.builder()
            .email("new@test.com")
            .nickname("새멤버")
            .password("password")
            .build());

        // when
        List<ChallengeParticipation> participations = challengeParticipationService.findByMember(newMember);

        // then
        assertThat(participations).isEmpty();
    }

    @Test
    @DisplayName("멤버의 챌린지 참여 목록 - 완료 상태 변경 후 조회")
    void findByMember_WithCompletedChallenge() {
        // given
        Challenge challenge = this.challenge;
        challengeParticipationService.joinChallenge(challenge.getId(), member);

        // 챌린지 완료 처리
        challengeParticipationService.completeChallenge(challenge.getId(), member);

        // when
        List<ChallengeParticipation> participations = challengeParticipationService.findByMember(member);

        // then
        assertThat(participations).hasSize(1);
        assertThat(participations.get(0))
            .satisfies(participation -> {
                assertThat(participation.getDone()).isTrue(); // 완료 상태 확인
                assertThat(participation.getChallenge().getTitle()).isEqualTo("테스트 챌린지");
                assertThat(participation.getMember()).isEqualTo(member);
            });
    }
}
