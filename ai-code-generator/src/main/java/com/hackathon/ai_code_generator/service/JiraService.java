package com.hackathon.ai_code_generator.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.ai_code_generator.config.JiraCloudInitializer;
import com.hackathon.ai_code_generator.dto.JiraStory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.*;

@Service
public class JiraService {

    @Value("${jira.auth.client-id}")
    private String clientId;

    @Value("${jira.auth.client-secret}")
    private String clientSecret;

    @Value("${jira.auth.token-url}")
    private String tokenUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // Optional token caching
    private String cachedToken;
    private Instant tokenExpiry;

    public JiraService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public JiraStory fetchStory(String storyKey) {
        try {
            String cloudId = JiraCloudInitializer.CLOUD_ID;

            if (cloudId == null || cloudId.isBlank()) {
                throw new IllegalStateException("Cloud ID not initialized. JiraCloudInitializer failed or did not run.");
            }

            String accessToken = getAccessToken();

            String issueUrl = String.format(
                    "https://api.atlassian.com/ex/jira/%s/rest/api/3/issue/%s?fields=summary,description",
                    cloudId,
                    storyKey
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            ResponseEntity<String> response =
                    restTemplate.exchange(issueUrl, HttpMethod.GET, new HttpEntity<>(headers), String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Failed to fetch Jira issue: " + response.getStatusCode());
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode fields = root.path("fields");

            String summary = fields.path("summary").asText("");
            JsonNode descriptionNode = fields.path("description");

            String fullDescription = extractTextFromADF(descriptionNode);
            String acceptanceCriteria = extractAcceptanceCriteria(fullDescription);

            JiraStory story = new JiraStory();
            story.setKey(storyKey);
            story.setSummary(summary);
            story.setDescription(fullDescription);
            story.setAcceptanceCriteria(acceptanceCriteria);

            return story;

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch Jira story: " + storyKey, e);
        }
    }

    // -----------------------------
    // TOKEN HANDLING (with caching)
    // -----------------------------
    private String getAccessToken() {
        try {
            if (cachedToken != null && tokenExpiry != null && Instant.now().isBefore(tokenExpiry)) {
                return cachedToken;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            body.put("grant_type", "client_credentials");
            body.put("client_id", clientId);
            body.put("client_secret", clientSecret);
            body.put("audience", "api.atlassian.com");

            ResponseEntity<String> response =
                    restTemplate.postForEntity(tokenUrl, new HttpEntity<>(body, headers), String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Failed to get access token: " + response.getStatusCode());
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            cachedToken = root.path("access_token").asText();
            int expiresIn = root.path("expires_in").asInt(3600);
            tokenExpiry = Instant.now().plusSeconds(expiresIn - 60);

            return cachedToken;

        } catch (Exception e) {
            throw new RuntimeException("Failed to obtain Jira OAuth token", e);
        }
    }

    // -----------------------------
    // ADF PARSING
    // -----------------------------
    private String extractTextFromADF(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        traverseAdf(node, sb);
        return sb.toString().replaceAll("\\n{3,}", "\n\n").trim();
    }

    private void traverseAdf(JsonNode node, StringBuilder sb) {
        if (node.isObject()) {

            if (node.has("text")) {
                sb.append(node.get("text").asText());
            }

            if (node.has("content")) {
                for (JsonNode child : node.get("content")) {
                    traverseAdf(child, sb);
                }
            }

            if (node.has("type")) {
                String type = node.get("type").asText();

                switch (type) {
                    case "paragraph":
                    case "heading", "bulletList", "orderedList", "listItem":
                        sb.append("\n");
                        break;
                }
            }

        } else if (node.isArray()) {
            for (JsonNode child : node) {
                traverseAdf(child, sb);
            }
        }
    }

    // -----------------------------
    // ACCEPTANCE CRITERIA EXTRACTION
    // -----------------------------
    private String extractAcceptanceCriteria(String fullDescription) {
        if (fullDescription == null || fullDescription.isBlank()) {
            return "";
        }

        String[] lines = fullDescription.split("\\r?\\n");
        boolean inAc = false;
        StringBuilder ac = new StringBuilder();

        for (String line : lines) {
            String trimmed = line.trim();

            if (!inAc && trimmed.matches("(?i).*acceptance criteria.*")) {
                inAc = true;
                continue;
            }

            if (inAc) {
                if (trimmed.matches("^[A-Z][A-Za-z ]+:$")) {
                    break;
                }
                ac.append(line).append("\n");
            }
        }

        return ac.toString().trim();
    }
}