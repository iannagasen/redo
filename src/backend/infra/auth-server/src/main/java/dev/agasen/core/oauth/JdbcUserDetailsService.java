package dev.agasen.core.oauth;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class JdbcUserDetailsService implements UserDetailsService {

   private final JdbcTemplate jdbcTemplate;

   public JdbcUserDetailsService( JdbcTemplate jdbcTemplate ) {
      this.jdbcTemplate = jdbcTemplate;
   }

   @Override
   public UserDetails loadUserByUsername( String username ) throws UsernameNotFoundException {
      var users = jdbcTemplate.query(
         "SELECT id, username, password, enabled, locked, deleted FROM users WHERE username = ?",
         ( rs, rowNum ) -> new UserRecord(
            rs.getLong( "id" ),
            rs.getString( "username" ),
            rs.getString( "password" ),
            rs.getBoolean( "enabled" ),
            rs.getBoolean( "locked" ),
            rs.getBoolean( "deleted" )
         ),
         username
      );

      if ( users.isEmpty() ) {
         throw new UsernameNotFoundException( "User not found: " + username );
      }

      UserRecord userRecord = users.getFirst();

      List< GrantedAuthority > authorities = new ArrayList<>();

      // Load roles
      List< String > roles = jdbcTemplate.queryForList(
         "SELECT r.name FROM roles r " +
         "JOIN user_role ur ON ur.role_id = r.id " +
         "WHERE ur.user_id = ?",
         String.class,
         userRecord.id()
      );
      roles.forEach( role -> authorities.add( new SimpleGrantedAuthority( "ROLE_" + role ) ) );

      // Load permissions (through roles)
      List< String > permissions = jdbcTemplate.queryForList(
         "SELECT DISTINCT p.name FROM permission p " +
         "JOIN role_permission rp ON rp.permission_id = p.id " +
         "JOIN user_role ur ON ur.role_id = rp.role_id " +
         "WHERE ur.user_id = ?",
         String.class,
         userRecord.id()
      );
      permissions.forEach( perm -> authorities.add( new SimpleGrantedAuthority( perm ) ) );

      return User.builder()
         .username( userRecord.username() )
         .password( userRecord.password() )
         .authorities( authorities )
         .disabled( !userRecord.enabled() )
         .accountLocked( userRecord.locked() )
         .accountExpired( userRecord.deleted() )
         .build();
   }

   private record UserRecord( long id, String username, String password, boolean enabled, boolean locked, boolean deleted ) {}
}
