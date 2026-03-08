package com.pay.user.adapter.in.security;

import com.pay.user.domain.User;
import com.pay.user.domain.UserStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    // 계정 만료 여부 (true : 만료X)
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // 계정 잠김 여부 (true : 잠김X)
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // 비밀번호 만료 여부 (true: 만료X)
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // 계정 활성화 여부 (true : 활성화)
    @Override
    public boolean isEnabled() {
        return user.getStatus().equals(UserStatus.ACTIVE);
    }
}
