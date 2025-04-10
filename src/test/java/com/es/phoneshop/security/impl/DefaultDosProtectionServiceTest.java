package com.es.phoneshop.security.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DefaultDosProtectionServiceTest {
    private static final String TEST_IP = "192.168.1.1";
    private static final String TEST_IP_2 = "10.0.0.1";
    private static final String INSTANCE = "instance";
    private static final int MAX_REQUESTS = 20;

    private DefaultDosProtectionService service;

    @Before
    public void setup() {
        service = DefaultDosProtectionService.getInstance();

    }

    @After
    public void tearDown() {
        resetSingleton();
    }

    private void resetSingleton() {
        try {
            Field instance = DefaultDosProtectionService.class.getDeclaredField(INSTANCE);
            instance.setAccessible(true);
            instance.set(null, null);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    @Test
    public void testFirstRequestAllowed() {
        assertTrue(service.isAllowed(TEST_IP));
    }

    @Test
    public void testMaxRequestsAllowed() {
        for (int i = 0; i < MAX_REQUESTS; i++) {
            assertTrue(service.isAllowed(TEST_IP));
        }
    }

    @Test
    public void testOverMaxRequestsDenied() {
        for (int i = 0; i < MAX_REQUESTS; i++) {
            service.isAllowed(TEST_IP);
        }

        assertFalse(service.isAllowed(TEST_IP));
    }

    @Test
    public void testDifferentIpsAreTrackedSeparately() {
        for (int i = 0; i < MAX_REQUESTS; i++) {
            assertTrue(service.isAllowed(TEST_IP));
        }

        assertFalse(service.isAllowed(TEST_IP));
        assertTrue(service.isAllowed(TEST_IP_2));
    }

    @Test
    public void testExactlyMaxRequestsAllowed() {
        for (int i = 0; i < MAX_REQUESTS - 1; i++) {
            service.isAllowed(TEST_IP);
        }

        assertTrue(service.isAllowed(TEST_IP));
    }
}
