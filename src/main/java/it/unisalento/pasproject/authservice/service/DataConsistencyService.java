package it.unisalento.pasproject.authservice.service;

import it.unisalento.pasproject.authservice.business.io.producer.MessageProducer;
import it.unisalento.pasproject.authservice.dto.RegistrationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DataConsistencyService {
    @Autowired
    private MessageProducer messageProducer;


    @Value("${rabbitmq.exchange.data.name}")
    private String dataExchange;

    @Value("${rabbitmq.routing.data.key}")
    private String dataRoutingKey;

    public void alertDataConsistency(RegistrationDTO user) {
        messageProducer.sendMessage(user,dataRoutingKey,dataExchange);
    }
}
