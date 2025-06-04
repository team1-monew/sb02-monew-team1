package com.team1.monew.security;

import com.team1.monew.user.entity.User;
import java.util.Collection;
import java.util.Collections;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class CustomUserDetails implements UserDetails {

  private final User user;

  public CustomUserDetails(User user) {
    this.user = user;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    // 권한이 따로 없으면 비워도 됨
    return Collections.emptyList();
    // 또는 다음과 같이 역할이 있다면:
    // return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
  }

  @Override
  public String getPassword() {
    return user.getPassword(); // 보통 암호화된 패스워드
  }

  @Override
  public String getUsername() {
    return user.getEmail(); // 로그인 식별자로 사용하는 값
  }

  @Override
  public boolean isAccountNonExpired() {
    return true; // 계정 만료 정책 없으면 true
  }

  @Override
  public boolean isAccountNonLocked() {
    return true; // 계정 잠금 정책 없으면 true
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true; // 비밀번호 만료 정책 없으면 true
  }

  @Override
  public boolean isEnabled() {
    return true; // 활성 사용자만 로그인 허용할 경우에 활용 가능
  }

  // 편의 메서드
  public Long getId() {
    return user.getId();
  }
}