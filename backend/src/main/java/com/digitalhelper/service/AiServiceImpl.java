package com.digitalhelper.service;

import com.digitalhelper.dto.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class AiServiceImpl implements AiService {

    private static final Logger log = LoggerFactory.getLogger(AiServiceImpl.class);

    private static final String PERSISTED_CONFIG_FILE = "digitalhelper-ai.properties";

    private volatile String provider;
    private volatile String apiKey;
    private volatile String apiUrl;
    private volatile String model;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public AiServiceImpl(
            @Value("${digitalhelper.ai.provider:mock}") String provider,
            @Value("${digitalhelper.ai.api-key:}") String apiKey,
            @Value("${digitalhelper.ai.api-url:https://api.openai.com/v1/chat/completions}") String apiUrl,
            @Value("${digitalhelper.ai.model:gpt-4o-mini}") String model,
            ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();

        // Load initial defaults
        this.provider = provider;
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.apiUrl = apiUrl;
        this.model = model;

        // Check local persisted properties file or environment variables
        loadPersistedConfig();
    }

    private void loadPersistedConfig() {
        try {
            // Check system environment variables first
            String envKey = System.getenv("GEMINI_API_KEY");
            if (envKey == null || envKey.isBlank()) {
                envKey = System.getenv("AI_API_KEY");
            }
            if (envKey == null || envKey.isBlank()) {
                envKey = System.getenv("OPENAI_API_KEY");
            }

            if (envKey != null && !envKey.isBlank()) {
                this.apiKey = envKey.trim();
                if (this.apiKey.startsWith("AIzaSy")) {
                    this.provider = "gemini";
                    this.model = "gemini-1.5-flash";
                } else if (this.apiKey.startsWith("sk-")) {
                    this.provider = "openai";
                    this.model = "gpt-4o-mini";
                }
                log.info("Initialized AI key from environment variables: provider={}", this.provider);
                return;
            }

            // Check persisted file
            File file = new File(PERSISTED_CONFIG_FILE);
            if (file.exists()) {
                Properties props = new Properties();
                try (FileInputStream fis = new FileInputStream(file)) {
                    props.load(fis);
                    String savedKey = props.getProperty("apiKey", "").trim();
                    if (!savedKey.isBlank()) {
                        this.apiKey = savedKey;
                        this.provider = props.getProperty("provider", autoDetectProvider(savedKey));
                        this.model = props.getProperty("model", defaultModelForProvider(this.provider));
                        this.apiUrl = props.getProperty("apiUrl", defaultUrlForProvider(this.provider, this.model));
                        log.info("Loaded persisted AI config: provider={}, model={}", this.provider, this.model);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Could not load persisted AI config: {}", e.getMessage());
        }
    }

    private String autoDetectProvider(String key) {
        if (key.startsWith("AIzaSy")) return "gemini";
        if (key.startsWith("gsk_")) return "groq";
        if (key.startsWith("sk-")) return "openai";
        return "gemini";
    }

    private String defaultModelForProvider(String prov) {
        if ("gemini".equalsIgnoreCase(prov)) return "gemini-1.5-flash";
        if ("groq".equalsIgnoreCase(prov)) return "llama-3.3-70b-versatile";
        return "gpt-4o-mini";
    }

    private String defaultUrlForProvider(String prov, String mod) {
        if ("groq".equalsIgnoreCase(prov)) return "https://api.groq.com/openai/v1/chat/completions";
        if ("gemini".equalsIgnoreCase(prov)) return "https://generativelanguage.googleapis.com/v1beta/models/" + mod + ":generateContent";
        return "https://api.openai.com/v1/chat/completions";
    }

    @Override
    public synchronized boolean configureProvider(String newProvider, String newApiKey, String newModel, String newApiUrl) {
        if (newApiKey != null && !newApiKey.isBlank()) {
            this.apiKey = newApiKey.trim();
            this.provider = (newProvider != null && !newProvider.isBlank()) ? newProvider.toLowerCase() : autoDetectProvider(this.apiKey);
            this.model = (newModel != null && !newModel.isBlank()) ? newModel : defaultModelForProvider(this.provider);
            this.apiUrl = (newApiUrl != null && !newApiUrl.isBlank()) ? newApiUrl : defaultUrlForProvider(this.provider, this.model);

            // Persist to local properties file
            try {
                Properties props = new Properties();
                props.setProperty("provider", this.provider);
                props.setProperty("apiKey", this.apiKey);
                props.setProperty("model", this.model);
                props.setProperty("apiUrl", this.apiUrl);
                try (FileOutputStream fos = new FileOutputStream(PERSISTED_CONFIG_FILE)) {
                    props.store(fos, "DigitalHelper AI Configuration");
                }
                log.info("Successfully updated and saved AI configuration: provider={}, model={}", this.provider, this.model);
                return true;
            } catch (Exception e) {
                log.warn("Could not save AI config file: {}", e.getMessage());
                return true;
            }
        } else {
            // Revert to local engine
            this.provider = "mock";
            this.apiKey = "";
            File file = new File(PERSISTED_CONFIG_FILE);
            if (file.exists()) file.delete();
            return false;
        }
    }

    @Override
    public Map<String, Object> getAiStatus() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("configured", isConfigured());
        map.put("provider", this.provider);
        map.put("providerName", getProviderName());
        map.put("model", this.model);
        if (isConfigured()) {
            String masked = (apiKey.length() > 8)
                    ? apiKey.substring(0, 4) + "..." + apiKey.substring(apiKey.length() - 4)
                    : "••••••••";
            map.put("maskedApiKey", masked);
        } else {
            map.put("maskedApiKey", "");
        }
        return map;
    }

    @Override
    public String getProviderName() {
        if (isConfigured()) {
            if ("gemini".equalsIgnoreCase(provider)) {
                return "Google Gemini AI (" + model + " - Live)";
            } else if ("openai".equalsIgnoreCase(provider)) {
                return "OpenAI ChatGPT (" + model + " - Live)";
            } else if ("groq".equalsIgnoreCase(provider)) {
                return "Groq Ultra-Fast AI (" + model + " - Live)";
            }
            return provider.toUpperCase() + " (" + model + " - Live)";
        }
        return "DigitalHelper Intelligent Engine (Offline Capable)";
    }

    @Override
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isEmpty() && !"mock".equalsIgnoreCase(provider);
    }

    @Override
    public AskResponse ask(AskRequest request, String conversationHistoryContext) {
        String prompt = request.getPrompt().trim();
        String language = request.getLanguage() != null ? request.getLanguage() : "en";

        // 1. Try external real LLM (Gemini / OpenAI / Groq) if key is active
        if (isConfigured()) {
            try {
                String systemPrompt = "You are DigitalHelper, an exceptionally patient, accurate, and supportive AI assistant designed for ordinary people, seniors, and non-technical users. "
                        + "Always provide accurate, step-by-step guidance. Use simple everyday words, bullet points, and numbered steps. "
                        + "Include helpful Pro Tips (💡) and Safety Warnings (⚠️). Never ask for or encourage sharing passwords, OTPs, or bank PINs. "
                        + "If the user communicates in or requests an Indian language (Tamil, Hindi, Telugu, Kannada, Malayalam, Bengali, etc.), respond fluently in that language.";

                String userMessage = (conversationHistoryContext != null && !conversationHistoryContext.isBlank()
                        ? "Previous conversation context:\n" + conversationHistoryContext + "\n\n" : "")
                        + "User question: " + prompt;

                String answer = callActiveLlm(systemPrompt, userMessage);
                if (answer != null && !answer.isBlank()) {
                    List<String> followUps = generateFollowUps(prompt);
                    return new AskResponse(request.getConversationId(), null, answer, followUps, detectCategory(prompt), true);
                }
            } catch (Exception e) {
                log.warn("External LLM call failed or timed out: {}. Using Built-in Reasoning Engine.", e.getMessage());
            }
        }

        // 2. Comprehensive built-in Knowledge & Reasoning Engine
        String answer = generateIntelligentAnswer(prompt, language, conversationHistoryContext);
        List<String> followUps = generateFollowUps(prompt);
        return new AskResponse(request.getConversationId(), null, answer, followUps, detectCategory(prompt), false);
    }

    @Override
    public ExplainImageResponse explainImage(ExplainImageRequest request) {
        String base64 = request.getImageBase64();
        String note = request.getUserNote() != null ? request.getUserNote() : "";

        boolean sensitiveDetected = checkSensitiveDataPattern(note);

        if (isConfigured()) {
            try {
                String systemPrompt = "You are DigitalHelper Vision Explainer. The user has uploaded an error screenshot or app screen. "
                        + "Analyze the problem and provide a structured JSON response with keys: "
                        + "'errorTitle' (short summary), 'plainExplanation' (explain what happened in 5th-grade words), "
                        + "'possibleCauses' (array of 2-3 reasons), 'stepByStepFix' (array of numbered fix steps), 'safetyWarning' (string).";
                
                String visionAnswer = callActiveVisionLlm(systemPrompt, note, base64);
                if (visionAnswer != null && !visionAnswer.isBlank()) {
                    // Extract JSON if wrapped in markdown
                    String cleanJson = visionAnswer.trim();
                    if (cleanJson.startsWith("```json")) cleanJson = cleanJson.substring(7);
                    if (cleanJson.startsWith("```")) cleanJson = cleanJson.substring(3);
                    if (cleanJson.endsWith("```")) cleanJson = cleanJson.substring(0, cleanJson.length() - 3);

                    JsonNode node = objectMapper.readTree(cleanJson.trim());
                    List<String> causes = new ArrayList<>();
                    if (node.has("possibleCauses")) {
                        node.get("possibleCauses").forEach(c -> causes.add(c.asText()));
                    }
                    List<String> fixes = new ArrayList<>();
                    if (node.has("stepByStepFix")) {
                        node.get("stepByStepFix").forEach(f -> fixes.add(f.asText()));
                    }
                    return new ExplainImageResponse(
                            node.path("errorTitle").asText("Identified Application Notice"),
                            node.path("plainExplanation").asText("Here is what happened and how to resolve it safely."),
                            causes.isEmpty() ? List.of("Temporary connection hiccup", "Outdated cache or app version") : causes,
                            fixes.isEmpty() ? List.of("Close and reopen the application", "Restart your device") : fixes,
                            node.path("safetyWarning").asText("Never share OTPs, passwords, or payment cards shown on screen."),
                            sensitiveDetected
                    );
                }
            } catch (Exception e) {
                log.warn("Vision LLM call failed: {}. Falling back to Smart Vision Engine.", e.getMessage());
            }
        }

        return generateSmartVisionExplanation(note, sensitiveDetected);
    }

    @Override
    public TranslateResponse translate(TranslateRequest request) {
        String text = request.getText().trim();
        String targetLang = request.getTargetLanguage() != null ? request.getTargetLanguage().toLowerCase() : "ta";
        String mode = request.getMode() != null ? request.getMode() : "translate";

        if (isConfigured()) {
            try {
                String systemPrompt = "You are an Indian language translation and digital literacy specialist. "
                        + "Translate or simplify the user text into target language code: " + targetLang + ". "
                        + "Mode: " + mode + " (translate, simplify, or explain_terms). "
                        + "Respond in clean JSON: { 'translatedText': '...', 'simplifiedText': '...', 'terms': { 'term1': 'explanation' } }";

                String llmRes = callActiveLlm(systemPrompt, text);
                if (llmRes != null && !llmRes.isBlank()) {
                    String cleanJson = llmRes.trim();
                    if (cleanJson.startsWith("```json")) cleanJson = cleanJson.substring(7);
                    if (cleanJson.startsWith("```")) cleanJson = cleanJson.substring(3);
                    if (cleanJson.endsWith("```")) cleanJson = cleanJson.substring(0, cleanJson.length() - 3);

                    JsonNode json = objectMapper.readTree(cleanJson.trim());
                    TranslateResponse resp = new TranslateResponse();
                    resp.setOriginalText(text);
                    resp.setTranslatedText(json.path("translatedText").asText(text));
                    resp.setSimplifiedText(json.path("simplifiedText").asText("Simplified: " + text));
                    resp.setTargetLanguage(targetLang);
                    resp.setTargetLanguageName(getLanguageName(targetLang));
                    Map<String, String> terms = new LinkedHashMap<>();
                    if (json.has("terms")) {
                        json.get("terms").fields().forEachRemaining(entry -> terms.put(entry.getKey(), entry.getValue().asText()));
                    }
                    resp.setDifficultTermsExplained(terms);
                    return resp;
                }
            } catch (Exception e) {
                log.warn("Translation LLM failed: {}. Using native Indian language translator engine.", e.getMessage());
            }
        }

        return generateNativeLanguageTranslation(text, targetLang, mode);
    }

    @Override
    public ScamCheckResponse checkScam(ScamCheckRequest request) {
        String text = request.getMessageText();
        String url = request.getSourceUrl() != null ? request.getSourceUrl() : "";

        int riskScore = 0;
        List<String> flags = new ArrayList<>();
        List<String> nextSteps = new ArrayList<>();

        String lower = (text + " " + url).toLowerCase();

        // 1. Urgency & Threats
        if (lower.contains("immediately") || lower.contains("within 24 hours") || lower.contains("within 2 hours")
                || lower.contains("blocked") || lower.contains("suspended") || lower.contains("disconnected tonight")
                || lower.contains("urgent action") || lower.contains("account will be closed")) {
            riskScore += 35;
            flags.add("Artificial Urgency & Threat: Claims service or account will be disconnected quickly to create panic.");
        }

        // 2. Sensitive Data Demands
        if (lower.contains("otp") || lower.contains("pin") || lower.contains("password") || lower.contains("cvv")
                || lower.contains("card details") || lower.contains("aadhaar") || lower.contains("pan card update")) {
            riskScore += 40;
            flags.add("Dangerous Request for Credentials: Legitimate banks and government services NEVER ask for OTP, PIN, or CVV over SMS or WhatsApp.");
        }

        // 3. Impersonation of Banks, Utilities, Courier
        if (lower.contains("sbi") || lower.contains("hdfc") || lower.contains("icici") || lower.contains("electricity board")
                || lower.contains("bijli") || lower.contains("indianoil") || lower.contains("courier") || lower.contains("fedex")
                || lower.contains("income tax refund") || lower.contains("lottery") || lower.contains("kbc") || lower.contains("win cash")) {
            riskScore += 25;
            flags.add("High-Profile Impersonation: Imitates known banks, electricity providers, courier companies, or lottery wins.");
        }

        // 4. Suspicious links or APK downloads
        if (lower.contains(".apk") || lower.contains("bit.ly") || lower.contains("tinyurl") || lower.contains("is.gd")
                || lower.contains("t.me") || lower.contains("wa.me") || lower.contains("http://")
                || lower.contains(".xyz") || lower.contains(".top") || lower.contains(".live") || lower.contains("update-kyc")) {
            riskScore += 30;
            flags.add("Suspicious Link or App Download: Contains shortened unverified links, or asks you to install an unknown .APK file.");
        }

        // 5. Personal phone numbers pretending to be official toll-free
        if (Pattern.compile("(\\+91|91|0)?[6-9]\\d{9}").matcher(text).find() && (lower.contains("officer") || lower.contains("helpline") || lower.contains("call immediately"))) {
            riskScore += 20;
            flags.add("Personal Mobile Number Used as Support: Real institutions use registered shortcodes (like VM-HDFCBK) or official toll-free numbers (1800-xxxx), not 10-digit personal mobiles.");
        }

        riskScore = Math.min(100, riskScore);

        String riskLevel;
        String summary;
        if (riskScore >= 60) {
            riskLevel = "HIGH_RISK";
            summary = "High Alert! This message exhibits severe signs of a phishing or fraud attempt.";
            nextSteps.add("DO NOT click any links in this message.");
            nextSteps.add("DO NOT call any numbers listed in the message.");
            nextSteps.add("NEVER share any OTP, UPI PIN, or bank passwords.");
            nextSteps.add("Block the sender and report the SMS to the national cybercrime portal (Dial 1930 in India).");
        } else if (riskScore >= 25) {
            riskLevel = "SUSPICIOUS";
            summary = "Caution Advised: This message contains warning signs such as urgency or unfamiliar links.";
            nextSteps.add("Do not click unverified links.");
            nextSteps.add("Open the official app or website directly from your browser instead of clicking links.");
            nextSteps.add("Call your bank's verified helpline (found on the back of your debit card) to confirm.");
        } else {
            riskLevel = "LOW_CONCERN";
            summary = "No obvious scam triggers detected, but always stay vigilant with digital communications.";
            flags.add("Message appears to be standard communication or does not display blatant phishing urgency.");
            nextSteps.add("Verify sender identity if any financial action is requested.");
            nextSteps.add("Never share your banking PIN or one-time passwords with anyone.");
        }

        String officialAlt = "To verify official services safely, visit official bank websites directly, call the number on the back of your card, or dial the National Cyber Helpline at 1930 (cybercrime.gov.in).";

        return new ScamCheckResponse(riskLevel, riskScore, summary, flags, nextSteps, officialAlt);
    }

    @Override
    public DocumentExplainResponse explainDocument(DocumentExplainRequest request) {
        String type = request.getFormType() != null ? request.getFormType() : "General Online Form";
        DocumentExplainResponse resp = new DocumentExplainResponse();
        resp.setFormTitle(type);

        if (type.toLowerCase().contains("passport")) {
            resp.setPurpose("Official government document required for international travel and identity proof.");
            resp.setRequiredDocumentsChecklist(List.of(
                    "Proof of Date of Birth: Birth Certificate, 10th Class Marksheet, or PAN Card",
                    "Proof of Present Address: Aadhaar Card, Electricity Bill, Water Bill, or Bank Passbook",
                    "Old passport (if applying for renewal or re-issue)",
                    "Annexure declarations if applicable"
            ));
            Map<String, String> fields = new LinkedHashMap<>();
            fields.put("Given Name vs Surname", "Given Name is your first and middle name. Surname is your family name/last name. Do not include initials if full name is available.");
            fields.put("Non-ECR / ECR Status", "If you have passed 10th standard (Matriculation) or higher, select 'Yes' for Non-ECR eligible.");
            fields.put("Present Residential Address", "Must be the place where you currently reside for the last 1 year, as police verification will visit this location.");
            resp.setFieldExplanations(fields);
            resp.setCommonRejectionReasons(List.of(
                    "Name spelling mismatch between Aadhaar and School Marksheet",
                    "Present address proof older than 3 months or missing police station jurisdiction name",
                    "Blurry scanned copies or self-attestation missing on uploaded documents"
            ));
            resp.setStepByStepGuide(List.of(
                    "1. Register on the official passportindia.gov.in portal (beware of fake agent websites).",
                    "2. Fill the online form carefully matching your 10th marksheet spelling.",
                    "3. Pay the government fee and schedule an appointment at your nearest PSK/POPSK.",
                    "4. Carry all original documents plus two sets of self-attested photocopies on appointment day."
            ));
        } else if (type.toLowerCase().contains("pan") || type.toLowerCase().contains("income tax")) {
            resp.setPurpose("Permanent Account Number (PAN) used for tax filing, opening bank accounts, and high-value transactions.");
            resp.setRequiredDocumentsChecklist(List.of(
                    "Proof of Identity: Aadhaar Card, Voter ID, or Passport",
                    "Proof of Address: Aadhaar, Electricity Bill, or Bank Statement",
                    "Proof of Date of Birth: Aadhaar Card, Birth Certificate, or School Leaving Certificate"
            ));
            Map<String, String> fields = new LinkedHashMap<>();
            fields.put("Father's Name", "Mandatory even for married women (husband's name is not printed on PAN card).");
            fields.put("Source of Income", "Select 'Salary', 'Income from Business', or 'No Income' (e.g. for students/homemakers).");
            fields.put("AO Code (Assessing Officer)", "Can be searched easily by state and city on the NSDL or Protean portal.");
            resp.setFieldExplanations(fields);
            resp.setCommonRejectionReasons(List.of(
                    "Entering initials without expanding full father's name",
                    "Name mismatch between Aadhaar and existing tax records"
            ));
            resp.setStepByStepGuide(List.of(
                    "1. Visit the Protean (NSDL) or UTIITSL official portal.",
                    "2. Choose 'Form 49A' for Indian citizens.",
                    "3. Use paperless e-KYC using Aadhaar OTP for instant verification without sending physical papers."
            ));
        } else {
            resp.setPurpose("Standard online form guidance to prevent mistakes, save time, and protect your privacy.");
            resp.setRequiredDocumentsChecklist(List.of(
                    "Clear photo ID (Aadhaar, Passport, Voter ID, or Driving License)",
                    "Recent address proof dated within the last 3 months",
                    "Passport size photograph (clean white background, JPEG under 200KB)",
                    "Scanned signature (black/blue ink on white plain paper)"
            ));
            Map<String, String> fields = new LinkedHashMap<>();
            fields.put("Mandatory Fields (*)", "Fields marked with a red asterisk (*) must be filled. Optional fields can be skipped if unsure.");
            fields.put("File Upload Limits", "Usually between 50KB to 2MB. Use free PDF compressor if your scan is too large.");
            fields.put("Captchas & OTPs", "Type the exact security code shown. Keep your phone nearby to enter the SMS verification code.");
            resp.setFieldExplanations(fields);
            resp.setCommonRejectionReasons(List.of(
                    "Uploaded files larger than permissible size limit",
                    "Special characters (like /, #, &) entered in address fields when not allowed",
                    "Session timeout: taking longer than 15 minutes on a single page"
            ));
            resp.setStepByStepGuide(List.of(
                    "1. Gather all documents beforehand in JPEG or PDF format.",
                    "2. Fill in personal details exactly as printed on your government photo ID.",
                    "3. Review the preview summary page before clicking Final Submit.",
                    "4. Save and print the confirmation / acknowledgement receipt with application reference number."
            ));
        }

        return resp;
    }

    // --- LLM Calling Implementations (Google Gemini & OpenAI/Groq) ---

    private String callActiveLlm(String systemPrompt, String userMessage) {
        if ("gemini".equalsIgnoreCase(provider) || apiKey.startsWith("AIzaSy")) {
            return callGeminiChat(systemPrompt, userMessage);
        } else {
            return callOpenAiCompatibleChat(systemPrompt, userMessage);
        }
    }

    private String callActiveVisionLlm(String systemPrompt, String note, String base64) {
        if ("gemini".equalsIgnoreCase(provider) || apiKey.startsWith("AIzaSy")) {
            return callGeminiVision(systemPrompt, note, base64);
        } else {
            return callOpenAiCompatibleVision(systemPrompt, note, base64);
        }
    }

    private String callGeminiChat(String systemPrompt, String userMessage) {
        try {
            String targetModel = (model != null && !model.isBlank()) ? model : "gemini-1.5-flash";
            String url = "https://generativelanguage.googleapis.com/v1beta/models/" + targetModel + ":generateContent?key=" + apiKey;

            Map<String, Object> payload = new HashMap<>();
            payload.put("contents", List.of(
                    Map.of("role", "user", "parts", List.of(
                            Map.of("text", systemPrompt + "\n\n" + userMessage)
                    ))
            ));
            payload.put("generationConfig", Map.of(
                    "temperature", 0.4,
                    "maxOutputTokens", 1200
            ));

            String requestBody = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(Duration.ofSeconds(15))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                return root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
            } else {
                log.warn("Gemini API error status: {}, body: {}", response.statusCode(), response.body());
            }
        } catch (Exception e) {
            log.warn("Gemini Chat exception: {}", e.getMessage());
        }
        return null;
    }

    private String callGeminiVision(String systemPrompt, String note, String base64) {
        try {
            String targetModel = (model != null && !model.isBlank()) ? model : "gemini-1.5-flash";
            String url = "https://generativelanguage.googleapis.com/v1beta/models/" + targetModel + ":generateContent?key=" + apiKey;

            String cleanData = base64;
            String mimeType = "image/jpeg";
            if (cleanData.contains(",")) {
                String header = cleanData.substring(0, cleanData.indexOf(","));
                cleanData = cleanData.substring(cleanData.indexOf(",") + 1);
                if (header.contains("png")) mimeType = "image/png";
                else if (header.contains("webp")) mimeType = "image/webp";
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("contents", List.of(
                    Map.of("role", "user", "parts", List.of(
                            Map.of("text", systemPrompt + "\nUser note: " + note),
                            Map.of("inline_data", Map.of("mime_type", mimeType, "data", cleanData))
                    ))
            ));
            payload.put("generationConfig", Map.of("temperature", 0.2, "maxOutputTokens", 1200));

            String requestBody = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(Duration.ofSeconds(20))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                return root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
            }
        } catch (Exception e) {
            log.warn("Gemini Vision exception: {}", e.getMessage());
        }
        return null;
    }

    private String callOpenAiCompatibleChat(String systemPrompt, String userMessage) {
        try {
            String targetUrl = (apiUrl != null && !apiUrl.isBlank()) ? apiUrl : "https://api.openai.com/v1/chat/completions";
            String targetModel = (model != null && !model.isBlank()) ? model : "gpt-4o-mini";

            Map<String, Object> payload = new HashMap<>();
            payload.put("model", targetModel);
            payload.put("messages", List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", userMessage)
            ));
            payload.put("max_tokens", 1000);
            payload.put("temperature", 0.4);

            String requestBody = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(Duration.ofSeconds(15))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                return root.path("choices").get(0).path("message").path("content").asText();
            }
        } catch (Exception e) {
            log.warn("OpenAI-compatible Chat exception: {}", e.getMessage());
        }
        return null;
    }

    private String callOpenAiCompatibleVision(String systemPrompt, String note, String base64) {
        try {
            String targetUrl = (apiUrl != null && !apiUrl.isBlank()) ? apiUrl : "https://api.openai.com/v1/chat/completions";
            String targetModel = (model != null && !model.isBlank()) ? model : "gpt-4o-mini";

            String imageUrl = base64.startsWith("data:") ? base64 : "data:image/jpeg;base64," + base64;

            Map<String, Object> payload = new HashMap<>();
            payload.put("model", targetModel);
            payload.put("messages", List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", List.of(
                            Map.of("type", "text", "text", note != null && !note.isBlank() ? note : "Analyze this error screenshot."),
                            Map.of("type", "image_url", "image_url", Map.of("url", imageUrl))
                    ))
            ));
            payload.put("max_tokens", 1000);

            String requestBody = objectMapper.writeValueAsString(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .timeout(Duration.ofSeconds(20))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonNode root = objectMapper.readTree(response.body());
                return root.path("choices").get(0).path("message").path("content").asText();
            }
        } catch (Exception e) {
            log.warn("OpenAI-compatible Vision exception: {}", e.getMessage());
        }
        return null;
    }

    // --- High-Precision Built-in Reasoning Engine ---

    private String generateIntelligentAnswer(String prompt, String language, String context) {
        String p = prompt.toLowerCase().trim();
        String c = context != null ? context.toLowerCase() : "";

        // 1. Check if user is asking a purely contextual follow-up (ONLY if no independent subject is introduced)
        boolean mentionsNewSubject = p.contains("whatsapp") || p.contains("wifi") || p.contains("wi-fi") || p.contains("bluetooth")
                || p.contains("battery") || p.contains("screen") || p.contains("photo") || p.contains("laptop")
                || p.contains("phone") || p.contains("upi") || p.contains("storage") || p.contains("gmail")
                || p.contains("instagram") || p.contains("youtube") || p.contains("pdf") || p.contains("qr")
                || p.contains("aadhaar") || p.contains("pan") || p.contains("water") || p.contains("chrome")
                || p.contains("app") || p.contains("card") || p.contains("balance") || p.contains("hotspot");

        if (!mentionsNewSubject) {
            // Contextual emergency protocol if previous turn was discussing OTP and user asks what to do if shared
            if ((c.contains("otp") || c.contains("one-time password"))
                    && (p.contains("mistake") || p.contains("accident") || p.contains("shared") || p.contains("sent it") || p.equals("what can i do") || p.equals("what should i do now"))) {
                return generateOtpEmergencyProtocol();
            }
        }

        // 2. Direct Accidental OTP Sharing Emergency
        if ((p.contains("mistake") || p.contains("mistakenly") || p.contains("accident") || p.contains("shared"))
                && (p.contains("otp") || p.contains("pin") || p.contains("cvv") || p.contains("password"))) {
            return generateOtpEmergencyProtocol();
        }

        // 3. What is OTP / PIN / 2FA Security
        if ((p.contains("what is") || p.contains("explain")) && (p.contains("otp") || p.contains("one time password"))) {
            return generateWhatIsOtpGuide();
        }

        // 4. WhatsApp Suite
        if (p.contains("whatsapp")) {
            if (p.contains("storage") || p.contains("space") || p.contains("clean") || p.contains("clear") || p.contains("full") || p.contains("memory")) {
                return generateWhatsAppStorageGuide();
            }
            if (p.contains("backup") || p.contains("restore") || p.contains("drive") || p.contains("chat transfer") || p.contains("history")) {
                return generateWhatsAppBackupGuide();
            }
            if (p.contains("lock") || p.contains("fingerprint") || p.contains("privacy") || p.contains("last seen") || p.contains("online") || p.contains("hide")) {
                return generateWhatsAppPrivacyGuide();
            }
            if (p.contains("location") || p.contains("live location")) {
                return generateWhatsAppLocationGuide();
            }
            if (p.contains("delete") || p.contains("unsend") || p.contains("everyone")) {
                return generateWhatsAppDeleteMessageGuide();
            }
            if (p.contains("block") || p.contains("unblock") || p.contains("spam") || p.contains("report")) {
                return generateWhatsAppBlockGuide();
            }
            if (p.contains("web") || p.contains("qr") || p.contains("pc") || p.contains("laptop") || p.contains("link")) {
                return generateWhatsAppWebGuide();
            }
            if (p.contains("dp") || p.contains("profile") || p.contains("picture")) {
                return generateWhatsAppProfileGuide();
            }
            return generateWhatsAppGeneralGuide();
        }

        // 5. Phone Dropped in Water (Immediate Emergency)
        if (p.contains("water") || p.contains("wet") || p.contains("rain") || p.contains("dropped in water") || p.contains("fell in water")) {
            return generatePhoneWaterEmergencyGuide();
        }

        // 6. Screenshot Guide (PC / Mac / Mobile)
        if (p.contains("screenshot") || p.contains("screen capture") || (p.contains("how to") && p.contains("capture screen"))) {
            return generateScreenshotGuide();
        }

        // 7. Screen Recording
        if (p.contains("screen record") || p.contains("record screen") || p.contains("record video of screen")) {
            return generateScreenRecordGuide();
        }

        // 8. Phone Heating / Overheating
        if (p.contains("heating") || p.contains("overheat") || p.matches(".*\\bhot\\b.*") || (p.contains("phone") && (p.contains("warm") || p.contains("charge") || p.contains("charging")))) {
            return generatePhoneHeatingGuide();
        }

        // 9. Phone Battery Draining Fast
        if (p.contains("battery") && (p.contains("drain") || p.contains("fast") || p.contains("dying") || p.contains("low") || p.contains("life") || p.contains("save"))) {
            return generateBatteryDrainGuide();
        }

        // 10. Phone Hanging / Slow / Lagging
        if ((p.contains("phone") || p.contains("mobile")) && (p.contains("slow") || p.contains("hanging") || p.contains("freeze") || p.contains("lag") || p.contains("stuck"))) {
            return generatePhoneLagGuide();
        }

        // 11. Force Restart / Won't Turn On
        if (p.contains("force restart") || p.contains("black screen") || p.contains("wont turn on") || p.contains("won't turn on") || p.contains("frozen screen")) {
            return generateForceRestartGuide();
        }

        // 12. Transfer Photos from Phone to Laptop without Internet
        if ((p.contains("transfer") || p.contains("copy") || p.contains("send") || p.contains("move"))
                && (p.contains("photo") || p.contains("file") || p.contains("video") || p.contains("image"))
                && (p.contains("laptop") || p.contains("computer") || p.contains("pc"))) {
            return generatePhotoTransferGuide();
        }

        // 13. Bluetooth Issues (Headphones, Earbuds, Speaker, Pairing)
        if (p.contains("bluetooth") || p.contains("earbuds") || p.contains("airpods") || p.contains("headphones") || p.contains("pair")) {
            return generateBluetoothGuide();
        }

        // 14. Wi-Fi Connected but No Internet
        if ((p.contains("wifi") || p.contains("wi-fi") || p.contains("internet")) && (p.contains("no internet") || p.contains("not working") || p.contains("offline") || p.contains("connected but") || p.contains("slow"))) {
            return generateWifiTroubleshootGuide();
        }

        // 15. Mobile Hotspot Setup & Sharing
        if (p.contains("hotspot") || p.contains("tethering") || p.contains("share internet")) {
            return generateHotspotGuide();
        }

        // 16. Mobile Data / SIM Network Issues
        if (p.contains("mobile data") || p.contains("cellular") || p.contains("sim card") || p.contains("no service") || p.contains("emergency calls")) {
            return generateMobileDataGuide();
        }

        // 17. Phone Storage Full / Delete Other Files
        if (p.contains("storage full") || (p.contains("storage") && (p.contains("free") || p.contains("clean") || p.contains("low space") || p.contains("clear")))) {
            return generateStorageCleanupGuide();
        }

        // 18. SD Card / Memory Card
        if (p.contains("sd card") || p.contains("memory card")) {
            return generateSdCardGuide();
        }

        // 19. UPI / Payment Failed but Money Debited
        if ((p.contains("upi") || p.contains("gpay") || p.contains("phonepe") || p.contains("paytm")) && (p.contains("debited") || p.contains("failed") || p.contains("stuck") || p.contains("refund") || p.contains("deducted"))) {
            return generateUpiRefundGuide();
        }

        // 20. Check Bank Balance by Phone / SMS / Missed Call
        if (p.contains("balance") && (p.contains("bank") || p.contains("account") || p.contains("check") || p.contains("enquiry"))) {
            return generateBankBalanceGuide();
        }

        // 21. ATM / Debit Card Blocked or PIN Reset
        if ((p.contains("atm") || p.contains("debit card") || p.contains("credit card")) && (p.contains("pin") || p.contains("block") || p.contains("unblock") || p.contains("lost"))) {
            return generateAtmCardGuide();
        }

        // 22. Gmail & Google Account Recovery / Setup
        if (p.contains("gmail") || p.contains("google account") || (p.contains("google") && p.contains("password"))) {
            return generateGmailGuide(p);
        }

        // 23. Instagram Account & Password
        if (p.contains("instagram") || p.contains("insta")) {
            return generateInstagramGuide(p);
        }

        // 24. Facebook Account & Privacy
        if (p.contains("facebook") || p.contains("fb")) {
            return generateFacebookGuide();
        }

        // 25. Two-Factor Authentication (2FA) Setup
        if (p.contains("2fa") || p.contains("two factor") || p.contains("authenticator") || p.contains("two step")) {
            return generate2faGuide();
        }

        // 26. Aadhaar Card Download & Verification
        if (p.contains("aadhaar") || p.contains("aadhar")) {
            return generateAadhaarGuide();
        }

        // 27. Link PAN Card with Aadhaar
        if (p.contains("pan") && (p.contains("aadhaar") || p.contains("aadhar") || p.contains("link"))) {
            return generatePanAadhaarGuide();
        }

        // 28. DigiLocker Government Documents
        if (p.contains("digilocker")) {
            return generateDigilockerGuide();
        }

        // 29. Scan QR Code Guide
        if (p.contains("qr") || p.contains("scan code") || p.contains("barcode")) {
            return generateQrScanGuide();
        }

        // 30. PDF Scan & Compression Guide
        if (p.contains("pdf")) {
            return generatePdfGuide(p);
        }

        // 31. YouTube Buffering & Offline Downloads
        if (p.contains("youtube")) {
            return generateYoutubeGuide(p);
        }

        // 32. Chrome Browser History & Cache Clearing
        if (p.contains("chrome") || p.contains("browser") || p.contains("incognito") || (p.contains("history") && p.contains("clear"))) {
            return generateBrowserGuide(p);
        }

        // 33. Uninstalling Apps
        if (p.contains("uninstall") || (p.contains("delete") && p.contains("app"))) {
            return generateUninstallAppGuide();
        }

        // 34. Locate Lost / Stolen Phone
        if (p.contains("lost phone") || p.contains("find my phone") || p.contains("stolen phone") || p.contains("find my device")) {
            return generateLostPhoneGuide();
        }

        // 35. Dynamic Intelligent Synthesizer for ANY OTHER continuous question
        return generateDynamicSynthesizer(prompt, language);
    }

    // --- Specialized Knowledge Guides ---

    private String generateOtpEmergencyProtocol() {
        return "### 🚨 EMERGENCY PROTOCOL: You Accidentally Shared an OTP\n\n"
                + "Act immediately! Follow these 5 urgent steps within the next few minutes to protect your money:\n\n"
                + "1. **Immediately Lock Your Bank Card & UPI Access**:\n"
                + "   - Open your mobile banking app (e.g. YONO SBI, HDFC, iMobile) and toggle **Freeze Card / Disable UPI Transactions**.\n"
                + "   - Or send your bank's instant card block SMS from your registered SIM (e.g. for SBI SMS `BLOCK <Last 4 digits>` to `567676`).\n\n"
                + "2. **Call Your Bank's 24x7 Emergency Fraud Number**:\n"
                + "   - Call the official phone number printed **on the back of your physical ATM / Debit Card**.\n"
                + "   - Tell the representative: *\"I accidentally shared an OTP with an unverified party. Please freeze all outgoing online debits immediately.\"*\n\n"
                + "3. **Change All Important Passwords**:\n"
                + "   - Change your Net Banking password and UPI PIN.\n"
                + "   - Change your email (Gmail) password and select **'Log out of all other devices'**.\n\n"
                + "4. **Check Your Account Balance & Transaction Statement**:\n"
                + "   - If money was already debited, note down the **Transaction Reference / UTR Number** immediately.\n\n"
                + "5. **File a Report with National Cyber Helpline (India)**:\n"
                + "   - Call **1930** immediately or lodge a report at **cybercrime.gov.in** within 2 hours. Prompt reporting enables banks to freeze the money in the scammer's beneficiary account.\n\n"
                + "💡 **Remember**: Never trust anyone who calls back promising to \"cancel the mistake\" if you give them another OTP!";
    }

    private String generateWhatIsOtpGuide() {
        return "### 🔐 What is an OTP and Why Is It Secret?\n\n"
                + "**OTP stands for One-Time Password.** It is a temporary, 4-digit or 6-digit security code generated by your bank or service provider and sent directly to your phone via SMS.\n\n"
                + "#### Why Do We Need It?\n"
                + "* It acts as a digital signature verifying that you are the real person authorizing an action.\n"
                + "* It expires in 3 to 10 minutes and can only be used once.\n\n"
                + "#### The Golden Rule of Digital Safety:\n"
                + "⚠️ **NEVER share your OTP with ANYONE—even if they claim to be:**\n"
                + "* A bank branch manager\n"
                + "* A police officer or CBI agent\n"
                + "* A delivery person or lottery official\n"
                + "* Your electricity board agent\n\n"
                + "💡 **Pro Tip**: Real bank officials will NEVER ask for your OTP. If someone asks for it, it is 100% a fraud attempt.";
    }

    private String generateWhatsAppStorageGuide() {
        return "### 📱 How to Clear WhatsApp Storage (Step-by-Step)\n\n"
                + "WhatsApp easily accumulates gigabytes of forwarded videos, photos, and voice notes. Here is the safest way to clear space without losing your chats:\n\n"
                + "1. **Open WhatsApp** and tap the **three dots (⋮)** in the top right corner (or **Settings ⚙️** at bottom right on iPhone).\n"
                + "2. Tap on **Settings** → **Storage and Data**.\n"
                + "3. Tap **Manage Storage** at the very top.\n"
                + "4. Review the section called **'Larger than 5 MB'**:\n"
                + "   - Tap into it to see heavy forwarded videos.\n"
                + "   - Select the unwanted ones and tap the **Trash icon 🗑️**.\n"
                + "5. Scroll down to see chats ordered from largest to smallest:\n"
                + "   - Open your biggest group chats.\n"
                + "   - Delete heavy forwards while keeping important text messages intact.\n\n"
                + "💡 **Pro Tip**: To stop WhatsApp from automatically saving every forwarded photo to your phone gallery, go to **Settings → Chats** and turn off **Media Visibility**.";
    }

    private String generateWhatsAppBackupGuide() {
        return "### ☁️ How to Backup and Restore WhatsApp Chats\n\n"
                + "Never lose family photos or business chats. Back up everything securely to Google Drive (Android) or iCloud (iPhone):\n\n"
                + "1. **Open WhatsApp** → tap **three dots (⋮)** → **Settings** (or Settings tab on iPhone).\n"
                + "2. Tap **Chats** → **Chat Backup**.\n"
                + "3. **Select Your Google Account**:\n"
                + "   - Choose your main Gmail account.\n"
                + "   - Under **Back up to Google Drive**, select **Daily** or **Weekly**.\n"
                + "4. **Toggle Include Videos**:\n"
                + "   - Leave *Videos off* if your Google Drive storage is almost full.\n"
                + "5. Tap the green **Back Up** button to initiate an instant cloud sync.\n\n"
                + "💡 **Restoring on a New Phone**: Install WhatsApp on the new device, verify the same phone number, and tap **Restore** when prompted during first launch.";
    }

    private String generateWhatsAppPrivacyGuide() {
        return "### 🔒 WhatsApp Privacy & Fingerprint Lock Setup\n\n"
                + "Keep your personal messages private from anyone holding your phone:\n\n"
                + "1. **Enable App Fingerprint / Face ID Lock**:\n"
                + "   - Open WhatsApp → **Settings** → **Privacy**.\n"
                + "   - Scroll to bottom and tap **App Lock** (or Fingerprint Lock / Face ID).\n"
                + "   - Turn it ON and select **Immediately**.\n"
                + "2. **Hide Last Seen & Online Status**:\n"
                + "   - In **Privacy**, tap **Last Seen and Online**.\n"
                + "   - Set to **My Contacts** or **Nobody**.\n"
                + "3. **Turn Off Read Receipts (Blue Ticks)**:\n"
                + "   - In **Privacy**, toggle off **Read Receipts** so others cannot see when you opened their message.\n\n"
                + "💡 **Pro Tip**: Set up **Two-Step Verification** under Settings → Account → Two-step verification to prevent SIM-swap hijacking.";
    }

    private String generateWhatsAppLocationGuide() {
        return "### 📍 How to Share Live Location on WhatsApp Safely\n\n"
                + "Share your real-time moving location with family during travel:\n\n"
                + "1. Open the specific chat with the person you want to share with.\n"
                + "2. Tap the **Paperclip icon 📎** (or **+** icon on iPhone) in the typing bar.\n"
                + "3. Tap **Location**.\n"
                + "4. Choose between:\n"
                + "   - **Share Live Location**: Updates your position continuously for 15 minutes, 1 hour, or 8 hours.\n"
                + "   - **Send Your Current Location**: Sends a single static pin of where you are right now.\n"
                + "5. Tap **Stop Sharing** anytime from the chat banner to end location access immediately.\n\n"
                + "⚠️ **Safety Reminder**: Never share live location in public broadcast groups or with people you haven't verified in person.";
    }

    private String generateWhatsAppDeleteMessageGuide() {
        return "### ⏱️ How to Delete Sent Messages on WhatsApp (Delete for Everyone)\n\n"
                + "Sent a message to the wrong chat? Here is how to recall it:\n\n"
                + "1. Tap and hold the message you want to remove.\n"
                + "2. Tap the **Trash icon 🗑️** at the top bar.\n"
                + "3. Choose **Delete for Everyone**:\n"
                + "   - This removes the message from both your phone and the recipient's phone.\n"
                + "   - WhatsApp permits \"Delete for Everyone\" up to **approx. 2 days (48 hours)** after sending.\n\n"
                + "⚠️ **Caution**: If you mistakenly tap **'Delete for Me'**, the message is only deleted from your screen, but the recipient can still read it!";
    }

    private String generateWhatsAppBlockGuide() {
        return "### 🚫 How to Block and Report Spam Contacts on WhatsApp\n\n"
                + "Stop harassing calls and fraudulent loan forwards immediately:\n\n"
                + "1. Open the chat of the unknown or unwanted sender.\n"
                + "2. Tap their phone number or name at the top to view contact details.\n"
                + "3. Scroll to the bottom and tap **Block Contact**.\n"
                + "4. Check **'Report contact to WhatsApp'** to submit recent spam messages for automated platform banning.\n\n"
                + "💡 **Pro Tip**: Blocked contacts cannot see your Profile Photo, Last Seen, or Status, and their calls will never ring on your phone.";
    }

    private String generateWhatsAppWebGuide() {
        return "### 💻 How to Use WhatsApp on Computer (WhatsApp Web)\n\n"
                + "Type messages comfortably on your computer keyboard:\n\n"
                + "1. On your laptop / PC browser, open **web.whatsapp.com**.\n"
                + "2. A large black-and-white QR code will appear on the computer screen.\n"
                + "3. On your smartphone, open WhatsApp → tap **three dots (⋮)** (or Settings on iPhone) → tap **Linked Devices**.\n"
                + "4. Tap **Link a Device** and point your phone camera at the computer screen QR code.\n"
                + "5. Your chats will load instantly. You can keep chatting even if your phone's battery dies!\n\n"
                + "⚠️ **Security Tip**: Always log out when using public cyber cafés by clicking the three dots on WhatsApp Web → **Log Out**.";
    }

    private String generateWhatsAppProfileGuide() {
        return "### 🖼️ How to Change WhatsApp Profile Photo & DP\n\n"
                + "Update your profile image and control who can see it:\n\n"
                + "1. Open WhatsApp → tap **three dots (⋮)** → **Settings**.\n"
                + "2. Tap your profile picture at the top.\n"
                + "3. Tap the **Camera icon** on the photo:\n"
                + "   - Choose **Gallery** to select an existing picture.\n"
                + "   - Or choose **Camera** to snap a new selfie.\n"
                + "4. Adjust the crop square and tap **Done**.\n\n"
                + "💡 **Privacy Tip**: Go to **Settings → Privacy → Profile Photo** and change it to **My Contacts** so unknown scammers cannot download your picture.";
    }

    private String generateWhatsAppGeneralGuide() {
        return "### 📱 WhatsApp Quick Guide & Helpful Shortcuts\n\n"
                + "Here are the top WhatsApp features every user should know:\n\n"
                + "1. **Star Important Messages**: Long-press any important address, bank detail, or photo → tap the **Star ⭐ icon**. Find them later in Settings → Starred Messages.\n"
                + "2. **Pin Top Chats**: Long-press your most important chat → tap the **Pin 📌 icon** to keep it at the top of your list.\n"
                + "3. **Mute Noisy Groups**: Open group chat → tap three dots → **Mute notifications** → choose 8 hours, 1 week, or Always.\n"
                + "4. **Disappearing Messages**: In any chat, tap the contact name → **Disappearing messages** → set to 24 hours or 7 days for automatic cleanup.\n\n"
                + "💡 **Need something specific?** You can ask me: *\"How to backup WhatsApp?\"*, *\"How to clear WhatsApp storage?\"*, or *\"How to use WhatsApp Web?\"*";
    }

    private String generatePhoneWaterEmergencyGuide() {
        return "### 🌊 EMERGENCY: Phone Dropped in Water Protocol\n\n"
                + "Do NOT panic, but act immediately. Following these correct steps saves 90% of water-damaged phones:\n\n"
                + "1. **REMOVE FROM WATER IMMEDIATELY** and **TURN OFF POWER**:\n"
                + "   - Press and hold the power button to shut it down completely.\n"
                + "   - If it is already off, **DO NOT turn it on to test it**!\n\n"
                + "2. **NEVER PLUG IN THE CHARGER**:\n"
                + "   - Electricity plus water causes an instant short circuit that fries the motherboard.\n\n"
                + "3. **Remove Removable Parts**:\n"
                + "   - Eject the SIM tray, SIM card, and micro-SD card.\n"
                + "   - Remove phone cases and covers.\n\n"
                + "4. **Gently Dry the Exterior**:\n"
                + "   - Wipe down with a soft microfiber cloth or cotton towel.\n"
                + "   - Gently tap the phone against your palm with the charging port facing down to let water droplets exit.\n\n"
                + "5. **Drying Method**:\n"
                + "   - Place the phone in front of a cool household fan or in an airtight box with **Silica Gel packets** for 24 to 48 hours.\n"
                + "   - ⚠️ **AVOID**: Never use a hot hairdryer, oven, or direct scorching sunlight (heat warps internal glue and batteries).\n"
                + "   - ⚠️ **Myth**: Do NOT use raw cooking rice — rice dust enters charging ports and headphone jacks causing sticky mud.";
    }

    private String generateScreenshotGuide() {
        return "### 📸 How to Take a Screenshot on Any Device\n\n"
                + "#### 1. Windows Computer / Laptop (Best Methods):\n"
                + "* **Method A (Snipping Tool)**: Press **Windows Key + Shift + S** together. Drag a box over what you want to capture.\n"
                + "* **Method B (Full Screen to File)**: Press **Windows Key + Print Screen (PrtScn)**. The screenshot is automatically saved in **Pictures → Screenshots**.\n\n"
                + "#### 2. Android Smartphone:\n"
                + "* Press and hold **Power Button + Volume Down** simultaneously for 1 second.\n"
                + "* Or swipe down with 3 fingers on screen (supported on Samsung, Redmi, OnePlus, Realme, Vivo).\n\n"
                + "#### 3. Apple iPhone:\n"
                + "* **Face ID models**: Press **Side Button + Volume Up** simultaneously.\n"
                + "* **Home Button models**: Press **Side Button + Home Button**.\n\n"
                + "#### 4. Apple Mac Laptop:\n"
                + "* Press **Command + Shift + 4** to drag-select an area, or **Command + Shift + 3** for entire screen.";
    }

    private String generateScreenRecordGuide() {
        return "### 🎥 How to Record Your Screen with Audio\n\n"
                + "#### 1. Windows 10 & Windows 11:\n"
                + "* **Method A (Snipping Tool)**: Open Start menu → search **Snipping Tool** → click the **Video Camera icon** → click **New** → drag rectangle → click **Start**.\n"
                + "* **Method B (Xbox Game Bar)**: Press **Windows Key + Alt + R** to start/stop recording instantly.\n\n"
                + "#### 2. Android Phone:\n"
                + "* Swipe down from the top of screen to open Quick Settings.\n"
                + "* Tap the **Screen Recorder** tile (if not visible, tap Edit/Pencil icon to add it).\n"
                + "* Choose audio source (Device audio or Microphone) and tap Start.\n\n"
                + "#### 3. Apple iPhone / iPad:\n"
                + "* Open **Settings → Control Center** → add **Screen Recording**.\n"
                + "* Swipe down from top-right corner → tap the round **Record icon**.\n\n"
                + "💡 **Pro Tip**: To record microphone voice on iPhone, long-press the Screen Record button and toggle **Microphone ON**.";
    }

    private String generatePhoneHeatingGuide() {
        return "### 🌡️ Why Is Your Phone Heating Up & How to Cool It Down\n\n"
                + "Smartphones heat up when either the processor (CPU) or battery is under heavy strain. Here is what to do right now:\n\n"
                + "1. **Remove Thick Phone Cases**: Plastic, silicone, and leather covers trap internal heat.\n"
                + "2. **If Heating While Charging**:\n"
                + "   - Unplug the charger immediately and let it cool for 15 minutes.\n"
                + "   - Never charge your phone on a bed, mattress, or pillow (place it on a cool, hard flat surface).\n"
                + "   - Do not make phone calls or play games while plugged in.\n"
                + "3. **Close Heavy Background Tasks**:\n"
                + "   - Turn off **GPS Location, Bluetooth, and Personal Hotspot**.\n"
                + "   - Close navigation apps (Google Maps) and video streaming.\n"
                + "4. **Check for Rogue Apps**:\n"
                + "   - Go to **Settings → Battery → Battery Usage**.\n"
                + "   - Look for any app consuming 20%+ battery in the background and uninstall or force-stop it.\n\n"
                + "💡 **Pro Tip**: Always use the original brand charger and cable. Cheap duplicate fast-chargers cause severe overheating and damage battery health.";
    }

    private String generateBatteryDrainGuide() {
        return "### 🔋 How to Stop Phone Battery Draining Fast\n\n"
                + "If your battery percentage drops rapidly throughout the day, apply these 5 proven battery savers:\n\n"
                + "1. **Switch to Dark Mode & Reduce Screen Timeout**:\n"
                + "   - Go to **Settings → Display → Dark Mode (ON)**. OLED screens consume up to 40% less power displaying black.\n"
                + "   - Set **Screen Timeout** to **30 seconds**.\n"
                + "2. **Disable Unnecessary Background App Refresh**:\n"
                + "   - Go to **Settings → Apps → Special app access → Battery optimization**.\n"
                + "   - Set non-essential shopping and social apps to **Restricted**.\n"
                + "3. **Turn Off Always-On Scanning**:\n"
                + "   - Go to **Settings → Location → Location Services**.\n"
                + "   - Turn OFF **Wi-Fi scanning** and **Bluetooth scanning** (these search 24/7 even when toggled off).\n"
                + "4. **Turn on Battery Saver Mode**:\n"
                + "   - Swipe down settings and tap **Battery Saver / Low Power Mode** when below 30%.\n\n"
                + "💡 **The 20-80% Rule**: Try to recharge your phone when it hits 20% and unplug at 85-90%. Running battery to 0% degrades lithium cells fastest.";
    }

    private String generatePhoneLagGuide() {
        return "### ⚡ Why Is Your Phone Hanging / Lagging & How to Fix It\n\n"
                + "Phones become sluggish when RAM memory is overloaded or flash storage is above 85% capacity:\n\n"
                + "1. **Restart the Phone**:\n"
                + "   - Power off and power on after 30 seconds. This flushes temporary background memory leaks.\n"
                + "2. **Free Up Internal Storage (Must keep 15%+ empty)**:\n"
                + "   - Go to **Settings → Storage**.\n"
                + "   - If your phone has less than 10GB free space, delete downloaded movies and unused games.\n"
                + "3. **Clear App Cache for Heavy Apps**:\n"
                + "   - Go to **Settings → Apps → Manage Apps**.\n"
                + "   - Tap **Chrome, Instagram, YouTube, and Facebook** → tap **Clear Cache**.\n"
                + "4. **Turn Off System Animations (Instant Speed Boost)**:\n"
                + "   - Go to **Settings → Accessibility → Vision → Remove Animations** (or Developer Options → set Animation Scales to 0.5x).\n\n"
                + "💡 **Pro Tip**: Avoid downloading third-party \"RAM Cleaner\" or \"Antivirus Booster\" apps — they run heavy ads in the background and make phones slower!";
    }

    private String generateForceRestartGuide() {
        return "### 🔄 How to Force Restart a Frozen Phone (Black / Stuck Screen)\n\n"
                + "When your phone screen freezes completely and does not respond to touch:\n\n"
                + "#### 1. Android Phones (Samsung, OnePlus, Xiaomi, Realme, Vivo, Pixel):\n"
                + "* Press and hold the **Power Button + Volume Down Button** simultaneously for **10 to 15 seconds**.\n"
                + "* Keep holding until the screen goes black and the manufacturer logo vibrates and reappears.\n\n"
                + "#### 2. Apple iPhone (iPhone 8, X, 11, 12, 13, 14, 15, 16):\n"
                + "* Quickly press and release **Volume Up**.\n"
                + "* Quickly press and release **Volume Down**.\n"
                + "* Press and hold the **Side Power Button** for 10 seconds until the Apple logo appears on screen.\n\n"
                + "💡 **If Nothing Happens**: Plug your phone into an official wall charger for 20 minutes before repeating the button combination (a completely drained battery displays a frozen black screen).";
    }

    private String generatePhotoTransferGuide() {
        return "### 💻 How to Transfer Photos to Laptop Without Internet\n\n"
                + "You don't need Wi-Fi or mobile data to move your pictures. Here are the 2 easiest methods:\n\n"
                + "#### Method 1: Using Your Charging USB Cable (Fastest)\n"
                + "1. Connect your phone to your laptop using your USB charging cable.\n"
                + "2. On your phone screen, swipe down the notification bar.\n"
                + "3. Tap **'Charging this device via USB'** and change it to **'File Transfer'** (or **MTP**).\n"
                + "4. On your laptop, open **This PC (File Explorer)** → double click your phone's name.\n"
                + "5. Open **Internal Storage → DCIM → Camera**.\n"
                + "6. Select the photos you want, right click **Copy**, and **Paste** into your laptop's Pictures folder.\n\n"
                + "#### Method 2: Nearby Share / Quick Share for Windows\n"
                + "1. Download Google's free **Quick Share for Windows** on your PC.\n"
                + "2. On phone, select photos → tap **Share** → **Quick Share**.\n"
                + "3. Tap your laptop's name to send wirelessly at ultra-high speed via direct Wi-Fi.\n\n"
                + "💡 **Pro Tip**: Safely disconnect the USB cable after transfer by clicking the USB tray icon on Windows and choosing **'Eject'**.";
    }

    private String generateBluetoothGuide() {
        return "### 🎧 Bluetooth Not Connecting / Pairing Troubleshooting\n\n"
                + "When wireless earbuds, speakers, or car audio refuse to link:\n\n"
                + "1. **Put Audio Device in Pairing Mode**:\n"
                + "   - Most earbuds or speakers require pressing and holding the power/case button for **5 to 7 seconds** until the light flashes blue and red (or rapid white).\n"
                + "2. **Turn Off Bluetooth on Other Nearby Devices**:\n"
                + "   - If your earbuds were previously linked to your laptop or tablet, they will auto-connect to that device instead of your phone.\n"
                + "3. **Forget and Re-pair**:\n"
                + "   - On your phone: **Settings → Bluetooth** → find device name → tap gear/info icon → **Unpair / Forget**.\n"
                + "   - Toggle Bluetooth OFF, wait 5 seconds, turn it ON, and tap **Scan**.\n"
                + "4. **Reset Earbuds**:\n"
                + "   - Put both earbuds in the charging case, open the lid, and hold the case button for 10 seconds until lights blink to factory reset them.\n\n"
                + "💡 **Pro Tip**: Restart your phone. Bluetooth system cache files frequently get congested after system updates.";
    }

    private String generateWifiTroubleshootGuide() {
        return "### 📶 Wi-Fi Connected but No Internet? How to Fix It\n\n"
                + "When your phone or PC shows \"Connected, no internet\", your device is linked to the router, but the router cannot access your ISP cable.\n\n"
                + "1. **The 30-Second Router Power Cycle (Works 80% of the time)**:\n"
                + "   - Unplug the power adapter from the back of your Wi-Fi router.\n"
                + "   - Wait a full 30 seconds to let the internal capacitors discharge.\n"
                + "   - Plug it back in and wait 2 minutes until the **Internet / Globe light turns solid Green**.\n\n"
                + "2. **Toggle Airplane Mode on Phone**:\n"
                + "   - Swipe down settings → turn **Airplane Mode ON** for 10 seconds → turn it **OFF**.\n\n"
                + "3. **Forget and Reconnect Network**:\n"
                + "   - Go to **Settings → Wi-Fi** → tap your network gear icon → tap **Forget**.\n"
                + "   - Re-select the Wi-Fi network and re-enter your password.\n\n"
                + "4. **Check if Bill or Area Maintenance is Active**:\n"
                + "   - Open your ISP app (Airtel Thanks, JioFiber, ACT) to verify if an area outage is reported.";
    }

    private String generateHotspotGuide() {
        return "### 📡 How to Set Up and Share Mobile Hotspot\n\n"
                + "Share your phone's mobile 4G/5G internet with your laptop or other phones:\n\n"
                + "1. **Turn on Mobile Hotspot**:\n"
                + "   - Go to **Settings → Portable Hotspot** (or Personal Hotspot on iPhone).\n"
                + "   - Tap **Set up Portable Hotspot**.\n"
                + "2. **Configure Hotspot Details**:\n"
                + "   - **Network Name (SSID)**: Set a name you recognize.\n"
                + "   - **Security**: Choose **WPA2-Personal** (do NOT leave on None).\n"
                + "   - **Password**: Set an 8-character password you can share with friends.\n"
                + "   - **AP Band**: Select **2.4 GHz** for maximum compatibility with older laptops.\n"
                + "3. Toggle **Portable Hotspot ON**.\n"
                + "4. On your laptop: Open Wi-Fi list → select your phone's hotspot name → enter the password.\n\n"
                + "💡 **Pro Tip**: Turn on *\"Turn off hotspot automatically\"* in settings to save battery when no devices are connected.";
    }

    private String generateMobileDataGuide() {
        return "### 📶 Mobile Data / SIM Network Not Working Fix\n\n"
                + "When cellular data is turned on but websites and WhatsApp do not load:\n\n"
                + "1. **Toggle Airplane Mode**:\n"
                + "   - Turn Airplane Mode ON for 15 seconds, then turn it OFF. This forces the SIM card to re-register with the nearest cell tower.\n"
                + "2. **Check Daily Data Limit**:\n"
                + "   - Dial your carrier USSD code (e.g. Jio app, `*121#` for Airtel) to ensure your 1.5GB/day limit hasn't run out.\n"
                + "3. **Verify Preferred Network Type**:\n"
                + "   - Go to **Settings → SIM Cards & Mobile Networks** → tap your SIM → **Preferred Network Type** → choose **5G / 4G (LTE) Auto**.\n"
                + "4. **Reset APN (Access Point Names)**:\n"
                + "   - In SIM settings, tap **Access Point Names (APN)** → tap **Reset to Default**.\n\n"
                + "💡 **Pro Tip**: If problem persists, re-seat your SIM card into slot 1 using the ejector pin.";
    }

    private String generateStorageCleanupGuide() {
        return "### 💾 How to Free Up Phone Storage Safely\n\n"
                + "When internal storage is full, your phone slows down and apps crash. Follow this checklist to clear 10GB+ in minutes:\n\n"
                + "1. **Clear App Cache (No chats or logins are lost)**:\n"
                + "   - Go to **Settings → Apps → Manage Apps**.\n"
                + "   - Tap high-usage apps like **Chrome, YouTube, Instagram, and Gallery**.\n"
                + "   - Tap **Storage → Clear Cache** (do NOT tap Clear All Data).\n\n"
                + "2. **Use Google Files App**:\n"
                + "   - Download or open the official **Files by Google** app.\n"
                + "   - Tap the **Clean** tab at bottom left.\n"
                + "   - Delete \"Junk Files\", \"Duplicate Files\", and \"Old Screenshots\" in one click.\n\n"
                + "3. **Back up Photos to Google Photos**:\n"
                + "   - Open Google Photos → tap Profile icon → tap **'Free up space on this device'**.\n"
                + "   - It safely removes photos already backed up to the cloud while keeping them viewable.\n\n"
                + "4. **Delete Unused Apps and Large Offline Downloads**:\n"
                + "   - Open Netflix, YouTube, or Prime Video → check **Downloads** and delete watched movies.";
    }

    private String generateSdCardGuide() {
        return "### 🗂️ How to Use and Format Micro-SD Card Safely\n\n"
                + "Expand your phone storage and organize large files:\n\n"
                + "1. **Insert SD Card** into the SIM tray and push back into phone.\n"
                + "2. **Format SD Card** (Warning: Erases all existing data on the card):\n"
                + "   - Go to **Settings → Storage** → scroll to **SD Card**.\n"
                + "   - Tap **Format** (or Erase SD card) to set up compatible exFAT/FAT32 filesystem.\n"
                + "3. **Change Camera Save Location to SD Card**:\n"
                + "   - Open **Camera app → Settings ⚙️** → toggle **Save to SD Card (ON)**.\n"
                + "   - New photos and 4K videos will now save directly to memory card without filling phone storage.\n\n"
                + "⚠️ **Important**: Always tap **Unmount SD Card** in Settings before physically ejecting the card to prevent file corruption.";
    }

    private String generateUpiRefundGuide() {
        return "### 💳 Payment Failed but Money Deducted from Bank?\n\n"
                + "This is a very common issue called an **Unsettled Transaction**. Here is the exact status and procedure:\n\n"
                + "#### What Happened:\n"
                + "Your bank debited the funds, but the receiving bank's network timed out during peak server load. The money is safe in an escrow clearing pool.\n\n"
                + "#### Step-by-Step Resolution:\n"
                + "1. **Wait 24 to 48 Hours**: Reserve Bank of India (RBI) mandates that failed UPI transactions auto-reverse within **T+1 business days**.\n"
                + "2. **Check App Status**: Open GPay / PhonePe / Paytm → tap **Transaction History** → tap the failed payment → tap **'Check Bank Status'**.\n"
                + "3. **Do NOT Pay Again to Merchant**: If money left your bank, show the merchant the UTR / Ref Number before paying twice.\n"
                + "4. **Raise an In-App Dispute**:\n"
                + "   - Inside the transaction screen, tap **'Having an Issue?'** → select *'Money debited but not received by beneficiary'*.\n"
                + "   - This registers a dispute with the National Payments Corporation of India (NPCI).\n\n"
                + "💡 **Pro Tip**: If the amount is not refunded after 5 business days, banks are mandated by RBI to pay ₹100/day compensation.";
    }

    private String generateBankBalanceGuide() {
        return "### 🏦 How to Check Bank Balance by Missed Call & SMS\n\n"
                + "Check your account balance in 10 seconds without internet from your registered mobile number:\n\n"
                + "* **State Bank of India (SBI)**: Give a missed call to **`9223766666`** (or SMS `BAL` to `9223766666`).\n"
                + "* **HDFC Bank**: Give a missed call to **`18002703333`**.\n"
                + "* **ICICI Bank**: Give a missed call to **`9594612612`**.\n"
                + "* **Axis Bank**: Give a missed call to **`18004195959`**.\n"
                + "* **Punjab National Bank (PNB)**: Give a missed call to **`18001802223`**.\n"
                + "* **Bank of Baroda**: Give a missed call to **`8468001111`**.\n\n"
                + "#### Using UPI (Instant):\n"
                + "Open Google Pay / PhonePe / Paytm → tap **Check Bank Balance** → enter your 4 or 6-digit secret UPI PIN.\n\n"
                + "⚠️ **Safety Reminder**: Checking balance NEVER requires sharing an SMS or clicking a link sent to you!";
    }

    private String generateAtmCardGuide() {
        return "### 💳 ATM / Debit Card Management & PIN Reset\n\n"
                + "Manage your bank card safely:\n\n"
                + "1. **If Your Card is Lost / Stolen (Urgent)**:\n"
                + "   - Immediately open your mobile banking app → **Cards** → tap **Block Card Permanently**.\n"
                + "   - Or call your bank's 24-hour toll-free card blocking number (1800-xxxx-xxxx).\n"
                + "2. **How to Generate or Reset ATM PIN**:\n"
                + "   - Insert card into your bank's official ATM machine.\n"
                + "   - Select **PIN Generation / Green PIN** → enter your 10-digit registered mobile number.\n"
                + "   - An OTP will arrive on your phone. Enter OTP and choose a new 4-digit secret PIN.\n"
                + "3. **Turn OFF International / E-commerce Transactions**:\n"
                + "   - In your mobile banking app, keep *International Usage* turned OFF unless traveling abroad. This prevents foreign hacker transactions.\n\n"
                + "⚠️ **Safety Rule**: Never write your ATM PIN on the back of the card or in phone contacts.";
    }

    private String generateGmailGuide(String prompt) {
        if (prompt.contains("create") || prompt.contains("new") || prompt.contains("signup")) {
            return "### 📧 How to Create a New Gmail / Google Account\n\n"
                    + "Set up a secure, professional email account:\n\n"
                    + "1. Open your browser and go to **accounts.google.com/signup**.\n"
                    + "2. Enter your **First Name** and **Last Name**.\n"
                    + "3. Choose a unique username (e.g., `yourname.work@gmail.com`). If taken, add numbers or middle initial.\n"
                    + "4. Create a **Strong Password** (at least 8 characters with letters, numbers, and symbols like `#`, `!`).\n"
                    + "5. Enter your active mobile phone number to receive a verification SMS code.\n"
                    + "6. Add a **Recovery Email** and your Date of Birth.\n"
                    + "7. Review and accept Terms of Service.\n\n"
                    + "💡 **Pro Tip**: Write your password down in a private notebook at home so you never get locked out.";
        }

        return "### 🔑 How to Recover a Forgotten Gmail / Google Password\n\n"
                + "Recover access to your locked Gmail account safely:\n\n"
                + "1. Open **accounts.google.com/signin/recovery** in your browser.\n"
                + "2. Enter your Gmail address and click **Next**.\n"
                + "3. Click **'Try another way'** if you do not remember your last password.\n"
                + "4. Choose how to verify:\n"
                + "   - **SMS / Phone Call**: Google sends a 6-digit `G-XXXXXX` verification code to your phone.\n"
                + "   - **Recovery Email**: Google sends a reset link to your secondary email.\n"
                + "   - **Google Prompt**: Tap 'Yes, it's me' on a phone already signed in.\n"
                + "5. Once verified, create a fresh new password and click **Save Password**.\n\n"
                + "⚠️ **Safety Reminder**: Google support never calls you asking for password reset codes. Never share the `G-XXXXXX` code with anyone.";
    }

    private String generateInstagramGuide(String prompt) {
        if (prompt.contains("delete") || prompt.contains("deactivate")) {
            return "### 🛑 How to Deactivate or Delete Instagram Account\n\n"
                    + "1. Open Instagram app → tap profile icon at bottom-right.\n"
                    + "2. Tap **three lines (≡)** at top-right → **Accounts Centre**.\n"
                    + "3. Tap **Personal details** → **Account ownership and control**.\n"
                    + "4. Tap **Deactivation or deletion** → select your account.\n"
                    + "5. Choose:\n"
                    + "   - **Deactivate account**: Hides profile temporarily until you log in again.\n"
                    + "   - **Delete account**: Permanently deletes photos, chats, and followers after 30 days.\n"
                    + "6. Enter your current password and confirm.";
        }

        return "### 🔑 How to Reset Instagram Password\n\n"
                + "1. On login screen, tap **'Forgot password?'** (or 'Get help logging in').\n"
                + "2. Enter your **Username, Email address, or Phone number** linked to the account.\n"
                + "3. Tap **Next / Continue**.\n"
                + "4. Open the SMS or email sent by Instagram → tap the **Reset your password** link.\n"
                + "5. Type your new strong password twice and tap **Reset Password**.\n\n"
                + "💡 **Pro Tip**: Enable **Two-Factor Authentication** in Settings → Accounts Centre → Password and security → Two-factor authentication.";
    }

    private String generateFacebookGuide() {
        return "### 👤 Facebook Account Security & Password Reset\n\n"
                + "1. **Reset Forgotten Password**:\n"
                + "   - Visit **facebook.com/login/identify**.\n"
                + "   - Enter your phone number or email linked to Facebook.\n"
                + "   - Select how to receive the 6-digit confirmation code (SMS or Email).\n"
                + "   - Enter code and set a new password.\n"
                + "2. **Log Out of Suspicious Devices**:\n"
                + "   - Open Facebook → **Settings & Privacy → Settings → Accounts Centre → Password & Security**.\n"
                + "   - Tap **Where you're logged in** → select any device you don't recognize → tap **Log Out**.\n\n"
                + "💡 **Pro Tip**: Set your profile visibility to 'Friends only' in Privacy settings to prevent strangers from downloading your personal photos.";
    }

    private String generate2faGuide() {
        return "### 🛡️ What is Two-Factor Authentication (2FA) and How to Set It Up\n\n"
                + "**2FA adds a second lock to your digital accounts.** Even if a hacker steals your password, they cannot log in without the temporary code from your phone.\n\n"
                + "#### The 2 Types of 2FA:\n"
                + "1. **SMS OTP**: A code sent to your phone number (easy, but vulnerable to SIM-swap).\n"
                + "2. **Authenticator App (Most Secure)**: Free apps like **Google Authenticator** or **Microsoft Authenticator** generate a new 6-digit code every 30 seconds offline.\n\n"
                + "#### How to Enable 2FA on Google / Gmail:\n"
                + "1. Open **myaccount.google.com/security**.\n"
                + "2. Under 'How you sign in to Google', tap **2-Step Verification**.\n"
                + "3. Tap **Get Started** and choose your phone number or Authenticator app.\n"
                + "4. Confirm with the test code.\n\n"
                + "💡 **Pro Tip**: Always save your **Backup Codes** in a safe place at home in case you lose your phone.";
    }

    private String generateAadhaarGuide() {
        return "### 🇮🇳 How to Download e-Aadhaar from Official UIDAI Portal\n\n"
                + "Download your official, legally valid digital Aadhaar card in PDF format:\n\n"
                + "1. Open the official UIDAI website: **myaadhaar.uidai.gov.in**.\n"
                + "2. Click **Download Aadhaar**.\n"
                + "3. Enter your **12-digit Aadhaar Number** (or 28-digit Enrolment ID) and the Captcha code.\n"
                + "4. Click **Send OTP** → enter the 6-digit OTP received on your Aadhaar-registered mobile number.\n"
                + "5. **Masked Aadhaar Option**: Check the box *'Do you want a masked Aadhaar?'* if using for hotel/travel check-in (shows only last 4 digits `XXXX-XXXX-1234` for privacy).\n"
                + "6. Click **Verify & Download**.\n\n"
                + "🔐 **How to Open the PDF Password**:\n"
                + "The password is **FIRST 4 LETTERS OF YOUR NAME IN CAPITAL** + **4 DIGIT YEAR OF BIRTH**.\n"
                + "*Example*: If your name is SURESH KUMAR born in 1985, password is `SURE1985`.";
    }

    private String generatePanAadhaarGuide() {
        return "### 📋 How to Link PAN Card with Aadhaar Card Online\n\n"
                + "Check linking status and complete the mandatory government linking:\n\n"
                + "1. Visit the Income Tax official e-Filing portal: **incometax.gov.in**.\n"
                + "2. On the left side under 'Quick Links', click **Link Aadhaar Status** first:\n"
                + "   - Enter your PAN and Aadhaar number to verify if already linked.\n"
                + "3. If not linked, click **Link Aadhaar**:\n"
                + "   - Enter your PAN and Aadhaar number.\n"
                + "   - Enter your name exactly as printed on the Aadhaar card.\n"
                + "   - Enter your mobile number to receive OTP.\n"
                + "4. Pay the government fee (Minor Head 500 under Challan No./ITNS 280) if required.\n"
                + "5. Submit the OTP and note down the Request Reference Number.\n\n"
                + "⚠️ **Important**: Ensure your Date of Birth and Name spelling match exactly on both PAN and Aadhaar records.";
    }

    private String generateDigilockerGuide() {
        return "### 📂 How to Use DigiLocker for Official Government Documents\n\n"
                + "DigiLocker documents (Driving License, Vehicle RC, Marksheets, Insurance) are 100% legally recognized by Indian Traffic Police and government authorities under IT Act 2000:\n\n"
                + "1. Download the official **DigiLocker app** from Google Play Store or visit **digilocker.gov.in**.\n"
                + "2. Tap **Sign Up** → enter your full name, date of birth, mobile number, and set a 6-digit security PIN.\n"
                + "3. Enter your **Aadhaar Number** to verify identity with OTP.\n"
                + "4. **Fetch Documents**:\n"
                + "   - Tap **Search** → choose **Ministry of Road Transport** → enter Driving License Number.\n"
                + "   - Tap **Get Document**. It saves directly into your **Issued Documents** tab with a verifiable QR code.\n\n"
                + "💡 **Pro Tip**: You do not need to carry physical plastic DL or RC cards anywhere in India if shown from DigiLocker.";
    }

    private String generateQrScanGuide() {
        return "### 🔍 How to Scan QR Codes on Android & iPhone\n\n"
                + "Scan restaurant menus, UPI payment codes, and Wi-Fi QR cards easily:\n\n"
                + "#### 1. Android Smartphone (2 Simple Methods):\n"
                + "* **Method A (Camera App)**: Open your normal phone Camera app and point it at the QR code for 2 seconds. A yellow web link or banner will pop up on screen.\n"
                + "* **Method B (Google Lens)**: Tap the **Google Lens icon** in the Google search bar on your home screen → point at the code.\n\n"
                + "#### 2. Apple iPhone / iPad:\n"
                + "* Open the built-in **Camera app** → hold over the QR code → tap the yellow link banner.\n\n"
                + "#### 3. For UPI Payment QR Codes:\n"
                + "* Open **Google Pay, PhonePe, or Paytm** → tap the **Scan QR icon** in top bar.\n\n"
                + "⚠️ **Safety Warning**: Never scan unknown QR codes in car parks or text messages that claim to \"send you lottery money\" — scanning a QR code is only for paying or opening a website!";
    }

    private String generatePdfGuide(String prompt) {
        if (prompt.contains("compress") || prompt.contains("size") || prompt.contains("reduce") || prompt.contains("mb")) {
            return "### 🗜️ How to Compress PDF File Size for Online Form Uploads\n\n"
                    + "Online government forms often reject PDFs larger than 200KB or 2MB. Here is how to reduce size safely:\n\n"
                    + "1. Open your browser and search for **free PDF compressor** (e.g. Adobe Acrobat Online Free Compressor or iLovePDF).\n"
                    + "2. Click **Select PDF file** and upload your document.\n"
                    + "3. Choose **Medium Compression** (preserves clear text while shrinking size by 60-80%).\n"
                    + "4. Click **Compress** and download the resulting file.\n"
                    + "5. Open the downloaded file to verify text and signature are still sharp and readable.\n\n"
                    + "💡 **Pro Tip**: Never upload highly confidential legal documents with sensitive bank passwords to unknown foreign websites.";
        }

        return "### 📄 How to Scan Documents to PDF Using Your Phone Camera\n\n"
                + "Convert paper certificates, marksheets, and receipts into clean PDFs without a scanner machine:\n\n"
                + "#### Method 1: Using Google Drive (Built-in on all Androids)\n"
                + "1. Open the **Google Drive app** on your phone.\n"
                + "2. Tap the **+** (Plus) button at bottom-right → tap **Scan (Camera icon)**.\n"
                + "3. Snap a clear photo of your paper document in good lighting.\n"
                + "4. Tap the **Crop icon** to adjust the 4 corners.\n"
                + "5. Tap **+** to add more pages to the same PDF, or tap **Save**.\n\n"
                + "#### Method 2: iPhone Notes App\n"
                + "1. Open **Notes** → start new note → tap Camera icon → **Scan Documents**.\n"
                + "2. Position paper in frame → tap **Save**.\n\n"
                + "💡 **Pro Tip**: Place paper on a dark, flat table so the camera detects page borders automatically.";
    }

    private String generateYoutubeGuide(String prompt) {
        if (prompt.contains("history") || prompt.contains("clear") || prompt.contains("delete")) {
            return "### 🗑️ How to Clear YouTube Search and Watch History\n\n"
                    + "1. Open the YouTube app → tap your **Profile icon** at bottom-right.\n"
                    + "2. Tap **Settings ⚙️** at top-right.\n"
                    + "3. Tap **Manage all history**.\n"
                    + "4. Tap the **DELETE** button → choose **Delete today** or **Delete all time**.\n"
                    + "5. To stop YouTube from saving history entirely, tap **Saving your YouTube history** and toggle **Turn Off**.";
        }

        return "### 📺 How to Fix YouTube Buffering & Save Videos Offline\n\n"
                + "#### 1. Stop YouTube Buffering & Slow Loading:\n"
                + "* **Lower Video Resolution**: Tap the gear icon ⚙️ on the video → tap **Quality** → select **480p or 720p** instead of 1080p/4K.\n"
                + "* **Clear YouTube App Cache**: Settings → Apps → YouTube → Storage → Clear Cache.\n\n"
                + "#### 2. How to Save Videos Offline for Travel (No Internet Needed):\n"
                + "* Under the playing video, tap the **Download button ⬇️**.\n"
                + "* Choose download quality (High or Medium).\n"
                + "* Find your saved videos anytime in the **You (Library) tab → Downloads**.";
    }

    private String generateBrowserGuide(String prompt) {
        return "### 🌐 How to Clear Browser History & Cache in Google Chrome\n\n"
                + "Fix slow page loads, expired website cookies, and clear search history:\n\n"
                + "#### On Computer (Windows / Mac):\n"
                + "1. Open Chrome and press **Ctrl + Shift + Delete** (or `Cmd + Shift + Delete` on Mac).\n"
                + "2. In the popup, set Time Range to **All time**.\n"
                + "3. Check boxes for:\n"
                + "   - **Browsing history**\n"
                + "   - **Cookies and other site data**\n"
                + "   - **Cached images and files**\n"
                + "4. Click **Clear data**.\n\n"
                + "#### On Android Phone or iPhone:\n"
                + "1. Open Chrome app → tap **three dots (⋮)** → **History**.\n"
                + "2. Tap **Clear browsing data**.\n"
                + "3. Select **All time** and tap **Clear data**.\n\n"
                + "💡 **What is Incognito Mode?** Press `Ctrl + Shift + N` to open a private window that leaves zero browsing history or cookies when closed.";
    }

    private String generateUninstallAppGuide() {
        return "### 🗑️ How to Completely Uninstall Unwanted Apps\n\n"
                + "Remove unused apps and free up phone storage:\n\n"
                + "#### 1. Android Phones:\n"
                + "* **Quick Method**: Long-press the app icon on your home screen or app drawer → tap **Uninstall (Trash icon)** → confirm OK.\n"
                + "* **Deep Method (For system bloatware)**:\n"
                + "  1. Go to **Settings → Apps → Manage Apps**.\n"
                + "  2. Tap the specific app.\n"
                + "  3. Tap **Clear Data → Clear All Data** first, then tap **Uninstall** (or **Disable** if pre-installed by carrier).\n\n"
                + "#### 2. Windows PC / Laptop:\n"
                + "* Press **Windows Key + I** to open Settings → **Apps → Installed Apps**.\n"
                + "* Find the application → click the **three dots (...)** → click **Uninstall**.\n\n"
                + "💡 **Pro Tip**: After uninstalling heavy games, restart your device to reclaim temporary cache blocks.";
    }

    private String generateLostPhoneGuide() {
        return "### 📍 How to Track and Secure a Lost or Stolen Phone\n\n"
                + "Act quickly to locate your device or lock your confidential data:\n\n"
                + "#### 1. For Android Phones (Google Find My Device):\n"
                + "1. From another phone or computer, open **google.com/android/find**.\n"
                + "2. Sign in with the same **Gmail account** used on the lost phone.\n"
                + "3. You will see the phone's live location on Google Maps.\n"
                + "4. Choose an action:\n"
                + "   - **Play Sound**: Rings at maximum volume for 5 minutes (even if set to silent).\n"
                + "   - **Secure Device**: Locks phone with a PIN and displays an emergency phone number on screen for finder to call.\n"
                + "   - **Erase Device**: Permanently wipes all personal photos and logins if stolen.\n\n"
                + "#### 2. For Apple iPhone (Apple Find My):\n"
                + "1. Visit **icloud.com/find** from any browser.\n"
                + "2. Sign in with your **Apple ID** → tap your iPhone.\n"
                + "3. Toggle **Lost Mode** to lock the phone and track its movements.\n\n"
                + "📞 **Report to Police / CEIR**: In India, visit **ceir.gov.in** (Central Equipment Identity Register) to block the phone's IMEI number across all telecom networks.";
    }

    // --- Dynamic Synthesizer for Any Continuous Question ---

    private String generateDynamicSynthesizer(String prompt, String language) {
        String p = prompt.trim();
        String pLower = p.toLowerCase();

        // Detect action category
        String actionType = "guide";
        if (pLower.contains("install") || pLower.contains("download") || pLower.contains("setup") || pLower.contains("create") || pLower.contains("make")) {
            actionType = "setup";
        } else if (pLower.contains("delete") || pLower.contains("remove") || pLower.contains("uninstall") || pLower.contains("clear") || pLower.contains("clean")) {
            actionType = "removal";
        } else if (pLower.contains("fix") || pLower.contains("repair") || pLower.contains("solve") || pLower.contains("not working") || pLower.contains("error") || pLower.contains("problem")) {
            actionType = "troubleshoot";
        } else if (pLower.contains("connect") || pLower.contains("pair") || pLower.contains("link") || pLower.contains("share")) {
            actionType = "connectivity";
        } else if (pLower.contains("speed up") || pLower.contains("slow") || pLower.contains("faster") || pLower.contains("optimize")) {
            actionType = "optimize";
        } else if (pLower.contains("recover") || pLower.contains("restore") || pLower.contains("forgot") || pLower.contains("find")) {
            actionType = "recovery";
        } else if (pLower.contains("password") || pLower.contains("protect") || pLower.contains("security") || pLower.contains("lock")) {
            actionType = "security";
        }

        // Detect target platform
        String platform = "your device";
        if (pLower.contains("windows") || pLower.contains("laptop") || pLower.contains("pc") || pLower.contains("computer")) {
            platform = "Windows Computer";
        } else if (pLower.contains("android") || pLower.contains("samsung") || pLower.contains("oneplus") || pLower.contains("redmi")) {
            platform = "Android Phone";
        } else if (pLower.contains("iphone") || pLower.contains("ios") || pLower.contains("ipad") || pLower.contains("apple")) {
            platform = "Apple iPhone";
        } else if (pLower.contains("mac") || pLower.contains("macbook")) {
            platform = "Apple Mac";
        } else if (pLower.contains("tv") || pLower.contains("smart tv")) {
            platform = "Smart TV";
        }

        // Clean subject name
        String subject = p.replaceAll("(?i)^(how to|how do i|why is|what is|can i|explain|fix|solve|please tell me|where is|steps to)\\s+", "").trim();
        if (subject.isEmpty()) subject = "Your Request";
        String titleSubject = capitalize(subject);

        // Native Tamil Response
        if ("ta".equalsIgnoreCase(language) || isTamilScript(p)) {
            return "### 💡 " + titleSubject + " பற்றிய படிப்படியான வழிகாட்டுதல்\n\n"
                    + "உங்கள் " + platform + " சாதனத்தில் இதை எளிதாக செய்ய பின்வரும் படிகளைப் பின்பற்றவும்:\n\n"
                    + "1. **அமைப்புகள் (Settings) திறக்கவும்**: உங்கள் சாதனத்தில் முதன்மை அமைப்புகள் பக்கத்திற்குச் செல்லவும்.\n"
                    + "2. **சம்பந்தப்பட்ட பகுதியைத் தேர்ந்தெடுக்கவும்**: நீங்கள் மாற்ற விரும்பும் விருப்பத்தை (Network, Apps, அல்லது Account) தேர்ந்தெடுக்கவும்.\n"
                    + "3. **வழிகாட்டப்பட்ட மாற்றத்தைச் செய்யவும்**: திரையில் தோன்றும் வழிகாட்டுதல்களைப் படித்து உறுதிப்படுத்தவும்.\n"
                    + "4. **சாதனத்தை மறுதொடக்கம் (Restart) செய்யவும்**: மாற்றங்கள் முழுமையாக செயல்பட சாதனத்தை ஒருமுறை ஆஃப் செய்து ஆன் செய்யவும்.\n\n"
                    + "💡 **சிறப்பு குறிப்பு (Pro Tip)**: எப்போதுமே அதிகாரப்பூர்வ பயன்பாடுகளை மட்டுமே பயன்படுத்தவும். அறிமுகமில்லாத இணைப்புகளைத் திறக்காதீர்கள்.\n\n"
                    + "⚠️ **பாதுகாப்பு எச்சரிக்கை**: வங்கி ரகசிய குறியீடுகள் (OTP, PIN) அல்லது கடவுச்சொற்களை யாரிடமும் பகிர வேண்டாம்.";
        }

        // Native Hindi Response
        if ("hi".equalsIgnoreCase(language) || isHindiScript(p)) {
            return "### 💡 " + titleSubject + " के लिए आसान चरणबद्ध समाधान\n\n"
                    + "अपने " + platform + " पर इसे आसानी से करने के लिए इन चरणों का पालन करें:\n\n"
                    + "1. **सेटिंग्स (Settings) खोलें**: अपने डिवाइस की मुख्य सेटिंग्स में जाएं।\n"
                    + "2. **सही विकल्प चुनें**: संबंधित मेनू (नेटवर्क, ऐप्स या अकाउंट) पर टैप करें।\n"
                    + "3. **निर्देशों का पालन करें**: स्क्रीन पर दिए गए निर्देशों को ध्यान से पढ़कर पुष्टि करें।\n"
                    + "4. **डिवाइस रीस्टार्ट करें**: सेटिंग्स को सही ढंग से लागू करने के लिए डिवाइस को एक बार रीस्टार्ट करें।\n\n"
                    + "💡 **महत्वपूर्ण टिप (Pro Tip)**: हमेशा आधिकारिक ऐप स्टोर से ही ऐप्स डाउनलोड करें।\n\n"
                    + "⚠️ **सुरक्षा चेतावनी**: किसी भी स्थिति में अपना ओटीपी (OTP) या बैंक पिन किसी अनजान व्यक्ति को न दें।";
        }

        // Tailored English Response based on Action Type
        StringBuilder sb = new StringBuilder();
        sb.append("### 💡 Step-by-Step Guidance: ").append(titleSubject).append("\n\n");
        sb.append("Here is the clear, verified procedure for **").append(platform).append("**:\n\n");

        if ("troubleshoot".equals(actionType)) {
            sb.append("#### 🛠️ Troubleshooting Steps:\n")
              .append("1. **Check Basic Connectivity & Battery**: Ensure your device has at least 20% battery and a stable connection.\n")
              .append("2. **Perform a Clean Restart**: Power off your ").append(platform).append(", wait 20 seconds, and turn it back on to clear stuck memory tasks.\n")
              .append("3. **Inspect Application & System Permissions**: Go to **Settings ⚙️** → search for the specific service or app → verify that required permissions are allowed.\n")
              .append("4. **Clear Temporary Cache**: In Settings → Apps, locate the relevant tool and tap **Clear Cache**.\n")
              .append("5. **Check for Recent Updates**: Install any pending system or app updates from the official app store.\n\n")
              .append("💡 **Pro Tip**: If an error code or popup dialog appears, note down the exact wording or take a screenshot to identify the exact cause.\n\n");
        } else if ("setup".equals(actionType)) {
            sb.append("#### 🚀 Setup & Installation Steps:\n")
              .append("1. **Open the Verified Source**: Always use official channels (Google Play Store, Apple App Store, or official verified website).\n")
              .append("2. **Locate & Download**: Search for the official application, verify the developer name, and click **Install / Download**.\n")
              .append("3. **Follow the Setup Wizard**: Launch the app and grant only necessary permissions (e.g. storage or camera only when required).\n")
              .append("4. **Complete Verification**: If account sign-in is required, enter your verified mobile number or email and verify with SMS code.\n\n")
              .append("💡 **Pro Tip**: Avoid installing unverified third-party APK files from the web; they frequently contain hidden malware.\n\n");
        } else if ("removal".equals(actionType)) {
            sb.append("#### 🗑️ Clean Removal Steps:\n")
              .append("1. **Close Active Tasks**: Close the tool or file completely before attempting removal.\n")
              .append("2. **Locate in Settings**: Open **Settings ⚙️** → **Apps / Installed Applications**.\n")
              .append("3. **Clear Data Prior to Removal**: Tap **Clear Cache & Data** first to ensure no orphan clutter remains on storage.\n")
              .append("4. **Uninstall / Remove**: Tap **Uninstall** (or Delete) and confirm the prompt.\n\n")
              .append("💡 **Pro Tip**: Restart your device after uninstalling to let the storage subsystem reclaim free space.\n\n");
        } else if ("recovery".equals(actionType)) {
            sb.append("#### 🔍 Recovery & Retrieval Steps:\n")
              .append("1. **Check Trash / Recycle Bin First**: Most modern systems keep deleted files or photos in a 'Recently Deleted' folder for 30 days.\n")
              .append("2. **Use Official Account Recovery**: If recovering an account, navigate to the official recovery URL (e.g. Google Account Recovery).\n")
              .append("3. **Verify via Secondary Device**: Approve the sign-in prompt sent to your registered phone or recovery email address.\n")
              .append("4. **Set a Fresh Strong Password**: Once access is restored, update your credentials and save them in a secure place.\n\n")
              .append("💡 **Pro Tip**: Write down your recovery keys or emergency backup codes offline in a physical notebook.\n\n");
        } else if ("connectivity".equals(actionType)) {
            sb.append("#### 🔗 Connection & Pairing Steps:\n")
              .append("1. **Enable Discovery Mode**: Ensure both devices are powered on and placed within 5 meters of each other.\n")
              .append("2. **Open Network / Connection Settings**: On your ").append(platform).append(", open **Settings ⚙️ → Connections**.\n")
              .append("3. **Search for Available Devices**: Tap Scan or Add Device and select the target name from the list.\n")
              .append("4. **Confirm the Pairing Code**: If a numeric passcode appears on both screens, verify they match before pressing Pair.\n\n")
              .append("💡 **Pro Tip**: If connection drops, toggle Airplane Mode or Bluetooth OFF and ON to refresh the radio antenna.\n\n");
        } else {
            sb.append("#### 📋 Recommended Action Steps:\n")
              .append("1. **Open Settings**: Navigate to **Settings ⚙️** on your ").append(platform).append(".\n")
              .append("2. **Search for the Feature**: Use the search magnifying glass at the top of Settings to type '").append(titleSubject).append("'.\n")
              .append("3. **Review Current Configuration**: Inspect the existing toggles and options before modifying them.\n")
              .append("4. **Save and Apply**: Confirm the desired setting and test the change to verify proper operation.\n\n")
              .append("💡 **Pro Tip**: You can ask me follow-up questions anytime (e.g., *\"Can you explain step 2?\"* or *\"How do I do this on another device?\"*).\n\n");
        }

        sb.append("⚠️ **Safety Reminder**: Never disclose OTPs, banking PINs, or confidential identity details to anyone offering remote technical support.");

        return sb.toString();
    }

    private boolean isTamilScript(String text) {
        if (text == null) return false;
        for (char ch : text.toCharArray()) {
            if (ch >= 0x0B80 && ch <= 0x0BFF) return true;
        }
        return false;
    }

    private boolean isHindiScript(String text) {
        if (text == null) return false;
        for (char ch : text.toCharArray()) {
            if (ch >= 0x0900 && ch <= 0x097F) return true;
        }
        return false;
    }

    private String capitalize(String text) {
        if (text == null || text.isEmpty()) return "Your Request";
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    private List<String> generateFollowUps(String prompt) {
        String p = prompt.toLowerCase();
        if (p.contains("otp") || p.contains("password")) {
            return List.of(
                    "What should I do if I mistakenly shared an OTP?",
                    "How to lock my bank card instantly?",
                    "What is 2-factor authentication (2FA)?"
            );
        }
        if (p.contains("whatsapp") || p.contains("storage")) {
            return List.of(
                    "How to backup WhatsApp chats to Google Drive?",
                    "How to stop photos from saving to gallery automatically?",
                    "How to move WhatsApp to an SD card?"
            );
        }
        if (p.contains("heat") || p.contains("battery") || p.contains("charging")) {
            return List.of(
                    "Why is my phone battery draining fast?",
                    "Is it safe to charge phone overnight?",
                    "How to check battery health?"
            );
        }
        if (p.contains("upi") || p.contains("payment") || p.contains("money")) {
            return List.of(
                    "How long does an unsettled UPI refund take?",
                    "How to change my UPI PIN in GPay / PhonePe?",
                    "What to do if money sent to wrong UPI number?"
            );
        }
        return List.of(
                "Can you explain this step in more detail?",
                "How to do this on iPhone instead?",
                "What safety precautions should I take?"
        );
    }

    private String detectCategory(String prompt) {
        String p = prompt.toLowerCase();
        if (p.contains("bank") || p.contains("upi") || p.contains("money") || p.contains("otp") || p.contains("card")) {
            return "Banking & Payments";
        }
        if (p.contains("whatsapp") || p.contains("facebook") || p.contains("instagram") || p.contains("chat")) {
            return "WhatsApp & Social";
        }
        if (p.contains("phone") || p.contains("battery") || p.contains("storage") || p.contains("laptop") || p.contains("wifi")) {
            return "Device Troubleshooting";
        }
        if (p.contains("passport") || p.contains("aadhaar") || p.contains("pan") || p.contains("form")) {
            return "Official Forms & IDs";
        }
        return "General Tech Help";
    }

    private ExplainImageResponse generateSmartVisionExplanation(String note, boolean sensitiveDetected) {
        String n = note.toLowerCase();
        String errorTitle = "Identified Screen Notice / Error Message";
        String explanation = "We analyzed your screen image. The application encountered an unexpected state, network disruption, or session timeout.";
        List<String> causes = new ArrayList<>(List.of(
                "Temporary internet connectivity interruption or DNS timeout",
                "Outdated temporary app cache or cookies needing a refresh",
                "Server busy or undergoing scheduled maintenance"
        ));
        List<String> fixes = new ArrayList<>(List.of(
                "1. Check your Wi-Fi or mobile data signal.",
                "2. Force close the application and reopen it.",
                "3. If in a web browser, perform a hard refresh (Ctrl + F5 on PC, or reload icon).",
                "4. Check for pending updates in the Play Store / App Store."
        ));

        if (n.contains("upi") || n.contains("payment") || n.contains("transaction") || n.contains("failed")) {
            errorTitle = "Payment / UPI Transaction Alert";
            explanation = "The payment could not be processed immediately by your bank or the merchant gateway.";
            causes = List.of(
                    "Bank server temporary peak load or response timeout",
                    "Incorrect UPI PIN entered or daily bank transaction limit reached",
                    "Poor mobile network during the secure payment handshake"
            );
            fixes = List.of(
                    "1. Check your bank account balance to see if money was debited.",
                    "2. If money was debited, DO NOT pay again immediately. The banking system auto-reverses failed amounts within 24 to 48 hours.",
                    "3. If money was not debited, wait 10 minutes and try again with strong internet."
            );
        } else if (n.contains("wifi") || n.contains("connect") || n.contains("internet") || n.contains("no internet")) {
            errorTitle = "Network Connection Disconnected";
            explanation = "Your device is connected to the Wi-Fi signal, but the router does not have active internet access from the service provider.";
            causes = List.of(
                    "Wi-Fi router modem needs a reboot",
                    "ISP cable disconnected or temporary area maintenance",
                    "Device IP address conflict"
            );
            fixes = List.of(
                    "1. Turn off your Wi-Fi router power plug, wait 30 seconds, and turn it back on.",
                    "2. Toggle Airplane mode ON and OFF on your phone.",
                    "3. Check if other family members are able to use the same Wi-Fi."
            );
        }

        String warning = sensitiveDetected
                ? "⚠️ Warning: Sensitive personal numbers or identifiers may be visible in this screenshot. Always blur out passwords, OTPs, and card numbers before sharing screenshots."
                : "Remember: Keep passwords, account numbers, and OTPs hidden when taking screenshots.";

        return new ExplainImageResponse(errorTitle, explanation, causes, fixes, warning, sensitiveDetected);
    }

    private TranslateResponse generateNativeLanguageTranslation(String text, String targetLang, String mode) {
        TranslateResponse resp = new TranslateResponse();
        resp.setOriginalText(text);
        resp.setTargetLanguage(targetLang);
        resp.setTargetLanguageName(getLanguageName(targetLang));

        Map<String, String> terms = new LinkedHashMap<>();
        terms.put("KYC (Know Your Customer)", "வாடிக்கையாளர் சரிபார்ப்பு / உங்கள் அடையாளத்தை உறுதி செய்யும் செயல்முறை (Proof of Identity & Address verification).");
        terms.put("Phishing (மோசடி)", "போலி இணையதளம் அல்லது குறுஞ்செய்தி மூலம் உங்கள் கடவுச்சொல்லை திருடும் மோசடி.");
        terms.put("Cache (தற்காலிக நினைவகம்)", "ஆப்ஸ் வேகமாக இயங்க சேமிக்கப்படும் தற்காலிக கோப்புகள். இதை நீக்கினால் போன் இடம் மிச்சமாகும்.");
        terms.put("2FA (Two Factor Authentication)", "கடவுச்சொல்லுடன் மொபைல் OTP இணைத்து கணக்கைப் பாதுகாக்கும் இரட்டை அடுக்கு பாதுகாப்பு.");
        resp.setDifficultTermsExplained(terms);

        String translated;
        String simplified;

        switch (targetLang) {
            case "ta": // Tamil
                translated = "தொழில்நுட்ப உதவி: " + text + " — உங்கள் சாதனத்தை பாதுகாப்பாக பயன்படுத்த, ஆப் அமைப்புகளில் தேவையான மாற்றங்களைச் செய்து, கடவுச்சொற்களை யாரிடமும் பகிராதீர்கள்.";
                simplified = "எளிய விளக்கம் (தமிழ்): தேவையில்லாத கோப்புகளை நீக்கி, புதிய பதிப்பிற்கு அப்டேட் செய்தால் உங்கள் பிரச்சனை சரியாகிவிடும். ஓடிபி (OTP) யாரிடமும் கூறாதீர்கள்.";
                break;
            case "hi": // Hindi
                translated = "डिजिटल सहायता: " + text + " — अपने डिवाइस को सुरक्षित रखने के लिए सेटिंग्स की जांच करें और किसी के साथ पासवर्ड या ओटीपी साझा न करें।";
                simplified = "सरल भाषा (हिंदी): ऐप को अपडेट करें और फोन को एक बार रीस्टार्ट करें। किसी भी अनजान व्यक्ति को अपना बैंक ओटीपी या पिन कभी न बताएं।";
                break;
            case "te": // Telugu
                translated = "డిజిటల్ సహాయం: " + text + " — మీ పరికరాన్ని సురక్షితంగా ఉంచడానికి సెట్టింగ్‌లను తనిఖీ చేయండి మరియు పాస్‌వర్డ్ లేదా OTP ఎవరితోనూ పంచుకోవద్దు.";
                simplified = "సులభమైన వివరణ (తెలుగు): యాప్‌ను అప్‌డేట్ చేసి, ఫోన్‌ను రీస్టార్ట్ చేయండి. మీ రహస్య పిన్ లేదా ఓటీపీని ఎవరికీ చెప్పవద్దు.";
                break;
            case "kn": // Kannada
                translated = "ಡಿಜಿಟಲ್ ಸಹಾಯ: " + text + " — ನಿಮ್ಮ ಸಾಧನವನ್ನು ಸುರಕ್ಷಿತವಾಗಿರಿಸಲು ಸೆಟ್ಟಿಂಗ್‌ಗಳನ್ನು ಪರಿಶೀಲಿಸಿ ಮತ್ತು ಪಾಸ್‌ವರ್ಡ್ ಅಥವಾ OTP ಹಂಚಿಕೊಳ್ಳಬೇಡಿ.";
                simplified = "ಸರಳ ವಿವರಣೆ (ಕನ್ನಡ): ಅಪ್ಲಿಕೇಶನ್ ಅನ್ನು ನವೀಕರಿಸಿ ಮತ್ತು ಫೋನ್ ಅನ್ನು ಮರುಪ್ರಾರಂಭಿಸಿ. ಯಾರಿಗೂ ಒಟಿಪಿ ನೀಡಬೇಡಿ.";
                break;
            case "ml": // Malayalam
                translated = "ഡിജിറ്റൽ സഹായം: " + text + " — നിങ്ങളുടെ ഉപകരണം സുരക്ഷിതമായി സൂക്ഷിക്കുക. പാസ്‌വേഡോ ഒടിപിയോ ആരുമായും പങ്കിടരുത്.";
                simplified = "ലളിതമായ വിവരണം (മലയാളം): ആപ്പ് അപ്‌ഡേറ്റ് ചെയ്‌ത് ഫോൺ പുനരാരംഭിക്കുക. അപരിചിതർക്ക് ഒടിപി നൽകരുത്.";
                break;
            case "bn": // Bengali
                translated = "ডিজিটাল সহায়তা: " + text + " — আপনার ডিভাইস সুরক্ষিত রাখতে সেটিংস পরীক্ষা করুন এবং পাসওয়ার্ড বা ওটিপি শেয়ার করবেন না।";
                simplified = "সহজ ব্যাখ্যা (বাংলা): অ্যাপটি আপডেট করুন এবং ফোন রিস্টার্ট করুন। কারো সাথে ওটিপি শেয়ার করবেন না।";
                break;
            default:
                translated = "English Translation / Simplified: " + text;
                simplified = "Plain English: Keep apps updated, clear old cache files, restart the device, and never share OTPs or passwords with anyone.";
                break;
        }

        resp.setTranslatedText(translated);
        resp.setSimplifiedText(simplified);
        return resp;
    }

    private String getLanguageName(String code) {
        return switch (code.toLowerCase()) {
            case "ta" -> "Tamil (தமிழ்)";
            case "hi" -> "Hindi (हिंदी)";
            case "te" -> "Telugu (తెలుగు)";
            case "kn" -> "Kannada (ಕನ್ನಡ)";
            case "ml" -> "Malayalam (മലയാളം)";
            case "bn" -> "Bengali (বাংলা)";
            case "mr" -> "Marathi (मराठी)";
            case "gu" -> "Gujarati (ગુજરાતી)";
            default -> "English";
        };
    }

    private boolean checkSensitiveDataPattern(String text) {
        if (text == null) return false;
        String t = text.toLowerCase();
        return t.contains("otp") || t.contains("cvv") || t.contains("password") || t.contains("pin")
                || Pattern.compile("\\b\\d{4}[ -]?\\d{4}[ -]?\\d{4}[ -]?\\d{4}\\b").matcher(t).find()
                || Pattern.compile("\\b\\d{4}[ -]?\\d{4}[ -]?\\d{4}\\b").matcher(t).find();
    }
}
