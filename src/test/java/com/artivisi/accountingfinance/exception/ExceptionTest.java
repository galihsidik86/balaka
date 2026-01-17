package com.artivisi.accountingfinance.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionTest {

    @Test
    void reportGenerationException_shouldPreserveMessageAndCause() {
        String message = "Failed to generate PDF report";
        Throwable cause = new RuntimeException("IO error");

        ReportGenerationException exception = new ReportGenerationException(message, cause);

        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    void dataExportException_shouldPreserveMessageAndCause() {
        String message = "Failed to export data";
        Throwable cause = new IllegalStateException("Database error");

        DataExportException exception = new DataExportException(message, cause);

        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception.getCause()).isEqualTo(cause);
    }

    @Test
    void reportGenerationException_shouldBeRuntimeException() {
        ReportGenerationException exception = new ReportGenerationException("test", new RuntimeException());
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    void dataExportException_shouldBeRuntimeException() {
        DataExportException exception = new DataExportException("test", new RuntimeException());
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }
}
