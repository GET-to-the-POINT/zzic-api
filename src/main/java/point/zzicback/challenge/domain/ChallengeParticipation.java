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

    @Column(nullable = false)
    private Boolean done;

    @ManyToOne
    @JoinColumn(name = "challenge_id")
    private Challenge challenge;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    public ChallengeParticipation(Challenge challenge, Member member) {
        this.challenge = challenge;
        this.member = member;
        this.done = false;
    }

    public void setDone(Boolean done) {
        this.done = done;
    }

    public void complete(Member member) {
        if (!this.member.equals(member)) {
            throw new IllegalArgumentException("Only the participant can complete the challenge");
        }
        this.done = true;
    }

    public boolean isCompleted() {
        return this.done;
    }
}

