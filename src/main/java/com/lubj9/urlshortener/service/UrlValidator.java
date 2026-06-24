package com.lubj9.urlshortener.service;

import com.lubj9.urlshortener.exception.InvalidUrlException;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

/**
 * Validação semântica de URLs antes da persistência.
 * Bloqueia esquemas perigosos (javascript:, data:, file:) que poderiam ser
 * usados para XSS ou phishing.
 */
@Component
public class UrlValidator {

    private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https");

    public void validate(String url) {
        if (url == null || url.isBlank()) {
            throw new InvalidUrlException("url não pode ser vazia");
        }

        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new InvalidUrlException("url malformada: " + e.getMessage());
        }

        String scheme = uri.getScheme();
        if (scheme == null) {
            throw new InvalidUrlException("url deve incluir o protocolo (http:// ou https://)");
        }

        if (!ALLOWED_SCHEMES.contains(scheme.toLowerCase())) {
            throw new InvalidUrlException(
                    "protocolo '" + scheme + "' não suportado. Use http:// ou https://");
        }

        if (uri.getHost() == null || uri.getHost().isBlank()) {
            throw new InvalidUrlException("url deve incluir um host válido");
        }
    }
}
