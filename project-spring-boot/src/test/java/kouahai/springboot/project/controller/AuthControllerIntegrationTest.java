package kouahai.springboot.project.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import kouahai.springboot.project.dto.auth.LoginRequest;
import kouahai.springboot.project.dto.auth.RegisterRequest;
import kouahai.springboot.project.model.User;
import kouahai.springboot.project.model.UserStatus;
import kouahai.springboot.project.repository.UserRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(locations = "classpath:application.properties")
class AuthControllerIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        @BeforeEach
        void setUp() {
                userRepository.deleteAll();
        }

        @Test
        void testRegisterUser_Success() throws Exception {
                RegisterRequest request = new RegisterRequest(
                                "testuser",
                                "test@example.com",
                                "password123",
                                "Test User");

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.userId").exists())
                                .andExpect(jsonPath("$.username").value("testuser"))
                                .andExpect(jsonPath("$.email").value("test@example.com"))
                                .andExpect(jsonPath("$.message")
                                                .value("User registered successfully. Please verify your email."));
        }

        @Test
        void testRegisterUser_DuplicateUsername() throws Exception {
                // Create existing user
                User existingUser = new User();
                existingUser.setUsername("testuser");
                existingUser.setEmail("existing@example.com");
                existingUser.setPasswordHash(passwordEncoder.encode("password"));
                existingUser.setFullName("Existing User");
                existingUser.setStatus(UserStatus.ACTIVE);
                userRepository.save(existingUser);

                RegisterRequest request = new RegisterRequest(
                                "testuser",
                                "new@example.com",
                                "password123",
                                "New User");

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").value("Username already exists"));
        }

        @Test
        void testRegisterUser_DuplicateEmail() throws Exception {
                // Create existing user
                User existingUser = new User();
                existingUser.setUsername("existinguser");
                existingUser.setEmail("test@example.com");
                existingUser.setPasswordHash(passwordEncoder.encode("password"));
                existingUser.setFullName("Existing User");
                existingUser.setStatus(UserStatus.ACTIVE);
                userRepository.save(existingUser);

                RegisterRequest request = new RegisterRequest(
                                "newuser",
                                "test@example.com",
                                "password123",
                                "New User");

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.message").value("Email already exists"));
        }

        @Test
        void testLogin_Success() throws Exception {
                // Create user
                User user = new User();
                user.setUsername("testuser");
                user.setEmail("test@example.com");
                user.setPasswordHash(passwordEncoder.encode("password123"));
                user.setFullName("Test User");
                user.setStatus(UserStatus.ACTIVE);
                userRepository.save(user);

                LoginRequest request = new LoginRequest("testuser", "password123");

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.token").exists())
                                .andExpect(jsonPath("$.username").value("testuser"))
                                .andExpect(jsonPath("$.email").value("test@example.com"))
                                .andExpect(jsonPath("$.type").value("Bearer"));
        }

        @Test
        void testLogin_InvalidCredentials() throws Exception {
                LoginRequest request = new LoginRequest("nonexistent", "wrongpassword");

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        void testAccessPublicEndpoint_NoAuth() throws Exception {
                mockMvc.perform(get("/api/test/public"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").value("Public endpoint - No authentication required"));
        }

        @Test
        void testAccessAuthenticatedEndpoint_NoToken() throws Exception {
                mockMvc.perform(get("/api/test/authenticated"))
                                .andExpect(status().isForbidden());
        }

        @Test
        void testAccessAuthenticatedEndpoint_WithToken() throws Exception {
                // Create and authenticate user
                User user = new User();
                user.setUsername("testuser");
                user.setEmail("test@example.com");
                user.setPasswordHash(passwordEncoder.encode("password123"));
                user.setFullName("Test User");
                user.setStatus(UserStatus.ACTIVE);
                userRepository.save(user);

                LoginRequest loginRequest = new LoginRequest("testuser", "password123");

                String response = mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginRequest)))
                                .andExpect(status().isOk())
                                .andReturn()
                                .getResponse()
                                .getContentAsString();

                String token = objectMapper.readTree(response).get("token").asText();

                mockMvc.perform(get("/api/test/authenticated")
                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$")
                                                .value(org.hamcrest.Matchers.containsString("Welcome testuser")));
        }

        @Test
        void testRegisterUser_ValidationErrors() throws Exception {
                RegisterRequest request = new RegisterRequest(
                                "ab", // Too short username
                                "invalid-email", // Invalid email
                                "123", // Too short password
                                "" // Empty full name
                );

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }
}
