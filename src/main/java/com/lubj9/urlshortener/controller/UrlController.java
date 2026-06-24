package com.lubj9.urlshortener.controller;

import com.lubj9.urlshortener.dto.CreateUrlRequest;
import com.lubj9.urlshortener.dto.StatsResponse;
import com.lubj9.urlshortener.dto.UrlResponse;
import com.lubj9.urlshortener.service.UrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/urls")
@Tag(name = "URLs", description = "Gestão dos códigos curtos e estatísticas")
public class UrlController {

    private final UrlService service;

    public UrlController(UrlService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Cria uma URL curta para a URL fornecida")
    public ResponseEntity<UrlResponse> criar(@Valid @RequestBody CreateUrlRequest request) {
        UrlResponse resp = service.criar(request);
        return ResponseEntity.created(URI.create(resp.shortUrl())).body(resp);
    }

    @GetMapping("/{code}/stats")
    @Operation(summary = "Retorna estatísticas de acesso da URL curta")
    public StatsResponse estatisticas(@PathVariable String code) {
        return service.buscarEstatisticas(code);
    }

    @DeleteMapping("/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove a URL curta")
    public void deletar(@PathVariable String code) {
        service.deletar(code);
    }
}
