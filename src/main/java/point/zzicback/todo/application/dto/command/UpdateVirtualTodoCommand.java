package point.zzicback.todo.application.dto.command;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

public record UpdateVirtualTodoCommand(
        String virtualTodoId,
        UUID memberId,
        String title,
        String description,
        Boolean complete,
        Integer priorityId,
        Long categoryId,
        LocalDate date,
        LocalTime time,
        Set<String> tags
) {}
