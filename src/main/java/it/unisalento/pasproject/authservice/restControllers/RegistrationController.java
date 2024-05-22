package it.unisalento.pasproject.authservice.restControllers;

import it.unisalento.pasproject.authservice.domain.User;
import it.unisalento.pasproject.authservice.dto.RegistrationDTO;
import it.unisalento.pasproject.authservice.dto.UserDTO;
import it.unisalento.pasproject.authservice.exceptions.UserAlreadyExist;
import it.unisalento.pasproject.authservice.repositories.UserRepository;
import it.unisalento.pasproject.authservice.service.DataConsistencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

import static it.unisalento.pasproject.authservice.configuration.SecurityConfig.passwordEncoder;

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

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public UserDTO post(@RequestBody RegistrationDTO registrationDTO) {
        User existingUser = userRepository.findByEmail(registrationDTO.getEmail());
        if (existingUser != null) {
            throw new UserAlreadyExist("User already exists: " + registrationDTO.getEmail());
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
