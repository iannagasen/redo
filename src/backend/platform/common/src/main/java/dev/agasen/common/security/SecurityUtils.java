package dev.agasen.common.security;

import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

   private SecurityUtils() {}

   public static String currentUserId() {
      return SecurityContextHolder.getContext().getAuthentication().getName();
   }
}
