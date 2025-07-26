package dev.agasen.core.product.productservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ProductDetailsControllerTest {

   @Autowired
   private MockMvc mockMvc;

   @Test
   void testGetProducts() throws Exception {
      mockMvc.perform( get( "/api/v1/products" )
                  .param( "page", "0" )
                  .param( "size", "10" )
                  .with( withReadScopeJwt() )
            )
            .andExpect( status().isOk() )
            .andDo( print() );
   }

   // Simulate an authenticated user with 'read' scope using a mock JWT (no real token validation)
   private RequestPostProcessor withReadScopeJwt() {
      return jwt().jwt( jwt -> jwt
            .claim( "scope", "read" )
            .claim( "sub", "admin" )
      );
   }
}
