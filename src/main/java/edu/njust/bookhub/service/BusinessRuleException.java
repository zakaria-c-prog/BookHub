package edu.njust.bookhub.service;

/**
 * A request was well-formed but breaks a catalogue rule (duplicate ISBN,
 * selling more copies than are in stock, ...). The message is shown to the user.
 */
public class BusinessRuleException extends RuntimeException {

    /** Name of the form field the problem belongs to, or null if it is general. */
    private final String field;

    public BusinessRuleException(String message) {
        this(null, message);
    }

    public BusinessRuleException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
