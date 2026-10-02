package edu.njust.bookhub.web;

import edu.njust.bookhub.service.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** Turns lookup failures into a friendly error page instead of a stack trace. */
@ControllerAdvice
public class ErrorAdvice {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String notFound(NotFoundException e, Model model) {
        model.addAttribute("status", 404);
        model.addAttribute("message", e.getMessage());
        return "error";
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String badParameter(MethodArgumentTypeMismatchException e, Model model) {
        model.addAttribute("status", 400);
        model.addAttribute("message", "Invalid value \"" + e.getValue() + "\" for " + e.getName() + ".");
        return "error";
    }
}
