package dev.agasen.core.user.domain;

import dev.agasen.core.user.persistence.entity.Role;
import dev.agasen.core.user.persistence.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserEntityTest {

    @Test
    @DisplayName("addRole should add role when it doesn't exist")
    void addRole_ShouldAddRole_WhenNotExists() {
        // Arrange
        User user = new User();
        Role adminRole = new Role();
        adminRole.setId(1L);
        adminRole.setName("ADMIN");

        // Act
        user.addRole(adminRole);

        // Assert
        assertThat(user.getUserRoles()).hasSize(1);
        assertThat(user.getUserRoles().iterator().next().getRole()).isEqualTo(adminRole);
    }

    @Test
    @DisplayName("addRole should not add duplicate role")
    void addRole_ShouldNotAddDuplicate() {
        // Arrange
        User user = new User();
        Role adminRole = new Role();
        adminRole.setId(1L);
        adminRole.setName("ADMIN");

        // Act
        user.addRole(adminRole);
        user.addRole(adminRole); // Duplicate

        // Assert
        assertThat(user.getUserRoles()).hasSize(1);
    }
}
