package net.whgkswo.excuse_bundle.entities.members.core.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_bundle.auth.redis.RedisKey;
import net.whgkswo.excuse_bundle.auth.service.AuthService;
import net.whgkswo.excuse_bundle.entities.members.core.entitiy.Member;
import net.whgkswo.excuse_bundle.entities.members.core.mapper.MemberMapper;
import net.whgkswo.excuse_bundle.entities.members.core.dtos.MemberRegistrationDto;
import net.whgkswo.excuse_bundle.entities.members.core.repositoriy.MemberRepository;
import net.whgkswo.excuse_bundle.entities.members.email.service.EmailService;
import net.whgkswo.excuse_bundle.entities.members.nicknames.NicknameService;
import net.whgkswo.excuse_bundle.entities.members.rank.MemberRank;
import net.whgkswo.excuse_bundle.exceptions.BusinessLogicException;
import net.whgkswo.excuse_bundle.exceptions.ExceptionType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberMapper memberMapper;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final NicknameService nicknameService;
    private final EmailService emailService;
    private final AuthService authService;

    // 회원가입 시 입력 유효성 검증
    private void validateRegistrationDto(MemberRegistrationDto dto){
        // 이메일 유효성 검사
        emailService.validateEmail(dto.email());

        // 이메일 인증여부 검사
        authService.checkEmailVerified(dto.email(), RedisKey.Prefix.VERIFICATION_COMPLETE_REGISTRATION);

        // 닉네임 유효성 검증
        nicknameService.validateNickname(dto.nickname());
    }

    // 회원가입
    @Transactional
    public long createMember(MemberRegistrationDto dto){
        // 유효성 검증
        validateRegistrationDto(dto);

        Member member = memberMapper.dtoToUser(dto);

        // 비밀번호 암호화 적용
        String encryptedPassword = passwordEncoder.encode(dto.rawPassword());
        member.setPassword(encryptedPassword);

        // 등급 초기화
        MemberRank memberRank = new MemberRank(MemberRank.Type.TADPOLE);
        member.setMemberRank(memberRank);

        // 권한 설정
        authService.giveRoles(member);

        // 리포지토리 세이브
        memberRepository.save(member);

        return member.getId();
    }

    // 이메일로 가입 여부 검사
    public boolean isEmailRegistered(String email){
        return memberRepository.existsByEmail(email);
    }

    // 멤버 찾기 (이메일)
    public Member findByEmail(String email){
        Optional<Member> optionalMember = memberRepository.findByEmail(email);
        return optionalMember.orElseThrow(() -> new BusinessLogicException(ExceptionType.MEMBER_NOT_FOUND_BY_EMAIL));
    }

    // 비밀번호 재설정
    @Transactional
    public void resetPassword(String email, String newPassword){
        // 이메일 인증여부 검증
        authService.checkEmailVerified(email, RedisKey.Prefix.VERIFICATION_COMPLETE_RESET_PASSWORD);

        // 회원 조회
        Member member = findByEmail(email);
        // 비밀번호 암호화
        String encryptedPassword = passwordEncoder.encode(newPassword);
        member.setPassword(encryptedPassword);

        // 멤버 객체 저장
        memberRepository.save(member);
    }
}
