package it.unisalento.pasproject.authservice.exceptions;

import org.springframework.http.HttpStatus;

public class IllegalRequestException extends CustomErrorException{
    public IllegalRequestException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
