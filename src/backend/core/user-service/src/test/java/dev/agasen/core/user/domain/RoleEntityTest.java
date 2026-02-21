package dev.agasen.core.user.domain;

import dev.agasen.core.user.persistence.entity.Permission;
import dev.agasen.core.user.persistence.entity.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RoleEntityTest {

    @Test
    @DisplayName("addPermission should add permission when it doesn't exist")
    void addPermission_ShouldAdd_WhenNotExists() {
        // Arrange
        Role role = new Role();
        Permission permission = new Permission();
        permission.setId(1L);
        permission.setName("READ");

        // Act
        role.addPermission(permission);

        // Assert
        assertThat(role.getRolePermissions()).hasSize(1);
        assertThat(role.getRolePermissions().iterator().next().getPermission()).isEqualTo(permission);
    }

    @Test
    @DisplayName("addPermission should not add duplicate permission")
    void addPermission_ShouldNotAddDuplicate() {
        // Arrange
        Role role = new Role();
        Permission permission = new Permission();
        permission.setId(1L);
        permission.setName("READ");

        // Act
        role.addPermission(permission);
        role.addPermission(permission);

        // Assert
        assertThat(role.getRolePermissions()).hasSize(1);
    }
}
