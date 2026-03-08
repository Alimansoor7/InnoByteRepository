package com.hackathon.ai_code_generator.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GeneratedCode {
    private String reactComponents;
    private String springEntities;
    private String restControllers;
    private String apiClient;
}
