package it.unisalento.pasproject.authservice.exceptions;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends CustomErrorException {
    public UserNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
