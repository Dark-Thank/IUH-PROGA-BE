package com.proga.auth_service.security;

import com.proga.auth_service.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final long id;
    private final String username;
    private final String email;
    private final String password;
    private final Boolean isAdmin;
    private final Collection<? extends GrantedAuthority> authorities;

    public static CustomUserDetails create(User user) {
        String roleName = Boolean.TRUE.equals(user.getIsAdmin()) ? "ROLE_ADMIN" : "ROLE_MEMBER";
        GrantedAuthority authority = new SimpleGrantedAuthority(roleName);

        return new CustomUserDetails(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getIsAdmin(),
                Collections.singletonList(authority));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
