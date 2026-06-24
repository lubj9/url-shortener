package com.lubj9.urlshortener.repository;

import com.lubj9.urlshortener.model.Click;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public interface ClickRepository extends JpaRepository<Click, Long> {

    /**
     * Conta total de cliques de uma URL.
     * Spring Data JPA gera a query a partir do nome do método.
     */
    long countByUrlId(Long urlId);

    /**
     * Pega o timestamp do clique mais recente.
     * Usando @Query JPQL pra ilustrar consulta customizada.
     */
    @Query("SELECT MAX(c.clickedAt) FROM Click c WHERE c.urlId = :urlId")
    Optional<OffsetDateTime> findLastClickAt(@Param("urlId") Long urlId);
}
