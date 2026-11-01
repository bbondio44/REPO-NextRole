package com.nextrole.auth;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import java.util.UUID;
public final class CurrentUser {
    private CurrentUser() {}
    public static UUID id() {
        Object auth=SecurityContextHolder.getContext().getAuthentication();
        if(!(auth instanceof JwtAuthenticationToken token)) throw new org.springframework.security.access.AccessDeniedException("Authentication required");
        return UUID.fromString(token.getToken().getSubject());
    }
}
