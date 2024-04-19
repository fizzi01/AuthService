package it.unisalento.pasproject.authservice.dto;


// Risposta che contiene il token (JsonWebToken) di autenticazione
public class AuthenticationResponseDTO {
    private String jwt;

    public AuthenticationResponseDTO(String jwt) {
        this.jwt = jwt;
    }

    public String getJwt() {
        return jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }


}
