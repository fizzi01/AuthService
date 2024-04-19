package it.unisalento.pasproject.authservice.dto;


import lombok.Getter;
import lombok.Setter;

// Risposta che contiene il token (JsonWebToken) di autenticazione
@Getter
@Setter
public class AuthenticationResponseDTO {
    private String jwt;

    public AuthenticationResponseDTO(String jwt) {
        this.jwt = jwt;
    }
}
