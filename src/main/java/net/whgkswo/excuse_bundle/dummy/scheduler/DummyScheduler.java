package net.whgkswo.excuse_bundle.dummy.scheduler;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_bundle.dummy.comments.DummyCommentsHelper;
import net.whgkswo.excuse_bundle.dummy.dto.CreateDummyExcuseDto;
import net.whgkswo.excuse_bundle.dummy.member.DummyMemberGenerator;
import net.whgkswo.excuse_bundle.entities.excuses.dto.ExcuseRequestDto;
import net.whgkswo.excuse_bundle.entities.members.core.entitiy.Member;
import net.whgkswo.excuse_bundle.entities.members.core.repositoriy.MemberRepository;
import net.whgkswo.excuse_bundle.entities.members.email.config.AdminEmailConfig;
import net.whgkswo.excuse_bundle.entities.posts.comments.dto.CreateOrUpdateCommentCommand;
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
import net.whgkswo.excuse_bundle.entities.posts.tags.entity.Tag;
import net.whgkswo.excuse_bundle.entities.posts.tags.service.TagService;
import net.whgkswo.excuse_bundle.entities.vote.entity.VoteType;
import net.whgkswo.excuse_bundle.gemini.prompt.PromptBuilder;
import net.whgkswo.excuse_bundle.gemini.service.GeminiService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DummyScheduler {

    private final DummyMemberGenerator dummyMemberGenerator;
    private final PostService postService;
    private final CommentService commentService;
    private final ReplyService replyService;
    private final GeminiService geminiService;
    private final PromptBuilder promptBuilder;
    private final TagService tagService;
    private final DummyCommentsHelper dummyCommentsHelper;
    private final Random random = new Random();

    private static final double UPVOTE_CHANCE = 0.8;

    public static final Queue<ExcuseRequestDto> DUMMY_EXCUSES = new LinkedList<>();
    public static final int GENERATE_DUMMY_EXCUSES_AMOUNT = 10;

    // 게시물 자동 등록
    @Scheduled(cron = "0 0 */3 * * *")
    @Transactional
    public void createDummyPosts(){
        // 남은 거 없으면 채우기
        if(DUMMY_EXCUSES.isEmpty()) {
            String prompt = promptBuilder.buildPostPrompt(GENERATE_DUMMY_EXCUSES_AMOUNT);
            CreateDummyExcuseDto excuses = geminiService.generateText(prompt, CreateDummyExcuseDto.class).block();

            DUMMY_EXCUSES.addAll(excuses.getExcuses());
        }

        // 더미 계정 생성
        Member dummyMember = dummyMemberGenerator.createDummyMember();

        // 하나씩 뽑아서 게시글 등록
        ExcuseRequestDto excuse = DUMMY_EXCUSES.poll();

        // 태그는 그냥 랜덤 선정
        int tagCount = random.nextInt(5); // 0~4 갯수 랜덤
        Set<String> tags = tagService.getRandomTags(tagCount).stream()
                        .map(Tag::getTagKey)
                        .collect(Collectors.toSet());
        postService.createPost(dummyMember.getId(), excuse.situation(), excuse.excuse(), tags);
    }

    // 게시물 자동 추천/비추천
    @Scheduled(cron = "0 10 * * * *")
    @Transactional
    public void createDummyVote(){

        Member dummyMember = dummyMemberGenerator.createDummyMember();

        // Set으로 중복 제거
        Set<Post> posts = new HashSet<>();

        // 최근 7일 간 게시물 중 랜덤 3개
        posts.addAll(postService.getRandomPosts(3, 7));

        // 전체 중 랜덤 2개
        posts.addAll(postService.getRandomPosts(2));

        for(Post post : posts){
            // 일정 확률로 추천/비추천
            VoteCommand command = createDummyVoteCommand(post.getId(), dummyMember.getId());
            postService.voteToPost(command);
        }
    }

    // 댓글 자동 생성
    @Scheduled(cron = "0 20 * * * *")
    @Transactional
    public void createDummyComment(){
        Member dummyMember = dummyMemberGenerator.createDummyMember();

        // set으로 중복 제거
        Set<Post> posts = new HashSet<>();

        // 최근 일주일간 게시글 2개
        posts.addAll(postService.getRandomPosts(2, 7));

        // 전체 게시글 중 하나
        posts.addAll(postService.getRandomPosts(1));

        for(Post post : posts){
            // 랜덤 댓글 하나 가져오기
            String dummyComment = dummyCommentsHelper.getRandomComment();
            // 댓글 작성
            commentService.createComment(new CreateOrUpdateCommentCommand(post.getId(), dummyMember.getId(), dummyComment));
        }
    }

    // 답글 자동 생성
    @Scheduled(cron = "0 30 * * * *")
    @Transactional
    public void createDummyReply(){
        Member dummyMember = dummyMemberGenerator.createDummyMember();

        // set으로 중복 제거
        Set<Comment> comments = new HashSet<>();

        // 최근 일주일간 댓글 3개
        comments.addAll(commentService.getRandomComments(3, 7));

        // 전체 댓글 중 2개
        comments.addAll(commentService.getRandomComments(2));

        for(Comment comment : comments){
            // 랜덤 댓글 하나 가져오기
            String dummyComment = dummyCommentsHelper.getRandomComment();
            // 댓글 작성
            replyService.createReply(new CreateOrUpdateCommentCommand(comment.getId(), dummyMember.getId(), dummyComment));
        }
    }

    // 댓글 자동 추천/비추천
    @Scheduled(cron = "0 40 * * * *")
    @Transactional
    public void createDummyCommentVote(){
        Member dummyMember = dummyMemberGenerator.createDummyMember();

        // Set으로 중복 제거
        Set<Comment> comments = new HashSet<>();

        // 최근 일주일 간 랜덤 댓글 3개
        comments.addAll(commentService.getRandomComments(3, 7));

        // 전체 댓글 중 2개
        comments.addAll(commentService.getRandomComments(2));

        for(Comment comment : comments){
            VoteType voteType = random.nextDouble() < UPVOTE_CHANCE ? VoteType.UPVOTE : VoteType.DOWNVOTE;

            commentService.voteToComment(new VoteCommand(comment.getId(), dummyMember.getId(), voteType));
        }
    }

    // 답글 자동 추천/비추천
    @Scheduled(cron = "0 50 * * * *")
    @Transactional
    public void createDummyReplyVote(){
        Member dummyMember = dummyMemberGenerator.createDummyMember();

        // Set으로 중복 제거
        Set<Reply> replies = new HashSet<>();

        // 최근 일주일 간 랜덤 답글 3개
        replies.addAll(replyService.getRandomReplies(3, 7));

        // 전체 댓글 중 2개
        replies.addAll(replyService.getRandomReplies(2));

        for(Reply reply : replies){
            VoteType voteType = random.nextDouble() < UPVOTE_CHANCE ? VoteType.UPVOTE : VoteType.DOWNVOTE;

            replyService.voteToReplies(new VoteCommand(reply.getId(), dummyMember.getId(), voteType));
        }
    }

    private VoteCommand createDummyVoteCommand(long targetId, long memberId){
        VoteType voteType = random.nextDouble() < UPVOTE_CHANCE ? VoteType.UPVOTE : VoteType.DOWNVOTE;
        return new VoteCommand(targetId, memberId, voteType);
    }
}
