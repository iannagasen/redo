package dev.agasen.core.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for the SecurityConfig filter chains in user-service.
 * Extends BaseIntegrationTest to reuse the Postgres and Redis infrastructure.
 */
public class SecurityFilterIntegrationTest extends BaseIntegrationTest {

    @Value("${internal.api.key}")
    private String internalApiKey;

    @Test
    void whenAccessInternalEndpointWithValidKey_thenAuthorized() throws Exception {
        // 'admin' user is seeded by Liquibase.
        // Successful authentication should lead to the controller and return 200.
        mockMvc.perform(get("/internal/users/admin/auth-info")
                .header("X-Internal-Api-Key", internalApiKey)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void whenAccessInternalEndpointWithInvalidKey_thenUnauthorized() throws Exception {
        mockMvc.perform(get("/internal/users/admin/auth-info")
                .header("X-Internal-Api-Key", "wrong-key")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void whenAccessInternalEndpointWithoutKey_thenUnauthorized() throws Exception {
        mockMvc.perform(get("/internal/users/admin/auth-info")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void whenAccessPublicEndpoint_thenAuthorized() throws Exception {
        // Actuator health check should be public. 
        // With BaseIntegrationTest, Redis is UP, so this should return 200.
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void whenAccessStandardEndpointWithoutToken_thenUnauthorized() throws Exception {
        // Standard endpoints (catch-all) require OAuth2 token.
        mockMvc.perform(get("/users"))
                .andExpect(status().isUnauthorized());
    }
}
