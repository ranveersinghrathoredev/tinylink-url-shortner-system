package com.github.ranveer.tinylink.services;

import org.junit.jupiter.api.Test;
import com.github.ranveer.tinylink.dto.ShortenUrlRequest;
import com.github.ranveer.tinylink.dto.ShortenUrlResponse;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class UrlShortnerServiceTest {

   @Test
    void shouldShortenUrlWithCustomAlias() {

        UrlShortnerService service = new UrlShortnerService(null);
       ReflectionTestUtils.setField(service, "baseUrl", "http://localhost:8080");

        ShortenUrlRequest request = ShortenUrlRequest.builder()
                .originalUrl("https://www.google.com")
                .customAlias("google")
                .build();

        ShortenUrlResponse response =
                service.shortenUrl(request, "127.0.0.1");

        assertEquals("google", response.getShortCode());
        assertEquals("https://www.google.com", response.getOriginalUrl());
        assertTrue(response.getShortUrl().endsWith("/google"));
    }

    @Test
    void shouldRejectDuplicateCustomAlias() {

        UrlShortnerService service = new UrlShortnerService(null);
        ReflectionTestUtils.setField(service, "baseUrl", "http://localhost:8080");

        ShortenUrlRequest firstRequest = ShortenUrlRequest.builder()
                .originalUrl("https://www.google.com")
                .customAlias("google")
                .build();

        ShortenUrlRequest secondRequest = ShortenUrlRequest.builder()
                .originalUrl("https://www.youtube.com")
                .customAlias("google")
                .build();

        service.shortenUrl(firstRequest, "127.0.0.1");

        assertThrows(IllegalArgumentException.class,
                () -> service.shortenUrl(secondRequest, "127.0.0.1"));
    }

    @Test
    void shouldGenerateUniqueShortCode() {

        UrlShortnerService service = new UrlShortnerService(null);

        ReflectionTestUtils.setField(service, "baseUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(service, "shortCodeLength", 6);

        ShortenUrlRequest request = ShortenUrlRequest.builder()
                .originalUrl("https://www.google.com")
                .build();

        ShortenUrlResponse response =
                service.shortenUrl(request, "127.0.0.1");

        assertNotNull(response.getShortCode());
        assertEquals(6, response.getShortCode().length());
    }
}