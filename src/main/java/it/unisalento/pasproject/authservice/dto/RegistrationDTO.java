package it.unisalento.pasproject.authservice.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RegistrationDTO {
    private String name;

    private String surname;

    private String email;

    private String role;

    private String password;
}
