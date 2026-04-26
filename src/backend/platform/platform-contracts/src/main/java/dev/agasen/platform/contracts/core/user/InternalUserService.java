package dev.agasen.platform.contracts.core.user;

import dev.agasen.platform.contracts.core.user.user.UserAuthInfo;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

public interface InternalUserService {

   @GetExchange( "/internal/users/{username}/auth-info" )
   UserAuthInfo getAuthInfo( @PathVariable( "username" ) String username );
}
