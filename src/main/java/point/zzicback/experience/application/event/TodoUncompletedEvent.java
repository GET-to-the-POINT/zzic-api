package point.zzicback.experience.application.event;

import java.util.UUID;

public record TodoUncompletedEvent(
        UUID memberId,
        Long todoId,
        String todoTitle
) {
}
