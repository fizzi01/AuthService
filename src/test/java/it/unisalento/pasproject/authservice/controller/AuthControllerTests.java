package it.unisalento.pasproject.authservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.unisalento.pasproject.authservice.TestSecurityConfig;
import it.unisalento.pasproject.authservice.domain.CredentialsRestore;
import it.unisalento.pasproject.authservice.domain.User;
import it.unisalento.pasproject.authservice.dto.ChangePasswordDTO;
import it.unisalento.pasproject.authservice.dto.LoginDTO;
import it.unisalento.pasproject.authservice.dto.ResetPasswordDTO;
import it.unisalento.pasproject.authservice.exceptions.TokenException;
import it.unisalento.pasproject.authservice.exceptions.UserNotFoundException;
import it.unisalento.pasproject.authservice.repositories.UserRepository;
import it.unisalento.pasproject.authservice.restControllers.AuthController;
import it.unisalento.pasproject.authservice.security.JwtUtilities;
import it.unisalento.pasproject.authservice.service.recovery.UserCredentialsRecoveryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static it.unisalento.pasproject.authservice.security.SecurityConstants.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
@Import(TestSecurityConfig.class)
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private AuthenticationManager authenticationManager;


    @MockBean
    private JwtUtilities jwtUtilities;

    @MockBean
    private UserCredentialsRecoveryService userCredentialsRecoveryService;

    @InjectMocks
    private AuthController authController;

    private static final String VALID_EMAIL = "user@example.com";
    private static final String VALID_PASSWORD = "password";
    private static final String INVALID_EMAIL = "invalid@example.com";
    private static final String INVALID_PASSWORD = "wrongpassword";
    private static final String JWT_TOKEN = "Bearer_123456";

    private static final String EMAIL = "user@example.com";
    private static final String TOKEN = "validToken";
    private static final String NEW_PASSWORD = "newPassword123";


    @BeforeEach
    void setUp() {
        User user = new User();
        user.setEmail(VALID_EMAIL);
        user.setPassword(VALID_PASSWORD);
        user.setRole(ROLE_MEMBRO);

        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(user);
        when(jwtUtilities.generateToken(VALID_EMAIL, ROLE_MEMBRO)).thenReturn(JWT_TOKEN);
    }

    @Test
    @WithMockUser(username = VALID_EMAIL, roles = {ROLE_MEMBRO})
    void authenticationWhenValidCredentialsReturnJwtToken() throws Exception {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail(VALID_EMAIL);
        loginDTO.setPassword(VALID_PASSWORD);
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(mock(Authentication.class));

        mockMvc.perform(post("/api/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwt").value(JWT_TOKEN));
    }

    @Test
    @WithMockUser(username = "valid@example.com", roles = {ROLE_MEMBRO})
    void authenticationWithInvalidEmailThrowsUsernameNotFoundException() throws Exception {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail(INVALID_EMAIL);
        loginDTO.setPassword(INVALID_PASSWORD);
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(mock(Authentication.class));
        given(userRepository.findByEmail(INVALID_EMAIL)).willReturn(null);

        mockMvc.perform(post("/api/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(loginDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = VALID_EMAIL, roles = {ROLE_MEMBRO})
    void authenticationWithInvalidPasswordThrowsAuthenticationException() throws Exception {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail(VALID_EMAIL);
        loginDTO.setPassword(INVALID_PASSWORD);
        when(authenticationManager.authenticate(any(Authentication.class))).thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(loginDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Bad credentials"));
    }

    @Test
    @WithMockUser(username = VALID_EMAIL, roles = {ROLE_MEMBRO})
    void recoverCredentialsWhenEmailExistsSendsRecoveryEmail() throws Exception {
        CredentialsRestore restore = new CredentialsRestore();
        restore.setEmail(VALID_EMAIL);
        restore.setExpirationDate(LocalDateTime.now().plusDays(1));
        restore.setUsed(false);

        when(userCredentialsRecoveryService.recoverCredentials(VALID_EMAIL)).thenReturn(restore);

        mockMvc.perform(get("/api/recover")
                        .param("email", EMAIL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.msg").value("Recovery email sent"));
    }

    @Test
    @WithMockUser(username = INVALID_EMAIL, roles = {ROLE_MEMBRO})
    void recoverCredentialsWhenEmailDoesNotExistThrowsUserNotFoundException() throws Exception {
        given(userCredentialsRecoveryService.recoverCredentials(INVALID_EMAIL))
                .willThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(get("/api/recover")
                        .param("email", "nonexistent@example.com"));

        assertThatThrownBy(() -> userCredentialsRecoveryService.recoverCredentials(INVALID_EMAIL))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    @WithMockUser(username = VALID_EMAIL, roles = {ROLE_MEMBRO})
    void resetPasswordWithValidTokenResetsPassword() throws Exception {
        ResetPasswordDTO resetPasswordDTO = new ResetPasswordDTO();
        resetPasswordDTO.setNewPassword(NEW_PASSWORD);
        resetPasswordDTO.setToken(TOKEN);

        mockMvc.perform(post("/api/recover/{token}", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(resetPasswordDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("Password reset"));
    }

    @Test
    @WithMockUser(username = VALID_EMAIL, roles = {ROLE_MEMBRO})
    void resetPasswordWithInvalidTokenThrowsException() throws Exception {
        ResetPasswordDTO resetPasswordDTO = new ResetPasswordDTO();
        resetPasswordDTO.setNewPassword(NEW_PASSWORD);
        resetPasswordDTO.setToken("invalidToken");

        doThrow(new TokenException("Request not found")).when(userCredentialsRecoveryService).resetPassword("invalidToken", NEW_PASSWORD);

        mockMvc.perform(post("/api/recover/{token}", "invalidToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(resetPasswordDTO)))
                .andExpect(status().isExpectationFailed());

        assertThatThrownBy(() -> userCredentialsRecoveryService.resetPassword("invalidToken", NEW_PASSWORD))
                .isInstanceOf(TokenException.class);

    }

    @Test
    @WithMockUser(username = VALID_EMAIL, roles = {ROLE_MEMBRO})
    void changePasswordWithValidCredentialsChangesPassword() throws Exception {
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO();
        changePasswordDTO.setOldPassword("oldPassword");
        changePasswordDTO.setNewPassword(NEW_PASSWORD);

        mockMvc.perform(post("/api/change/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(changePasswordDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.msg").value("Password changed"));
    }

    @Test
    @WithMockUser(username = VALID_EMAIL, roles = {ROLE_ADMIN})
    void getRequestsAsAdminReturnsAllRequests() throws Exception {
        List<CredentialsRestore> requests = new ArrayList<>();
        CredentialsRestore request = new CredentialsRestore();
        request.setEmail(EMAIL);
        request.setToken(TOKEN);
        request.setRequestDate(LocalDateTime.now());
        request.setExpirationDate(LocalDateTime.now().plusDays(1));
        requests.add(request);

        when(userCredentialsRecoveryService.getAllCredentialsRestore()).thenReturn(requests);

        mockMvc.perform(get("/api/find/recover/requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.list[0].email").value(EMAIL));
    }

    @Test
    @WithMockUser(username = VALID_EMAIL, roles = {ROLE_MEMBRO})
    void getRequestsAsNonAdminThrowsAccessDeniedException() throws Exception {
        mockMvc.perform(get("/api/find/recover/requests"))
                .andExpect(status().isForbidden());
    }

}