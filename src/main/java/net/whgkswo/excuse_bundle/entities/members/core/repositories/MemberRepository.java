package net.whgkswo.excuse_bundle.entities.members.core.repositories;

import net.whgkswo.excuse_bundle.entities.members.core.entities.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);
    boolean existsByEmail(String email);
}
