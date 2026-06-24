package com.lubj9.urlshortener.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record CreateUrlRequest(

    @NotBlank(message = "url é obrigatória")
    @Size(max = 2048, message = "url muito longa (máximo 2048 caracteres)")
    String url,

    @Future(message = "data de expiração deve estar no futuro")
    OffsetDateTime expiresAt
) {}
