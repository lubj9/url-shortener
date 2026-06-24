package com.lubj9.urlshortener.service;

import com.lubj9.urlshortener.dto.CreateUrlRequest;
import com.lubj9.urlshortener.dto.StatsResponse;
import com.lubj9.urlshortener.dto.UrlResponse;
import com.lubj9.urlshortener.exception.ExpiredUrlException;
import com.lubj9.urlshortener.exception.ResourceNotFoundException;
import com.lubj9.urlshortener.model.Url;
import com.lubj9.urlshortener.repository.ClickRepository;
import com.lubj9.urlshortener.repository.UrlRepository;
import com.lubj9.urlshortener.util.Base62;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UrlService {

    private final UrlRepository urlRepository;
    private final ClickRepository clickRepository;
    private final UrlValidator validator;
    private final String baseUrl;

    public UrlService(UrlRepository urlRepository,
                      ClickRepository clickRepository,
                      UrlValidator validator,
                      @Value("${app.base-url}") String baseUrl) {
        this.urlRepository = urlRepository;
        this.clickRepository = clickRepository;
        this.validator = validator;
        this.baseUrl = baseUrl;
    }

    /**
     * Cria (ou reutiliza) URL curta para uma URL longa.
     *
     * Se já existe um registro idêntico SEM expiração, devolve o mesmo código —
     * decisão de design pra evitar inchar o banco com a mesma URL repetida.
     * URLs com expiração sempre geram novo registro.
     */
    @Transactional
    public UrlResponse criar(CreateUrlRequest request) {
        validator.validate(request.url());

        Url url;
        if (request.expiresAt() == null) {
            url = urlRepository.findByOriginalUrl(request.url())
                    .filter(u -> u.getExpiresAt() == null)
                    .orElseGet(() -> urlRepository.save(new Url(request.url(), null)));
        } else {
            url = urlRepository.save(new Url(request.url(), request.expiresAt()));
        }

        return toResponse(url);
    }

    /**
     * Resolve um código curto para a URL original.
     *
     * @Cacheable: o resultado é guardado em cache pelos próximos 5 minutos.
     * Isso é crítico porque redirects são a operação mais frequente do sistema —
     * sem cache, cada redirect seria uma query no banco.
     */
    @Cacheable(value = "urls", key = "#code")
    @Transactional(readOnly = true)
    public Url resolverPorCodigo(String code) {
        long id;
        try {
            id = Base62.decode(code);
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("código inválido: " + code);
        }

        Url url = urlRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("URL não encontrada: " + code));

        if (url.isExpired()) {
            throw new ExpiredUrlException("URL expirou em " + url.getExpiresAt());
        }
        return url;
    }

    @Transactional(readOnly = true)
    public StatsResponse buscarEstatisticas(String code) {
        long id;
        try {
            id = Base62.decode(code);
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("código inválido: " + code);
        }

        Url url = urlRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("URL não encontrada: " + code));

        long total = clickRepository.countByUrlId(id);
        var ultimoClique = clickRepository.findLastClickAt(id).orElse(null);

        return new StatsResponse(
                code,
                url.getOriginalUrl(),
                total,
                url.getCreatedAt(),
                ultimoClique,
                url.getExpiresAt(),
                url.isExpired()
        );
    }

    @CacheEvict(value = "urls", key = "#code")
    @Transactional
    public void deletar(String code) {
        long id;
        try {
            id = Base62.decode(code);
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("código inválido: " + code);
        }
        if (!urlRepository.existsById(id)) {
            throw new ResourceNotFoundException("URL não encontrada: " + code);
        }
        urlRepository.deleteById(id);
    }

    // ===== Helpers =====

    private UrlResponse toResponse(Url url) {
        String code = Base62.encode(url.getId());
        return new UrlResponse(
                code,
                baseUrl + "/" + code,
                url.getOriginalUrl(),
                url.getCreatedAt(),
                url.getExpiresAt()
        );
    }
}
