package it.unisalento.pasproject.authservice.domain;


import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Getter
@Setter
@Document("credentialsRestore")
public class CredentialsRestore {

        @Id
        private String id;
        private String email;
        private String token;
        private LocalDateTime requestDate;
        private LocalDateTime expirationDate;
        private boolean used;
}
