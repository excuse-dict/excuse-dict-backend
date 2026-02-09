package net.whgkswo.excuse_dict.entities.excuses;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import net.whgkswo.excuse_dict.entities.BaseEntity;
import net.whgkswo.excuse_dict.entities.posts.post_core.entity.Post;
import net.whgkswo.excuse_dict.entities.posts.tags.entity.Tag;
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

    @ElementCollection
    @CollectionTable(
            name = "excuse_situation_morpheme",
            joinColumns = @JoinColumn(name = "excuse_id"),
            indexes = @Index(name = "idx_situation_morpheme", columnList = "morpheme")
    )
    @Column(name = "morpheme")
    private Set<String> situationMorphemes = new HashSet<>();

    @ElementCollection
    @CollectionTable(
            name = "excuse_excuse_morpheme",
            joinColumns = @JoinColumn(name = "excuse_id"),
            indexes = @Index(name = "idx_excuse_morpheme", columnList = "morpheme")
    )
    @Column(name = "morpheme")
    private Set<String> excuseMorphemes = new HashSet<>();

    // Post <-> Excuse
    public void setPost(Post post){
        this.post = post;
        if(post.getExcuse() == null) post.setExcuse(this);
    }
}
