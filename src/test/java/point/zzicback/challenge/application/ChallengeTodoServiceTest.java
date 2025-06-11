package point.zzicback.challenge.application;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.*;
import org.springframework.context.annotation.Import;
import point.zzicback.challenge.application.dto.result.ChallengeTodoDto;
import point.zzicback.challenge.application.mapper.ChallengeApplicationMapperImpl;
import point.zzicback.challenge.domain.*;
import point.zzicback.challenge.infrastructure.*;
import point.zzicback.member.domain.*;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import({
    ChallengeTodoService.class,
    ChallengeService.class,
    ChallengeApplicationMapperImpl.class
})
class ChallengeTodoServiceTest {

    @Autowired
    private ChallengeTodoService challengeTodoService;

    @Autowired
    private ChallengeTodoRepository challengeTodoRepository;

    @Autowired
    private ChallengeParticipationRepository participationRepository;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Member testMember;
    private List<ChallengeParticipation> allParticipations;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .email("test@test.com")
                .password("password")
                .nickname("tester")
                .build();
        memberRepository.save(testMember);
        
        allParticipations = List.of(
            createChallengeWithParticipation(PeriodType.DAILY, "일간 챌린지"),
            createChallengeWithParticipation(PeriodType.WEEKLY, "주간 챌린지"),
            createChallengeWithParticipation(PeriodType.MONTHLY, "월간 챌린지")
        );
    }

    private ChallengeParticipation createChallengeWithParticipation(PeriodType periodType, String title) {
        var challenge = Challenge.builder()
                .title(title)
                .description(title + " 설명")
                .periodType(periodType)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .build();
        challengeRepository.save(challenge);

        var participation = ChallengeParticipation.builder()
                .member(testMember)
                .challenge(challenge)
                .build();
        participation = participationRepository.save(participation);
        
        // ChallengeTodo도 함께 생성 (실제 서비스 로직과 동일하게)
        LocalDate targetDate = calculateTargetDate(periodType);
        ChallengeTodo challengeTodo = ChallengeTodo.builder()
                .challengeParticipation(participation)
                .targetDate(targetDate)
                .build();
        challengeTodoRepository.save(challengeTodo);
        
        return participation;
    }

    private LocalDate calculateTargetDate(PeriodType periodType) {
        LocalDate today = LocalDate.now();
        return switch (periodType) {
            case DAILY -> today;
            case WEEKLY -> {
                LocalDate monday = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));
                yield monday;
            }
            case MONTHLY -> today.withDayOfMonth(1);
        };
    }

    private void completeChallengeTodo(ChallengeParticipation participation) {
        challengeTodoService.completeChallenge(participation, LocalDate.now());
        entityManager.flush();
    }

    @Test
    @DisplayName("챌린지 완료 처리")
    void completeChallenge() {
        var participation = allParticipations.get(0);
        
        challengeTodoService.completeChallenge(participation, LocalDate.now());

        var todos = challengeTodoRepository.findAll();
        assertThat(todos).hasSize(3); // 3개의 참여(DAILY, WEEKLY, MONTHLY)
        
        // 완료된 todo 찾기
        var completedTodo = todos.stream()
                .filter(todo -> todo.getChallengeParticipation().getId().equals(participation.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(completedTodo.getDone()).isTrue();
    }

    @Test
    @DisplayName("챌린지 완료 취소")
    void cancelCompleteChallenge() {
        var participation = allParticipations.get(0);
        completeChallengeTodo(participation);
        
        challengeTodoService.cancelCompleteChallenge(participation.getChallenge().getId(), testMember, LocalDate.now());

        var todos = challengeTodoRepository.findAll();
        assertThat(todos).hasSize(3); // 3개의 참여(DAILY, WEEKLY, MONTHLY)에서 모든 todo가 유지됨
        assertThat(todos.get(0).getDone()).isFalse(); // 첫 번째 todo가 취소되어 done이 false
    }

    @Test
    @DisplayName("미완료 투두만 있는 경우 - DAILY, WEEKLY, MONTHLY")
    void getTodos_OnlyIncomplete() {
        var allTodos = challengeTodoService.getAllChallengeTodos(testMember);
        var uncompletedTodos = challengeTodoService.getUncompletedChallengeTodos(testMember);

        assertThat(allTodos).hasSize(3);
        assertThat(uncompletedTodos).hasSize(3);
        assertThat(allTodos.stream().allMatch(dto -> !dto.done())).isTrue();
        assertThat(allTodos.stream().allMatch(dto -> !dto.isPersisted())).isTrue();

        var periodTypes = allTodos.stream().map(ChallengeTodoDto::periodType).toList();
        assertThat(periodTypes).containsExactlyInAnyOrder(PeriodType.DAILY, PeriodType.WEEKLY, PeriodType.MONTHLY);
    }

    @Test
    @DisplayName("완료된 투두만 있는 경우 - DAILY, WEEKLY, MONTHLY")
    void getTodos_OnlyCompleted() {
        allParticipations.forEach(this::completeChallengeTodo);

        var allTodos = challengeTodoService.getAllChallengeTodos(testMember);
        var uncompletedTodos = challengeTodoService.getUncompletedChallengeTodos(testMember);

        assertThat(allTodos).hasSize(3);
        assertThat(uncompletedTodos).isEmpty();
        assertThat(allTodos.stream().allMatch(ChallengeTodoDto::done)).isTrue();
        assertThat(allTodos.stream().allMatch(ChallengeTodoDto::isPersisted)).isTrue();

        var periodTypes = allTodos.stream().map(ChallengeTodoDto::periodType).toList();
        assertThat(periodTypes).containsExactlyInAnyOrder(PeriodType.DAILY, PeriodType.WEEKLY, PeriodType.MONTHLY);
    }

    @Test
    @DisplayName("완료/미완료 투두 혼재 - DAILY, WEEKLY, MONTHLY")
    void getTodos_Mixed() {
        completeChallengeTodo(allParticipations.get(0)); // DAILY만 완료

        var allTodos = challengeTodoService.getAllChallengeTodos(testMember);
        var uncompletedTodos = challengeTodoService.getUncompletedChallengeTodos(testMember);

        assertThat(allTodos).hasSize(3);
        assertThat(uncompletedTodos).hasSize(2);

        var completedTodos = allTodos.stream().filter(ChallengeTodoDto::done).toList();
        var incompleteTodos = allTodos.stream().filter(dto -> !dto.done()).toList();

        assertThat(completedTodos).hasSize(1);
        assertThat(incompleteTodos).hasSize(2);
        assertThat(completedTodos.get(0).periodType()).isEqualTo(PeriodType.DAILY);
        assertThat(completedTodos.get(0).isPersisted()).isTrue();

        var incompletePeriodTypes = incompleteTodos.stream().map(ChallengeTodoDto::periodType).toList();
        assertThat(incompletePeriodTypes).containsExactlyInAnyOrder(PeriodType.WEEKLY, PeriodType.MONTHLY);
    }

    @Test
    @DisplayName("예외 처리 - 참여하지 않은 챌린지")
    void cancelCompleteChallenge_NotParticipating() {
        var anotherMember = Member.builder()
                .email("another@test.com")
                .password("password")
                .nickname("another")
                .build();
        memberRepository.save(anotherMember);

        var participation = allParticipations.get(0);

        assertThatThrownBy(() -> 
            challengeTodoService.cancelCompleteChallenge(participation.getChallenge().getId(), anotherMember, LocalDate.now())
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("해당 챌린지에 참여하지 않았습니다.");
    }

    @Test
    @DisplayName("예외 처리 - Todo 찾을 수 없음")
    void cancelCompleteChallenge_TodoNotFound() {
        var participation = allParticipations.get(0);

        assertThatThrownBy(() -> 
            challengeTodoService.cancelCompleteChallenge(participation.getChallenge().getId(), testMember, LocalDate.now())
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("챌린지 Todo를 찾을 수 없습니다.");
    }
}
