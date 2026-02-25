package com.brentcodes.springboot.librarysystem.calendar;

import com.brentcodes.springboot.librarysystem.user.UserPrincipal;
import com.brentcodes.springboot.librarysystem.userproject.ProjectRole;
import com.brentcodes.springboot.librarysystem.userproject.UserProject;
import com.brentcodes.springboot.librarysystem.userproject.UserProjectRepository;
import com.brentcodes.springboot.librarysystem.uservulnerability.UserVulnerability;
import com.brentcodes.springboot.librarysystem.uservulnerability.UserVulnerabilityRepository;
import com.brentcodes.springboot.librarysystem.vulnerability.Vulnerability;
import com.brentcodes.springboot.librarysystem.vulnerability.VulnerabilityRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CalendarService {

    private final UserProjectRepository userProjectRepository;
    private final UserVulnerabilityRepository userVulnerabilityRepository;
    private final VulnerabilityRepository vulnerabilityRepository;

    public CalendarService(UserProjectRepository userProjectRepository,
                           UserVulnerabilityRepository userVulnerabilityRepository,
                           VulnerabilityRepository vulnerabilityRepository) {
        this.userProjectRepository = userProjectRepository;
        this.userVulnerabilityRepository = userVulnerabilityRepository;
        this.vulnerabilityRepository = vulnerabilityRepository;
    }

    @Transactional(readOnly = true)
    public List<CalendarVulnDto> getCalendarVulns(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        Long userId = principal.getId();

        // Collect unique vulns by id, preserving order
        Map<Long, Vulnerability> seen = new LinkedHashMap<>();

        List<UserProject> memberships = userProjectRepository.findByUserId(userId);

        for (UserProject up : memberships) {
            Long projectId = up.getProject().getId();

            if (up.getRole() == ProjectRole.LEAD) {
                // Leads see every vulnerability in the project that has a due date
                vulnerabilityRepository.findVulnerabilitiesByProjectId(projectId).stream()
                        .filter(v -> v.getDueAt() != null)
                        .forEach(v -> seen.putIfAbsent(v.getId(), v));
            } else {
                // Non-leads see only the vulns they are directly linked to (reporter/assignee/verifier)
                userVulnerabilityRepository.findByUserId(userId).stream()
                        .map(UserVulnerability::getVulnerability)
                        .filter(v -> v.getProject().getId().equals(projectId) && v.getDueAt() != null)
                        .forEach(v -> seen.putIfAbsent(v.getId(), v));
            }
        }

        return seen.values().stream()
                .map(v -> new CalendarVulnDto(
                        v.getId(),
                        v.getTitle(),
                        v.getSeverity(),
                        v.getStatus(),
                        v.getDueAt(),
                        v.getProject().getId(),
                        v.getProject().getName()
                ))
                .toList();
    }
}
