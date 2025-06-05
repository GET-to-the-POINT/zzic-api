package point.zzicback.todo.application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.zzicback.challenge.application.ChallengeParticipationService;
import point.zzicback.challenge.domain.ChallengeParticipation;
import point.zzicback.common.error.EntityNotFoundException;
import point.zzicback.member.application.MemberService;
import point.zzicback.member.domain.Member;
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

    // Member 조회
    Member member = memberService.findVerifiedMember(query.memberId());

    // 챌린지 참여 항목도 done 상태에 맞게 추가
    List<ChallengeParticipation> participations = participationService.findByMember(member);
    for (ChallengeParticipation participation : participations) {
        System.out.println("Participation found: " + participation.getChallenge().getTitle() + ", done: " + participation.getDone());
        if (Boolean.TRUE.equals(participation.getDone()) == Boolean.TRUE.equals(query.done())) {
            todoResponses.add(new TodoResponse(
                participation.getId(),
                participation.getChallenge().getTitle(),
                "챌린지: " + participation.getChallenge().getDescription(),
                participation.getDone()
            ));
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
