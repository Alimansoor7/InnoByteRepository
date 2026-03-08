package com.hackathon.ai_code_generator.controller;

import com.hackathon.ai_code_generator.dto.JiraStory;
import com.hackathon.ai_code_generator.service.JiraService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/jira")
@CrossOrigin(origins = "http://localhost:3000")
public class JiraController {

    private final JiraService jiraService;

    public JiraController(JiraService jiraService) {
        this.jiraService = jiraService;
    }

    @GetMapping("/story/{key}")
    public JiraStory getStory(@PathVariable String key) {
        return jiraService.fetchStory(key);
    }
}