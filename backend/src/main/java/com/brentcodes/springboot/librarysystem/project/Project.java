package com.brentcodes.springboot.librarysystem.project;

import com.brentcodes.springboot.librarysystem.userproject.UserProject;
import com.brentcodes.springboot.librarysystem.vulnerability.Vulnerability;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "project")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Project {
    @Id
    // Auto-Generation
    @SequenceGenerator(
            name = "project_sequence",         // Name used inside the Java app to refer to this generator
            sequenceName = "project_sequence", // Name of the actual database sequence object
            allocationSize = 1                 // Number of IDs to allocate at once (1 = no batching; get a new ID every time)
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,  // Use a database sequence to generate the ID
            generator = "project_sequence"       // Link this ID generator to the one defined above
    )
    private Long id;
    private String name;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;

    // Project ↔ User
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore // dont spam this when returning project in JSON format
    private Set<UserProject> userProjects = new HashSet<>();

    // Bidirectional relationship, no joining table
    // Project ↔ Vulnerability... we are sending info over to vuln (foreign key lives in vulnerability class)
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<Vulnerability> vulnerabilities = new HashSet<>();

    // ✅ Used for backend seeding (e.g., in DataInitializer), avoids @Id injection
    // ⚠️ Excludes auto-generated `id`
    // ℹ️ Holds similar value to Lombok builder but excludes id
    public Project(String name, String description, Instant createdAt, Instant updatedAt) {
        this.name = name; // The rest are in updateVulnerabilityInfo
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Project)) return false;
        Project other = (Project) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
