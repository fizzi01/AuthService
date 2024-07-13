package it.unisalento.pasproject.authservice.restControllers;


import it.unisalento.pasproject.authservice.domain.CredentialsRestore;
import it.unisalento.pasproject.authservice.domain.User;
import it.unisalento.pasproject.authservice.dto.*;
import it.unisalento.pasproject.authservice.exceptions.UserNotFoundException;
import it.unisalento.pasproject.authservice.repositories.UserRepository;
import it.unisalento.pasproject.authservice.security.JwtUtilities;
import it.unisalento.pasproject.authservice.service.recovery.UserCredentialsRecoveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static it.unisalento.pasproject.authservice.security.SecurityConstants.*;

@RestController
@RequestMapping("/api")
public class AuthController {

    final UserRepository userRepository;

    private final AuthenticationManager authenticationManager;

    private final JwtUtilities jwtUtilities;

    private final UserCredentialsRecoveryService userCredentialsRecoveryService;

    private final Logger logger = Logger.getLogger(AuthController.class.getName());

    @Autowired
    public AuthController(UserRepository userRepository, AuthenticationManager authenticationManager, JwtUtilities jwtUtilities, UserCredentialsRecoveryService userCredentialsRecoveryService) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtUtilities = jwtUtilities;
        this.userCredentialsRecoveryService = userCredentialsRecoveryService;
    }

    @GetMapping(value="/recover")
    public RecoveryResponseDTO recoverCredentials(@RequestParam String email) {
        CredentialsRestore restore = userCredentialsRecoveryService.recoverCredentials(email);
        RecoveryResponseDTO response = new RecoveryResponseDTO();
        response.setEmail(restore.getEmail());
        response.setMsg("Recovery email sent");
        response.setExpirationDate(restore.getExpirationDate());
        return response;
    }

    @PostMapping(value="/recover/{token}")
    public RecoveryResponseDTO resetPassword(@PathVariable String token, @RequestBody ResetPasswordDTO resetPasswordDTO) {
        userCredentialsRecoveryService.resetPassword(token, resetPasswordDTO.getNewPassword());
        RecoveryResponseDTO response = new RecoveryResponseDTO();
        response.setMsg("Password reset");
        return response;
    }

    @PostMapping(value="/recover")
    public RecoveryResponseDTO resetPassword(@RequestBody ResetPasswordDTO resetPasswordDTO) {
        userCredentialsRecoveryService.resetPassword(resetPasswordDTO.getToken(), resetPasswordDTO.getNewPassword());
        RecoveryResponseDTO response = new RecoveryResponseDTO();
        response.setMsg("Password reset");
        return response;
    }

    @PostMapping(value="/change/password")
    public RecoveryResponseDTO resetPassword(@RequestBody ChangePasswordDTO changePasswordDTO) {
        userCredentialsRecoveryService.changePassword(changePasswordDTO.getOldPassword(), changePasswordDTO.getNewPassword());
        RecoveryResponseDTO response = new RecoveryResponseDTO();
        response.setMsg("Password changed");
        return response;
    }

    @GetMapping(value="/find/recover/requests")
    @Secured({ROLE_ADMIN})
    public ListCredentialsRestoreDTO getRequests() {
        List<CredentialsRestore> requests = userCredentialsRecoveryService.getAllCredentialsRestore();
        ListCredentialsRestoreDTO response = new ListCredentialsRestoreDTO();
        List<CredentialsRestoreDTO> dtoList = new ArrayList<>();
        requests.forEach(request -> {
            CredentialsRestoreDTO dto = new CredentialsRestoreDTO();
            dto.setEmail(request.getEmail());
            dto.setToken(request.getToken());
            dto.setRequestDate(request.getRequestDate());
            dto.setExpirationDate(request.getExpirationDate());
            dtoList.add(dto);
        });
        response.setList(dtoList);
        return response;
    }


    @PostMapping(value="/authenticate")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody LoginDTO loginDTO) {
        try{
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDTO.getEmail(),
                            loginDTO.getPassword()
                    )
            );

            User user = userRepository.findByEmail(loginDTO.getEmail());
            if(user == null) {
                throw new UserNotFoundException("Not found user with email: " + loginDTO.getEmail() + ".");
            }

            SecurityContextHolder.getContext().setAuthentication(authentication);
            final String jwt = jwtUtilities.generateToken(user.getEmail(), user.getRole());
            return ResponseEntity.ok(new AuthenticationResponseDTO(jwt));

        } catch (Exception e) {
            throw new UserNotFoundException(e.getMessage());
        }

    }

}
