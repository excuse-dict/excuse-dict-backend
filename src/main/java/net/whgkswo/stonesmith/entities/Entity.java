package net.whgkswo.stonesmith.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
public class Entity {
    private Long id;
    private LocalDateTime createdAt = LocalDateTime.now();
    @Setter
    private LocalDateTime modifiedAt = LocalDateTime.now();
}
