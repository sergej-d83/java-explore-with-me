package ru.practicum.comments.service;

import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.dto.NewCommentDto;

import java.util.List;

public interface CommentService {

    CommentDto addComment(Long userId, Long eventId, NewCommentDto comment);

    CommentDto getComment(Long commentId);

    List<CommentDto> getComments(Long eventId);

    CommentDto updateComment(Long userId, Long commentId, NewCommentDto comment);

    void deleteComment(Long userId, Long commentId);
}
