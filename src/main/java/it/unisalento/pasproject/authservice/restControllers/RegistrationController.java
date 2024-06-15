package it.unisalento.pasproject.authservice.restControllers;

import it.unisalento.pasproject.authservice.domain.User;
import it.unisalento.pasproject.authservice.dto.RegistrationDTO;
import it.unisalento.pasproject.authservice.dto.UserDTO;
import it.unisalento.pasproject.authservice.exceptions.IllegalRequestException;
import it.unisalento.pasproject.authservice.exceptions.UserAlreadyExist;
import it.unisalento.pasproject.authservice.repositories.UserRepository;
import it.unisalento.pasproject.authservice.security.JwtUtilities;
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

    private final JwtUtilities jwtUtilities;

    @Autowired
    public RegistrationController(UserRepository userRepository, DataConsistencyService dataConsistencyService, JwtUtilities jwtUtilities) {
        this.userRepository = userRepository;
        this.dataConsistencyService = dataConsistencyService;
        this.jwtUtilities = jwtUtilities;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public UserDTO post(@RequestBody RegistrationDTO registrationDTO) {
        User existingUser = userRepository.findByEmail(registrationDTO.getEmail());
        if (existingUser != null) {
            throw new UserAlreadyExist("User already exists: " + registrationDTO.getEmail());
        }

        if (registrationDTO.getRole() == null) {
            registrationDTO.setRole(ROLE_MEMBRO);
        }else if(jwtUtilities.extractRole(registrationDTO.getRole()).equals(ROLE_ADMIN)){
            registrationDTO.setRole(ROLE_ADMIN);
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
            case ROLE_ADMIN -> user.setRole(ROLE_ADMIN);
            default -> throw new IllegalRequestException("Invalid role: " + registrationDTO.getRole());
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
