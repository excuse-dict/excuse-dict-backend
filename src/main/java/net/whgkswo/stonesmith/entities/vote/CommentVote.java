package net.whgkswo.stonesmith.entities.vote;

import net.whgkswo.stonesmith.entities.Entity;
import net.whgkswo.stonesmith.entities.comments.Comment;
import net.whgkswo.stonesmith.entities.users.User;

public class CommentVote extends Entity {
    private VoteType type;
    private Comment comment;
    private User user;
}
