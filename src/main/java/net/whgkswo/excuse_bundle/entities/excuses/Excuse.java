package net.whgkswo.excuse_bundle.entities.excuses;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import net.whgkswo.excuse_bundle.entities.BaseEntity;
import net.whgkswo.excuse_bundle.entities.posts.Post;

import java.util.HashSet;
import java.util.Set;

@Entity
public class Excuse extends BaseEntity {
    private String situation;
    private String excuse;
    private Set<String> tags = new HashSet<>();

    @OneToOne(mappedBy = "excuse")
    private Post post;
}
