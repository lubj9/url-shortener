package com.lubj9.urlshortener.controller;

import com.lubj9.urlshortener.model.Url;
import com.lubj9.urlshortener.service.ClickTrackerService;
import com.lubj9.urlshortener.service.UrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * Controller de redirect.
 *
 * Mapeamento na raiz (/{code}) é intencional: faz a URL curta o mais limpa
 * possível, no estilo bit.ly/abc123 ou lub.ji/abc123.
 */
@RestController
@Tag(name = "Redirect", description = "Resolução de códigos curtos para URLs originais")
public class RedirectController {

    private final UrlService urlService;
    private final ClickTrackerService tracker;

    public RedirectController(UrlService urlService, ClickTrackerService tracker) {
        this.urlService = urlService;
        this.tracker = tracker;
    }

    @GetMapping("/{code:[a-zA-Z0-9]{1,10}}")
    @Operation(summary = "Redireciona para a URL original e registra o clique assincronamente")
    public ResponseEntity<Void> redirect(@PathVariable String code, HttpServletRequest request) {
        Url url = urlService.resolverPorCodigo(code);

        // Tracking assíncrono: NÃO bloqueia a resposta.
        tracker.registrar(url.getId(),
                request.getHeader("User-Agent"),
                extrairIp(request));

        // 302 Found redireciona; cliente segue automaticamente.
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, url.getOriginalUrl())
                .build();
    }

    /**
     * Identifica o IP real considerando proxies reversos (Nginx, Cloudflare, Render).
     * Padrão de mercado: respeitar X-Forwarded-For se presente.
     */
    private String extrairIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            // X-Forwarded-For pode conter múltiplos IPs: o primeiro é o original
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
