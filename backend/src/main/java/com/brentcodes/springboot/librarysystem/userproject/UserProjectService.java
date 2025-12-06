package com.brentcodes.springboot.librarysystem.userproject;

import com.brentcodes.springboot.librarysystem.project.Project;
import com.brentcodes.springboot.librarysystem.project.ProjectRepository;
import com.brentcodes.springboot.librarysystem.user.User;
import com.brentcodes.springboot.librarysystem.user.UserPrincipal;
import com.brentcodes.springboot.librarysystem.user.UserRepository;
import com.brentcodes.springboot.librarysystem.uservulnerability.RoleInVuln;
import com.brentcodes.springboot.librarysystem.uservulnerability.UserVulnerability;
import com.brentcodes.springboot.librarysystem.uservulnerability.UserVulnerabilityRepository;
import com.brentcodes.springboot.librarysystem.vulnerability.VulnStatus;
import com.brentcodes.springboot.librarysystem.vulnerability.Vulnerability;
import com.brentcodes.springboot.librarysystem.vulnerability.VulnerabilityRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserProjectService {

    private final UserProjectRepository userProjectRepository;
    private final UserVulnerabilityRepository userVulnerabilityRepository;
    private final VulnerabilityRepository vulnerabilityRepository;

    // A lot of these imports of userRepo and projectRepo are for checking existence of a user or project
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    public UserProjectService(UserProjectRepository userProjectRepository, UserVulnerabilityRepository userVulnerabilityRepository,UserRepository userRepository,  VulnerabilityRepository vulnerabilityRepository, ProjectRepository projectRepository) {
        this.userProjectRepository = userProjectRepository;
        this.userVulnerabilityRepository = userVulnerabilityRepository;
        this.userRepository = userRepository;
        this.vulnerabilityRepository = vulnerabilityRepository;
        this.projectRepository = projectRepository;
    }

    @Transactional(readOnly = true)
    public List<ProjectSummaryDto> getMyProjects(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();


        userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Fetch UserProject links
        return userProjectRepository.findByUserId(userId).stream()
                .map(link -> {
                    Project p = link.getProject();
                    return new ProjectSummaryDto(
                            p.getId(),
                            p.getName(),
                            p.getDescription(),
                            p.getCreatedAt(),
                            p.getUpdatedAt(),
                            link.getRole().name()
                    );
                })
                .toList();
    }

    // Get one of MY Projects by Id
    @Transactional(readOnly = true)
    public ProjectSummaryDto getMyProjectById(Authentication authentication, Long projectId) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();

        userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        UserProject userProject = userProjectRepository
                .findByUserIdAndProjectId(userId, projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found for this user"));

        Project p = userProject.getProject();

        // Return project summary for formatting in the frontend
        return new ProjectSummaryDto(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getCreatedAt(),
                p.getUpdatedAt(),
                userProject.getRole().name()
        );
    }

    // Get my Projects by name (may be overlap)
    @Transactional(readOnly = true)
    public List<ProjectSummaryDto> getMyProjectByName(Authentication authentication, String name) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userId = userPrincipal.getId();

        userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<ProjectSummaryDto> results = userProjectRepository.findByUserId(userId).stream()
                .filter(link -> link.getProject().getName().equalsIgnoreCase(name))
                .map(link -> {
                    Project p = link.getProject();
                    return new ProjectSummaryDto(
                            p.getId(),
                            p.getName(),
                            p.getDescription(),
                            p.getCreatedAt(),
                            p.getUpdatedAt(),
                            link.getRole().name()
                    );
                })
                .toList();

        if (results.isEmpty()) {
            throw new EntityNotFoundException("No projects found with name: " + name);
        }

        return results;
    }

    @Transactional
    public RoleCheckerResponse changeUserRoleInProject(ChangeProjectRoleCommand request) {
        Long projectId = request.projectId();
        Long userId = request.userId();

        // 1. Validate that the user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // 2. Validate they are part of the project
        UserProject userProject = userProjectRepository.findByUserIdAndProjectId(userId, projectId)
                .orElseThrow(() -> new IllegalStateException("User is not part of this project"));

        ProjectRole oldRole = userProject.getRole();
        ProjectRole newRole = request.newRole();

        // 3. If the role isn't changing, bail out
        if (oldRole == newRole) {
            throw new IllegalStateException("User already has this project role");
        }

        // 4. If demoting (e.g., PROGRAMMER -> TESTER), clean up their vuln assignments
        if (oldRole == ProjectRole.PROGRAMMER && newRole == ProjectRole.TESTER) {
            // Find all ASSIGNEE vuln links for this user in this project
            List<UserVulnerability> assigneeLinks =
                    userVulnerabilityRepository.findByUserIdAndVulnerabilityProjectIdAndRole(userId, projectId, RoleInVuln.ASSIGNEE);

            for (UserVulnerability link : assigneeLinks) {
                Vulnerability vuln = link.getVulnerability();

                // Remove the assignee link
                userVulnerabilityRepository.delete(link);

                // If no other assignees exist, revert vuln status to REPORTED
                boolean hasOtherAssignees = userVulnerabilityRepository
                        .existsByVulnerabilityIdAndRole(vuln.getId(), RoleInVuln.ASSIGNEE);
                if (!hasOtherAssignees && vuln.getStatus() == VulnStatus.IN_PROGRESS) {
                    vuln.setStatus(VulnStatus.REPORTED);
                    vuln.setUpdatedAt(Instant.now());
                    vulnerabilityRepository.save(vuln);
                }
            }
        }

        // 5. If promoting TESTER -> PROGRAMMER, no cleanup needed
        if (oldRole == ProjectRole.TESTER && newRole == ProjectRole.PROGRAMMER) {
            // Find all VERIFIER vuln links for this user in this project
            List<UserVulnerability> verifierLinks =
                    userVulnerabilityRepository.findByUserIdAndVulnerabilityProjectIdAndRole(userId, projectId, RoleInVuln.VERIFIER);

            for (UserVulnerability link : verifierLinks) {
                Vulnerability vuln = link.getVulnerability();

                // Remove the verifier link
                userVulnerabilityRepository.delete(link);

                // If no other assignees exist, revert vuln status to IN_PROGRESS
                boolean hasOtherVerifiers = userVulnerabilityRepository
                        .existsByVulnerabilityIdAndRole(vuln.getId(), RoleInVuln.VERIFIER);
                if (!hasOtherVerifiers && vuln.getStatus() == VulnStatus.UNDER_REVIEW) {
                    vuln.setStatus(VulnStatus.IN_PROGRESS);
                    vuln.setUpdatedAt(Instant.now());
                    vulnerabilityRepository.save(vuln);
                }

                // If the vuln status is "Verified" nothing has to happen since the task it out of control
            }
        }

        // 6. If demoting from LEAD, clean up roles based on new role permissions
        if (oldRole == ProjectRole.LEAD && newRole != ProjectRole.LEAD) {
            // LEADs can have both ASSIGNEE and VERIFIER roles, so we need to check both

            // Get all ASSIGNEE links
            List<UserVulnerability> assigneeLinks =
                    userVulnerabilityRepository.findByUserIdAndVulnerabilityProjectIdAndRole(userId, projectId, RoleInVuln.ASSIGNEE);

            // Get all VERIFIER links
            List<UserVulnerability> verifierLinks =
                    userVulnerabilityRepository.findByUserIdAndVulnerabilityProjectIdAndRole(userId, projectId, RoleInVuln.VERIFIER);

            // If demoting to TESTER, remove all ASSIGNEE roles
            if (newRole == ProjectRole.TESTER) {
                for (UserVulnerability link : assigneeLinks) {
                    Vulnerability vuln = link.getVulnerability();

                    // Remove the assignee link
                    userVulnerabilityRepository.delete(link);

                    // If no other assignees exist, revert vuln status to REPORTED
                    boolean hasOtherAssignees = userVulnerabilityRepository
                            .existsByVulnerabilityIdAndRole(vuln.getId(), RoleInVuln.ASSIGNEE);
                    if (!hasOtherAssignees && vuln.getStatus() == VulnStatus.IN_PROGRESS) {
                        vuln.setStatus(VulnStatus.REPORTED);
                        vuln.setUpdatedAt(Instant.now());
                        vulnerabilityRepository.save(vuln);
                    }
                }
                // Keep VERIFIER roles (TESTERs can be VERIFIERs)
            }

            // If demoting to PROGRAMMER, remove all VERIFIER roles
            if (newRole == ProjectRole.PROGRAMMER) {
                for (UserVulnerability link : verifierLinks) {
                    Vulnerability vuln = link.getVulnerability();

                    // Remove the verifier link
                    userVulnerabilityRepository.delete(link);

                    // If no other verifiers exist, revert vuln status to IN_PROGRESS
                    boolean hasOtherVerifiers = userVulnerabilityRepository
                            .existsByVulnerabilityIdAndRole(vuln.getId(), RoleInVuln.VERIFIER);
                    if (!hasOtherVerifiers && vuln.getStatus() == VulnStatus.UNDER_REVIEW) {
                        vuln.setStatus(VulnStatus.IN_PROGRESS);
                        vuln.setUpdatedAt(Instant.now());
                        vulnerabilityRepository.save(vuln);
                    }
                }
                // Keep ASSIGNEE roles (PROGRAMMERs can be ASSIGNEEs)
            }
        }

        // Only show warning if promoting to LEAD
        if (newRole == ProjectRole.LEAD) {
            return new RoleCheckerResponse(
                    true,
                    "You are about to promote this user to LEAD. This will add an additional project lead.",
                    "PROMOTING_MEMBER_LEAD"
            );
        }

        // 6. Update project role
        userProject.setRole(newRole);
        userProjectRepository.save(userProject);
        return new RoleCheckerResponse(false, null);
    }


    public List<ProjectMemberResponse> getProjectMembers(Long projectId) {
        return userProjectRepository.findAllByProjectId(projectId)
                .stream()
                .map(up -> new ProjectMemberResponse(
                        up.getUser().getId(),
                        up.getUser().getFirstname(),
                        up.getUser().getLastname(),
                        up.getRole()   // ✅ THIS IS THE PROJECT ROLE
                ))
                .toList();
    }

    @Transactional
    public RoleCheckerResponse addMemberToProject(AddMemberRequest request) {
        Project project = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Prevent duplicate membership
        boolean alreadyMember = userProjectRepository.findByUserIdAndProjectId(user.getId(), request.projectId()).isPresent();
        if (alreadyMember) {
            throw new IllegalStateException("User is already a member of this project");
        }

        ProjectRole role = request.role();

        if (role == ProjectRole.LEAD) {
            return new RoleCheckerResponse(
                    true,
                    "You are adding a new LEAD to this project. This will grant them full permissions.",
                    "ADDING_MEMBER_LEAD"
            );
        }

        UserProject userProject = new UserProject();
        userProject.setUser(user);
        userProject.setProject(project);
        userProject.setRole(role);

        userProjectRepository.save(userProject);
        return new RoleCheckerResponse(
                false,
                "Member successfully added."
        );
    }

    @Transactional(readOnly = true)
    public RoleCheckerResponse validateRemoveMember(RemoveMemberRequest request) {
        Long userId = request.userId();
        Long projectId = request.projectId();

        UserProject userProject = userProjectRepository
                .findByUserIdAndProjectId(userId, projectId)
                .orElseThrow(() -> new IllegalArgumentException("User is not a member of this project"));

        ProjectRole removedRole = userProject.getRole();

        List<UserProject> remainingMembers =
                userProjectRepository.findAllByProjectId(projectId)
                        .stream()
                        .filter(up -> !up.getUser().getId().equals(userId))
                        .toList();

        // Last LEAD?
        if (removedRole == ProjectRole.LEAD) {
            boolean hasOtherLeads = remainingMembers.stream()
                    .anyMatch(up -> up.getRole() == ProjectRole.LEAD);

            if (!hasOtherLeads) {
                return new RoleCheckerResponse(
                        true,
                        "Removing this member will leave the project without a lead.",
                        "REMOVING_MEMBER_LEAD"
                );
            }
        }

        // Safe → return "SAFE_TO_DELETE"
        return new RoleCheckerResponse(false, "SAFE_TO_DELETE");
    }

    @Transactional
    public void removeMemberFromProject(RemoveMemberRequest request) {
        // Delete UserProject links (membership info)
        UserProject link = userProjectRepository
                .findByUserIdAndProjectId(request.userId(), request.projectId())
                .orElseThrow(() -> new IllegalArgumentException("User not in project"));

        // Delete all UserVulnerability assignments for this user in this project
        userVulnerabilityRepository.deleteAllAssignmentsForUserInProject(
                request.userId(),
                request.projectId()
        );

        userProjectRepository.delete(link);
    }

    @Transactional(readOnly = true)
    public RoleCheckerResponse validateSelfRemove(Long projectId, Authentication auth) {

        Long userId = ((UserPrincipal) auth.getPrincipal()).getId();

        UserProject userProject = userProjectRepository
                .findByUserIdAndProjectId(userId, projectId)
                .orElseThrow(() -> new IllegalArgumentException("You are not a member of this project"));

        ProjectRole role = userProject.getRole();

        List<UserProject> remainingMembers =
                userProjectRepository.findAllByProjectId(projectId)
                        .stream()
                        .filter(up -> !up.getUser().getId().equals(userId))
                        .toList();

        // Last member (deleting project)?
        if (remainingMembers.isEmpty()) {
            return new RoleCheckerResponse(
                    true,
                    "You are the last member. Leaving will delete the project.",
                    "SELF_REMOVE_LAST_MEMBER"
            );
        }

        // Last LEAD?
        boolean hasOtherLeads = remainingMembers.stream()
                .anyMatch(up -> up.getRole() == ProjectRole.LEAD);

        if (role == ProjectRole.LEAD && !hasOtherLeads) {
            return new RoleCheckerResponse(
                    true,
                    "You are the last lead. Promote another member first.",
                    "SELF_REMOVE_LAST_LEAD"
            );
        }

        return new RoleCheckerResponse(false, "SAFE_TO_DELETE");
    }
    @Transactional
    public void selfRemoveFromProject(Long projectId, Authentication authentication) {
        Long userId = ((UserPrincipal) authentication.getPrincipal()).getId();

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with ID " + projectId));

        UserProject link = userProjectRepository
                .findByUserIdAndProjectId(userId, projectId)
                .orElseThrow(() -> new IllegalArgumentException("Not a member"));

        boolean isLead = link.getRole() == ProjectRole.LEAD;

        // Count how many members exist in this project
        long totalMembers = userProjectRepository.countByProject(project);

        // --------------------------------------------
        // CASE 1 → NON-LEAD MEMBER SELF-REMOVAL
        // --------------------------------------------
        if (!isLead) {

            // Remove them from project membership
            userProjectRepository.delete(link);

            // Remove their vulnerability assignments
            userVulnerabilityRepository.deleteAllAssignmentsForUserInProject(userId, projectId);

            return; // DONE — project stays alive
        }

        // --------------------------------------------
        // CASE 2 → LEAD SELF-REMOVAL
        // --------------------------------------------

        // Remove the lead from the project
        userProjectRepository.delete(link);

        // Remove their vulnerability assignments
        userVulnerabilityRepository.deleteAllAssignmentsForUserInProject(userId, projectId);

        // If they were the ONLY member left, safely delete entire project
        if (totalMembers == 1) {
            projectRepository.delete(project);
        }

        // Otherwise, just remove the lead — project lives
    }
}
