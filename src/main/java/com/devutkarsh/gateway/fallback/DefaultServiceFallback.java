package com.devutkarsh.gateway.fallback;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.route.FallbackProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;

public class DefaultServiceFallback implements FallbackProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultServiceFallback.class);

    /**
     * 
     * This method is to enable fallback for all routes
     */
    @Override
    public String getRoute() {
        return "*";
    }

    private ClientHttpResponse response(final HttpStatus status, String message) {
        return new ClientHttpResponse() {
            @Override
            public HttpStatus getStatusCode() {
                return status;
            }

            @Override
            public int getRawStatusCode() {
                return status.value();
            }

            @Override
            public String getStatusText() {
                return status.getReasonPhrase();
            }

            @Override
            public void close() {
            }

            @Override
            public InputStream getBody() {
                byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
                return new ByteArrayInputStream(bytes);
            }

            @Override
            public HttpHeaders getHeaders() {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                return headers;
            }
        };
    }

    @Override
    public ClientHttpResponse fallbackResponse(String route, Throwable cause) {
        LOGGER.error("DefaultServiceFallback.fallbackResponse() : route: {}, cause  {} ",route, cause.getMessage());
        return response(HttpStatus.GATEWAY_TIMEOUT, "Gateway Timeout. Please try again later.");
    }

}
