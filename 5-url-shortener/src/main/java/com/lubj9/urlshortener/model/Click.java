package com.lubj9.urlshortener.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

/**
 * Registro individual de cada acesso a uma URL curta.
 *
 * É persistido de forma assíncrona (via @Async no service) para não bloquear
 * a resposta do redirect — o usuário não deve esperar pelo tracking.
 *
 * Em produção com volume alto, esse modelo de "linha por clique" seria substituído
 * por agregações pré-calculadas ou ingestão em um data warehouse via Kafka.
 */
@Entity
@Table(name = "clicks", indexes = {
        @Index(name = "idx_clicks_url_id", columnList = "url_id"),
        @Index(name = "idx_clicks_at", columnList = "clicked_at")
})
public class Click {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "url_id", nullable = false)
    private Long urlId;

    @Column(name = "clicked_at", nullable = false)
    private OffsetDateTime clickedAt;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "ip_address", length = 45)   // IPv6 max length
    private String ipAddress;

    public Click() {}

    public Click(Long urlId, String userAgent, String ipAddress) {
        this.urlId = urlId;
        this.userAgent = userAgent;
        this.ipAddress = ipAddress;
        this.clickedAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public Long getUrlId() { return urlId; }
    public OffsetDateTime getClickedAt() { return clickedAt; }
    public String getUserAgent() { return userAgent; }
    public String getIpAddress() { return ipAddress; }
}
