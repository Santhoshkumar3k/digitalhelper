package com.digitalhelper.service;

import com.digitalhelper.dto.DocumentExplainRequest;
import com.digitalhelper.dto.DocumentExplainResponse;
import org.springframework.stereotype.Service;

@Service
public class DocumentGuideService {

    private final AiService aiService;

    public DocumentGuideService(AiService aiService) {
        this.aiService = aiService;
    }

    public DocumentExplainResponse explain(DocumentExplainRequest request) {
        return aiService.explainDocument(request);
    }
}
