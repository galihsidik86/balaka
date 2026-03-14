package com.artivisi.accountingfinance.controller;

import com.artivisi.accountingfinance.controller.RestExceptionHandler.ErrorResponse;
import com.artivisi.accountingfinance.controller.RestExceptionHandler.ValidationErrorResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.apache.catalina.connector.ClientAbortException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for RestExceptionHandler.
 */
@DisplayName("RestExceptionHandler Tests")
class RestExceptionHandlerTest {

    private RestExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new RestExceptionHandler();
    }

    @Nested
    @DisplayName("EntityNotFoundException")
    class EntityNotFoundTests {

        @Test
        @DisplayName("Should return 404 for EntityNotFoundException")
        void shouldReturn404() {
            EntityNotFoundException ex = new EntityNotFoundException("User not found with id: 123");
            ResponseEntity<ErrorResponse> response = handler.handleEntityNotFound(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(404);
            assertThat(response.getBody().error()).isEqualTo("Not Found");
            assertThat(response.getBody().message()).isEqualTo("The requested resource was not found.");
            assertThat(response.getBody().timestamp()).isNotNull();
        }
    }

    @Nested
    @DisplayName("IllegalArgumentException")
    class IllegalArgumentTests {

        @Test
        @DisplayName("Should return 400 for IllegalArgumentException")
        void shouldReturn400() {
            IllegalArgumentException ex = new IllegalArgumentException("Invalid input value");
            ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(400);
            assertThat(response.getBody().error()).isEqualTo("Bad Request");
            assertThat(response.getBody().message()).isEqualTo("The request was invalid.");
        }
    }

    @Nested
    @DisplayName("IllegalStateException")
    class IllegalStateTests {

        @Test
        @DisplayName("Should return 409 for IllegalStateException")
        void shouldReturn409() {
            IllegalStateException ex = new IllegalStateException("Cannot modify posted transaction");
            ResponseEntity<ErrorResponse> response = handler.handleIllegalState(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(409);
            assertThat(response.getBody().error()).isEqualTo("Conflict");
        }
    }

    @Nested
    @DisplayName("MethodArgumentNotValidException")
    class ValidationTests {

        @Test
        @DisplayName("Should return 400 with field errors for validation failure")
        void shouldReturn400WithFieldErrors() {
            BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");
            bindingResult.addError(new FieldError("target", "name", "must not be blank"));
            bindingResult.addError(new FieldError("target", "amount", "must be positive"));

            MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);
            ResponseEntity<ValidationErrorResponse> response = handler.handleValidationErrors(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(400);
            assertThat(response.getBody().error()).isEqualTo("Validation Failed");
            assertThat(response.getBody().fieldErrors()).containsKey("name");
            assertThat(response.getBody().fieldErrors()).containsKey("amount");
            assertThat(response.getBody().fieldErrors().get("name")).isEqualTo("must not be blank");
        }
    }

    @Nested
    @DisplayName("ConstraintViolationException")
    class ConstraintViolationTests {

        @Test
        @DisplayName("Should return 400 for ConstraintViolationException")
        void shouldReturn400() {
            ConstraintViolationException ex = new ConstraintViolationException("Validation error", Set.of());
            ResponseEntity<ErrorResponse> response = handler.handleConstraintViolation(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().error()).isEqualTo("Validation Failed");
        }
    }

    @Nested
    @DisplayName("AccessDeniedException")
    class AccessDeniedTests {

        @Test
        @DisplayName("Should return 403 for AccessDeniedException")
        void shouldReturn403ForAccessDenied() {
            AccessDeniedException ex = new AccessDeniedException("Access is denied");
            ResponseEntity<ErrorResponse> response = handler.handleAccessDenied(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(403);
            assertThat(response.getBody().error()).isEqualTo("Forbidden");
        }

        @Test
        @DisplayName("Should return 403 for AuthorizationDeniedException")
        void shouldReturn403ForAuthorizationDenied() {
            AuthorizationDeniedException ex = new AuthorizationDeniedException("Not authorized");
            ResponseEntity<ErrorResponse> response = handler.handleAccessDenied(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }
    }

    @Nested
    @DisplayName("NoResourceFoundException")
    class NoResourceFoundTests {

        @Test
        @DisplayName("Should return 404 for NoResourceFoundException")
        void shouldReturn404() throws NoResourceFoundException {
            NoResourceFoundException ex = new NoResourceFoundException(
                    org.springframework.http.HttpMethod.GET, "/api/nonexistent", "Resource not found");
            ResponseEntity<ErrorResponse> response = handler.handleNoResourceFound(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().message()).isEqualTo("Resource tidak ditemukan.");
        }
    }

    @Nested
    @DisplayName("HttpRequestMethodNotSupportedException")
    class MethodNotSupportedTests {

        @Test
        @DisplayName("Should return 405 for unsupported HTTP method")
        void shouldReturn405() {
            HttpRequestMethodNotSupportedException ex =
                    new HttpRequestMethodNotSupportedException("PATCH", Set.of("GET", "POST"));
            ResponseEntity<ErrorResponse> response = handler.handleMethodNotSupported(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().message()).contains("PATCH");
        }
    }

    @Nested
    @DisplayName("ClientAbortException")
    class ClientAbortTests {

        @Test
        @DisplayName("Should return 200 for client abort")
        void shouldReturn200ForClientAbort() {
            ClientAbortException ex = new ClientAbortException(new java.io.IOException("Connection reset"));
            ResponseEntity<Void> response = handler.handleClientAbort(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Nested
    @DisplayName("OptimisticLockingFailureException")
    class OptimisticLockingTests {

        @Test
        @DisplayName("Should return 409 for optimistic locking failure")
        void shouldReturn409() {
            OptimisticLockingFailureException ex =
                    new OptimisticLockingFailureException("Row was updated by another user");
            ResponseEntity<ErrorResponse> response = handler.handleOptimisticLockingFailure(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().error()).isEqualTo("Concurrent Update Detected");
        }
    }

    @Nested
    @DisplayName("MissingServletRequestParameterException")
    class MissingParameterTests {

        @Test
        @DisplayName("Should return 400 for missing request parameter")
        void shouldReturn400() {
            MissingServletRequestParameterException ex =
                    new MissingServletRequestParameterException("startDate", "String");
            ResponseEntity<ErrorResponse> response = handler.handleMissingServletRequestParameter(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().message()).contains("startDate");
        }
    }

    @Nested
    @DisplayName("MethodArgumentTypeMismatchException")
    class TypeMismatchTests {

        @Test
        @DisplayName("Should return 400 for type mismatch")
        void shouldReturn400() {
            MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                    "abc", Integer.class, "id", null, new NumberFormatException("For input string: abc"));
            ResponseEntity<ErrorResponse> response = handler.handleMethodArgumentTypeMismatch(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().message()).contains("id");
        }
    }

    @Nested
    @DisplayName("HttpMessageNotReadableException")
    class MessageNotReadableTests {

        @Test
        @DisplayName("Should return 400 for malformed request body")
        void shouldReturn400() {
            RuntimeException cause = new RuntimeException("Unexpected token");
            HttpMessageNotReadableException ex = new HttpMessageNotReadableException(
                    "JSON parse error", cause, (org.springframework.http.HttpInputMessage) null);
            ResponseEntity<ErrorResponse> response = handler.handleHttpMessageNotReadable(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(400);
        }
    }

    @Nested
    @DisplayName("DataIntegrityViolationException")
    class DataIntegrityTests {

        @Test
        @DisplayName("Should return 409 for data integrity violation")
        void shouldReturn409() {
            DataIntegrityViolationException ex = new DataIntegrityViolationException(
                    "Unique constraint violated", new RuntimeException("duplicate key value"));
            ResponseEntity<ErrorResponse> response = handler.handleDataIntegrityViolation(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().message()).isEqualTo("Operasi gagal karena constraint data.");
        }
    }

    @Nested
    @DisplayName("Generic Exception")
    class GenericExceptionTests {

        @Test
        @DisplayName("Should return 500 for unexpected exception")
        void shouldReturn500() {
            Exception ex = new RuntimeException("Something went terribly wrong");
            ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(500);
            assertThat(response.getBody().error()).isEqualTo("Internal Server Error");
        }

        @Test
        @DisplayName("Should return 200 for wrapped ClientAbortException")
        void shouldReturn200ForWrappedClientAbort() {
            ClientAbortException clientAbort = new ClientAbortException(new java.io.IOException("reset"));
            Exception ex = new RuntimeException("Wrapper", clientAbort);
            ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("Should return 200 for Broken pipe exception")
        void shouldReturn200ForBrokenPipe() {
            Exception ex = new RuntimeException("Broken pipe");
            ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("Should return 200 for Connection reset exception")
        void shouldReturn200ForConnectionReset() {
            Exception ex = new RuntimeException("Connection reset by peer");
            ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("Should return 500 for unrelated nested exception")
        void shouldReturn500ForUnrelatedNestedException() {
            Exception ex = new RuntimeException("Outer",
                    new IllegalStateException("Inner"));
            ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
