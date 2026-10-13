package com.digitalhelper.service;

import com.digitalhelper.dto.SavedAnswerDto;
import com.digitalhelper.dto.SavedAnswerRequest;
import com.digitalhelper.exception.ResourceNotFoundException;
import com.digitalhelper.model.SavedAnswer;
import com.digitalhelper.model.User;
import com.digitalhelper.repository.SavedAnswerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SavedAnswerService {

    private final SavedAnswerRepository savedAnswerRepository;
    private final UserService userService;

    public SavedAnswerService(SavedAnswerRepository savedAnswerRepository, UserService userService) {
        this.savedAnswerRepository = savedAnswerRepository;
        this.userService = userService;
    }

    public List<SavedAnswerDto> getSavedAnswers(String userEmail, String category, String query) {
        User user = userService.getUserByEmail(userEmail);
        List<SavedAnswer> list;

        if (query != null && !query.trim().isEmpty()) {
            list = savedAnswerRepository.searchByUserIdAndQuery(user.getId(), query.trim());
        } else if (category != null && !category.trim().isEmpty() && !"All".equalsIgnoreCase(category)) {
            list = savedAnswerRepository.findByUserIdAndCategoryOrderByCreatedAtDesc(user.getId(), category.trim());
        } else {
            list = savedAnswerRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        }

        return list.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public SavedAnswerDto saveAnswer(String userEmail, SavedAnswerRequest request) {
        User user = userService.getUserByEmail(userEmail);

        SavedAnswer answer = new SavedAnswer(
                user,
                request.getTitle(),
                request.getOriginalQuestion(),
                request.getContent(),
                request.getCategory() != null ? request.getCategory() : "General",
                request.getTags()
        );

        answer = savedAnswerRepository.save(answer);
        return toDto(answer);
    }

    @Transactional
    public SavedAnswerDto toggleFavorite(String userEmail, Long id) {
        User user = userService.getUserByEmail(userEmail);
        SavedAnswer answer = savedAnswerRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Saved answer not found"));

        answer.setFavorite(!answer.isFavorite());
        answer = savedAnswerRepository.save(answer);
        return toDto(answer);
    }

    @Transactional
    public void deleteSavedAnswer(String userEmail, Long id) {
        User user = userService.getUserByEmail(userEmail);
        SavedAnswer answer = savedAnswerRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Saved answer not found"));

        savedAnswerRepository.delete(answer);
    }

    private SavedAnswerDto toDto(SavedAnswer a) {
        return new SavedAnswerDto(
                a.getId(),
                a.getTitle(),
                a.getOriginalQuestion(),
                a.getContent(),
                a.getCategory(),
                a.getTags(),
                a.isFavorite(),
                a.getCreatedAt()
        );
    }
}
