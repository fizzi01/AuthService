package it.unisalento.pasproject.authservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RecoveryResponseDTO {

    private String email;
    private String msg;
    private LocalDateTime expirationDate;
}
