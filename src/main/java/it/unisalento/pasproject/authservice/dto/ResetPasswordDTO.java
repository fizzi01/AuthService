package it.unisalento.pasproject.authservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordDTO {
    private String newPassword;
    private String token;
}
