package dev.agasen.core.user;

import dev.agasen.api.user.user.UserPasswordChange;
import dev.agasen.core.user.mapper.UserMapper;
import dev.agasen.core.user.persistence.RoleRepository;
import dev.agasen.core.user.persistence.UserRepository;
import dev.agasen.core.user.persistence.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRestServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private UserMapper userMapper;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserRestService userRestService;

    @Test
    @DisplayName("updateUserPassword should update password when current password matches")
    void updatePassword_ShouldWork_WhenCurrentPasswordMatches() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setPassword("old-encoded-password");
        
        UserPasswordChange change = new UserPasswordChange("current-pass", "new-pass");
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("current-pass", "old-encoded-password")).thenReturn(true);
        when(passwordEncoder.encode("new-pass")).thenReturn("new-encoded-password");

        // Act
        userRestService.updateUserPassword(userId, change);

        // Assert
        verify(passwordEncoder).encode("new-pass");
        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("updateUserPassword should throw exception when current password does not match")
    void updatePassword_ShouldThrow_WhenCurrentPasswordDoesNotMatch() {
        // Arrange
        Long userId = 1L;
        User user = new User();
        user.setPassword("old-encoded-password");
        
        UserPasswordChange change = new UserPasswordChange("wrong-pass", "new-pass");
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-pass", "old-encoded-password")).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> userRestService.updateUserPassword(userId, change));
        verify(passwordEncoder, never()).encode(anyString());
    }
}
