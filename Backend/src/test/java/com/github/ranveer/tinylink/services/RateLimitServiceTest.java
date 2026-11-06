package com.github.ranveer.tinylink.services;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimitServiceTest {

    @Test
    void shouldBlockRequestsAfterLimitIsReached() {

        RateLimitService service = new RateLimitService(null);

        ReflectionTestUtils.setField(service, "requestsPerMinute", 3);
        ReflectionTestUtils.setField(service, "requestsPerHour", 100);

        assertTrue(service.isAllowed("127.0.0.1"));
        assertTrue(service.isAllowed("127.0.0.1"));
        assertTrue(service.isAllowed("127.0.0.1"));

        assertFalse(service.isAllowed("127.0.0.1"));
    }
}