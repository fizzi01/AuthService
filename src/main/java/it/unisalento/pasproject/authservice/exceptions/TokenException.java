package it.unisalento.pasproject.authservice.exceptions;

import org.springframework.http.HttpStatus;

public class TokenException extends CustomErrorException{
    public TokenException(String message) {
        super(message, HttpStatus.EXPECTATION_FAILED);
    }
}
