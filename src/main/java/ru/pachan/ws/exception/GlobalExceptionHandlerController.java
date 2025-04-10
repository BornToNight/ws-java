package ru.pachan.ws.exception;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestControllerAdvice
public class GlobalExceptionHandlerController {

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<String> handleRequestException(HttpServletResponse res, ResponseStatusException e) {
        return new ResponseEntity<>(e.getMessage(), e.getStatusCode());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<String[]> validEntityException(MethodArgumentNotValidException e) {
        return new ResponseEntity<>(
                e.getBindingResult().getFieldErrors().stream().map(it -> it.getField() + ": " + it.getDefaultMessage()).toArray(String[]::new),
                BAD_REQUEST
        );
    }

}
