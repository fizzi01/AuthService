package it.unisalento.pasproject.authservice.domain;

// Classe di Model vera e propria a differenza del DTO che serve solo per trasferire dati
// Oggetto che puo usare MongoDB per serializzarlo. Dobbiamo quindi dire quale è l'id in modo che MongoDB applichi le proprietà giuste


import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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

    public User() {
    }

    public User(String id, String name, String surname, String email) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.email = email;
    }

}
