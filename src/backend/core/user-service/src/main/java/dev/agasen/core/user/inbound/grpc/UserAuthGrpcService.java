package dev.agasen.core.user.inbound.grpc;

import dev.agasen.core.user.persistence.UserRepository;
import dev.agasen.core.user.persistence.entity.User;
import dev.agasen.platform.contracts.api.grpc.user.UserAuthInfo;
import dev.agasen.platform.contracts.api.grpc.user.UserAuthInfoRequest;
import dev.agasen.platform.contracts.api.grpc.user.UserAuthServiceGrpc;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class UserAuthGrpcService extends UserAuthServiceGrpc.UserAuthServiceImplBase {

   private final UserRepository userRepository;

   public UserAuthGrpcService( UserRepository userRepository ) {
      this.userRepository = userRepository;
   }

   @Override public void getUserAuthInfo( UserAuthInfoRequest request, StreamObserver< UserAuthInfo > responseObserver ) {
      var username = request.getUsername();

      var user = userRepository.findByUsername( username );

      if ( user.isPresent() ) {
         var userAuthInfo = mapToUserAuthInfo( user.get() );
         responseObserver.onNext( userAuthInfo );
         responseObserver.onCompleted();
      } else {
         responseObserver.onError(
            Status.NOT_FOUND
               .withDescription( "User Not Found: " + username )
               .asRuntimeException()
         );
      }
   }

   private static @NonNull UserAuthInfo mapToUserAuthInfo( User u ) {
      List< String > roles = u.getUserRoles().stream()
         .map( ur -> ur.getRole().getName() )
         .toList();

      List< String > permissions = u.getUserRoles().stream()
         .flatMap( ur -> ur.getRole().getRolePermissions().stream() )
         .map( urp -> urp.getPermission().getName() )
         .toList();

      var userAuthInfo = UserAuthInfo.newBuilder()
         .setId( u.getId() )
         .setUsername( u.getUsername() )
         .setPasswordHash( u.getPassword() )
         .setEnabled( u.isEnabled() )
         .setLocked( u.isLocked() )
         .setDeleted( u.isDeleted() )
         .addAllRoles( roles )
         .addAllPermissions( permissions )
         .build();
      return userAuthInfo;
   }
}
