package com.example.demo.integration;

import com.example.demo.config.ChuckProperties;
import com.example.demo.model.ChuckResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerErrorException;

@Service
public class ChuckClient {
    private final RestTemplate restTemplate;
    private final ChuckProperties properties;

    public ChuckClient(RestTemplate restTemplate, ChuckProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    public ChuckResponse getJoke() {
        try {
            ResponseEntity<ChuckResponse> response = restTemplate.exchange(
                    properties.getUrl(),
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {
                    });
            return response.getBody();
        } catch(HttpServerErrorException error) {
            if(error.getStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR) {
                return new ChuckResponse("Случайная шутка");
            }
            throw error;
        }
    }
}
