package ru.practicum.comments.service;

import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.dto.NewCommentDto;

public interface CommentService {

    CommentDto addComment(Long userId, Long eventId, NewCommentDto comment);

    CommentDto updateComment(Long userId, Long commentId, NewCommentDto comment);

    void deleteComment(Long userId, Long commentId);
}
