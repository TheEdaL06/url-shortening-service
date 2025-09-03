package org.enrique.urlshorteningservice.service;

import org.enrique.urlshorteningservice.domain.UrlMapping;
import org.enrique.urlshorteningservice.exception.NotFoundException;
import org.enrique.urlshorteningservice.repository.UrlMappingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;

@Service
public class ShortUrlService {

    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int INITIAL_CODE_LENGTH = 6;
    private static final int MAX_ATTEMPTS = 50;

    private final UrlMappingRepository repository;
    private final SecureRandom random = new SecureRandom();

    public ShortUrlService(UrlMappingRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public UrlMapping create(String url) {
        String shortCode = generateUniqueShortCode();
        Instant now = Instant.now();
        UrlMapping mapping = UrlMapping.builder()
                .url(url)
                .shortCode(shortCode)
                .createdAt(now)
                .updatedAt(now)
                .accessCount(0)
                .build();
        return repository.save(mapping);
    }

    @Transactional
    public UrlMapping getAndIncrement(String shortCode) {
        UrlMapping mapping = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new NotFoundException("Short URL not found: " + shortCode));
        mapping.setAccessCount(mapping.getAccessCount() + 1);
        // Do not update updatedAt on access to match the spec's semantics
        return repository.save(mapping);
    }

    @Transactional(readOnly = true)
    public UrlMapping get(String shortCode) {
        return repository.findByShortCode(shortCode)
                .orElseThrow(() -> new NotFoundException("Short URL not found: " + shortCode));
    }

    @Transactional
    public UrlMapping update(String shortCode, String newUrl) {
        UrlMapping mapping = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new NotFoundException("Short URL not found: " + shortCode));
        mapping.setUrl(newUrl);
        mapping.setUpdatedAt(Instant.now());
        return repository.save(mapping);
    }

    @Transactional
    public void delete(String shortCode) {
        UrlMapping mapping = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new NotFoundException("Short URL not found: " + shortCode));
        repository.delete(mapping);
    }

    private String generateUniqueShortCode() {
        int length = INITIAL_CODE_LENGTH;
        for (int attempts = 0; attempts < MAX_ATTEMPTS; attempts++) {
            String code = randomCode(length);
            if (!repository.existsByShortCode(code)) {
                return code;
            }
            // Gradually increase length if collisions happen
            if ((attempts + 1) % 10 == 0) {
                length++;
            }
        }
        // In the unlikely event of many collisions, fall back to a timestamp-based code
        String fallback = Long.toString(Math.abs(random.nextLong()), 36) + Long.toString(System.nanoTime(), 36);
        return fallback.substring(0, Math.min(12, fallback.length()));
    }

    private String randomCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int idx = random.nextInt(ALPHABET.length());
            sb.append(ALPHABET.charAt(idx));
        }
        return sb.toString();
    }
}
