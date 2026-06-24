package com.lubj9.urlshortener.dto;

import java.time.OffsetDateTime;

public record UrlResponse(
    String code,
    String shortUrl,
    String originalUrl,
    OffsetDateTime createdAt,
    OffsetDateTime expiresAt
) {}
