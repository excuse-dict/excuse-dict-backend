package net.whgkswo.lo8pinggye.entities.members;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.whgkswo.lo8pinggye.entities.TimeStampedEntity;
import net.whgkswo.lo8pinggye.entities.cards.Card;
import net.whgkswo.lo8pinggye.entities.members.rank.Rank;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Member extends TimeStampedEntity {

    private String nickname;

    private String email;

    private String password;

    @OneToMany(mappedBy = "member")
    private final List<Card> cards = new ArrayList<>();

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL)
    private Rank rank;

    // 굳이 Role까지 엔티티로 할 필요 없을 것 같아서 이렇게 함
    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "member_roles", joinColumns = @JoinColumn(name = "member_id"))
    @Column(name = "role")
    private Set<Role> roles = new HashSet<>();

    public Member(String nickname,
                  String email,
                  String password
                ){
        this.nickname = nickname;
        this.email = email;
        this.password = password;
    }

    public void setRank(Rank rank){
        this.rank = rank;
        if(rank.getMember() == null) rank.setMember(this);
    }

    public void addRole(Role role){
        if(!this.roles.contains(role)) roles.add(role);
    }

    public enum Role {
        ADMIN,
        USER
    }
}
