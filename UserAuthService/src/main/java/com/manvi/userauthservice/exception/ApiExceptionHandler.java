package com.manvi.userauthservice.exception;


import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleInvalid(MethodArgumentNotValidException e){
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,"Some fields are not valid");
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()){
            errors.put(error.getField(), error.getDefaultMessage());
        }
        problemDetail.setProperty("erros", errors);
        return problemDetail;
    }


    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDuplicate(DataIntegrityViolationException e){
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "This email is already registered");
        //Three layers protect the same rule. For "one account per email" there are now three checks:
        // @Valid stops badly shaped input before the service, existsByEmail stops the usual duplicate in the service, and the unique column stops the rare simultaneous duplicate in the database.
        // Each catches what the one before it cannot. This is called defence in depth.

    }



}
