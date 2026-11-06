package com.github.ranveer.tinylink.services;

import com.github.ranveer.tinylink.dto.ShortenUrlRequest;
import com.github.ranveer.tinylink.dto.ShortenUrlResponse;
import com.github.ranveer.tinylink.dto.UrlAnalyticsResponse;
import com.github.ranveer.tinylink.dto.UrlStatsResponse;
import com.github.ranveer.tinylink.models.ClickEvent;
import com.github.ranveer.tinylink.models.UrlData;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j

public class UrlShortnerService {
    private final RedisTemplate<String, Object> redisTemplate;

    private final Map<String, UrlData> urlMappings = new ConcurrentHashMap<>();

    private final Map<String, List<ClickEvent>> clickAnalytics = new ConcurrentHashMap<>();

    @Value("http://localhost:8080")
    private String baseUrl;
    @Value("6")
    private int shortCodeLength;
    @Value("10")
    private int maxGenerationAttempts;
    @Value("30")
    private int cacheTtlMinutes;

    private static final String BASE_62_CHARS =
            "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";


    public ShortenUrlResponse shortenUrl(ShortenUrlRequest request, String clientIp) {
        String shortCode = request.getCustomAlias();
        if (shortCode == null || shortCode.trim().isEmpty()) {
            shortCode = generateUniqueShortCode();
        } else {
            shortCode = shortCode.trim();
            if (shortCodeExists(shortCode)) {
                throw new IllegalArgumentException(" Custom alias already exists: " + shortCode);
            }
        }

        UrlData urlData = UrlData.builder()
                .originalUrl(request.getOriginalUrl())
                .shortCode(shortCode)
                .expiresAt(request.getExpiresAt())
                .createdAt(LocalDateTime.now())
                .createdBy(clientIp)
                .clickCount(0)
                .isActive(true)
                .clickEvents(new ArrayList<>())
                .build();

        urlMappings.put(shortCode, urlData);
        clickAnalytics.put(shortCode, new ArrayList<>());

        cacheUrl(shortCode, request.getOriginalUrl());

        log.info("Created short URL: {} -> {}", shortCode, request.getOriginalUrl());

        return ShortenUrlResponse.builder()
                .shortUrl(buildShortUrl(shortCode))
                .shortCode(shortCode)
                .originalUrl(request.getOriginalUrl())
                .createdAt(urlData.getCreatedAt())
                .expiresAt(urlData.getExpiresAt())
                .build();
    }

    private String buildShortUrl(String shortCode) {
        String normalizedBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return normalizedBaseUrl + "/api/" + shortCode;
    }

    private void cacheUrl(String shortCode, String originalUrl) {
        try {
            redisTemplate.opsForValue().set("url:" + shortCode, originalUrl, cacheTtlMinutes, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn(" Failed to cache URL for {}: {}", shortCode, e.getMessage());
        }
    }

    private String generateUniqueShortCode() {
        for (int attempt = 0; attempt < maxGenerationAttempts; attempt++) {
            String code = generateRandomBase62();
            if (!shortCodeExists(code)) {
                return code;
            }
        }
        throw new RuntimeException("Failed to generate unique short code after" + maxGenerationAttempts + " attempts");
    }


    private boolean shortCodeExists(String code) {
        return urlMappings.containsKey(code);
    }

    private String generateRandomBase62() {
        StringBuilder sb = new StringBuilder(shortCodeLength);
        for (int i = 0; i < shortCodeLength; i++) {
            int index = ThreadLocalRandom.current().nextInt(BASE_62_CHARS.length());

            sb.append(BASE_62_CHARS.charAt(index));
        }
        return sb.toString();
    }

    public Optional<String> getOriginalUrl(String shortCode) {

        UrlData urlData = urlMappings.get(shortCode);

        if(urlData == null || !urlData.isActive()) {
            return Optional.empty();
        }

        if(isExpired(urlData)) {
            urlData.setActive(false);
            deleteCacheUrl(shortCode);
            return Optional.empty();
        }

        String cachedUrl = getCachedUrl(shortCode);
        if (cachedUrl != null) {
            return Optional.of(cachedUrl);
        }

        String originalUrl = urlData.getOriginalUrl();
            cacheUrl(shortCode, originalUrl);
            return Optional.of(originalUrl);
    }

    private boolean isExpired(UrlData urlData) {
        return urlData.getExpiresAt() != null && urlData.getExpiresAt().isBefore(LocalDateTime.now());
    }

    private String getCachedUrl(String shortCode) {
        try {
            return (String) redisTemplate.opsForValue().get("url:" + shortCode);

        } catch (Exception e) {
            log.warn("Failed to reach cache URL for {}: {}", shortCode, e.getMessage());
            return null;
        }
    }

    public void recordClick(String shortCode, String clientIp, String userAgent, String referrer) {
        UrlData urlData = urlMappings.get(shortCode);
        if (urlData != null && urlData.isActive()) {
            urlData.setClickCount(urlData.getClickCount() + 1);

            ClickEvent clickEvent = ClickEvent.builder()
                    .timeStamp(LocalDateTime.now())
                    .ipAddress(clientIp)
                    .userAgent(userAgent)
                    .referrer(referrer)
                    .build();

            clickAnalytics.get(shortCode).add(clickEvent);
            log.debug(" Recorder click for short code: {}", shortCode);
        }
    }

    public Optional<UrlStatsResponse> getUrlStats(String shortCode) {
        UrlData urlData = urlMappings.get(shortCode);

        if(urlData == null) {
            return Optional.empty();
        }
        return Optional.of(
                UrlStatsResponse.builder()
                        .shortCode(shortCode)
                        .originalUrl(urlData.getOriginalUrl())
                        .clickCount(urlData.getClickCount())
                        .createdAt(urlData.getCreatedAt())
                        .expiresAt(urlData.getExpiresAt())
                        .isActive(urlData.isActive())
                        .createdBy(urlData.getCreatedBy())
                        .build()
        );
    }

    public Optional<UrlAnalyticsResponse> getUrlAnalytics(String shortCode) {
        UrlData urlData = urlMappings.get(shortCode);

        if(urlData == null) {
            return Optional.empty();
        }

        List<ClickEvent> clicks = clickAnalytics.getOrDefault(shortCode, new ArrayList<>());

        Map<String, Integer> clicksByReferrer = clicks.stream()
                .filter(c->c.getReferrer() != null)
                .collect(Collectors.groupingBy(
                        ClickEvent::getReferrer, Collectors.summingInt(e->1)
                ));

        Map<String, Integer> clicksByHour = clicks.stream()
                .collect(Collectors.groupingBy(
                        c->c.getTimeStamp().getHour() + ":00",
                        Collectors.summingInt(e->1)
                ));

        Map<String, Integer> clicksByDay = clicks.stream()
                .collect(Collectors.groupingBy(
                        c->c.getTimeStamp().toLocalDate().toString(),
                        Collectors.summingInt(e->1)
                ));

        List<ClickEvent> recentClicks = clicks.stream()
                .sorted((a,b)->b.getTimeStamp().compareTo(a.getTimeStamp()))
                .limit(10)
                .toList();

        return Optional.of(
                UrlAnalyticsResponse.builder()
                        .shortCode(shortCode)
                        .originalUrl(urlData.getOriginalUrl())
                        .totalClicks(urlData.getClickCount())
                        .createdAt(urlData.getCreatedAt())
                        .expiresAt(urlData.getExpiresAt())
                        .recentClicks(recentClicks)
                        .clicksByReferrer(clicksByReferrer)
                        .clicksByHour(clicksByHour)
                        .clicksByDay(clicksByDay)
                        .build()
        );
    }

    public boolean deleteUrl(String shortCode) {
        UrlData urlData = urlMappings.get(shortCode);
        if(urlData != null) {
            urlData.setActive(false);
            deleteCacheUrl(shortCode);
            log.info("Deleted URL: {}", shortCode);
            return true;
        }
        return false;
    }

    private void deleteCacheUrl(String shortCode) {
        try{
            redisTemplate.delete("url:" +shortCode);
        } catch(Exception e) {
            log.warn("Failed to delete cache URL for {}: {}",shortCode,e.getMessage());
        }

    }
    public void cleanupExpiredUrls() {
        int cleanedCount = 0;

        LocalDateTime now = LocalDateTime.now();

        for(Map.Entry<String, UrlData> entry: urlMappings.entrySet()) {
            UrlData urlData = entry.getValue();
            if(urlData.getExpiresAt() != null && urlData.getExpiresAt().isBefore(now) && urlData.isActive()) {
                urlData.setActive(false);
                deleteCacheUrl(entry.getKey());
                cleanedCount++;
            }
        }
        if(cleanedCount > 0) {
            log.info("Cleaned up {} expired URLs", cleanedCount);
        }
    }
}
