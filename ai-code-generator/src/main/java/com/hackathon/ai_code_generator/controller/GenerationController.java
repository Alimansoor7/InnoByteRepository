package com.hackathon.ai_code_generator.controller;

import com.hackathon.ai_code_generator.dto.DesignSpecification;
import com.hackathon.ai_code_generator.dto.GeneratedCode;
import com.hackathon.ai_code_generator.dto.JiraStory;
import com.hackathon.ai_code_generator.service.CodeGenerationService;
import com.hackathon.ai_code_generator.service.DesignAnalyzerService;
import com.hackathon.ai_code_generator.service.JiraService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/generate")
@CrossOrigin(origins = "http://localhost:3000")
public class GenerationController {

    private final JiraService jiraService;
    private final DesignAnalyzerService designAnalyzerService;
    private final CodeGenerationService codeGenerationService;

    public GenerationController(JiraService jiraService,
                                DesignAnalyzerService designAnalyzerService,
                                CodeGenerationService codeGenerationService) {
        this.jiraService = jiraService;
        this.designAnalyzerService = designAnalyzerService;
        this.codeGenerationService = codeGenerationService;
    }

    @PostMapping(value = "/fullstack", consumes = {"multipart/form-data"})
    public ResponseEntity<GeneratedCode> generateFullStack(
            @RequestParam("storyKey") String storyKey,
            @RequestParam("design") MultipartFile designFile) {

        try {
            JiraStory story = jiraService.fetchStory(storyKey);
            DesignSpecification design = designAnalyzerService.analyzeDesign(designFile);
            GeneratedCode code = codeGenerationService.generateFullStack(story, design);
            return ResponseEntity.ok(code);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}