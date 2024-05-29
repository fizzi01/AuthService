package it.unisalento.pasproject.authservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDetailsDTO {
    private String email;
    private String role;
    private Boolean enabled;

}
