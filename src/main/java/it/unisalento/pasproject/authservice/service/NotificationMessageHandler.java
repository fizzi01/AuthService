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

    public static NotificationMessageDTO buildNotificationMessage(String receiver, String message, String subject, String type, boolean email, boolean notification) {
        NotificationMessageDTO notificationMessage = new NotificationMessageDTO();
        notificationMessage.setReceiver(receiver);
        notificationMessage.setMessage(message);
        notificationMessage.setSubject(subject);
        notificationMessage.setType(type);
        notificationMessage.setEmail(email);
        notificationMessage.setNotification(notification);
        return notificationMessage;
    }

    public void sendNotificationMessage(NotificationMessageDTO message) {
        messageProducer.sendMessage(message, notificationRoutingKey, notificationExchange);
    }
}
