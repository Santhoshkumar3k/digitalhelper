package com.digitalhelper.controller;

import com.digitalhelper.dto.ApiResponse;
import com.digitalhelper.dto.DocumentExplainRequest;
import com.digitalhelper.dto.DocumentExplainResponse;
import com.digitalhelper.service.DocumentGuideService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/documents")
public class DocumentController {

    private final DocumentGuideService documentGuideService;

    public DocumentController(DocumentGuideService documentGuideService) {
        this.documentGuideService = documentGuideService;
    }

    @PostMapping("/explain")
    public ResponseEntity<ApiResponse<DocumentExplainResponse>> explainDocument(@RequestBody DocumentExplainRequest request) {
        DocumentExplainResponse response = documentGuideService.explain(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
