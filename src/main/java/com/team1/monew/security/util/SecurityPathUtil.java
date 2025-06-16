package com.team1.monew.security.util;

import org.springframework.util.AntPathMatcher;

import java.util.Set;

public class SecurityPathUtil {
  private static final AntPathMatcher pathMatcher = new AntPathMatcher();

  private static final Set<String> excludePatterns = Set.of(
      "/", "/index.html", "/favicon.ico", "/error",
      "/static/**", "/css/**", "/js/**", "/images/**", "/assets/**",
      "/api/users", "/api/users/login", "/api/health", "/actuator/**"
  );

  public static boolean shouldExclude(String path) {
    return excludePatterns.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
  }

  public static Set<String> getExcludePatterns() {
    return excludePatterns;
  }
}