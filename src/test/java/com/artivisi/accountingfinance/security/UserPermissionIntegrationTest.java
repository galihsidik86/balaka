package com.artivisi.accountingfinance.security;

import com.artivisi.accountingfinance.TestcontainersConfiguration;
import com.artivisi.accountingfinance.entity.User;
import com.artivisi.accountingfinance.enums.Role;
import com.artivisi.accountingfinance.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests to verify test users have correct roles and permissions.
 * These tests ensure the security test data is properly configured.
 */
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@DisplayName("User Permission Integration Tests")
class UserPermissionIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserDetailsService userDetailsService;

    @Test
    @DisplayName("Staff user should exist with STAFF role")
    void staffUserShouldExistWithStaffRole() {
        User staff = userRepository.findByUsername("staff")
                .orElseThrow(() -> new AssertionError("Staff user not found in database"));

        assertThat(staff.getActive()).isTrue();
        assertThat(staff.getRoles()).contains(Role.STAFF);
        assertThat(staff.getRoles()).doesNotContain(Role.ADMIN, Role.OWNER, Role.ACCOUNTANT);
    }

    @Test
    @DisplayName("Staff user should have correct permissions via UserDetailsService")
    void staffUserShouldHaveCorrectPermissions() {
        UserDetails staffDetails = userDetailsService.loadUserByUsername("staff");

        Set<String> authorities = staffDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        // Staff SHOULD have these permissions
        assertThat(authorities).contains(
                "ROLE_STAFF",
                Permission.DASHBOARD_VIEW,
                Permission.TRANSACTION_VIEW,
                Permission.TRANSACTION_CREATE
        );

        // Staff should NOT have these permissions
        assertThat(authorities).doesNotContain(
                Permission.USER_VIEW,
                Permission.USER_CREATE,
                Permission.PAYROLL_VIEW,
                Permission.TRANSACTION_EDIT,
                Permission.TRANSACTION_POST
        );
    }

    @Test
    @DisplayName("Employee user should exist with EMPLOYEE role")
    void employeeUserShouldExistWithEmployeeRole() {
        User employee = userRepository.findByUsername("employee")
                .orElseThrow(() -> new AssertionError("Employee user not found in database"));

        assertThat(employee.getActive()).isTrue();
        assertThat(employee.getRoles()).contains(Role.EMPLOYEE);
        assertThat(employee.getRoles()).doesNotContain(Role.ADMIN, Role.OWNER, Role.ACCOUNTANT, Role.STAFF);
    }

    @Test
    @DisplayName("Employee user should have correct permissions via UserDetailsService")
    void employeeUserShouldHaveCorrectPermissions() {
        UserDetails employeeDetails = userDetailsService.loadUserByUsername("employee");

        Set<String> authorities = employeeDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        // Employee SHOULD have these permissions (self-service only)
        assertThat(authorities).contains(
                "ROLE_EMPLOYEE",
                Permission.OWN_PAYSLIP_VIEW,
                Permission.OWN_PROFILE_VIEW,
                Permission.OWN_PROFILE_EDIT
        );

        // Employee should NOT have these permissions
        assertThat(authorities).doesNotContain(
                Permission.DASHBOARD_VIEW,
                Permission.TRANSACTION_VIEW,
                Permission.PAYROLL_VIEW,
                Permission.USER_VIEW,
                Permission.EMPLOYEE_VIEW
        );
    }

    @Test
    @DisplayName("Auditor user should exist with AUDITOR role")
    void auditorUserShouldExistWithAuditorRole() {
        User auditor = userRepository.findByUsername("auditor")
                .orElseThrow(() -> new AssertionError("Auditor user not found in database"));

        assertThat(auditor.getActive()).isTrue();
        assertThat(auditor.getRoles()).contains(Role.AUDITOR);
        assertThat(auditor.getRoles()).doesNotContain(Role.ADMIN, Role.OWNER, Role.ACCOUNTANT);
    }

    @Test
    @DisplayName("Auditor user should have correct permissions via UserDetailsService")
    void auditorUserShouldHaveCorrectPermissions() {
        UserDetails auditorDetails = userDetailsService.loadUserByUsername("auditor");

        Set<String> authorities = auditorDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        // Auditor SHOULD have these permissions (read-only)
        assertThat(authorities).contains(
                "ROLE_AUDITOR",
                Permission.DASHBOARD_VIEW,
                Permission.TRANSACTION_VIEW,
                Permission.REPORT_VIEW,
                Permission.PAYROLL_VIEW,
                Permission.AUDIT_LOG_VIEW
        );

        // Auditor should NOT have these permissions (no create/edit)
        assertThat(authorities).doesNotContain(
                Permission.TRANSACTION_CREATE,
                Permission.TRANSACTION_EDIT,
                Permission.TRANSACTION_POST,
                Permission.USER_VIEW,
                Permission.USER_CREATE
        );
    }

    @Test
    @DisplayName("Admin user should have all permissions")
    void adminUserShouldHaveAllPermissions() {
        UserDetails adminDetails = userDetailsService.loadUserByUsername("admin");

        Set<String> authorities = adminDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        // Admin SHOULD have these key permissions
        assertThat(authorities).contains(
                "ROLE_ADMIN",
                Permission.USER_VIEW,
                Permission.USER_CREATE,
                Permission.USER_EDIT,
                Permission.DASHBOARD_VIEW,
                Permission.TRANSACTION_VIEW,
                Permission.TRANSACTION_CREATE,
                Permission.TRANSACTION_EDIT,
                Permission.TRANSACTION_POST,
                Permission.PAYROLL_VIEW,
                Permission.AUDIT_LOG_VIEW
        );
    }

    @Test
    @DisplayName("Print all authorities for debugging")
    void printAuthoritiesForDebugging() {
        System.out.println("\n=== User Authorities Debug ===\n");

        for (String username : new String[]{"admin", "staff", "employee", "auditor"}) {
            try {
                UserDetails details = userDetailsService.loadUserByUsername(username);
                System.out.println("User: " + username);
                System.out.println("Authorities (" + details.getAuthorities().size() + "):");
                details.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .sorted()
                        .forEach(a -> System.out.println("  - " + a));
                System.out.println();
            } catch (Exception e) {
                System.out.println("User: " + username + " - ERROR: " + e.getMessage());
            }
        }
    }
}
