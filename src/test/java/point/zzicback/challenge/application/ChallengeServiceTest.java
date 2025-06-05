package point.zzicback.challenge.application;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import point.zzicback.challenge.application.dto.command.CreateChallengeCommand;
import point.zzicback.challenge.application.dto.command.UpdateChallengeCommand;
import point.zzicback.challenge.domain.Challenge;
import point.zzicback.challenge.domain.ChallengeParticipation;
import point.zzicback.challenge.domain.ChallengeParticipationRepository;
import point.zzicback.challenge.domain.ChallengeRepository;
import point.zzicback.challenge.presentation.mapper.ChallengeMapper;
import point.zzicback.challenge.presentation.dto.response.ChallengeParticipantsResponse;
import point.zzicback.challenge.presentation.dto.response.ChallengeResponse;
import point.zzicback.member.application.MemberService;
import point.zzicback.member.application.dto.command.CreateMemberCommand;
import point.zzicback.member.domain.Member;
import point.zzicback.member.domain.MemberRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({ChallengeService.class, ChallengeMapper.class, MemberService.class})
class ChallengeServiceTest {

    @Autowired
    private ChallengeService challengeService;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private ChallengeParticipationRepository participationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberService memberService;

    private CreateChallengeCommand createCommand;
    private UpdateChallengeCommand updateCommand;
    private Challenge testChallenge;
    private Member member1;
    private Member member2;
    private ChallengeParticipation testParticipation1;
    private ChallengeParticipation testParticipation2;

    @BeforeEach
    void setUp() {
        // 테스트용 데이터 준비 - ID는 자동 생성되므로 설정 불필요
        testChallenge = Challenge.builder()
                .title("테스트 챌린지")
                .description("테스트 설명")
                .build();

        // 테스트용 챌린지 참여 데이터 생성
        CreateMemberCommand createMemberCommand = new CreateMemberCommand("aa@bb.com", "password", "nickname");
        member1 = memberService.createMember(createMemberCommand);
        CreateMemberCommand createMemberCommand2 = new CreateMemberCommand("aa2@bb.com", "password2", "nickname2");
        member2 = memberService.createMember(createMemberCommand2);

        // 첫 번째 참여자
        testParticipation1 = new ChallengeParticipation(testChallenge, member1);
        testChallenge.getParticipations().add(testParticipation1);

        // 두 번째 참여자 (성공한 참여자)
        testParticipation2 = new ChallengeParticipation(testChallenge, member2);
        testChallenge.getParticipations().add(testParticipation2);

        testChallenge = challengeRepository.save(testChallenge);

        createCommand = new CreateChallengeCommand("새 챌린지", "새 설명");
        updateCommand = new UpdateChallengeCommand("수정된 챌린지", "수정된 설명");
    }

    @Test
    @DisplayName("챌린지 생성 성공")
    void createChallenge_Success() {
        // when
        Long challengeId = challengeService.createChallenge(createCommand);

        // then
        Optional<Challenge> foundChallenge = challengeRepository.findById(challengeId);
        assertThat(foundChallenge).isPresent();
        assertThat(foundChallenge.get().getTitle()).isEqualTo(createCommand.title());
        assertThat(foundChallenge.get().getDescription()).isEqualTo(createCommand.description());
    }

    @Test
    @DisplayName("챌린지 목록 조회 성공")
    void getChallenges_Success() {
        // given
        // setUp에서 이미 testChallenge가 저장됨

        // when
        List<ChallengeResponse> results = challengeService.getChallenges();

        // then
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().title()).isEqualTo(testChallenge.getTitle());
        assertThat(results.getFirst().description()).isEqualTo(testChallenge.getDescription());
    }

    @Test
    @DisplayName("챌린지 업데이트 성공")
    void updateChallenge_Success() {
        // given
        // testChallenge는 setUp에서 이미 저장됨

        // when
        challengeService.updateChallenge(testChallenge.getId(), updateCommand);

        // then
        Optional<Challenge> updatedChallengeOpt = challengeRepository.findById(testChallenge.getId());
        assertThat(updatedChallengeOpt).isPresent();
        Challenge updatedChallenge = updatedChallengeOpt.get();
        assertThat(updatedChallenge.getTitle()).isEqualTo(updateCommand.title());
        assertThat(updatedChallenge.getDescription()).isEqualTo(updateCommand.description());
    }

    // 챌린지 삭제 테스트 (필요시 추가)
    @Test
    @DisplayName("챌린지 삭제 성공")
    void deleteChallenge_Success() {
        // given
        // testChallenge는 setUp에서 이미 저장됨
        Long challengeIdToDelete = testChallenge.getId();

        // when
        challengeService.deleteChallenge(challengeIdToDelete);

        // then
        Optional<Challenge> deletedChallenge = challengeRepository.findById(challengeIdToDelete);
        assertThat(deletedChallenge).isNotPresent();
    }

    @Test
    @DisplayName("챌린지 참여자 목록 조회 성공")
    void getChallengeParticipants_Success() {
        // given
        // setUp에서 이미 testChallenge와 참여자들이 저장됨
        Long challengeId = testChallenge.getId();

        // when
        List<ChallengeParticipantsResponse> response = challengeService.getChallengeParticipants(challengeId);

        // 참여자 정보 확인
        assertThat(response.getFirst().challengeId()).isEqualTo(challengeId);
        assertThat(response.getFirst().member().getId()).isEqualTo(testParticipation1.getMember().getId());

        assertThat(response.get(1).challengeId()).isEqualTo(challengeId);
        assertThat(response.get(1).member().getId()).isEqualTo(testParticipation2.getMember().getId());
    }

    @Test
    @DisplayName("존재하지 않는 챌린지의 참여자 목록 조회 시 예외 발생")
    void getChallengeParticipants_NotFound() {
        // given
        Long nonExistingChallengeId = 9999L;

        // when & then
        assertThatThrownBy(() -> challengeService.getChallengeParticipants(nonExistingChallengeId))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("참여자가 없는 챌린지의 참여자 목록 조회 시 빈 목록 반환")
    void getChallengeParticipants_EmptyList() {
        // given
        // 참여자가 없는 새 챌린지 생성
        Challenge emptyChallenge = Challenge.builder()
                .title("참여자 없는 챌린지")
                .description("아직 아무도 참여하지 않았어요")
                .build();
        Challenge savedEmptyChallenge = challengeRepository.save(emptyChallenge);

        // when
        List<ChallengeParticipantsResponse> response = challengeService.getChallengeParticipants(savedEmptyChallenge.getId());

        // then
        assertThat(response).isEmpty();
    }
}
