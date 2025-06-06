package net.whgkswo.excuse_bundle.entities.excuses;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;
import net.whgkswo.excuse_bundle.entities.BaseEntity;
import net.whgkswo.excuse_bundle.entities.posts.core.entity.Post;

import java.util.HashSet;
import java.util.Set;

@Entity
@Setter
@Getter
public class Excuse extends BaseEntity {
    private String situation;
    private String excuse;
    private Set<String> tags = new HashSet<>();

    @OneToOne(mappedBy = "excuse")
    private Post post;

    // Post <-> Excuse
    public void setPost(Post post){
        this.post = post;
        if(post.getExcuse() == null) post.setExcuse(this);
    }
}
