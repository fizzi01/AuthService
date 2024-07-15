package it.unisalento.pasproject.authservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.unisalento.pasproject.authservice.TestSecurityConfig;
import it.unisalento.pasproject.authservice.domain.User;
import it.unisalento.pasproject.authservice.dto.RegistrationDTO;
import it.unisalento.pasproject.authservice.exceptions.IllegalRequestException;
import it.unisalento.pasproject.authservice.exceptions.UserAlreadyExist;
import it.unisalento.pasproject.authservice.repositories.UserRepository;
import it.unisalento.pasproject.authservice.restControllers.RegistrationController;
import it.unisalento.pasproject.authservice.security.JwtUtilities;
import it.unisalento.pasproject.authservice.service.DataConsistencyService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static it.unisalento.pasproject.authservice.security.SecurityConstants.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RegistrationController.class)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
@Import(TestSecurityConfig.class)
class RegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private DataConsistencyService dataConsistencyService;

    @MockBean
    private JwtUtilities jwtUtilities;

    @InjectMocks
    private RegistrationController registrationController;

    private static final String VALID_EMAIL = "user@example.com";
    private static final String EXISTING_EMAIL = "existing@example.com";
    private static final String INVALID_ROLE = "INVALID_ROLE";
    private static final String ADMIN_TOKEN_ROLE = "ADMIN_TOKEN_SECRET";

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setEmail(EXISTING_EMAIL);
        user.setRole(ROLE_MEMBRO);
        user.setPassword("password");
        user.setName("John");
        user.setSurname("Doe");
        user.setId("1");
        user.setRegistrationDate(LocalDateTime.now().minusDays(10));

        given(userRepository.findByEmail(EXISTING_EMAIL)).willReturn(user);
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));
        when(jwtUtilities.extractRole(anyString())).thenReturn(ROLE_ADMIN);
    }

    @Test
    @WithMockUser(username = "user")
    void successfulRegistrationCreatesUser() throws Exception {
        RegistrationDTO registrationDTO = new RegistrationDTO();
        registrationDTO.setName("John");
        registrationDTO.setSurname("Doe");
        registrationDTO.setEmail(VALID_EMAIL);
        registrationDTO.setPassword("password");
        registrationDTO.setRole(ROLE_MEMBRO);

        mockMvc.perform(post("/api/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(registrationDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(VALID_EMAIL))
                .andExpect(jsonPath("$.role").value(ROLE_MEMBRO))
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.surname").value("Doe"))
                .andExpect(jsonPath("$.registrationDate").exists());


        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @WithMockUser(username = "user")
    void registrationWithExistingEmailThrowsUserAlreadyExistException() throws Exception {
        RegistrationDTO registrationDTO = new RegistrationDTO();
        registrationDTO.setName("Jane");
        registrationDTO.setSurname("Doe");
        registrationDTO.setEmail(EXISTING_EMAIL);
        registrationDTO.setPassword("password");
        registrationDTO.setRole(ROLE_MEMBRO);

        mockMvc.perform(post("/api/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(registrationDTO)))
                .andExpect(status().isConflict())
                .andExpect(result -> assertInstanceOf(UserAlreadyExist.class, result.getResolvedException()));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @WithMockUser(username = "user")
    void registrationWithInvalidRoleThrowsIllegalRequestException() throws Exception {
        RegistrationDTO registrationDTO = new RegistrationDTO();
        registrationDTO.setName("John");
        registrationDTO.setSurname("Doe");
        registrationDTO.setEmail(VALID_EMAIL);
        registrationDTO.setPassword("password");
        registrationDTO.setRole(INVALID_ROLE);

        mockMvc.perform(post("/api/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(registrationDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(IllegalRequestException.class, result.getResolvedException()));
    }

    @Test
    @WithMockUser(username = "user")
    void registrationWithAdminRoleSetsRoleCorrectly() throws Exception {
        RegistrationDTO registrationDTO = new RegistrationDTO();
        registrationDTO.setName("Admin");
        registrationDTO.setSurname("User");
        registrationDTO.setEmail(VALID_EMAIL);
        registrationDTO.setPassword("password");
        registrationDTO.setRole(ADMIN_TOKEN_ROLE);

        mockMvc.perform(post("/api/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(registrationDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value(ROLE_ADMIN));

        verify(userRepository, times(1)).save(any(User.class));
    }
}
