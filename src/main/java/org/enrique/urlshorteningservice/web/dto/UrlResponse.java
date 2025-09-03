package org.enrique.urlshorteningservice.web.dto;

import lombok.Builder;
import lombok.Value;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.time.Instant;

@Value
@Builder
@JsonInclude(Include.NON_NULL)
public class UrlResponse {
    String id;
    String url;
    String shortCode;
    Instant createdAt;
    Instant updatedAt;
    Long accessCount;
}
