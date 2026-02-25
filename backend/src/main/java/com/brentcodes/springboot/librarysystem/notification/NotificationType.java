package com.brentcodes.springboot.librarysystem.notification;

public enum NotificationType {
    VULN_ASSIGNED,        // You were assigned to a vulnerability by a lead
    VULN_UNASSIGNED,      // You were unassigned from a vulnerability
    VULN_REPORTED,        // A new vulnerability was reported in your project
    VULN_STATUS_CHANGED,  // A vulnerability you are on changed status
    MEMBER_SELF_ASSIGNED, // Someone self-assigned to a vuln (for leads)
    MEMBER_SELF_REVOKED,  // Someone self-revoked from a vuln (for leads)
    PROJECT_ADDED,        // You were added to a project
    PROJECT_REMOVED       // You were removed from a project
}
