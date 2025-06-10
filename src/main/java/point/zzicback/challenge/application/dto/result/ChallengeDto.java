package point.zzicback.challenge.application.dto.result;

import java.time.LocalDate;

public record ChallengeDto(
        Long id,
        String title,
        String description,
        LocalDate startDate,
        LocalDate endDate
) {
}