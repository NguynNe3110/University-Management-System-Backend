package com.university.shared.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Technical scope checks. Business modules supply the actual object scope. */
public final class Access {
    private Access() {}

    public static Authentication authentication() {
        var a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !a.isAuthenticated() || "anonymousUser".equals(a.getPrincipal()))
            throw new AccessDeniedException("Authentication required");
        return a;
    }

    public static String username() {
        return authentication().getName();
    }

    public static boolean authority(String value) {
        return authentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(value));
    }

    public static boolean can(String role, String type, String id) {
        return authority("SCOPE_" + role + "|GLOBAL|*")
                || (id != null && authority("SCOPE_" + role + "|" + type + "|" + id));
    }

    public static void require(String role, String type, String id) {
        if (!can(role, type, id))
            throw new AccessDeniedException("Permission outside assigned scope");
    }

    public static boolean self(String kind, String id) {
        if ((kind.equals("STUDENT") || kind.equals("LECTURER")) && !authority("ROLE_" + kind))
            return false;
        return id != null && authority("SELF_" + kind + "|" + id);
    }

    public static String selfId(String kind) {
        if ((kind.equals("STUDENT") || kind.equals("LECTURER")) && !authority("ROLE_" + kind))
            throw new AccessDeniedException("Personal role is no longer active");
        return authentication().getAuthorities().stream()
                .map(a -> a.getAuthority())
                .filter(a -> a.startsWith("SELF_" + kind + "|"))
                .map(a -> a.substring(a.indexOf('|') + 1))
                .findFirst()
                .orElseThrow(() -> new AccessDeniedException("No linked " + kind + " profile"));
    }

    public static void requireSelf(String kind, String id) {
        if (!self(kind, id))
            throw new AccessDeniedException("This record belongs to another person");
    }
}
