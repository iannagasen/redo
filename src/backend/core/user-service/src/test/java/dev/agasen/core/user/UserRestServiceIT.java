package dev.agasen.core.user;

import dev.agasen.core.user.persistence.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc( addFilters = false )
class UserRestServiceIT extends BaseIntegrationTest {

   @Autowired UserRepository userRepository;

   @Test
   void getUsers_shouldReturnUsers() throws Exception {
      var getUsers = get( "/users" )
         .param( "page", "0" )
         .param( "size", "10" )
         .accept( MediaType.APPLICATION_JSON );

      mockMvc.perform( getUsers )
         .andExpect( status().isOk() )
         .andExpect( jsonPath( "$.content" ).isArray() )
         .andExpect( jsonPath( "$.content[?(@.username == 'admin')]" ).exists() );
   }

}
