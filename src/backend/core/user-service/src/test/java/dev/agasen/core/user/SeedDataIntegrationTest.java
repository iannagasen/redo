package dev.agasen.core.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

public class SeedDataIntegrationTest extends BaseIntegrationTest {

   @Autowired
   private JdbcTemplate jdbcTemplate;

   @Test
   @DisplayName("Should have at least one user in the database")
   void testUserCount() {
      Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
      assertThat(userCount)
            .withFailMessage("Expected at least 1 user in the database, but found %d", userCount)
            .isGreaterThanOrEqualTo(1);
   }

   @Test
   @DisplayName("Should contain an admin user")
   void testAdminUserExists() {
      String adminUser = jdbcTemplate.queryForObject("SELECT username FROM users WHERE username = 'admin'", String.class);
      assertThat(adminUser)
            .withFailMessage("Expected to find user 'admin', but found '%s'", adminUser)
            .isEqualTo("admin");
   }

   @Test
   @DisplayName("Should have at least two roles (ADMIN, USER)")
   void testRoleCount() {
      Integer roleCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM roles", Integer.class);
      assertThat(roleCount)
            .withFailMessage("Expected at least 2 roles (ADMIN, USER), but found %d", roleCount)
            .isGreaterThanOrEqualTo(2);
   }

   @Test
   @DisplayName("Should have at least 9 permissions")
   void testPermissionCount() {
      Integer permissionCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM permission", Integer.class);
      assertThat(permissionCount)
            .withFailMessage("Expected at least 9 permissions, but found %d", permissionCount)
            .isGreaterThanOrEqualTo(9);
   }

   @Test
   @DisplayName("Admin user should have exactly one role assigned")
   void testAdminUserRoleAssociation() {
      Integer userRoleCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM user_role ur JOIN users u ON ur.user_id = u.id WHERE u.username = 'admin'",
            Integer.class
      );
      assertThat(userRoleCount)
            .withFailMessage("Expected 'admin' user to have 1 assigned role, but found %d", userRoleCount)
            .isEqualTo(1);
   }
}
