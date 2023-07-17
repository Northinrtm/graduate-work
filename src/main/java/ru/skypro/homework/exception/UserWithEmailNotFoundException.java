package ru.skypro.homework.exception;

public class UserWithEmailNotFoundException extends RuntimeException{
    public UserWithEmailNotFoundException(String email) {
        super(String.format("User with email: %s not found",email));
    }
}
