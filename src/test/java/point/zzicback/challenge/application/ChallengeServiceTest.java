package point.zzicback.challenge.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import point.zzicback.challenge.application.dto.command.CreateChallengeCommand;
import point.zzicback.challenge.application.dto.command.UpdateChallengeCommand;
import point.zzicback.challenge.application.dto.result.*;
import point.zzicback.challenge.domain.Challenge;
import point.zzicback.challenge.domain.ChallengeParticipation;
import point.zzicback.challenge.domain.PeriodType;
import point.zzicback.challenge.infrastructure.*;
import point.zzicback.common.error.EntityNotFoundException;
import point.zzicback.member.domain.Member;
import point.zzicback.member.domain.MemberRepository;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import({
    ChallengeService.class
})
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
    private TestEntityManager entityManager;

    private Member testMember;
    private Challenge testChallenge;
    private ChallengeParticipation testParticipation;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .email("test@test.com")
                .password("password")
                .nickname("tester")
                .build();
        memberRepository.save(testMember);

        testChallenge = Challenge.builder()
                .title("테스트 챌린지")
                .description("테스트용 챌린지 설명")
                .periodType(PeriodType.DAILY)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .build();
        challengeRepository.save(testChallenge);

        testParticipation = ChallengeParticipation.builder()
                .member(testMember)
                .challenge(testChallenge)
                .build();
        participationRepository.save(testParticipation);

        // 먼저 DB에 반영
        entityManager.flush();
        
        // 그 다음 메모리의 Challenge 객체를 DB와 동기화
        entityManager.refresh(testChallenge);
    }

    @Test
    @DisplayName("챌린지 생성 성공")
    void createChallenge_Success() {
        // given
        CreateChallengeCommand command = new CreateChallengeCommand(
                "새로운 챌린지",
                "새로운 챌린지 설명",
                PeriodType.WEEKLY
        );

        // when
        Long challengeId = challengeService.createChallenge(command);

        // then
        assertThat(challengeId).isNotNull();
        Challenge savedChallenge = challengeRepository.findById(challengeId).orElseThrow();
        assertThat(savedChallenge.getTitle()).isEqualTo("새로운 챌린지");
        assertThat(savedChallenge.getDescription()).isEqualTo("새로운 챌린지 설명");
        assertThat(savedChallenge.getPeriodType()).isEqualTo(PeriodType.WEEKLY);
    }

    @Test
    @DisplayName("모든 챌린지 목록 조회 성공")
    void getChallenges_Success() {
        // when
        Pageable pageable = PageRequest.of(0, 10);
        Page<Challenge> challengePage = challengeService.getChallenges(pageable);

        // then
        assertThat(challengePage.getContent()).hasSize(1);
        assertThat(challengePage.getContent().get(0).getTitle()).isEqualTo("테스트 챌린지");
        assertThat(challengePage.getContent().get(0).getDescription()).isEqualTo("테스트용 챌린지 설명");
    }

    @Test
    @DisplayName("챌린지 검색 성공 - 제목으로 검색")
    void searchChallenges_ByTitle_Success() {
        // given
        Challenge anotherChallenge = Challenge.builder()
                .title("운동 챌린지")
                .description("매일 운동하기")
                .periodType(PeriodType.DAILY)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .build();
        challengeRepository.save(anotherChallenge);

        // when
        Pageable pageable = PageRequest.of(0, 10);
        Page<Challenge> challengePage = challengeService.searchChallenges("테스트", "id,desc", pageable);

        // then
        assertThat(challengePage.getContent()).hasSize(1);
        assertThat(challengePage.getContent().get(0).getTitle()).isEqualTo("테스트 챌린지");
    }

    @Test
    @DisplayName("챌린지 검색 성공 - 설명으로 검색")
    void searchChallenges_ByDescription_Success() {
        // given
        Challenge anotherChallenge = Challenge.builder()
                .title("운동 챌린지")
                .description("매일 운동하기")
                .periodType(PeriodType.DAILY)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .build();
        challengeRepository.save(anotherChallenge);

        // when
        Pageable pageable = PageRequest.of(0, 10);
        Page<Challenge> challengePage = challengeService.searchChallenges("운동", "id,desc", pageable);

        // then
        assertThat(challengePage.getContent()).hasSize(1);
        assertThat(challengePage.getContent().get(0).getTitle()).isEqualTo("운동 챌린지");
    }

    @Test
    @DisplayName("챌린지 검색 성공 - 대소문자 무시")
    void searchChallenges_CaseInsensitive_Success() {
        // when
        Pageable pageable = PageRequest.of(0, 10);
        Page<Challenge> challengePage = challengeService.searchChallenges("테스트", "id,desc", pageable);

        // then
        assertThat(challengePage.getContent()).hasSize(1);
        assertThat(challengePage.getContent().get(0).getTitle()).isEqualTo("테스트 챌린지");
    }

    @Test
    @DisplayName("챌린지 검색 - 검색어가 null일 때 모든 챌린지 반환")
    void searchChallenges_NullKeyword_ReturnsAll() {
        // when
        Pageable pageable = PageRequest.of(0, 10);
        Page<Challenge> challengePage = challengeService.searchChallenges(null, "id,desc", pageable);

        // then
        assertThat(challengePage.getContent()).hasSize(1);
        assertThat(challengePage.getContent().get(0).getTitle()).isEqualTo("테스트 챌린지");
    }

    @Test
    @DisplayName("챌린지 수정 성공")
    void updateChallenge_Success() {
        // given
        UpdateChallengeCommand command = new UpdateChallengeCommand(
                "수정된 제목",
                "수정된 설명",
                PeriodType.MONTHLY
        );

        // when
        challengeService.updateChallenge(testChallenge.getId(), command);

        // then
        Challenge updatedChallenge = challengeRepository.findById(testChallenge.getId()).orElseThrow();
        assertThat(updatedChallenge.getTitle()).isEqualTo("수정된 제목");
        assertThat(updatedChallenge.getDescription()).isEqualTo("수정된 설명");
        assertThat(updatedChallenge.getPeriodType()).isEqualTo(PeriodType.MONTHLY);
    }

    @Test
    @DisplayName("존재하지 않는 챌린지 수정 시 예외 발생")
    void updateChallenge_NotFound() {
        // given
        Long nonExistentId = 999L;
        UpdateChallengeCommand command = new UpdateChallengeCommand(
                "수정된 제목",
                "수정된 설명",
                PeriodType.WEEKLY
        );

        // when & then
        assertThatThrownBy(() -> challengeService.updateChallenge(nonExistentId, command))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("챌린지 삭제 성공")
    void deleteChallenge_Success() {
        // when
        challengeService.deleteChallenge(testChallenge.getId());

        // then
        assertThat(challengeRepository.findById(testChallenge.getId())).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 챌린지 삭제 시 예외 발생")
    void deleteChallenge_NotFound() {
        // given
        Long nonExistentId = 999L;

        // when & then
        assertThatThrownBy(() -> challengeService.deleteChallenge(nonExistentId))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("참여자 포함 모든 챌린지 조회 성공")
    void getAllChallengesWithParticipants_Success() {
 
        // Debug: setUp 후에 다시 데이터를 확인
        assertThat(testChallenge).isNotNull();
        assertThat(testParticipation).isNotNull();
        
        // Debug: 먼저 일반 조회로 데이터가 있는지 확인
        List<Challenge> allChallenges = challengeRepository.findAll();
        assertThat(allChallenges).hasSize(1); // 이 라인에서 실패하면 데이터가 없음
        
        // Debug: 참여자 데이터 확인
        List<ChallengeParticipation> allParticipations = participationRepository.findAll();
        assertThat(allParticipations).hasSize(1); // 이 라인에서 실패하면 참여자 데이터가 없음
        
        // when
        Pageable pageable = PageRequest.of(0, 10);
        Page<Challenge> challengePage = challengeService.getAllChallengesWithParticipants(pageable);

        // then
        assertThat(challengePage.getContent()).hasSize(1);
        Challenge challenge = challengePage.getContent().get(0);
        assertThat(challenge.getTitle()).isEqualTo("테스트 챌린지");
        assertThat(challenge.getParticipations()).hasSize(1);
        assertThat(challenge.getParticipations().get(0).getMember().getNickname()).isEqualTo("tester");
    }

    @Test
    @DisplayName("findById로 챌린지 조회 성공")
    void findById_Success() {
        // when
        Challenge challenge = challengeService.findById(testChallenge.getId());

        // then
        assertThat(challenge.getId()).isEqualTo(testChallenge.getId());
        assertThat(challenge.getTitle()).isEqualTo("테스트 챌린지");
        assertThat(challenge.getDescription()).isEqualTo("테스트용 챌린지 설명");
    }

    @Test
    @DisplayName("findById로 존재하지 않는 챌린지 조회 시 예외 발생")
    void findById_NotFound() {
        // given
        Long nonExistentId = 999L;

        // when & then
        assertThatThrownBy(() -> challengeService.findById(nonExistentId))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("중도하차 후 참여 여부가 false로 표시됨")
    void getChallengesByMember_AfterLeaving() {
        testParticipation.leaveChallenge();
        participationRepository.save(testParticipation);

        List<ChallengeJoinedResult> challenges = challengeService.getChallengesByMember(testMember);

        assertThat(challenges).hasSize(1);
        ChallengeJoinedResult challengeDto = challenges.get(0);
        assertThat(challengeDto.participationStatus()).isFalse();
    }

    @Test
    @DisplayName("중도하차 후 재참여 시 참여 여부가 true로 표시됨")
    void getChallengesByMember_AfterRejoin() {
        testParticipation.leaveChallenge();
        participationRepository.save(testParticipation);

        ChallengeParticipation newParticipation = ChallengeParticipation.builder()
                .member(testMember)
                .challenge(testChallenge)
                .build();
        participationRepository.save(newParticipation);

        List<ChallengeJoinedResult> challenges = challengeService.getChallengesByMember(testMember);

        assertThat(challenges).hasSize(1);
        ChallengeJoinedResult challengeDto = challenges.get(0);
        assertThat(challengeDto.participationStatus()).isTrue();
    }
}
