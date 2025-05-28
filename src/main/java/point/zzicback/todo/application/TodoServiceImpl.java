package point.zzicback.todo.application;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import point.zzicback.member.domain.Member;
import point.zzicback.member.persistance.MemberRepository;
import point.zzicback.todo.domain.Todo;
import point.zzicback.todo.persistance.TodoRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TodoServiceImpl implements TodoService {

    private final TodoRepository todoRepository;
    private final MemberRepository memberRepository;

    @Override
    public List<Todo> getTodoList(Boolean done) {
        return todoRepository.findByDone(done);
    }

    @Override
    public List<Todo> getTodoListByMember(UUID memberId, Boolean done) {
        if (done == null) {
            return todoRepository.findByMemberId(memberId);
        }
        return todoRepository.findByMemberIdAndDone(memberId, done);
    }

    @Override
    public Todo getTodoById(Long id) {
        return todoRepository.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "해당 ID의 Todo를 찾을 수 없음: " + id
        ));
    }

    @Override
    public Todo getTodoByMemberIdAndTodoId(UUID memberId, Long todoId) {
        return todoRepository.findByIdAndMember_Id(todoId, memberId) .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "해당 ID의 Todo를 찾을 수 없음: " + todoId
        ));
    }

    @Override
    public void createTodo(UUID memberId, Todo todo) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "해당 ID의 Member를 찾을 수 없음: " + memberId
            ));
        todo.setMember(member);
        todoRepository.save(todo);
    }

    @Override
    public void updateTodo(UUID memberId, Todo todo) {
        Todo existingTodo = todoRepository.findByIdAndMember_Id(todo.getId(), memberId)
                .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "해당 ID의 Todo를 찾을 수 없음: " + todo.getId()
        ));
        if (existingTodo != null) {
            if (todo.getTitle() != null) {
                existingTodo.setTitle(todo.getTitle());
            }
            if (todo.getDescription() != null) {
                existingTodo.setDescription(todo.getDescription());
            }
            if (todo.getDone() != null) {
                existingTodo.setDone(todo.getDone());
            }
            todoRepository.save(existingTodo);
        }
    }

    @Override
    public void deleteTodo(UUID memberId, Long id) {

        Todo todo = todoRepository.findByIdAndMember_Id(id, memberId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Todo를 찾을 수 없습니다: id=" + id
                ));

        todoRepository.delete(todo);
    }
}
