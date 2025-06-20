package point.zzicback.member.application.dto.command;

import java.util.UUID;

public record UpdateMemberCommand(UUID memberId, String nickname, String introduction) {
    public boolean hasNickname() {
        return nickname != null && !nickname.trim().isEmpty();
    }
    
    public boolean hasIntroduction() {
        return introduction != null;
    }
}
