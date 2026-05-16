package com.fitlife.gym.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class GymUserPrincipal implements UserDetails {

    private final Long userId;
    private final Long organizationId;
    private final String email;
    private final String passwordHash;
    private final String fullName;
    private final boolean active;
    private final Set<Long> branchIds;
    private final Collection<? extends GrantedAuthority> authorities;

    public GymUserPrincipal(
            Long userId,
            Long organizationId,
            String email,
            String passwordHash,
            String fullName,
            boolean active,
            Set<String> roles,
            Set<Long> branchIds) {
        this.userId = userId;
        this.organizationId = organizationId;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.active = active;
        this.branchIds = branchIds;
        this.authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toSet());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
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
        return active;
    }
}
