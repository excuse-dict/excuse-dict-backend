package net.whgkswo.stonesmith.entities.vote;

import net.whgkswo.stonesmith.entities.Entity;
import net.whgkswo.stonesmith.entities.posts.Post;
import net.whgkswo.stonesmith.entities.users.User;

public class PostVote extends Entity {
    VoteType type;
    Post post;
    User user;
}
