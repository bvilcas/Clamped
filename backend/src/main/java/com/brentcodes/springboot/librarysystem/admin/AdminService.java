package com.brentcodes.springboot.librarysystem.admin;

import com.brentcodes.springboot.librarysystem.project.Project;
import com.brentcodes.springboot.librarysystem.project.ProjectRepository;
import com.brentcodes.springboot.librarysystem.userproject.UserProjectRepository;
import com.brentcodes.springboot.librarysystem.uservulnerability.*;
import com.brentcodes.springboot.librarysystem.vulnerability.Severity;
import com.brentcodes.springboot.librarysystem.vulnerability.VulnStatus;
import com.brentcodes.springboot.librarysystem.vulnerability.Vulnerability;
import com.brentcodes.springboot.librarysystem.vulnerability.VulnerabilityRepository;
import com.brentcodes.springboot.librarysystem.user.User;
import com.brentcodes.springboot.librarysystem.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class AdminService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    private final VulnerabilityRepository vulnerabilityRepository;
    private final ProjectRepository projectRepository;
    private final UserVulnerabilityRepository userVulnerabilityRepository;
    private final UserProjectRepository userProjectRepository;

    public AdminService(PasswordEncoder passwordEncoder, UserRepository userRepository,
                        VulnerabilityRepository vulnerabilityRepository,
                       UserVulnerabilityRepository userVulnerabilityRepository,
                        ProjectRepository projectRepository,
                        UserProjectRepository userProjectRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.vulnerabilityRepository = vulnerabilityRepository;
        this.userVulnerabilityRepository = userVulnerabilityRepository;
        this.projectRepository = projectRepository;
        this.userProjectRepository = userProjectRepository;
    }

    // interacts with database using JPA commands
    public List<User> getUsers() {
        // returns a list
        return userRepository.findAll();
    }
    public User getUserByEmail(String email) {
        // returns a list
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User with email '" + email + "' not found"));
    }

    // Seamless info change
    @Transactional
    public void updateUserFirstOrLastName(Long userId, String firstName, String lastName) {
        User user = userRepository.findById(userId) // we have this because we are just clicking a button that has id from the path (kinda)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        boolean updated = false;

        if (firstName != null && !firstName.isBlank()) {
            user.setFirstname(firstName);
            updated = true;
        }

        if (lastName != null && !lastName.isBlank()) {
            user.setLastname(lastName);
            updated = true;
        }

        if (!updated) {
            throw new IllegalArgumentException("At least one non-blank field (firstName or lastName) must be provided.");
        }

        userRepository.save(user);
    }

    // Authorized credentials change (not seamless)
    @Transactional
    public User updateUserEmailOrPassword(Long userId, String email, String password) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        boolean updated = false;

        if (email != null && !email.isBlank()) {
            user.setEmail(email);
            updated = true;
        }

        if (password != null && !password.isBlank()) {
            user.setPassword(passwordEncoder.encode(password));
            updated = true;
        }

        if (!updated) {
            throw new IllegalArgumentException("At least one non-blank field (firstName or lastName) must be provided.");
        }

        // else we have updated something to do with authentication
        user.setCredentialsChangedAt(Instant.now());

        userRepository.save(user);
        return user;
    }

    // ===========================================Admin Vulnerability Stuff=============================================
        // Later implementation
    @Transactional(readOnly = true) // Optional tag for read only stuff (basically all the methods in this class)
    public List<Vulnerability> getVulnerabilities() {
        // Lists cannot be optional so lambda statements that make easy conversion are kinda useless
        List<Vulnerability> vulnerabilities = vulnerabilityRepository.findAll();

        // I need to use is empty for lists
        if (vulnerabilities.isEmpty()) {
            throw new EntityNotFoundException("No vulnerabilities found");
        }

        return vulnerabilities;
    }

    public Vulnerability getVulnerabilityById(Long vulnerabilityId) {
        return vulnerabilityRepository.findVulnerabilityById(vulnerabilityId)
                .orElseThrow(() -> new EntityNotFoundException("Vulnerability with id '" + vulnerabilityId + "' not found"));
    }

    public Vulnerability getVulnerabilityByTitle(String title) {
        // use lambda statements for single object entities (not lists)
        return vulnerabilityRepository.findVulnerabilityByTitle(title)
                .orElseThrow(() -> new EntityNotFoundException("Vulnerability with title '" + title + "' not found"));
    }

    public List<Vulnerability> getVulnerabilitiesBySeverity(Severity severity) {
        List<Vulnerability> vulnerabilities = vulnerabilityRepository.findVulnerabilitiesBySeverity(severity);

        if (vulnerabilities.isEmpty()) {
            throw new EntityNotFoundException("Vulnerabilities with severity of " + severity + " not found");
        }
        return vulnerabilities;
    }

    public List<Vulnerability> getOverdueVulnerabilities() {
        List<Vulnerability> vulnerabilities = vulnerabilityRepository.findByDueAtBeforeAndStatusNot(Instant.now(), VulnStatus.VERIFIED);

        if (vulnerabilities.isEmpty()) {
            throw new EntityNotFoundException("No overdue vulnerabilities found");
        }
        return vulnerabilities;
    }

    // =============================================== Project Functions ====================================
    // Get All Projects
    @Transactional(readOnly = true)
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    // Get Project by ID
    @Transactional(readOnly = true)
    public Project getProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with ID " + id));
    }

    // ✅ Get Projects by name
    @Transactional(readOnly = true)
    public List<Project> getProjectsByName(String name) {
        List<Project> results = projectRepository.findByName(name);
        if (results.isEmpty()) {
            throw new EntityNotFoundException("No projects found with name: " + name);
        }
        return results;
    }






    // ===========================================Admin UserVuln Stuff=============================================
//    // Assign user to a vulnerability with a specific role
//    @Transactional
//    public User assignUserToVulnerability(VulnerabilityAssignAction action, RoleInVuln role) {
//        Long userId = action.userId();
//        Long vulnId = action.vulnId();
//        User user = userRepository.findById(action.userId())
//                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));
//
//        Vulnerability vuln = vulnerabilityRepository.findById(action.vulnId())
//                .orElseThrow(() -> new EntityNotFoundException("Vulnerability not found with ID: " + vulnId));
//
//        UserVulnerabilityId id = new UserVulnerabilityId(userId, vulnId);
//
//        if (userVulnerabilityRepository.existsById(id)) {
//            throw new IllegalStateException("User already assigned to this vulnerability");
//        }
//
//        UserVulnerability assignment = UserVulnerability.builder()
//                .id(id)
//                .user(user)
//                .vulnerability(vuln)
//                .role(role)
//                .assignedAt(Instant.now())
//                .selfAssigned(false)
//                .build();
//
//        userVulnerabilityRepository.save(assignment);
//        return user;
//    }
//
//    // Revoke a user's assignment from a vulnerability
//    @Transactional
//    public User revokeUserFromVulnerability(VulnerabilityAssignAction action) {
//        Long userId = action.userId();
//        Long vulnId = action.vulnId();
//        User user = userRepository.findById(action.userId())
//                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));
//
//        // checks user and vuln anyway so I don't need to do the user thing above, only needed it for returning.
//        UserVulnerabilityId id = new UserVulnerabilityId(userId, vulnId);
//
//        UserVulnerability existing = userVulnerabilityRepository.findById(id)
//                .orElseThrow(() -> new EntityNotFoundException("Assignment not found"));
//
//        userVulnerabilityRepository.delete(existing);
//        return user;
//    }
//
//
//    // Finds all reports by user
//    // Uses userVulnerabilityRepository JPA method
//    public List<UserVulnerability> getReportedByUser(Long userId) {
//        return userVulnerabilityRepository.findByUserId(userId);
//        // .stream()
//        // .map(UserVulnerability::getVulnerability)  // These are additional java methods that could be automated using @Query in the Repo
//        // .toList(); // We only needed them if we wanted to return a vulnerability, but now we are returning a uservulnerability!
//    }


}
