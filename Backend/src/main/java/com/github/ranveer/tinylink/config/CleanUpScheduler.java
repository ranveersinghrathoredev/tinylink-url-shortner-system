package com.github.ranveer.tinylink.config;

import com.github.ranveer.tinylink.services.UrlShortnerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CleanUpScheduler {
    private final UrlShortnerService urlShortnerService;

    @Scheduled(fixedRateString = "#{${tinylink.cleanup.interval-minutes} * 60 * 1000}")
    public void cleanupExpiredUrls() {

        try {
            log.debug("Running scheduled cleanup of expired URLs");
            urlShortnerService.cleanupExpiredUrls();

        } catch(Exception e) {
            log.error("Error during scheduled cleanup", e);
        }

    }


}
