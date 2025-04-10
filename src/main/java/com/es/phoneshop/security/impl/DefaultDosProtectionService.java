package com.es.phoneshop.security.impl;

import com.es.phoneshop.dao.impl.ArrayListOrderDao;
import com.es.phoneshop.security.DosProtectionService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class DefaultDosProtectionService implements DosProtectionService {
    private static final int MAX_REQUESTS = 20;
    private static final long TIME_WINDOW = TimeUnit.MINUTES.toMillis(1);
    private static DefaultDosProtectionService instance;
    private final Map<String, RequestInfo> requestCounts;

    private DefaultDosProtectionService() {
        requestCounts = new ConcurrentHashMap<>();
    }

    public static DefaultDosProtectionService getInstance() {
        if (instance == null) {
            synchronized (ArrayListOrderDao.class) {
                if (instance == null) {
                    instance = new DefaultDosProtectionService();
                }
            }
        }
        return instance;
    }

    private static class RequestInfo {
        int count;
        long firstRequestTime;

        RequestInfo(long currentTime) {
            this.count = 1;
            this.firstRequestTime = currentTime;
        }
    }

    protected long getCurrentTimeMillis() {
        return System.currentTimeMillis();
    }

    @Override
    public boolean isAllowed(String ip) {
        long currentTime = getCurrentTimeMillis();

        return requestCounts.compute(ip, (key, existingInfo) -> {
            if (existingInfo == null) {
                return new RequestInfo(currentTime);
            }
            if (currentTime - existingInfo.firstRequestTime > TIME_WINDOW) {
                return new RequestInfo(currentTime);
            }
            existingInfo.count++;
            return existingInfo;
        }).count <= MAX_REQUESTS;
    }
}
