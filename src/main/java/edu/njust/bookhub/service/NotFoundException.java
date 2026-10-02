package edu.njust.bookhub.service;

/** Thrown when a requested book, author or provider does not exist. */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String what, int id) {
        super(what + " #" + id + " was not found.");
    }
}
