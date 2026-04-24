package com.smartstudy.note.controller;

import com.smartstudy.common.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.Optional;

@RestControllerAdvice(basePackageClasses = NoteController.class)
public class NoteExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(MethodArgumentNotValidException exception) {
        return ResponseEntity.badRequest().body(ApiResponse.fail(resolveMessage(exception)));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(HandlerMethodValidationException exception) {
        return ResponseEntity.badRequest().body(ApiResponse.fail("Invalid request payload"));
    }

    private String resolveMessage(MethodArgumentNotValidException exception) {
        return Optional.ofNullable(exception.getBindingResult().getFieldError())
                .map(FieldError::getDefaultMessage)
                .filter(message -> message != null && !message.isBlank())
                .orElse("Invalid request payload");
    }
}
