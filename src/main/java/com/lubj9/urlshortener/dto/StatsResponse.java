package com.lubj9.urlshortener.dto;

import java.time.OffsetDateTime;

public record StatsResponse(
    String code,
    String originalUrl,
    long totalClicks,
    OffsetDateTime createdAt,
    OffsetDateTime lastClickAt,
    OffsetDateTime expiresAt,
    boolean expired
) {}
