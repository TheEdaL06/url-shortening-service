package org.enrique.urlshorteningservice.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
public class UpdateShortUrlRequest {
    @NotBlank(message = "url is required")
    @Pattern(regexp = "https?://.+", message = "url must start with http:// or https://")
    @URL(message = "url must be a valid URL")
    private String url;
}
