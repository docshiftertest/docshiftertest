package com.docshifter.core.config;

import org.javers.spring.auditable.AuthorProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Returns the current user name from Spring Security context
 */
public class SpringSecurityAuthorProvider implements AuthorProvider {

    @Override
    public String provide() {
        Authentication auth =  SecurityContextHolder.getContext().getAuthentication();

        if (auth == null) {
            return "unauthenticated";
        }

        // returns the name of the authenticated user
        return auth.getName();
    }
}
