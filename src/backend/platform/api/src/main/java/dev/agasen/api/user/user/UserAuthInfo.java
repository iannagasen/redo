package dev.agasen.api.user.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserAuthInfo {
    private Long id;
    private String username;
    private String password;
    private boolean enabled;
    private boolean locked;
    private boolean deleted;
    private Set<String> roles;
    private Set<String> permissions;
}
