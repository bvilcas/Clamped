package com.brentcodes.springboot.librarysystem.userproject;

import aj.org.objectweb.asm.commons.Remapper;
import com.brentcodes.springboot.librarysystem.project.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserProjectRepository extends JpaRepository<UserProject, UserProjectId> {
    Optional<UserProject> findByUserIdAndProjectId(Long userId, Long projectId);

    List<UserProject> findByUserId(Long userId);

    List<UserProject> findAllByProjectId(Long projectId);

    long countByProject(Project project);

}




