package point.zzicback.challenge.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

import point.zzicback.todo.domain.Todo;
import point.zzicback.member.domain.Member;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class ChallengeParticipation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private UUID memberId;

    @ManyToOne
    @JoinColumn(name = "challenge_id")
    private Challenge challenge;

    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "todo_id")
    private Todo todo;

    private String proofImageUrl;
    private LocalDateTime participatedAt;
    private LocalDateTime successAt;

    public ChallengeParticipation(UUID memberId, Challenge challenge) {
        this.memberId = memberId;
        this.challenge = challenge;
        this.participatedAt = LocalDateTime.now();
    }

    public void setProofImageUrl(String url) {
        this.proofImageUrl = url;
    }

    public void complete(Member member) {
        if (todo == null) {
            this.todo = Todo.builder()
                    .title(challenge.getTitle())
                    .description("챌린지 성공: " + challenge.getDescription())
                    .done(true)
                    .member(member)
                    .build();
            this.successAt = LocalDateTime.now();
        }
    }

    public boolean isCompleted() {
        return todo != null && todo.getDone();
    }
}
