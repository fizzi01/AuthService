package it.unisalento.pasproject.authservice.service;

import it.unisalento.pasproject.authservice.business.producer.MessageProducer;
import it.unisalento.pasproject.authservice.dto.NotificationMessageDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NotificationMessageHandler {
    private final MessageProducer messageProducer;

    @Value("${rabbitmq.exchange.notification.name}")
    private String notificationExchange;

    @Value("${rabbitmq.routing.notification.key}")
    private String notificationRoutingKey;

    @Autowired
    public NotificationMessageHandler(MessageProducer messageProducer) {
        this.messageProducer = messageProducer;
    }

    //TODO: Creare il dto apposito per inviare un NotificationDTO
    public void sendNotificationMessage(NotificationMessageDTO message) {
        messageProducer.sendMessage(message, notificationRoutingKey, notificationExchange);
    }
}
