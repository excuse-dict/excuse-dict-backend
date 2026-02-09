package net.whgkswo.excuse_dict.entities.excuses.repository;

import net.whgkswo.excuse_dict.entities.excuses.Excuse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExcuseRepository extends JpaRepository<Excuse, Long> {
}
