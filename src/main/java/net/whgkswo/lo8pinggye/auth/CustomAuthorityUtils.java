package net.whgkswo.lo8pinggye.auth;

import net.whgkswo.lo8pinggye.entities.members.Member;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

// 사용자가 설계한 Role 객체와 스프링에서 사용하는 GrantedAuthority 사이의 변환기
@Component
public class CustomAuthorityUtils {

    // 회원의 role을 받아 GrantedAuthority로 변환
    public List<GrantedAuthority> createAuthorities(Set<Member.Role> roles){
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
    }
    // 문자열 형태의 role을 받아 GrantedAuthority로 변환
    // JwtVerificationFilter에서, JWT토큰으로부터 role을 추출할 때에도 사용
    public List<GrantedAuthority> createAuthorities(String rolesString){
        // ADMIN, USER 등의 문자열 파싱
        return Arrays.stream(rolesString.split(","))
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.trim()))
                .collect(Collectors.toList());
    }
}
