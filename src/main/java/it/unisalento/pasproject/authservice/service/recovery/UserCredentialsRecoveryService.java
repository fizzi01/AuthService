package it.unisalento.pasproject.authservice.service.recovery;

import it.unisalento.pasproject.authservice.business.recovery.RecoveryUtils;
import it.unisalento.pasproject.authservice.domain.CredentialsRestore;
import it.unisalento.pasproject.authservice.domain.User;
import it.unisalento.pasproject.authservice.exceptions.TokenException;
import it.unisalento.pasproject.authservice.exceptions.UserNotFoundException;
import it.unisalento.pasproject.authservice.repositories.CredentialsRestoreRepository;
import it.unisalento.pasproject.authservice.repositories.UserRepository;
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
    private static final Logger LOGGER = Logger.getLogger(UserCredentialsRecoveryService.class.getName());
    @Autowired
    public UserCredentialsRecoveryService(UserRepository userRepository, CredentialsRestoreRepository credentialsRestoreRepository) {
        this.userRepository = userRepository;
        this.credentialsRestoreRepository = credentialsRestoreRepository;
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
        LOGGER.info("Password reset for user: " + user.getEmail());
        userRepository.save(user);
        LOGGER.info("Saved into DB");
        credentialsRestore.setUsed(true);
        credentialsRestoreRepository.save(credentialsRestore);
    }

    public List<CredentialsRestore> getAllCredentialsRestore() {
        return credentialsRestoreRepository.findAll();
    }
}
