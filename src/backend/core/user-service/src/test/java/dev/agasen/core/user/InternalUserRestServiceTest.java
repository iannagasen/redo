package dev.agasen.core.user;

import dev.agasen.api.user.user.UserAuthInfo;
import dev.agasen.core.user.persistence.UserRepository;
import dev.agasen.core.user.persistence.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InternalUserRestServiceTest {

    @Mock private UserRepository userRepository;

    @InjectMocks
    private InternalUserRestService internalUserRestService;

    @Test
    @DisplayName("getAuthInfo should correctly map nested roles and permissions")
    void getAuthInfo_ShouldMapNestedData() {
        // Arrange
        String username = "testuser";
        User user = new User();
        user.setId(1L);
        user.setUsername(username);
        user.setPassword("pass");
        user.setEnabled(true);

        Role adminRole = new Role();
        adminRole.setName("ADMIN");

        Permission readPerm = new Permission();
        readPerm.setName("READ");
        
        // Use the domain logic we just tested!
        adminRole.addPermission(readPerm);
        user.addRole(adminRole);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // Act
        UserAuthInfo result = internalUserRestService.getAuthInfo(username);

        // Assert
        assertThat(result.getUsername()).isEqualTo(username);
        assertThat(result.getRoles()).containsExactly("ADMIN");
        assertThat(result.getPermissions()).containsExactly("READ");
    }
}
