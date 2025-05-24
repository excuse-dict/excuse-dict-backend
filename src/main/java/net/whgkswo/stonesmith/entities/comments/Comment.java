package net.whgkswo.stonesmith.entities.comments;

import net.whgkswo.stonesmith.entities.Entity;
import net.whgkswo.stonesmith.entities.posts.Post;
import net.whgkswo.stonesmith.entities.users.User;

public class Comment extends Entity {
    private Post post;  // 최상위 댓글인 경우 사용
    private Comment comment;    // 대댓글인 경우 사용
    private User author;
}
