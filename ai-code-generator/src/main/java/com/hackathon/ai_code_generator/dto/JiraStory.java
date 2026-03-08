package com.hackathon.ai_code_generator.dto;

import lombok.Data;

@Data
public class JiraStory {
    private String key;
    private String summary;
    private String description;
    private String acceptanceCriteria;
}
