package com.example.Security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {
    // Security Context Holder - information storage about user after successful authentication
    public static  String getSessionUser()
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(!(authentication instanceof AnonymousAuthenticationToken))// user logged in
        {
            String currentUsername =authentication.getName();
            return currentUsername;
        }
        return null; // if user not logged in
    }
}
