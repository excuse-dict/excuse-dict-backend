package net.whgkswo.excuse_bundle.entities.members.core.entitiy;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.whgkswo.excuse_bundle.entities.TimeStampedEntity;
import net.whgkswo.excuse_bundle.entities.members.rank.MemberRank;
import net.whgkswo.excuse_bundle.entities.posts.core.entity.Post;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Member extends TimeStampedEntity {

    private String nickname;

    private String email;

    private String password;

    @OneToMany(mappedBy = "member")
    private final List<Post> posts = new ArrayList<>();

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL)
    private MemberRank memberRank;

    // 굳이 Role까지 엔티티로 할 필요 없을 것 같아서 이렇게 함
    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "member_roles", joinColumns = @JoinColumn(name = "member_id"))
    @Column(name = "role")
    private List<Role> roles = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;

    public Member(String nickname,
                  String email,
                  String password
                ){
        this.nickname = nickname;
        this.email = email;
        this.password = password;
    }

    public void setMemberRank(MemberRank memberRank){
        this.memberRank = memberRank;
        if(memberRank.getMember() == null) memberRank.setMember(this);
    }

    public void addRole(Role role){
        if(!this.roles.contains(role)) roles.add(role);
    }

    public enum Role {
        ADMIN,
        USER
    }

    public enum Status{
        ACTIVE,
        BANNED,
        QUIT
    }
}
