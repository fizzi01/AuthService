package it.unisalento.pasproject.authservice.exceptions;

import org.springframework.http.HttpStatus;

public class UserAlreadyExist extends CustomErrorException {
    public UserAlreadyExist(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
