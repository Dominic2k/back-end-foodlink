package org.datpham.foodlink.exception;

import org.datpham.foodlink.common.BaseResponse;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<BaseResponse<Object>> handleBusinessException(BusinessException ex) {
        return ResponseEntity.status(ex.getStatus()).body(
                new BaseResponse<>(null, ex.getMessage(), ex.getStatus().value())
        );
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<BaseResponse<Object>> handleNotFound(NoHandlerFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new BaseResponse<>(null, "API not found", 404)
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<Object>> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .orElse("Validation error");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new BaseResponse<>(null, errorMessage, 400)
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<BaseResponse<Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        HttpStatus status = HttpStatus.CONFLICT;
        return ResponseEntity.status(status).body(
                new BaseResponse<>(null, resolveDataIntegrityMessage(ex), status.value())
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Object>> handleException(Exception ex) {
        ex.printStackTrace(); // debug
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new BaseResponse<>(null, "Internal Error: " + ex.getMessage(), 500)
        );
    }

    private String resolveDataIntegrityMessage(DataIntegrityViolationException ex) {
        Throwable rootCause = NestedExceptionUtils.getMostSpecificCause(ex);
        String message = rootCause != null ? rootCause.getMessage() : ex.getMessage();
        if (message == null || message.isBlank()) {
            return "Request conflicts with existing data.";
        }

        String normalized = message.toLowerCase();
        if (normalized.contains("duplicate entry")) {
            return "Data already exists and must be unique.";
        }
        if (normalized.contains("foreign key constraint")) {
            return "Cannot delete this item because it is being used by other data.";
        }

        return "Request conflicts with existing data.";
    }
}
