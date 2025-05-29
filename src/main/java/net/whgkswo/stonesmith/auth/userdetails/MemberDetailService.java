package net.whgkswo.stonesmith.auth.userdetails;

import lombok.RequiredArgsConstructor;
import net.whgkswo.stonesmith.auth.CustomAuthorityUtils;
import net.whgkswo.stonesmith.entities.members.Member;
import net.whgkswo.stonesmith.entities.members.MemberRepository;
import net.whgkswo.stonesmith.exception.BusinessLogicException;
import net.whgkswo.stonesmith.exception.ExceptionType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MemberDetailService implements UserDetailsService {
    private final MemberRepository memberRepository;
    private final CustomAuthorityUtils authorityUtils;

    // 이메일로 MemberDetail 조회
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Member> optionalMember = memberRepository.findByEmail(username);
        Member member = optionalMember.orElseThrow(() -> new BusinessLogicException(ExceptionType.MEMBER_NOT_FOUND));

        return new MemberDetails(member);
    }

    private final class MemberDetails extends Member implements UserDetails{
        MemberDetails(Member member) {
            setId(member.getId());
            setEmail(member.getEmail());
            setPassword(member.getPassword());
            setRoles(member.getRoles());
        }

        // 사용자 권한 목록 반환
        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return authorityUtils.createAuthorities(this.getRoles());
        }

        @Override
        public String getUsername() {
            return getEmail();
        }

        // 만료되지 않은 계정인지 검사 (계정 만료 기능 미사용)
        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        // 계정 잠김 상태 검사(계정 잠금 기능 미사용)
        // 의심스러운 로그인 시도 등 보안 위험 시 자동 잠금 기능 사용 가능
        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        // 크리덴셜 만료 상태 검사(크리덴셜 만료 미사용)
        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        // 계정 활성화 여부 검사(계정 비활성화 기능 미사용)
        // 직원 퇴사 및 회원 제재 등 반영구적 계정 비활성화 시 사용 가능
        @Override
        public boolean isEnabled() {
            return true;
        }
    }

}
