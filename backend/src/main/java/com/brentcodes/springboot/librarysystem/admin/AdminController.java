package com.brentcodes.springboot.librarysystem.admin;

import com.brentcodes.springboot.librarysystem.authentication.AuthenticationService;
import com.brentcodes.springboot.librarysystem.authentication.RegisterRequest;
import com.brentcodes.springboot.librarysystem.backendconfig.ApiResponse;
import com.brentcodes.springboot.librarysystem.project.Project;
import com.brentcodes.springboot.librarysystem.project.ProjectService;
import com.brentcodes.springboot.librarysystem.sessionconfig.SessionService;
import com.brentcodes.springboot.librarysystem.user.Role;
import com.brentcodes.springboot.librarysystem.user.User;
import com.brentcodes.springboot.librarysystem.user.UserRepository;
import com.brentcodes.springboot.librarysystem.uservulnerability.RoleInVuln;
import com.brentcodes.springboot.librarysystem.uservulnerability.UserVulnerability;
import com.brentcodes.springboot.librarysystem.vulnerability.Severity;
import com.brentcodes.springboot.librarysystem.vulnerability.Vulnerability;
import com.brentcodes.springboot.librarysystem.vulnerability.VulnerabilityService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping(path = "api/v1/admin")
public class AdminController {
    private final AdminService adminService;
    private final AuthenticationService authenticationService;
    private final UserRepository userRepository;
    private final VulnerabilityService vulnerabilityService;
    private final SessionService sessionService;
    private final ProjectService projectService;

    public AdminController(AdminService adminService, AuthenticationService authenticationService,  UserRepository userRepository, VulnerabilityService vulnerabilityService, SessionService sessionService,  ProjectService projectService) {
        this.adminService = adminService;
        this.authenticationService = authenticationService;
        this.userRepository = userRepository;
        this.vulnerabilityService = vulnerabilityService;
        this.sessionService = sessionService;
        this.projectService = projectService;
    }
    // USER FUNCTIONS

    // 🔐 ADMIN: View all users; Returns List<User>
    @GetMapping(path = "/users") // manage users
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getUsers() {
        return ResponseEntity.ok(new ApiResponse(true, "Users retrieved successfully", adminService.getUsers()));
    }


    // 🔐 ADMIN: Find specific user by email; Returns User
    @GetMapping(path = "/users/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getUserByEmail(@PathVariable("email") String email) {
        return ResponseEntity.ok(new ApiResponse(true, "User with email " + email + " retrieved successfully", adminService.getUserByEmail(email)));
    }

    // Takes payload for a user and stores it in database (does not log them or give access tokens like we saw in the other register method)
    // THIS IS CALLED DTO since we are formatting json into a class "user"
    @PostMapping(path = "/users/add")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> addUser(@RequestBody RegisterRequest request,
                                               @RequestParam(defaultValue = "USER") Role role /* exclusive to admin registering a user */) {
        User adminCreatedUser = authenticationService.registerAsAdmin(request, role);
        return ResponseEntity.ok(new ApiResponse(true, "User added successfully", adminCreatedUser));
    }

    // Seamless because old access tokens can have expired first or last names (not needed for auth)
    @PutMapping(path = "/users/update/info/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> updateUserFirstOrLastName(
            @PathVariable("userId") Long userId,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName)
    {
        adminService.updateUserFirstOrLastName(userId, firstName, lastName);
        return ResponseEntity.ok(new ApiResponse(true, "First or Last name changed successfully"));
    }

    // Cannot be seamless because these are credentials required for authentication
    @PutMapping(path = "/users/update/credentials/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> updateUserEmailOrPassword(
            @PathVariable("userId") Long userId,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String password)
    {
        User user = adminService.updateUserEmailOrPassword(userId, email, password);

        // ✅ Revoke all sessions tied to this user
        sessionService.invalidateAllSessions(user.getId().toString());

        // ✅ Force token expiration by bumping credentialsChangedAt (redundant after delete, but useful if soft-deleting)
        user.setCredentialsChangedAt(Instant.now()); // Optional if you delete right after

        return ResponseEntity.ok(new ApiResponse(true, "Email or password changed successfully"));
    }

    // All deleting user logic is in the controller (http stuff kinda basically)
    @Transactional
    @DeleteMapping(path = "/users/delete/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(@PathVariable Long userId) {
        User user = userRepository.findById(userId) // we have this because we are just clicking a button that has id from the path (kinda)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        // ✅ 1. Revoke all sessions tied to this user
        sessionService.invalidateAllSessions(user.getId().toString());

        // ✅ 2. Force token expiration by bumping credentialsChangedAt (redundant after delete, but useful if soft-deleting)
        user.setCredentialsChangedAt(Instant.now()); // Optional if you delete right after

        // ✅ 3. Delete the user
        userRepository.deleteById(userId);
    }

    // =======================================Vulnerability Functions===========================================
    // View all vulnerabilities (whether resolved or not); Returns List<Vulnerability> (check status for service)
    @GetMapping(path = "/all")
    public ResponseEntity<ApiResponse> getVulnerabilities() {
        return ResponseEntity.ok(new ApiResponse(true, "Vulnerabilities retrieved successfully", adminService.getVulnerabilities()));
    }

    // Get vulnerability by ID
    @GetMapping("/{vulnerabilityId}")
    public ResponseEntity<ApiResponse> getVulnerabilityById(@PathVariable Long vulnerabilityId) {
        Vulnerability vuln = adminService.getVulnerabilityById(vulnerabilityId);
        return ResponseEntity.ok(new ApiResponse(true, "Vulnerability retrieved", vuln));
    }

    // Search by title
    @GetMapping("/title/{title}")
    public ResponseEntity<ApiResponse> getVulnerabilityByTitle(@PathVariable String title) {
        Vulnerability vuln = adminService.getVulnerabilityByTitle(title);
        return ResponseEntity.ok(new ApiResponse(true, "Vulnerability found", vuln));
    }

    // Filter by severity
    @GetMapping("/severity/{severity}")
    public ResponseEntity<ApiResponse> getVulnerabilitiesBySeverity(@PathVariable Severity severity) {
        return ResponseEntity.ok(new ApiResponse(true, "Filtered by severity", adminService.getVulnerabilitiesBySeverity(severity)));
    }

    // Get overdue vulnerabilities
    @GetMapping("/overdue")
    public ResponseEntity<ApiResponse> getOverdueVulnerabilities() {
        return ResponseEntity.ok(new ApiResponse(true, "Overdue vulnerabilities", adminService.getOverdueVulnerabilities()));
    }

    // update/delete vulnerabilities maybe

    // ======================================== Project Functions ==================================================
// ✅ Get All Projects
    @GetMapping
    public ResponseEntity<ApiResponse> getAllProjects() {
        List<Project> projects = adminService.getAllProjects();
        return ResponseEntity.ok(new ApiResponse(true, "Projects retrieved", projects));
    }

    // ✅ Get Project by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getProjectById(@PathVariable Long id) {
        Project project = adminService.getProjectById(id);
        return ResponseEntity.ok(new ApiResponse(true, "Project retrieved", project));
    }

//    // ✅ Get Project by name (list because we will have overlap)
//    @GetMapping("/{id}")
//    public ResponseEntity<ApiResponse> getProjectsByName(@PathVariable String name) {
//        List<Project> projects = adminService.getProjectsByName(name);
//        return ResponseEntity.ok(new ApiResponse(true, "Project retrieved", projects));
//    }

    // update/delete projects maybe
}