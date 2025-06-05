package point.zzicback.challenge.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import point.zzicback.challenge.application.dto.request.CreateChallengeCommand;
import point.zzicback.challenge.application.dto.request.UpdateChallengeCommand;
import point.zzicback.challenge.application.dto.response.ChallengeResponse;
import point.zzicback.challenge.domain.Challenge;
import point.zzicback.challenge.domain.ChallengeRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChallengeServiceTest {

    @Mock
    private ChallengeRepository challengeRepository;

    @InjectMocks
    private ChallengeService challengeService;

    private Challenge testChallenge;
    private CreateChallengeCommand createCommand;
    private UpdateChallengeCommand updateCommand;

    @BeforeEach
    void setUp() {
        // 테스트용 데이터 준비
        testChallenge = Challenge.builder()
                .title("테스트 챌린지")
                .description("테스트 설명")
                .build();
        
        // ID 설정을 위한 리플렉션 사용 (실제로는 더 좋은 방법이 있을 수 있음)
        try {
            java.lang.reflect.Field idField = Challenge.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(testChallenge, 1L);
        } catch (Exception e) {
            e.printStackTrace();
        }

        createCommand = new CreateChallengeCommand("새 챌린지", "새 설명");
        updateCommand = new UpdateChallengeCommand("수정된 챌린지", "수정된 설명");
    }

    @Test
    @DisplayName("챌린지 생성 성공")
    void createChallenge_Success() {
        // given
        when(challengeRepository.save(any(Challenge.class))).thenReturn(testChallenge);

        // when
        challengeService.createChallenge(createCommand);

        // then
        verify(challengeRepository, times(1)).save(any(Challenge.class));
    }

    @Test
    @DisplayName("챌린지 목록 조회 성공")
    void getChallenges_Success() {
        // given
        when(challengeRepository.findAll()).thenReturn(List.of(testChallenge));

        // when
        List<ChallengeResponse> results = challengeService.getChallenges();

        // then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).title()).isEqualTo(testChallenge.getTitle());
        assertThat(results.get(0).description()).isEqualTo(testChallenge.getDescription());
        verify(challengeRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("챌린지 업데이트 성공")
    void updateChallenge_Success() {
        // given
        when(challengeRepository.findById(1L)).thenReturn(Optional.of(testChallenge));

        // when
        challengeService.updateChallenge(1L, updateCommand);

        // then
        assertThat(testChallenge.getTitle()).isEqualTo("수정된 챌린지");
        assertThat(testChallenge.getDescription()).isEqualTo("수정된 설명");
        verify(challengeRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("존재하지 않는 챌린지 업데이트 시 예외 발생")
    void updateChallenge_NotFound_ThrowsException() {
        // given
        when(challengeRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> challengeService.updateChallenge(999L, updateCommand))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Challenge not found");
    }
}
