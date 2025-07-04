package point.zzicback.todo.application.dto.result;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

public record TodoResult(
        String id, 
        String title, 
        String description, 
        Boolean complete,
        Boolean isPinned,
        Integer displayOrder,
        Integer priorityId,
        String priorityName,
        Long categoryId,
        String categoryName,
        LocalDate date,
        LocalTime time,
        Integer repeatType,
        Integer repeatInterval,
        LocalDate repeatEndDate,
        Set<Integer> daysOfWeek,
        Long originalTodoId,
        Set<String> tags
) {
}
