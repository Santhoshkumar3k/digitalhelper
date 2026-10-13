package com.digitalhelper.service;

import com.digitalhelper.dto.*;
import com.digitalhelper.exception.ResourceNotFoundException;
import com.digitalhelper.model.Conversation;
import com.digitalhelper.model.Message;
import com.digitalhelper.model.User;
import com.digitalhelper.repository.ConversationRepository;
import com.digitalhelper.repository.MessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AssistantService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserService userService;
    private final AiService aiService;

    public AssistantService(ConversationRepository conversationRepository,
                            MessageRepository messageRepository,
                            UserService userService,
                            AiService aiService) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userService = userService;
        this.aiService = aiService;
    }

    @Transactional
    public AskResponse ask(String userEmail, AskRequest request) {
        String clientContext = request.getPreviousContext() != null ? request.getPreviousContext() : "";

        if (userEmail == null || userEmail.isBlank()) {
            // Guest mode: generate answer using previousContext
            AskResponse aiResponse = aiService.ask(request, clientContext);
            return aiResponse;
        }

        User user = userService.getUserByEmail(userEmail);

        Conversation conversation;
        if (request.getConversationId() != null) {
            conversation = conversationRepository.findByIdAndUserId(request.getConversationId(), user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
        } else {
            String title = request.getPrompt().length() > 60
                    ? request.getPrompt().substring(0, 57) + "..."
                    : request.getPrompt();
            conversation = new Conversation(user, title, "General Tech Help");
            conversation = conversationRepository.save(conversation);
        }

        // Build context from previous messages
        List<Message> history = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversation.getId());
        StringBuilder historyBuilder = new StringBuilder();
        if (!clientContext.isBlank()) {
            historyBuilder.append(clientContext).append("\n");
        }
        int startIndex = Math.max(0, history.size() - 6); // Last 6 messages for context
        for (int i = startIndex; i < history.size(); i++) {
            Message m = history.get(i);
            historyBuilder.append(m.getRole().toUpperCase()).append(": ").append(m.getContent()).append("\n");
        }

        // Save user message
        Message userMessage = new Message(conversation, "user", request.getPrompt());
        messageRepository.save(userMessage);

        // Get AI response
        AskResponse aiResponse = aiService.ask(request, historyBuilder.toString());

        // Update category if newly classified
        if (aiResponse.getCategory() != null) {
            conversation.setCategory(aiResponse.getCategory());
        }

        // Save assistant message
        Message assistantMessage = new Message(conversation, "assistant", aiResponse.getAnswer());
        assistantMessage = messageRepository.save(assistantMessage);

        conversation.setUpdatedAt(java.time.LocalDateTime.now());
        conversationRepository.save(conversation);

        aiResponse.setConversationId(conversation.getId());
        aiResponse.setMessageId(assistantMessage.getId());

        return aiResponse;
    }

    public List<Map<String, Object>> getConversations(String userEmail) {
        User user = userService.getUserByEmail(userEmail);
        List<Conversation> list = conversationRepository.findByUserIdOrderByUpdatedAtDesc(user.getId());

        return list.stream().map(c -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId());
            map.put("title", c.getTitle());
            map.put("category", c.getCategory());
            map.put("createdAt", c.getCreatedAt());
            map.put("updatedAt", c.getUpdatedAt());
            map.put("messageCount", c.getMessages() != null ? c.getMessages().size() : 0);
            return map;
        }).collect(Collectors.toList());
    }

    public Map<String, Object> getConversation(String userEmail, Long id) {
        User user = userService.getUserByEmail(userEmail);
        Conversation conv = conversationRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(conv.getId());

        Map<String, Object> map = new HashMap<>();
        map.put("id", conv.getId());
        map.put("title", conv.getTitle());
        map.put("category", conv.getCategory());
        map.put("createdAt", conv.getCreatedAt());
        map.put("updatedAt", conv.getUpdatedAt());
        map.put("messages", messages.stream().map(m -> {
            Map<String, Object> msgMap = new HashMap<>();
            msgMap.put("id", m.getId());
            msgMap.put("role", m.getRole());
            msgMap.put("content", m.getContent());
            msgMap.put("imageUrl", m.getImageUrl());
            msgMap.put("createdAt", m.getCreatedAt());
            return msgMap;
        }).collect(Collectors.toList()));

        return map;
    }

    @Transactional
    public void deleteConversation(String userEmail, Long id) {
        User user = userService.getUserByEmail(userEmail);
        Conversation conv = conversationRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
        conversationRepository.delete(conv);
    }

    public ExplainImageResponse explainImage(String userEmail, ExplainImageRequest request) {
        return aiService.explainImage(request);
    }

    public TranslateResponse translate(TranslateRequest request) {
        return aiService.translate(request);
    }

    public Map<String, Object> getAiStatus() {
        return aiService.getAiStatus();
    }

    public boolean configureAi(String provider, String apiKey, String model, String apiUrl) {
        return aiService.configureProvider(provider, apiKey, model, apiUrl);
    }
}
