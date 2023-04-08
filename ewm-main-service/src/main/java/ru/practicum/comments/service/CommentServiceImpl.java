package ru.practicum.comments.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.comments.Comment;
import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.dto.CommentMapper;
import ru.practicum.comments.dto.NewCommentDto;
import ru.practicum.comments.repository.CommentRepository;
import ru.practicum.event.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.status.EventStatus;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;


    @Override
    public CommentDto addComment(Long userId, Long eventId, NewCommentDto commentDto) {

        User author = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь под номером " + userId + " не найден."));

        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Событие под номером " + eventId + " не найдено."));

        if (event.getState() != EventStatus.PUBLISHED) {
            throw new ConflictException("Комментарий можно оставить только к опубликованным событиям.");
        }

        Comment comment = new Comment();

        comment.setText(commentDto.getText());
        comment.setEvent(event);
        comment.setAuthor(author);
        comment.setCreatedOn(LocalDateTime.now());

        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    public CommentDto updateComment(Long userId, Long commentId, NewCommentDto commentDto) {

        Comment comment = verifyUserAndComment(userId, commentId);

        comment.setText(commentDto.getText());

        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    public void deleteComment(Long userId, Long commentId) {

        Comment comment = verifyUserAndComment(userId, commentId);

        commentRepository.delete(comment);
    }

    private Comment verifyUserAndComment(Long userId, Long commentId) {

        userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь под номером " + userId + " не найден."));

        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new NotFoundException("Комментарий номер " + commentId + " не найден."));

        if (comment.getAuthor().getId() != null && !comment.getAuthor().getId().equals(userId)) {
            throw new ConflictException("Нельзя редактировать или удалять чужие комментарии.");
        }

        return comment;
    }
}
