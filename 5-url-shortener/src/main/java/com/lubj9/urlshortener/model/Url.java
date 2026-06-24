package com.lubj9.urlshortener.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * URL original cadastrada no sistema.
 *
 * O código curto NÃO é armazenado: ele é derivado do ID auto-incremental
 * via codificação Base62 sob demanda. Isso garante:
 * <ul>
 *   <li>Unicidade automática (sem risco de colisão)</li>
 *   <li>Sem necessidade de retry em caso de duplicata</li>
 *   <li>Menos espaço em disco</li>
 * </ul>
 */
@Entity
@Table(name = "urls", indexes = @Index(name = "idx_original_url", columnList = "original_url"))
public class Url {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_url", nullable = false, length = 2048)
    private String originalUrl;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    /** Data opcional após a qual a URL deixa de ser redirecionável. */
    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    public Url() {}

    public Url(String originalUrl, OffsetDateTime expiresAt) {
        this.originalUrl = originalUrl;
        this.expiresAt = expiresAt;
        this.createdAt = OffsetDateTime.now();
    }

    public boolean isExpired() {
        return expiresAt != null && OffsetDateTime.now().isAfter(expiresAt);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOriginalUrl() { return originalUrl; }
    public void setOriginalUrl(String originalUrl) { this.originalUrl = originalUrl; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public OffsetDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(OffsetDateTime expiresAt) { this.expiresAt = expiresAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Url url)) return false;
        return Objects.equals(id, url.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
