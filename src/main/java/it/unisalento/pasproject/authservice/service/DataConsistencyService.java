package it.unisalento.pasproject.authservice.service;

import it.unisalento.pasproject.authservice.business.io.producer.MessageProducer;
import it.unisalento.pasproject.authservice.domain.User;
import it.unisalento.pasproject.authservice.dto.RegistrationDTO;
import it.unisalento.pasproject.authservice.dto.UpdatedProfileMessageDTO;
import it.unisalento.pasproject.authservice.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DataConsistencyService {
    private final UserRepository userRepository;
    private final MessageProducer messageProducer;

    private static final Logger LOGGER = LoggerFactory.getLogger(DataConsistencyService.class);

    @Value("${rabbitmq.exchange.data.name}")
    private String dataExchange;

    @Value("${rabbitmq.routing.data.key}")
    private String dataRoutingKey;

    @Autowired
    public DataConsistencyService(MessageProducer messageProducer, UserRepository userRepository) {
        this.messageProducer = messageProducer;
        this.userRepository = userRepository;
    }

    public void alertDataConsistency(RegistrationDTO user) {
        messageProducer.sendMessage(user,dataRoutingKey,dataExchange);
    }

    @RabbitListener(queues = "${rabbitmq.queue.update.name}")
    public void receiveMessage(UpdatedProfileMessageDTO updatedProfileMessageDTO) {
        LOGGER.info("Received message {}", updatedProfileMessageDTO.toString());

        //TODO: Implementare logica per gestire il dto e aggiornare l'auth
        User user = userRepository.findByEmail(updatedProfileMessageDTO.getEmail());

        if(user != null) {
            user.setName(updatedProfileMessageDTO.getName());
            user.setSurname(updatedProfileMessageDTO.getSurname());
            userRepository.save(user);
        }

    }
}
