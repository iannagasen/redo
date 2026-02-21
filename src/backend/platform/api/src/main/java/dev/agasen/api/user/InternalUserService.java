package dev.agasen.api.user;

import dev.agasen.api.user.user.UserAuthInfo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

public interface InternalUserService {

    @GetMapping( "/internal/users/{username}/auth-info" )
    UserAuthInfo getAuthInfo(
            @PathVariable( name = "username" ) String username
    );
}
