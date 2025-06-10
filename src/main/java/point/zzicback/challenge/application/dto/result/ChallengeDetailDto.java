package point.zzicback.challenge.application.dto.result;

import java.time.LocalDate;
import java.util.List;

public record ChallengeDetailDto(
        Long id,
        String title,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        List<ParticipantDto> participants
) {
}