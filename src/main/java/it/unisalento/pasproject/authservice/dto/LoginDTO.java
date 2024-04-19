package it.unisalento.pasproject.authservice.dto;

//

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginDTO {
    private String email;
    private String password;
}
