package com.brentcodes.springboot.librarysystem.sessionconfig;

import org.springframework.context.annotation.Configuration;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;

/*
    @EnableJdbcHttpSession(maxInactiveIntervalInSeconds = 1800) or do this in applicationproperties up to u
    - Sets default session timeout (in seconds)
    - 1800 = 30 minutes
    */

@Configuration
public class SessionConfig {
    // You can leave this empty unless customizing

    // ──────────────────────────────────────────────────────────────
    // SessionConfig Customization Options
    // ──────────────────────────────────────────────────────────────

    // ─────────────────────────────────────────
    // Cookie Customization (Optional)
    /*
    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        serializer.setCookieName("SESSIONID");           // Rename cookie (default: JSESSIONID)
        serializer.setCookiePath("/");                   // Usually root
        serializer.setSameSite("Lax");                   // Can be "Strict", "Lax", or "None"
        serializer.setUseSecureCookie(true);             // Send only over HTTPS
        serializer.setUseHttpOnlyCookie(true);           // Prevent JavaScript access
        return serializer;
    }
    */

    // ─────────────────────────────────────────
    // 🛠️ Optional: Customize Repository (Advanced)
    /*
    @Bean
    public JdbcIndexedSessionRepositoryCustomizer customizer() {
        return (repository) -> {
            repository.setDefaultMaxInactiveInterval(1800); // Custom default timeout per session
        };
    }
    */

    // ─────────────────────────────────────────
    //  Rename Session Tables (if needed)
    /*
    @EnableJdbcHttpSession(tableName = "MY_SESSIONS")
    - Overrides the default SPRING_SESSION table name
    - Make sure to update schema in DB too!
    */
}