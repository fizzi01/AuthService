package it.unisalento.pasproject.authservice.service;

import it.unisalento.pasproject.authservice.domain.CredentialsRestore;
import it.unisalento.pasproject.authservice.domain.User;
import it.unisalento.pasproject.authservice.dto.NotificationMessageDTO;
import it.unisalento.pasproject.authservice.exceptions.TokenException;
import it.unisalento.pasproject.authservice.exceptions.UserNotFoundException;
import it.unisalento.pasproject.authservice.repositories.CredentialsRestoreRepository;
import it.unisalento.pasproject.authservice.repositories.UserRepository;
import it.unisalento.pasproject.authservice.service.recovery.UserCredentialsRecoveryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.Optional;

import static it.unisalento.pasproject.authservice.configuration.SecurityConfig.passwordEncoder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = {UserCredentialsRecoveryService.class})
class UserCredentialsRecoveryServiceTests {

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private CredentialsRestoreRepository credentialsRestoreRepository;

    @MockBean
    private NotificationMessageHandler notificationMessageHandler;

    @MockBean
    private UserCheckService userCheckService;

    @InjectMocks
    private UserCredentialsRecoveryService userCredentialsRecoveryService;

    private static final String VALID_EMAIL = "user@example.com";
    private static final String INVALID_EMAIL = "invalid@example.com";
    private static final String VALID_TOKEN = "validToken";
    private static final String INVALID_TOKEN = "invalidToken";
    private static final String NEW_PASSWORD = "newPassword123";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userRepository = mock(UserRepository.class);
        credentialsRestoreRepository = mock(CredentialsRestoreRepository.class);
        notificationMessageHandler = mock(NotificationMessageHandler.class);
        userCheckService = mock(UserCheckService.class);
        userCredentialsRecoveryService = new UserCredentialsRecoveryService(userRepository, credentialsRestoreRepository, notificationMessageHandler, userCheckService);
    }

    @Test
    void recoverCredentialsForExistingUserSendsNotification() {
        User user = new User();
        user.setEmail(VALID_EMAIL);
        user.setPassword("oldPassword");
        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(user);
        userCredentialsRecoveryService.recoverCredentials(VALID_EMAIL);

        verify(notificationMessageHandler, times(1)).sendNotificationMessage(any(NotificationMessageDTO.class));
    }

    @Test
    void recoverCredentialsForNonExistingUserThrowsException() {
        when(userRepository.findByEmail(INVALID_EMAIL)).thenReturn(null);
        assertThrows(UserNotFoundException.class, () -> userCredentialsRecoveryService.recoverCredentials(INVALID_EMAIL));
    }

    @Test
    void resetPasswordWithValidTokenUpdatesPassword() {
        CredentialsRestore credentialsRestore = new CredentialsRestore();
        credentialsRestore.setEmail(VALID_EMAIL);
        credentialsRestore.setToken(VALID_TOKEN);
        credentialsRestore.setExpirationDate(LocalDateTime.now().plusMinutes(15));
        credentialsRestore.setUsed(false);
        when(credentialsRestoreRepository.findByToken(VALID_TOKEN)).thenReturn(Optional.of(credentialsRestore));

        User user = new User();
        user.setEmail(VALID_EMAIL);
        user.setPassword("oldPassword");
        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(user);

        userCredentialsRecoveryService.resetPassword(VALID_TOKEN, NEW_PASSWORD);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertTrue(passwordEncoder().matches(NEW_PASSWORD, userCaptor.getValue().getPassword()));
    }

    @Test
    void resetPasswordWithInvalidTokenThrowsException() {
        when(credentialsRestoreRepository.findByToken(INVALID_TOKEN)).thenReturn(Optional.empty());

        assertThrows(TokenException.class, () -> userCredentialsRecoveryService.resetPassword(INVALID_TOKEN, NEW_PASSWORD));
    }

    @Test
    void resetPasswordWithExpiredTokenThrowsException() {
        CredentialsRestore credentialsRestore = new CredentialsRestore();
        credentialsRestore.setEmail(VALID_EMAIL);
        credentialsRestore.setToken(VALID_TOKEN);
        credentialsRestore.setExpirationDate(LocalDateTime.now().minusMinutes(1));
        credentialsRestore.setUsed(false);
        when(credentialsRestoreRepository.findByToken(VALID_TOKEN)).thenReturn(Optional.of(credentialsRestore));

        assertThrows(TokenException.class, () -> userCredentialsRecoveryService.resetPassword(VALID_TOKEN, NEW_PASSWORD));
    }

    @Test
    void changePasswordWithValidOldPasswordUpdatesPassword() {
        User user = new User();
        user.setEmail(VALID_EMAIL);
        user.setPassword(passwordEncoder().encode("oldPassword"));

        when(userCheckService.getCurrentUserEmail()).thenReturn(VALID_EMAIL);
        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(user);

        userCredentialsRecoveryService.changePassword("oldPassword", NEW_PASSWORD);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertTrue(passwordEncoder().matches(NEW_PASSWORD, userCaptor.getValue().getPassword()));
    }

    @Test
    void changePasswordWithInvalidOldPasswordThrowsException() {
        User user = new User();
        user.setEmail(VALID_EMAIL);
        user.setPassword(passwordEncoder().encode("oldPassword"));

        when(userCheckService.getCurrentUserEmail()).thenReturn(VALID_EMAIL);
        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(user);

        assertThrows(TokenException.class, () -> userCredentialsRecoveryService.changePassword("wrongOldPassword", NEW_PASSWORD));
    }
}