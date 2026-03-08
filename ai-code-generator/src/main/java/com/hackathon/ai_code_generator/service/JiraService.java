package com.hackathon.ai_code_generator.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.ai_code_generator.dto.JiraStory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class JiraService {

    @Value("${jira.auth.client-id}")
    private String clientId;

    @Value("${jira.auth.client-secret}")
    private String clientSecret;

    @Value("${jira.auth.token-url}")
    private String tokenUrl;

    @Value("${jira.auth.cloud-id}")
    private String cloudId;

    @Value("${jira.url}")
    private String jiraBaseUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    public JiraStory fetchStory(String storyKey) {
        String accessToken = getAccessToken();

        // Jira Cloud API URL using cloud-id
        String url = "https://api.atlassian.com/ex/jira/" + cloudId + "/rest/api/3/issue/" + storyKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, JsonNode.class
        );

        return parseIssue(response.getBody());
    }

    private String getAccessToken() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // OAuth request body
            String body = "{"
                    + "\"grant_type\": \"client_credentials\","
                    + "\"client_id\": \"" + clientId + "\","
                    + "\"client_secret\": \"" + clientSecret + "\","
                    + "\"audience\": \"api.atlassian.com\""
                    + "}";

            HttpEntity<String> entity = new HttpEntity<>(body, headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(
                    tokenUrl, HttpMethod.POST, entity, JsonNode.class
            );

            return response.getBody().get("access_token").asText();

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch Jira OAuth token", e);
        }
    }

    private JiraStory parseIssue(JsonNode issue) {
        JiraStory story = new JiraStory();

        story.setKey(issue.get("key").asText());
        story.setSummary(issue.get("fields").get("summary").asText());

        // Extract description (ADF → plain text)
        JsonNode descriptionNode = issue.get("fields").get("description");
        String fullDescription = extractTextFromADF(descriptionNode);
        story.setDescription(fullDescription.trim());

        // Extract acceptance criteria from inside description
        String acceptance = "";
        String lower = fullDescription.toLowerCase();

        if (lower.contains("acceptance criteria")) {
            int start = lower.indexOf("acceptance criteria");
            acceptance = fullDescription.substring(start).trim();
        }

        story.setAcceptanceCriteria(acceptance);

        return story;

    }

    private String extractTextFromADF(JsonNode node) {
        if (node == null || node.isNull()) return "";

        StringBuilder sb = new StringBuilder();

        if (node.has("content")) {
            for (JsonNode child : node.get("content")) {
                sb.append(extractTextFromADF(child));
            }
        }

        if (node.has("text")) {
            sb.append(node.get("text").asText()).append("\n");
        }

        return sb.toString();
    }
}