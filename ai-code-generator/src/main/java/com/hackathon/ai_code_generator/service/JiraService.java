package com.hackathon.ai_code_generator.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.hackathon.ai_code_generator.dto.JiraStory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;

@Service
public class JiraService {

    @Value("${jira.url}")
    private String jiraUrl;

    @Value("${jira.email}")
    private String email;

    @Value("${jira.api-token}")
    private String apiToken;

    private final RestTemplate restTemplate;

    public JiraService() {
        this.restTemplate = new RestTemplate();
    }

    public JiraStory fetchStory(String storyKey) {
        String url = jiraUrl + "/rest/api/2/issue/" + storyKey;

        HttpHeaders headers = createAuthHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, JsonNode.class);

        return parseIssue(response.getBody());
    }

    private HttpHeaders createAuthHeaders() {
        String auth = email + ":" + apiToken;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
        String authHeader = "Basic " + new String(encodedAuth);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private JiraStory parseIssue(JsonNode issue) {
        JiraStory story = new JiraStory();
        story.setKey(issue.get("key").asText());
        story.setSummary(issue.get("fields").get("summary").asText());
        story.setDescription(issue.get("fields").get("description").asText());

        // Custom field for acceptance criteria – adjust field ID as needed
        JsonNode customFields = issue.get("fields");
        if (customFields.has("customfield_10016")) {
            story.setAcceptanceCriteria(customFields.get("customfield_10016").asText());
        }
        return story;
    }
}