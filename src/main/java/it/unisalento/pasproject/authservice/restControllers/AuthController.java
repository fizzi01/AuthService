package it.unisalento.pasproject.authservice.restControllers;


import it.unisalento.pasproject.authservice.domain.User;
import it.unisalento.pasproject.authservice.dto.AuthenticationResponseDTO;
import it.unisalento.pasproject.authservice.dto.LoginDTO;
import it.unisalento.pasproject.authservice.repositories.UserRepository;
import it.unisalento.pasproject.authservice.security.JwtUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthController {

    final UserRepository userRepository;

    private final AuthenticationManager authenticationManager;

    private final JwtUtilities jwtUtilities;

    @Autowired
    public AuthController(UserRepository userRepository, AuthenticationManager authenticationManager, JwtUtilities jwtUtilities) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtUtilities = jwtUtilities;
    }


    @RequestMapping(value="/authenticate", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestBody LoginDTO loginDTO) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDTO.getEmail(),
                        loginDTO.getPassword()
                )
        );
        User user = userRepository.findByEmail(loginDTO.getEmail());
        if(user == null) {
            throw new UsernameNotFoundException("Not found user with email: " + loginDTO.getEmail() + ".");
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);
        final String jwt = jwtUtilities.generateToken(user.getEmail(), user.getRole());
        return ResponseEntity.ok(new AuthenticationResponseDTO(jwt));
    }

}
