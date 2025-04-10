package com.es.phoneshop.web;

import com.es.phoneshop.security.DosProtectionService;
import com.es.phoneshop.security.impl.DefaultDosProtectionService;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class DosFilter implements Filter {
    private static final int STATUS_CODE = 429;
    private DosProtectionService dosProtectionService;

    @Override
    public void init(FilterConfig filterConfig) {
        dosProtectionService = DefaultDosProtectionService.getInstance();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (dosProtectionService.isAllowed(request.getRemoteAddr())) {
            filterChain.doFilter(request, response);
        } else {
            ((HttpServletResponse) response).setStatus(STATUS_CODE);
        }
    }
}
