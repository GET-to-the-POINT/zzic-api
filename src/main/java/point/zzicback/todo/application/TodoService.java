package point.zzicback.todo.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.zzicback.challenge.application.ChallengeParticipationService;
import point.zzicback.challenge.domain.ChallengeParticipation;
import point.zzicback.common.error.EntityNotFoundException;
import point.zzicback.member.application.MemberService;
import point.zzicback.todo.application.dto.command.CreateTodoCommand;
import point.zzicback.todo.application.dto.command.UpdateTodoCommand;
import point.zzicback.todo.application.dto.query.TodoListQuery;
import point.zzicback.todo.application.dto.query.TodoQuery;
import point.zzicback.todo.application.dto.response.TodoResponse;
import point.zzicback.todo.application.mapper.TodoApplicationMapper;
import point.zzicback.todo.domain.Todo;
import point.zzicback.todo.domain.TodoRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoService {
  private final TodoRepository todoRepository;
  private final MemberService memberService;
  private final TodoApplicationMapper todoApplicationMapper;
  private final ChallengeParticipationService participationService;

  public Page<TodoResponse> getTodoList(TodoListQuery query) {
    Page<Todo> todoPage = todoRepository.findByMemberIdAndDone(query.memberId(), query.done(), query.pageable());
    List<TodoResponse> todoResponses = new ArrayList<>(todoPage.map(todoApplicationMapper::toResponse).getContent());

    // 이미 존재하는 todo의 id를 수집 (중복 방지)
    Set<Long> todoIds = todoPage.stream().map(Todo::getId).collect(Collectors.toSet());

    // 챌린지 참여만 했고 아직 인증하지 않은 경우도 done=false로 추가
    if (!query.done()) {
      List<ChallengeParticipation> participations = participationService.findByMemberId(query.memberId());
      for (ChallengeParticipation participation : participations) {
        // participation.getTodo() == null이고, todoResponses에 없는 경우만 추가
        if (participation.getTodo() == null && !todoIds.contains(participation.getId())) {
          todoResponses.add(new TodoResponse(
            participation.getId(),
            participation.getChallenge().getTitle(),
            "챌린지: " + participation.getChallenge().getDescription(),
            false
          ));
        }
      }
    }
    // 페이징 적용 (메모리 페이징)
    int start = (int) query.pageable().getOffset();
    int end = Math.min((start + query.pageable().getPageSize()), todoResponses.size());
    List<TodoResponse> pageContent = todoResponses.subList(Math.min(start, todoResponses.size()), end);
    return new org.springframework.data.domain.PageImpl<>(pageContent, query.pageable(), todoResponses.size());
  }

  public TodoResponse getTodo(TodoQuery query) {
    return todoRepository.findByIdAndMemberId(query.todoId(), query.memberId()).map(todoApplicationMapper::toResponse)
            .orElseThrow(() -> new EntityNotFoundException("Todo", query.todoId()));
  }

  @Transactional
  public void createTodo(CreateTodoCommand command) {
    var member = memberService.findVerifiedMember(command.memberId());
    Todo todo = todoApplicationMapper.toEntity(command);
    todo.setMember(member);
    todoRepository.save(todo);
  }

  @Transactional
  public void updateTodo(UpdateTodoCommand command) {
    Todo todo = todoRepository.findByIdAndMemberId(command.todoId(), command.memberId())
            .orElseThrow(() -> new EntityNotFoundException("Todo", command.todoId()));
    todoApplicationMapper.updateEntity(command, todo);
  }

  @Transactional
  public void deleteTodo(TodoQuery query) {
    todoRepository.findByIdAndMemberId(query.todoId(), query.memberId())
            .ifPresentOrElse(todo -> todoRepository.deleteById(query.todoId()), () -> {
              throw new EntityNotFoundException("Todo", query.todoId());
            });
  }
}
