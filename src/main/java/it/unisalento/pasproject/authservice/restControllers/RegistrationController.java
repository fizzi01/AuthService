package it.unisalento.pasproject.authservice.restControllers;

import it.unisalento.pasproject.authservice.domain.User;
import it.unisalento.pasproject.authservice.dto.RegistrationDTO;
import it.unisalento.pasproject.authservice.dto.UserDTO;
import it.unisalento.pasproject.authservice.repositories.UserRepository;
import it.unisalento.pasproject.authservice.service.DataConsistencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

import static it.unisalento.pasproject.authservice.configuration.SecurityConfig.passwordEncoder;

@RestController
@RequestMapping("/api/registration")
public class RegistrationController {
    @Autowired
    UserRepository userRepository;

    @Autowired
    private DataConsistencyService dataConsistencyService;

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public UserDTO post(@RequestBody RegistrationDTO registrationDTO) {
        User existingUser = userRepository.findByEmail(registrationDTO.getEmail());
        if (existingUser != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User already exists");
        }

        User user = new User();
        user.setName(registrationDTO.getName());
        user.setSurname(registrationDTO.getSurname());
        user.setEmail(registrationDTO.getEmail());
        user.setPassword(passwordEncoder().encode(registrationDTO.getPassword()));
        user.setRole(registrationDTO.getRole());
        user.setRegistrationDate(LocalDateTime.now());

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
