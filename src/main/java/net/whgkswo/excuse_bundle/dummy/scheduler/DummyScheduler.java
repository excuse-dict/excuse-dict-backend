package net.whgkswo.excuse_bundle.dummy.scheduler;

import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_bundle.dummy.member.DummyMemberGenerator;
import net.whgkswo.excuse_bundle.entities.excuses.dto.ExcuseRequestDto;
import net.whgkswo.excuse_bundle.entities.members.core.entitiy.Member;
import net.whgkswo.excuse_bundle.entities.members.core.repositoriy.MemberRepository;
import net.whgkswo.excuse_bundle.entities.members.email.config.AdminEmailConfig;
import net.whgkswo.excuse_bundle.entities.posts.comments.entity.Comment;
import net.whgkswo.excuse_bundle.entities.posts.comments.reply.entity.Reply;
import net.whgkswo.excuse_bundle.entities.posts.comments.reply.repository.ReplyRepository;
import net.whgkswo.excuse_bundle.entities.posts.comments.reply.service.ReplyService;
import net.whgkswo.excuse_bundle.entities.posts.comments.repository.CommentRepository;
import net.whgkswo.excuse_bundle.entities.posts.comments.service.CommentService;
import net.whgkswo.excuse_bundle.entities.posts.core.dto.VoteCommand;
import net.whgkswo.excuse_bundle.entities.posts.core.entity.Post;
import net.whgkswo.excuse_bundle.entities.posts.core.repository.PostRepository;
import net.whgkswo.excuse_bundle.entities.posts.core.service.PostService;
import net.whgkswo.excuse_bundle.entities.vote.entity.VoteType;
import net.whgkswo.excuse_bundle.gemini.prompt.PromptBuilder;
import net.whgkswo.excuse_bundle.gemini.service.GeminiService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class DummyScheduler {

    private final DummyMemberGenerator dummyMemberGenerator;
    private final PostService postService;
    private final CommentService commentService;
    private final ReplyService replyService;
    private final GeminiService geminiService;
    private final PromptBuilder promptBuilder;
    private final Random random = new Random();

    private static final double UPVOTE_CHANCE = 0.8;

    public static final Queue<ExcuseRequestDto> DUMMY_EXCUSES = new LinkedList<>();
    public static final int GENERATE_DUMMY_EXCUSES_AMOUNT = 10;

    // 게시물 자동 등록
    @Scheduled(cron = "0 0 */3 * * *")
    public void createDummyPosts(){
        if(DUMMY_EXCUSES.isEmpty()) {
            String prompt = promptBuilder.buildPostPrompt(GENERATE_DUMMY_EXCUSES_AMOUNT);
            //List<ExcuseRequestDto> excuses = geminiService.generateText(prompt, ExcuseRequestDto.class);
        }
    }

    // 게시물 자동 추천/비추천
    @Scheduled(cron = "0 10 * * * *")
    public void createDummyVote(){

        Member dummyMember = dummyMemberGenerator.createDummyMember();

        // 랜덤 5개 게시물 선정
        List<Post> randomPosts = postService.getRandomPosts(5);

        for(Post post : randomPosts){
            // 일정 확률로 추천/비추천
            VoteCommand command = createDummyVoteCommand(post.getId(), dummyMember.getId());
            postService.voteToPost(command);
        }
    }

    // 댓글 자동 추천/비추천
    @Scheduled(cron = "0 20 * * * *")
    public void createDummyCommentVote(){
        Member dummyMember = dummyMemberGenerator.createDummyMember();

        // 랜덤 3개씩 댓글/답글 선정
        List<Comment> randomComments = commentService.getRandomComments(3);
        List<Reply> randomReplies = replyService.getRandomReplies(3);

        for (Comment comment : randomComments) {
            VoteCommand command = createDummyVoteCommand(comment.getId(), dummyMember.getId());
            commentService.voteToComment(command);
        }
        for (Reply reply : randomReplies){
            VoteCommand command = createDummyVoteCommand(reply.getId(), dummyMember.getId());
            replyService.voteToReplies(command);
        }
    }

    private VoteCommand createDummyVoteCommand(long targetId, long memberId){
        VoteType voteType = random.nextDouble() < UPVOTE_CHANCE ? VoteType.UPVOTE : VoteType.DOWNVOTE;
        return new VoteCommand(targetId, memberId, voteType);
    }
}
