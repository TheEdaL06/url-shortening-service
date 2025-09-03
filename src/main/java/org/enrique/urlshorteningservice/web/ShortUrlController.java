package org.enrique.urlshorteningservice.web;

import jakarta.validation.Valid;
import org.enrique.urlshorteningservice.domain.UrlMapping;
import org.enrique.urlshorteningservice.service.ShortUrlService;
import org.enrique.urlshorteningservice.web.dto.CreateShortUrlRequest;
import org.enrique.urlshorteningservice.web.dto.UpdateShortUrlRequest;
import org.enrique.urlshorteningservice.web.dto.UrlResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shorten")
public class ShortUrlController {

    private final ShortUrlService service;

    public ShortUrlController(ShortUrlService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<UrlResponse> create(@Valid @RequestBody CreateShortUrlRequest request) {
        UrlMapping created = service.create(request.getUrl());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created, false));
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<UrlResponse> get(@PathVariable String shortCode) {
        UrlMapping mapping = service.getAndIncrement(shortCode);
        return ResponseEntity.ok(toResponse(mapping, false));
    }

    @PutMapping("/{shortCode}")
    public ResponseEntity<UrlResponse> update(@PathVariable String shortCode, @Valid @RequestBody UpdateShortUrlRequest request) {
        UrlMapping updated = service.update(shortCode, request.getUrl());
        return ResponseEntity.ok(toResponse(updated, false));
    }

    @DeleteMapping("/{shortCode}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String shortCode) {
        service.delete(shortCode);
    }

    @GetMapping("/{shortCode}/stats")
    public ResponseEntity<UrlResponse> stats(@PathVariable String shortCode) {
        UrlMapping mapping = service.get(shortCode);
        return ResponseEntity.ok(toResponse(mapping, true));
    }

    private UrlResponse toResponse(UrlMapping m, boolean includeAccessCount) {
        return UrlResponse.builder()
                .id(String.valueOf(m.getId()))
                .url(m.getUrl())
                .shortCode(m.getShortCode())
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt())
                .accessCount(includeAccessCount ? m.getAccessCount() : null)
                .build();
    }
}
