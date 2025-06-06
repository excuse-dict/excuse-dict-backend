package net.whgkswo.excuse_bundle.entities.posts.core.service;

import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_bundle.auth.service.AuthService;
import net.whgkswo.excuse_bundle.entities.excuses.Excuse;
import net.whgkswo.excuse_bundle.entities.excuses.service.ExcuseService;
import net.whgkswo.excuse_bundle.entities.members.core.entitiy.Member;
import net.whgkswo.excuse_bundle.entities.members.core.service.MemberService;
import net.whgkswo.excuse_bundle.entities.posts.core.entity.Post;
import net.whgkswo.excuse_bundle.entities.posts.core.repository.PostRepository;
import net.whgkswo.excuse_bundle.entities.posts.tags.entity.Tag;
import net.whgkswo.excuse_bundle.entities.posts.tags.repository.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class PostService {
    private final ExcuseService excuseService;
    private final MemberService memberService;
    private final PostRepository postRepository;

    @Transactional
    public Post createPost(long memberId, String situation, String excuseStr, Set<String> tags){

        Member member = memberService.findById(memberId);
        Excuse excuse = excuseService.createExcuse(situation, excuseStr, tags);

        Post post = new Post();

        post.setExcuse(excuse);
        post.setMember(member);

        return postRepository.save(post);
    }
}
