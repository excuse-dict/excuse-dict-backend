package net.whgkswo.excuse_bundle.entities;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
public class TimeStampedEntity extends BaseEntity {
    private final LocalDateTime createdAt;

    @Setter
    private LocalDateTime modifiedAt;

    public TimeStampedEntity() {
        // 두 시간이 미세하게 차이나는 것 방지
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.modifiedAt = now;
    }
}
