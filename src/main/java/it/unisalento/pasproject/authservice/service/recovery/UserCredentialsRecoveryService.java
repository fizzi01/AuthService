package it.unisalento.pasproject.authservice.service.recovery;

import it.unisalento.pasproject.authservice.business.recovery.RecoveryUtils;
import it.unisalento.pasproject.authservice.domain.CredentialsRestore;
import it.unisalento.pasproject.authservice.domain.User;
import it.unisalento.pasproject.authservice.exceptions.TokenException;
import it.unisalento.pasproject.authservice.exceptions.UserNotFoundException;
import it.unisalento.pasproject.authservice.repositories.CredentialsRestoreRepository;
import it.unisalento.pasproject.authservice.repositories.UserRepository;
import it.unisalento.pasproject.authservice.service.NotificationConstants;
import it.unisalento.pasproject.authservice.service.NotificationMessageHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import static it.unisalento.pasproject.authservice.configuration.SecurityConfig.passwordEncoder;

@Service
public class UserCredentialsRecoveryService {

    private final UserRepository userRepository;
    private final CredentialsRestoreRepository credentialsRestoreRepository;
    private final NotificationMessageHandler notificationMessageHandler;

    @Autowired
    public UserCredentialsRecoveryService(UserRepository userRepository, CredentialsRestoreRepository credentialsRestoreRepository, NotificationMessageHandler notificationMessageHandler) {
        this.userRepository = userRepository;
        this.credentialsRestoreRepository = credentialsRestoreRepository;
        this.notificationMessageHandler = notificationMessageHandler;
    }

    public CredentialsRestore recoverCredentials(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UserNotFoundException("User not found");
        }
        CredentialsRestore credentialsRestore = new CredentialsRestore();
        credentialsRestore.setEmail(email);
        credentialsRestore.setToken(RecoveryUtils.generateSafeToken());
        credentialsRestore.setRequestDate(LocalDateTime.now());
        credentialsRestore.setExpirationDate(LocalDateTime.now().plusMinutes(15));
        credentialsRestore.setUsed(false);
        credentialsRestoreRepository.save(credentialsRestore);

        //Notification service to send email
        try{
            notificationMessageHandler.sendNotificationMessage(NotificationMessageHandler
                .buildNotificationMessage(
                        email,
                        credentialsRestore.getToken(), //Mando solo il token perchè voglio che venga buildato un link
                        "Password recovery",
                        NotificationConstants.AUTH_NOTIFICATION_TYPE,
                        true, false));
        } catch (Exception e) {
            //Silenzio le eccezioni, non è un passagio necessario, se fallisce amen si esegue un altro recovery
            return credentialsRestore;
        }

        return credentialsRestore;
    }

    public void resetPassword(String token, String newPassword) {
        Optional<CredentialsRestore> ret = credentialsRestoreRepository.findByToken(token);

        if (ret.isEmpty()) {
            throw new TokenException("Request not found");
        }

        CredentialsRestore credentialsRestore = ret.get();

        if (credentialsRestore.getExpirationDate().isBefore(LocalDateTime.now()) || credentialsRestore.isUsed()) {
            throw new TokenException("Request expired");
        }

        User user = userRepository.findByEmail(credentialsRestore.getEmail());

        user.setPassword(passwordEncoder().encode(newPassword));
        userRepository.save(user);
        credentialsRestore.setUsed(true);
        credentialsRestoreRepository.save(credentialsRestore);
    }

    public List<CredentialsRestore> getAllCredentialsRestore() {
        return credentialsRestoreRepository.findAll();
    }
}
