package com.hackathon.ai_code_generator.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.ai_code_generator.dto.DesignSpecification;
import com.hackathon.ai_code_generator.dto.FileEntry;
import com.hackathon.ai_code_generator.dto.GeneratedCode;
import com.hackathon.ai_code_generator.dto.JiraStory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class CodeGenerationService {

    private final AIService aiService;
    private final FileStorageService fileStorageService;

    public CodeGenerationService(AIService aiService, FileStorageService fileStorageService) {
        this.aiService = aiService;
        this.fileStorageService = fileStorageService;
    }

    public GeneratedCode generateFullStack(JiraStory story, DesignSpecification design) throws IOException {
        String springPrompt = buildSpringPrompt(story, design);
        String aiResponse = aiService.generateCode(springPrompt);

        ObjectMapper mapper = new ObjectMapper();
        List<FileEntry> springFiles = mapper.readValue(aiResponse, new TypeReference<List<FileEntry>>() {});

        // Similarly for React files (could also be multiple)
        String reactPrompt = buildReactPrompt(story, design);
        String reactResponse = aiService.generateCode(reactPrompt);
        List<FileEntry> reactFiles = mapper.readValue(reactResponse, new TypeReference<List<FileEntry>>() {});

        // Save all files using FileStorageService
        fileStorageService.saveFiles(springFiles, "spring");
        fileStorageService.saveFiles(reactFiles, "react");

        // Optionally, still return a summary for preview
        return GeneratedCode.builder()
                .reactComponents("Generated " + reactFiles.size() + " files")
                .springEntities("Generated " + springFiles.size() + " files")
                .build();
    }

    private String buildReactPrompt(JiraStory story, DesignSpecification design) {
        return String.format("""
                Generate a React TypeScript components based on:
                User Story: %s
                Description: %s
                Acceptance Criteria: %s
                UI Components: %s
                Layout: %s
                Data Fields: %s
                
                Return a JSON array where each element is an object with "filename" and "content".
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
        Generate a complete Spring Boot application based on:
        User Story: %s
        Description: %s
        Acceptance Criteria: %s
        Data Fields: %s
        
        Return a JSON array where each element is an object with "filename" and "content".
        Include all necessary files: entities, repositories, services, controllers, and any configuration.
        The content should be the full Java code.
        """,
                story.getSummary(),
                story.getDescription(),
                story.getAcceptanceCriteria(),
                design.getDataFields()
        );
    }
}
