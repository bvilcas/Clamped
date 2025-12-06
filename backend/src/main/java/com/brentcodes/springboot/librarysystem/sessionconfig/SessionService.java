package com.brentcodes.springboot.librarysystem.sessionconfig;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final JdbcTemplate jdbcTemplate;

    // Invalidate the current session
    public void invalidateCurrentSession(HttpServletRequest httpReq) {
        // Attempts to retrieve the current HTTP session, without creating a new one (false means "don't create if absent").
        // If there’s no session (user is already logged out), it returns null.
        if (httpReq.getCookies() != null) {
            Arrays.stream(httpReq.getCookies())
                    .forEach(c -> System.out.println("🍪 Cookie: " + c.getName() + " = " + c.getValue()));
        } else {
            System.out.println("🚫 No cookies received");
        }

        HttpSession session = httpReq.getSession(false);
        if (session != null) {
            System.out.println("✅ Invalidating session: " + session.getId());
            session.invalidate();
        } else {
            System.out.println("⚠️ No session found for request");
        }
    }

    // Invalidate all sessions for a given user
    public void invalidateAllSessions(Long userId) {
        String sql = """
            DELETE FROM SPRING_SESSION
            WHERE PRIMARY_ID IN (
                SELECT SESSION_PRIMARY_ID 
                FROM SPRING_SESSION_ATTRIBUTES 
                WHERE ATTRIBUTE_NAME = 'userId'
                  AND CAST(ATTRIBUTE_BYTES AS VARCHAR) = ?
            )
            """;
        jdbcTemplate.update(sql, userId.toString());
    }
}

