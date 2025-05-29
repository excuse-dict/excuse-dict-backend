package net.whgkswo.excuse_bundle.entities;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@MappedSuperclass
public class TimeStampedEntity extends BaseEntity {
    private LocalDateTime createdAt = LocalDateTime.now();
    @Setter
    private LocalDateTime modifiedAt = LocalDateTime.now();
}
