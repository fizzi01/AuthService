package it.unisalento.pasproject.authservice.service;


import it.unisalento.pasproject.authservice.business.exchanger.MessageExchangeStrategy;
import it.unisalento.pasproject.authservice.business.exchanger.MessageExchanger;
import it.unisalento.pasproject.authservice.dto.UserDetailsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import static it.unisalento.pasproject.authservice.security.SecurityConstants.ROLE_ADMIN;


@Service
public class UserCheckService {

    private final MessageExchanger messageExchanger;

    @Value("${rabbitmq.exchange.security.name}")
    private String securityExchange;

    @Value("${rabbitmq.routing.security.key}")
    private String securityRequestRoutingKey;

    private static final Logger LOGGER = LoggerFactory.getLogger(UserCheckService.class);

    @Autowired
    public UserCheckService(MessageExchanger messageExchanger, @Qualifier("RabbitMQExchange") MessageExchangeStrategy messageExchangeStrategy) {
        this.messageExchanger = messageExchanger;
        this.messageExchanger.setStrategy(messageExchangeStrategy);
    }

    /**
     * Load the user details by email
     * @param email the email of the user
     * @return the user details
     * @throws UsernameNotFoundException if the user is not found
     */
    public UserDetailsDTO loadUserByUsername(String email) throws UsernameNotFoundException {

        //Chiamata MQTT a CQRS per ottenere i dettagli dell'utente
        UserDetailsDTO user = messageExchanger.exchangeMessage(email,securityRequestRoutingKey,securityExchange,UserDetailsDTO.class);

        if(user == null) {
            throw new UsernameNotFoundException(email);
        }

        return user;
    }


    public Boolean isEnable(Boolean enable) {
        return enable;
    }

    /**
     * Check if the current user is the user with the given email
     * @param email the email of the user to check
     * @return true if the current user is the user with the given email, false otherwise
     */
    public Boolean isCorrectUser(String email){
        return email.equals(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    /**
     * Check if the current user is an administrator
     * @return true if the current user is an administrator, false otherwise
     */
    public Boolean isAdministrator(){
        String currentRole = SecurityContextHolder.getContext().getAuthentication().getAuthorities().toString();
        return currentRole.equalsIgnoreCase(ROLE_ADMIN);
    }

}
