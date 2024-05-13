package it.unisalento.pasproject.authservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class RegistrationDTO {
    private String name;

    private String surname;

    private String email;

    private String role;

    private LocalDateTime registrationDate;

    private String password;
}
