package it.unisalento.pasproject.authservice.dto;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserDTO {
    private String id;

    private String name;

    private String surname;

    private String email;
}
