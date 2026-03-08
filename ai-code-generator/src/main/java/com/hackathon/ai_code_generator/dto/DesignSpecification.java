package com.hackathon.ai_code_generator.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class DesignSpecification {
    private List<String> components;      // e.g., ["button", "form", "table"]
    private String layout;                // e.g., "grid with 2 columns"
    private Map<String, String> colors;   // e.g., {"primary": "#2C3E50"}
    private List<String> dataFields;      // e.g., ["name", "email", "role"]
}
