package com.sougata.filezilabackend.controller;

import com.sougata.filezilabackend.config.GoogleProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class OAuthController {
    private final RestTemplate restTemplate;
    private final GoogleProperties properties;


    @PostMapping("/access-token")
    public ResponseEntity<Map<String, Object>> accessToken(@RequestBody Map<String, Object> body) {
        String code = body.get("code").toString();

        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("grant_type", "authorization_code");
        params.add("client_id", properties.getClientId());
        params.add("redirect_uri", properties.getRedirectUri());
        params.add("client_secret", properties.getClientSecret());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(params, headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                properties.getTokenUri(), HttpMethod.POST, request, ParameterizedTypeReference.forType(Map.class)
        );
        return ResponseEntity.ok(response.getBody());
    }
}
