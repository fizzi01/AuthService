package it.unisalento.pasproject.authservice.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@Document("user")
public class User {

    // Indica il campo che da l'identificativo del documento su MongoDB
    @Id
    private String id;
    private String name;
    private String surname;
    private String email;

    private String password;

    private String role;

    private LocalDateTime registrationDate;


    public User() {
    }

    public User(String id, String name, String surname, String email) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.email = email;
    }

}
