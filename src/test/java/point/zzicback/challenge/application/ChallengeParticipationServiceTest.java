package point.zzicback.challenge.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import point.zzicback.challenge.domain.Challenge;
import point.zzicback.challenge.domain.ChallengeParticipationRepository;
import point.zzicback.challenge.domain.ChallengeRepository;
import point.zzicback.member.domain.Member;
import point.zzicback.member.domain.MemberRepository;

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
}
