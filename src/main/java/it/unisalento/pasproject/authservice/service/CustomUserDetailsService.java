package it.unisalento.pasproject.authservice.service;

import it.unisalento.pasproject.authservice.domain.User;
import it.unisalento.pasproject.authservice.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


// Questa classe implementa l'interfaccia UserDetailsService di Spring Security, fornisce dettagli di un certo utente
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        final User user = userRepository.findByEmail(email);

        if(user == null) {
            throw new UsernameNotFoundException(email);
        }

        UserDetails userDetails = org.springframework.security.core.userdetails.User.withUsername(user.getEmail()).password(user.getPassword()).roles("USER").build();

        return userDetails;
    }
}
