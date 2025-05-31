package net.whgkswo.excuse_bundle.entities.members;

import lombok.RequiredArgsConstructor;
import net.whgkswo.excuse_bundle.auth.recaptcha.RecaptchaService;
import net.whgkswo.excuse_bundle.auth.service.AuthService;
import net.whgkswo.excuse_bundle.entities.members.email.EmailService;
import net.whgkswo.excuse_bundle.entities.members.nicknames.NicknameService;
import net.whgkswo.excuse_bundle.entities.members.rank.MemberRank;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberMapper memberMapper;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final NicknameService nicknameService;
    private final EmailService emailService;
    private final AuthService authService;
    private final RecaptchaService recaptchaService;

    // 회원가입 시 입력 유효성 검증
    private void validateRegistrationDto(MemberRegistrationDto dto){
        // reCAPTCHA 토큰 검증
        recaptchaService.verifyRecaptcha(dto.recaptchaToken());

        // 이메일 유효성 검사
        emailService.validateEmail(dto.email());

        // 이메일 인증여부 검사
        authService.checkEmailVerified(dto.email());

        // 닉네임 유효성 검증
        nicknameService.validateNickname(dto.nickname());
    }

    // 회원가입
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
}
