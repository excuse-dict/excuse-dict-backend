package net.whgkswo.excuse_bundle.entities.excuses;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import net.whgkswo.excuse_bundle.entities.BaseEntity;
import net.whgkswo.excuse_bundle.entities.posts.post_core.entity.Post;
import net.whgkswo.excuse_bundle.entities.posts.tags.entity.Tag;
import org.hibernate.annotations.BatchSize;

import java.util.HashSet;
import java.util.Set;

@Entity
@Setter
@Getter
public class Excuse extends BaseEntity {
    private String situation;
    private String excuse;

    @ManyToMany
    @BatchSize(size = 10)
    private Set<Tag> tags = new HashSet<>();

    @OneToOne(mappedBy = "excuse")
    private Post post;

    // Post <-> Excuse
    public void setPost(Post post){
        this.post = post;
        if(post.getExcuse() == null) post.setExcuse(this);
    }
}
