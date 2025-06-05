package point.zzicback.challenge.presentation.dto.response;

import point.zzicback.member.domain.Member;

public record ChallengeParticipantsResponse(
    Long challengeId,
    Member member,
    Boolean done
) {}
