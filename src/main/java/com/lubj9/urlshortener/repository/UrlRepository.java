package com.lubj9.urlshortener.repository;

import com.lubj9.urlshortener.model.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UrlRepository extends JpaRepository<Url, Long> {
    Optional<Url> findByOriginalUrl(String originalUrl);
}
