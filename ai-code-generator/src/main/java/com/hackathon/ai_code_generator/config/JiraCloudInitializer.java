package com.hackathon.ai_code_generator.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class JiraCloudInitializer implements CommandLineRunner {

    @Value("${jira.auth.client-id}")
    private String clientId;

    @Value("${jira.auth.client-secret}")
    private String clientSecret;

    @Value("${jira.auth.token-url}")
    private String tokenUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // This will be injected into JiraService
    public static String CLOUD_ID;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🔄 Fetching Jira Cloud ID...");

        String accessToken = getAccessToken();
        CLOUD_ID = fetchCloudId(accessToken);

        System.out.println("✅ Jira Cloud ID loaded ");
    }

    private String getAccessToken() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String body = String.format("""
        {
          "grant_type": "client_credentials",
          "client_id": "%s",
          "client_secret": "%s",
          "audience": "api.atlassian.com"
        }
        """, clientId, clientSecret);

        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response =
                restTemplate.postForEntity(tokenUrl, entity, String.class);

        JsonNode root = objectMapper.readTree(response.getBody());
        return root.get("access_token").asText();
    }

    private String fetchCloudId(String accessToken) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(MediaType.parseMediaTypes("application/json"));

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "https://api.atlassian.com/oauth/token/accessible-resources",
                HttpMethod.GET,
                entity,
                String.class
        );

        JsonNode root = objectMapper.readTree(response.getBody());
        return root.get(0).get("id").asText();
    }
}