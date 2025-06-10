package point.zzicback.challenge.application.dto.result;

import java.time.LocalDate;

public record ChallengeJoinedDto(
        Long id,
        String title,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        boolean participationStatus
) {
}