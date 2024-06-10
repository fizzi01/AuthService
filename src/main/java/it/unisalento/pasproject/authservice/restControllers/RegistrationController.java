package it.unisalento.pasproject.authservice.restControllers;

import it.unisalento.pasproject.authservice.domain.User;
import it.unisalento.pasproject.authservice.dto.RegistrationDTO;
import it.unisalento.pasproject.authservice.dto.UserDTO;
import it.unisalento.pasproject.authservice.exceptions.UserAlreadyExist;
import it.unisalento.pasproject.authservice.repositories.UserRepository;
import it.unisalento.pasproject.authservice.service.DataConsistencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

import static it.unisalento.pasproject.authservice.configuration.SecurityConfig.passwordEncoder;
import static it.unisalento.pasproject.authservice.security.SecurityConstants.*;

@RestController
@RequestMapping("/api/registration")
public class RegistrationController {
    private final UserRepository userRepository;

    private final DataConsistencyService dataConsistencyService;

    @Autowired
    public RegistrationController(UserRepository userRepository, DataConsistencyService dataConsistencyService) {
        this.userRepository = userRepository;
        this.dataConsistencyService = dataConsistencyService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public UserDTO post(@RequestBody RegistrationDTO registrationDTO) {
        User existingUser = userRepository.findByEmail(registrationDTO.getEmail());
        if (existingUser != null) {
            throw new UserAlreadyExist("User already exists: " + registrationDTO.getEmail());
        }

        //TODO: Defaulting role to MEMBRO, Only admin can set the role to greater than MEMBRO
        if (registrationDTO.getRole() == null) {
            registrationDTO.setRole(ROLE_MEMBRO);
        }

        User user = new User();
        user.setName(registrationDTO.getName());
        user.setSurname(registrationDTO.getSurname());
        user.setEmail(registrationDTO.getEmail());
        user.setRegistrationDate(LocalDateTime.now());
        user.setPassword(passwordEncoder().encode(registrationDTO.getPassword()));


        switch (registrationDTO.getRole().toUpperCase()) {
            case ROLE_MEMBRO -> user.setRole(ROLE_MEMBRO);
            case ROLE_UTENTE -> user.setRole(ROLE_UTENTE);
            default -> throw new IllegalArgumentException("Invalid role: " + registrationDTO.getRole());
        }

        //Così restituisce l'id assegnato da MongoDB
        user = userRepository.save(user);

        registrationDTO.setRegistrationDate(user.getRegistrationDate());
        dataConsistencyService.alertDataConsistency(registrationDTO);

        UserDTO retUser = new UserDTO();
        retUser.setId(user.getId());
        retUser.setName(user.getName());
        retUser.setSurname(user.getSurname());
        retUser.setEmail(user.getEmail());
        retUser.setRole(user.getRole());
        retUser.setRegistrationDate(user.getRegistrationDate());
        
        return retUser;
    }
}
