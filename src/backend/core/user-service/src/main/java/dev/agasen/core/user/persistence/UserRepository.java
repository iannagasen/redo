package dev.agasen.core.user.persistence;

import dev.agasen.core.user.persistence.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository< User, Long > {

   @Query(
         "select distinct u " +
         "from User u " +
         "left join u.userRoles ur " +
         "left join ur.role r " +
         "left join r.rolePermissions rp " +
         "left join rp.permission p " +
         "where ( :roles is null or r.name IN :roles ) " +
         "and (:permissions is null or p.name in :permissions)"
   )
   Page< User > findFilteredUsers(
         @Param( "roles" ) List< String > roles,
         @Param( "permissions" ) List< String > permissions,
         Pageable pageable
   );
}
