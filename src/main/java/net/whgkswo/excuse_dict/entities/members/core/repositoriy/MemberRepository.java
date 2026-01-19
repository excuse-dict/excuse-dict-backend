package net.whgkswo.excuse_dict.entities.members.core.repositoriy;

import net.whgkswo.excuse_dict.entities.members.core.entitiy.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
    boolean existsByEmail(String email);
}
