package com.github.ranveer.tinylink.dto;

import com.github.ranveer.tinylink.validator.ValidUrl;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Future;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortenUrlRequest {

    @NotBlank(message = "Original URL is required")
    @ValidUrl
    private String originalUrl;

    @Pattern(
            regexp = "^([a-zA-Z0-9_-]{3,30})?$",
            message = "Custom alias must be 3-30 characters and contain only letters, numbers, underscores or hyphens"
    )
    private String customAlias;

    @Future(message = "Expiry date must be in the future")
    private LocalDateTime expiresAt;


}
