package com.lubj9.urlshortener.service;

import com.lubj9.urlshortener.model.Click;
import com.lubj9.urlshortener.repository.ClickRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Persiste cliques de forma assíncrona.
 *
 * Decisão: usar @Async garante que o tracking não bloqueie o redirect.
 * O usuário é redirecionado imediatamente; a gravação no banco acontece
 * em uma thread separada (virtual thread, configurada em AsyncConfig).
 *
 * Esse padrão é crítico em redirects: latência adicional de I/O no caminho
 * crítico degrada experiência e pode acumular thread pools sob carga alta.
 */
@Service
public class ClickTrackerService {

    private static final Logger log = LoggerFactory.getLogger(ClickTrackerService.class);

    private final ClickRepository repository;

    public ClickTrackerService(ClickRepository repository) {
        this.repository = repository;
    }

    @Async("taskExecutor")
    public void registrar(Long urlId, String userAgent, String ipAddress) {
        try {
            repository.save(new Click(urlId, userAgent, ipAddress));
        } catch (Exception e) {
            // Falha em tracking não pode quebrar o redirect.
            // Logar e seguir é o comportamento correto.
            log.warn("Falha ao registrar clique para urlId={}: {}", urlId, e.getMessage());
        }
    }
}
