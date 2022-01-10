package com.kae.article.controller;

import com.kae.article.exception.NotFoundException;
import com.kae.article.exception.UnauthorizedException;
import com.kae.article.exception.UserFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Controller;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
@Controller
public class ValidationExceptionHandler extends ResponseEntityExceptionHandler {
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request
    ) {
        Map<String, Object> body = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
        return responseEntity(status, headers, body);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request
    ) {
        return responseEntity(status, headers, "Incorrect data!");
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Object> notFoundException(HttpServletResponse response) {
        return responseEntity(HttpStatus.NOT_FOUND, null, "NotFound!");
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Object> unauthorizedException(HttpServletResponse response) {
        return responseEntity(HttpStatus.UNAUTHORIZED, null, "Unauthorized!");
    }

    @ExceptionHandler(UserFoundException.class)
    public ResponseEntity<Object> userFoundException(HttpServletResponse response) {
        return responseEntity(HttpStatus.BAD_REQUEST, null, "User Exists!");
    }

    private ResponseEntity<Object> responseEntity(HttpStatus status, HttpHeaders headers, Object error) {
        ResponseBody responseBody = new ResponseBody(
                LocalDateTime.now(),
                status.value(),
                error
        );
        return new ResponseEntity<>(responseBody, headers, status);
    }
}