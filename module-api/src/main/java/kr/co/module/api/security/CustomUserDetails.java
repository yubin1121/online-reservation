package kr.co.module.api.security;

import kr.co.module.core.domain.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user){
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // User의 Role을 GrantedAuthority 형식으로 변환 (예: ROLE_USER)
        return Collections.singleton(() -> "ROLE_" + user.getRole().name());
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail(); // 일반적으로 이메일이 아이디로 쓰임
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;  // 필요에 따라 로직 구현 가능
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;  // 필요에 따라 로직 구현 가능
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;  // 필요에 따라 로직 구현 가능
    }

    @Override
    public boolean isEnabled() {
        return true;  // 필요에 따라 활성화 상태 체크 구현 가능
    }

    // 도메인 User 객체를 외부에서 참조해야 할 경우 아래 getter 추가 가능
    public User getUser() {
        return user;
    }
}