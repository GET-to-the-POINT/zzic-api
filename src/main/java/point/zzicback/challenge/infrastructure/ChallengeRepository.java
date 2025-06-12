package point.zzicback.challenge.infrastructure;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import point.zzicback.challenge.domain.Challenge;

import java.util.List;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

    @Query("SELECT DISTINCT c FROM Challenge c LEFT JOIN FETCH c.participations p LEFT JOIN FETCH p.member")
    List<Challenge> findAllWithParticipations();

    @Query(value = "SELECT DISTINCT c FROM Challenge c LEFT JOIN FETCH c.participations p LEFT JOIN FETCH p.member",
           countQuery = "SELECT COUNT(DISTINCT c) FROM Challenge c")
    Page<Challenge> findAllWithParticipations(Pageable pageable);

    @Query("SELECT c FROM Challenge c WHERE c.title ILIKE %:keyword% OR c.description ILIKE %:keyword%")
    Page<Challenge> searchByKeyword(String keyword, Pageable pageable);
}
