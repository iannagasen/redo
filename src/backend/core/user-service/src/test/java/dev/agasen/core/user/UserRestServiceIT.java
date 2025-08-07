package dev.agasen.core.user;

import dev.agasen.api.user.PermissionService;
import dev.agasen.api.user.RoleService;
import dev.agasen.api.user.UserService;
import dev.agasen.core.user.persistence.PermissionRepository;
import dev.agasen.core.user.persistence.RoleRepository;
import dev.agasen.core.user.persistence.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static dev.agasen.core.user.TestData.userCount;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserRestServiceIT extends BaseIntegrationTest {

   @Autowired UserService userService;
   @Autowired RoleService roleService;
   @Autowired PermissionService permissionService;

   @Autowired UserRepository userRepository;
   @Autowired PermissionRepository permissionRepository;
   @Autowired RoleRepository roleRepository;

   @BeforeEach
   void setUp() {
      TestData.permissionCreationDetails.forEach( permissionService::createPermission );
      TestData.roleCreationDetails.forEach( roleService::createRole );
      TestData.userCreationDetails.forEach( userService::createUser );
   }

   @AfterEach
   void tearDown() {
      userRepository.deleteAll();
   }

   @Test
   void getUsers_shouldReturnUsers() throws Exception {
      var getUsers = get( "/users" )
            .param( "page", "0" )
            .param( "size", "10" )
//            .param( "roles", "ADMIN" )
//            .param( "permissions", "USER" )
            .accept( MediaType.APPLICATION_JSON );

      mockMvc.perform( getUsers )
            .andExpect( status().isOk() )
            .andExpect( jsonPath( "$.content" ).isArray() )
            .andExpect( jsonPath( "$.content.length()" ).value( userCount ) );

      assertEquals( userCount, userRepository.count() );
   }


}