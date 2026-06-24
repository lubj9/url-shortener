package com.lubj9.urlshortener.service;

import com.lubj9.urlshortener.exception.InvalidUrlException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UrlValidatorTest {

    private UrlValidator validator;

    @BeforeEach
    void setup() {
        validator = new UrlValidator();
    }

    @Test
    @DisplayName("URLs http e https válidas devem passar")
    void urlsValidas() {
        assertThatCode(() -> validator.validate("https://google.com")).doesNotThrowAnyException();
        assertThatCode(() -> validator.validate("http://example.org/path?q=1")).doesNotThrowAnyException();
        assertThatCode(() -> validator.validate("https://sub.domain.co.uk/a/b/c")).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("javascript: e outros esquemas perigosos devem ser bloqueados (proteção contra XSS)")
    void bloqueiaEsquemaJavascript() {
        assertThatThrownBy(() -> validator.validate("javascript:alert('xss')"))
                .isInstanceOf(InvalidUrlException.class)
                .hasMessageContaining("não suportado");
    }

    @Test
    @DisplayName("data: e file: também devem ser bloqueados")
    void bloqueiaOutrosEsquemas() {
        assertThatThrownBy(() -> validator.validate("data:text/html,<script>x</script>"))
                .isInstanceOf(InvalidUrlException.class);
        assertThatThrownBy(() -> validator.validate("file:///etc/passwd"))
                .isInstanceOf(InvalidUrlException.class);
    }

    @Test
    @DisplayName("URL sem protocolo deve ser rejeitada")
    void semProtocolo() {
        assertThatThrownBy(() -> validator.validate("google.com"))
                .isInstanceOf(InvalidUrlException.class);
    }

    @Test
    @DisplayName("string vazia ou nula deve ser rejeitada")
    void vazia() {
        assertThatThrownBy(() -> validator.validate(""))
                .isInstanceOf(InvalidUrlException.class);
        assertThatThrownBy(() -> validator.validate("   "))
                .isInstanceOf(InvalidUrlException.class);
        assertThatThrownBy(() -> validator.validate(null))
                .isInstanceOf(InvalidUrlException.class);
    }
}
