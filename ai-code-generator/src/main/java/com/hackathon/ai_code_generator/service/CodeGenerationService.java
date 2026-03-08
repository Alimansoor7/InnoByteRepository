package com.hackathon.ai_code_generator.service;

import com.hackathon.ai_code_generator.dto.DesignSpecification;
import com.hackathon.ai_code_generator.dto.GeneratedCode;
import com.hackathon.ai_code_generator.dto.JiraStory;
import org.springframework.stereotype.Service;

@Service
public class CodeGenerationService {

    private final AIService aiService;

    public CodeGenerationService(AIService aiService) {
        this.aiService = aiService;
    }

    public GeneratedCode generateFullStack(JiraStory story, DesignSpecification design) {
        // Build prompts
        String reactPrompt = buildReactPrompt(story, design);
        String springPrompt = buildSpringPrompt(story, design);

        // Generate code
        String reactCode = aiService.generateCode(reactPrompt);
        String springCode = aiService.generateCode(springPrompt);

        return GeneratedCode.builder()
                .reactComponents(reactCode)
                .springEntities(springCode)
                .restControllers("// REST controllers would be generated here")
                .apiClient("// API client would be generated here")
                .build();
    }

    private String buildReactPrompt(JiraStory story, DesignSpecification design) {
        return String.format("""
                Generate a React TypeScript component based on:
                User Story: %s
                Description: %s
                Acceptance Criteria: %s
                UI Components: %s
                Layout: %s
                Data Fields: %s

                Use functional components with hooks, proper TypeScript interfaces,
                and Tailwind CSS classes for styling.
                """,
                story.getSummary(),
                story.getDescription(),
                story.getAcceptanceCriteria(),
                design.getComponents(),
                design.getLayout(),
                design.getDataFields()
        );
    }

    private String buildSpringPrompt(JiraStory story, DesignSpecification design) {
        return String.format("""
                Generate Spring Boot JPA entities and REST controllers for:
                User Story: %s
                Description: %s
                Acceptance Criteria: %s
                Data Fields: %s
                
                Include proper JPA annotations, relationships, and REST endpoints.
                """,
                story.getSummary(),
                story.getDescription(),
                story.getAcceptanceCriteria(),
                design.getDataFields()
        );
    }
}
