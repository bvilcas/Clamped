package com.brentcodes.springboot.librarysystem.backendconfig;

import com.brentcodes.springboot.librarysystem.jwtconfig.JwtService;
import com.brentcodes.springboot.librarysystem.project.Project;
import com.brentcodes.springboot.librarysystem.project.ProjectRepository;
import com.brentcodes.springboot.librarysystem.userproject.ProjectRole;
import com.brentcodes.springboot.librarysystem.userproject.UserProject;
import com.brentcodes.springboot.librarysystem.userproject.UserProjectRepository;
import com.brentcodes.springboot.librarysystem.uservulnerability.RoleInVuln;
import com.brentcodes.springboot.librarysystem.uservulnerability.UserVulnerability;
import com.brentcodes.springboot.librarysystem.vulnerability.Severity;
import com.brentcodes.springboot.librarysystem.vulnerability.VulnStatus;
import com.brentcodes.springboot.librarysystem.vulnerability.Vulnerability;
import com.brentcodes.springboot.librarysystem.vulnerability.VulnerabilityRepository;
import com.brentcodes.springboot.librarysystem.uservulnerability.UserVulnerabilityRepository;
import com.brentcodes.springboot.librarysystem.user.Role;
import com.brentcodes.springboot.librarysystem.user.User;
import com.brentcodes.springboot.librarysystem.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;

// Allow us to configure and create test data for Student, Vulnerability, and UserVulnerability (check relationships) classes
// Merged existing BookConfig and StudentConfig classes to prevent ordering problems
@Configuration
public class DataInitializer {
    @Bean // We are not using lombok, so I need to inject the beans into the constructors (@RequiredArgsConstructor)
    CommandLineRunner init(UserRepository userRepository, VulnerabilityRepository vulnerabilityRepository, ProjectRepository projectRepository, JwtService jwtService,
                           UserVulnerabilityRepository userVulnerabilityRepository, PasswordEncoder passwordEncoder,
                           UserProjectRepository userProjectRepository) {
        return args -> {
            // Create Projects and Users

            // === USER ===
            User user1 = new User();
            user1.setEmail("alex@gmail.com");
            user1.setPassword(passwordEncoder.encode("123")); // plain password for testing
            user1.setFirstname("Alex");
            user1.setLastname("Carter");
            user1.setRole(Role.USER);
            userRepository.save(user1);

            User brent = new User();
            brent.setEmail("bvilcas@gmail.com");
            brent.setPassword(passwordEncoder.encode("123")); // plain password for testing
            brent.setFirstname("Brent");
            brent.setLastname("Vilcas");
            brent.setRole(Role.USER);
            userRepository.save(brent);

            User user3 = new User();
            user3.setEmail("jordan@gmail.com");
            user3.setPassword(passwordEncoder.encode("123"));
            user3.setFirstname("Jordan");
            user3.setLastname("Reed");
            user3.setRole(Role.USER);
            userRepository.save(user3);

            User admin = new User();
            admin.setEmail("admin@gmail.com");
            admin.setPassword(passwordEncoder.encode("123"));
            admin.setFirstname("System");
            admin.setLastname("Admin");
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);

// === PROJECTS ===
            Project project1 = new Project(
                    "Authentication Project",
                    "This is the starter project for testing.",
                    Instant.now(),
                    Instant.now()
            );
            projectRepository.save(project1);

            Project project2 = new Project(
                    "Document Security Project",
                    "This contains document access control testing.",
                    Instant.now(),
                    Instant.now()
            );
            projectRepository.save(project2);

// === LINK USER ↔ PROJECT === (WE CAN DO THIS USING CREATE PROJECT METHOD BUT WE HAVE THAT CHECKED)
            UserProject link = new UserProject();
            link.setUser(user1);
            link.setProject(project1);
            link.setRole(ProjectRole.LEAD); // <-- user is project lead
            userProjectRepository.save(link);

            UserProject link1 = new UserProject();
            link1.setUser(brent);
            link1.setProject(project1);
            link1.setRole(ProjectRole.PROGRAMMER);
            userProjectRepository.save(link1);

            UserProject link2 = new UserProject();
            link2.setUser(user3);
            link2.setProject(project2);
            link2.setRole(ProjectRole.LEAD);
            userProjectRepository.save(link2);

/// ADMIN IS AN AUTO LEAD TO EVERYTHING SINCE HE IS ADMIN BY DEFINITION

// CREATE VULNERABILITIES
            Vulnerability v1 = new Vulnerability(
                    "Authentication bypass in MFA",
                    "Attackers can brute-force 2FA codes due to missing retry limits.",
                    "CVE-2025-4101",
                    "CWE-307",
                    Severity.HIGH,
                    VulnStatus.REPORTED,
                    Instant.now().minusSeconds(86400), // reported 1 day ago
                    Instant.parse("2025-12-20T23:59:59Z"),
                    Instant.parse("2025-11-25T17:00:00Z"), // due at
                    null, // patchedAt
                    null,    // verifiedAt
                    "github.com/brentcodes/auth-service",
                    "c88fa91bdd0e4f9e9afb77d502aaf922cbb994d2",
                    project1
            );

            Vulnerability v2 = new Vulnerability(
                    "SQL Injection in search query",
                    "Unsanitized input appended to SQL statement allows data exfiltration.",
                    "CVE-2025-5533",
                    "CWE-89",
                    Severity.CRITICAL,
                    VulnStatus.IN_PROGRESS,
                    Instant.now().minusSeconds(172800), // reported 2 days ago
                    Instant.parse("2025-12-10T23:59:59Z"),
                    null,
                    null,
                    null,
                    "github.com/brentcodes/search-service",
                    "91eeaf2230fc4cb3b21ef1299b7c11cb865fbb90",
                    project1
            );

            Vulnerability v3 = new Vulnerability(
                    "Broken access control in document preview",
                    "Users can view other teams' files by guessing preview IDs.",
                    null,
                    "CWE-639",
                    Severity.HIGH,
                    VulnStatus.REPORTED,
                    Instant.now(),
                    Instant.parse("2025-11-30T23:59:59Z"),
                    null,
                    null,
                    null,
                    "github.com/brentcodes/document-service",
                    "2ab44aa9de991122e34f3d12bb123e88ad6711ac",
                    project2
            );

///  NO USER LINKED TO THIS VULNERABILITY
            Vulnerability v4 = new Vulnerability(
                    "Insecure object reference",
                    "Users can access restricted files by guessing object IDs.",
                    null,
                    "CWE-639",
                    Severity.HIGH,
                    VulnStatus.REPORTED,
                    Instant.now(),
                    Instant.parse("2025-11-30T23:59:59Z"),
                    null,
                    null,
                    null,
                    "github.com/brentcodes/document-service",
                    "2ab44aa9de991122e34f3d12bb123e88ad6711ac",
                    project1
            );

            vulnerabilityRepository.save(v1);
            vulnerabilityRepository.save(v2);
            vulnerabilityRepository.save(v3);
            vulnerabilityRepository.save(v4);

// Create UserVulnerability Links
            UserVulnerability uv1 = new UserVulnerability(
                    user1,
                    v1,
                    RoleInVuln.REPORTER,
                    Instant.now().minusSeconds(86400), // reported 1 day ago
                    false,  // reporters are never "self-assigned"
                    null    // not completed
            );

            UserVulnerability uv2 = new UserVulnerability(
                    brent,
                    v2,
                    RoleInVuln.ASSIGNEE,
                    Instant.now(),
                    true,   // assigned himself as assignee
                    null
            );

            UserVulnerability uv3 = new UserVulnerability(
                    user3,
                    v3,
                    RoleInVuln.VERIFIER,
                    Instant.now(),
                    false,
                    null
            );

            userVulnerabilityRepository.save(uv1);
            userVulnerabilityRepository.save(uv2);
            userVulnerabilityRepository.save(uv3);



            System.out.println("✅✅✅ Ready for Import ✅✅✅");
        };
    }
}
