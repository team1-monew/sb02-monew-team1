package com.team1.monew.security.config;

import com.team1.monew.security.filter.ExceptionHandlingFilter;
import com.team1.monew.security.filter.HeaderUserAuthenticationFilter;
import com.team1.monew.security.support.ErrorResponseWriter;
import com.team1.monew.security.util.SecurityPathUtil;
import com.team1.monew.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
  private final UserRepository userRepository;
  private final ErrorResponseWriter errorResponseWriter;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    log.info("SecurityFilterChain 설정 시작");

    http
        .formLogin(AbstractHttpConfigurer::disable)
        .httpBasic(AbstractHttpConfigurer::disable)
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(SecurityPathUtil.getExcludePatterns().toArray(new String[0])).permitAll()
            .anyRequest().authenticated()
        )
        .addFilterBefore(new HeaderUserAuthenticationFilter(userRepository), UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(new ExceptionHandlingFilter(errorResponseWriter), HeaderUserAuthenticationFilter.class);

    log.info("SecurityFilterChain 설정 완료");
    return http.build();
  }
}
