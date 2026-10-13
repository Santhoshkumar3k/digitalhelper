package com.digitalhelper.service;

import com.digitalhelper.dto.*;
import java.util.Map;

public interface AiService {
    AskResponse ask(AskRequest request, String conversationHistoryContext);
    ExplainImageResponse explainImage(ExplainImageRequest request);
    TranslateResponse translate(TranslateRequest request);
    ScamCheckResponse checkScam(ScamCheckRequest request);
    DocumentExplainResponse explainDocument(DocumentExplainRequest request);
    String getProviderName();
    boolean isConfigured();
    Map<String, Object> getAiStatus();
    boolean configureProvider(String provider, String apiKey, String model, String apiUrl);
}
