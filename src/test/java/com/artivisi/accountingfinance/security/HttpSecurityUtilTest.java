package com.artivisi.accountingfinance.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("HttpSecurityUtil Tests")
class HttpSecurityUtilTest {

    @Test
    @DisplayName("Should generate attachment Content-Disposition header")
    void shouldGenerateAttachmentContentDisposition() {
        String result = HttpSecurityUtil.attachmentContentDisposition("report.pdf");
        assertThat(result).contains("attachment");
        assertThat(result).contains("report.pdf");
    }

    @Test
    @DisplayName("Should generate inline Content-Disposition header")
    void shouldGenerateInlineContentDisposition() {
        String result = HttpSecurityUtil.inlineContentDisposition("image.png");
        assertThat(result).contains("inline");
        assertThat(result).contains("image.png");
    }

    @Test
    @DisplayName("Should sanitize null filename to default")
    void shouldSanitizeNullFilename() {
        String result = HttpSecurityUtil.sanitizeFilename(null);
        assertThat(result).isEqualTo("download");
    }

    @Test
    @DisplayName("Should sanitize empty filename to default")
    void shouldSanitizeEmptyFilename() {
        String result = HttpSecurityUtil.sanitizeFilename("");
        assertThat(result).isEqualTo("download");
    }

    @Test
    @DisplayName("Should sanitize dots-only filename to default")
    void shouldSanitizeDotsOnlyFilename() {
        String result = HttpSecurityUtil.sanitizeFilename("...");
        assertThat(result).isEqualTo("download");
    }

    @Test
    @DisplayName("Should remove path separators from filename")
    void shouldRemovePathSeparators() {
        String result = HttpSecurityUtil.sanitizeFilename("path/to\\file.txt");
        assertThat(result).doesNotContain("/");
        assertThat(result).doesNotContain("\\");
        assertThat(result).contains("file.txt");
    }

    @Test
    @DisplayName("Should remove null bytes from filename")
    void shouldRemoveNullBytes() {
        String result = HttpSecurityUtil.sanitizeFilename("file\0name.txt");
        assertThat(result).doesNotContain("\0");
        assertThat(result).isEqualTo("filename.txt");
    }

    @Test
    @DisplayName("Should replace dangerous characters")
    void shouldReplaceDangerousCharacters() {
        String result = HttpSecurityUtil.sanitizeFilename("file<>:\"|?*.txt");
        assertThat(result).doesNotContain("<");
        assertThat(result).doesNotContain(">");
        assertThat(result).doesNotContain("\"");
        assertThat(result).endsWith(".txt");
    }

    @Test
    @DisplayName("Should truncate long filenames preserving extension")
    void shouldTruncateLongFilenames() {
        String longName = "a".repeat(250) + ".pdf";
        String result = HttpSecurityUtil.sanitizeFilename(longName);
        assertThat(result.length()).isLessThanOrEqualTo(200);
        assertThat(result).endsWith(".pdf");
    }

    @Test
    @DisplayName("Should truncate long filenames without extension")
    void shouldTruncateLongFilenamesWithoutExtension() {
        String longName = "a".repeat(250);
        String result = HttpSecurityUtil.sanitizeFilename(longName);
        assertThat(result.length()).isEqualTo(200);
    }

    @Test
    @DisplayName("Should handle normal filenames unchanged")
    void shouldHandleNormalFilenames() {
        String result = HttpSecurityUtil.sanitizeFilename("laporan-keuangan_2025.xlsx");
        assertThat(result).isEqualTo("laporan-keuangan_2025.xlsx");
    }

    @Test
    @DisplayName("Should handle Indonesian filenames with spaces")
    void shouldHandleIndonesianFilenames() {
        String result = HttpSecurityUtil.sanitizeFilename("Laporan Pajak Maret 2025.pdf");
        assertThat(result).isEqualTo("Laporan Pajak Maret 2025.pdf");
    }
}
