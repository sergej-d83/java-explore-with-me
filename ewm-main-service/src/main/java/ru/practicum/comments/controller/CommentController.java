package ru.practicum.comments.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.dto.NewCommentDto;
import ru.practicum.comments.service.CommentService;

import javax.validation.Valid;

@RestController
@RequestMapping("/users/{userId}")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/events/{eventId}/comments")
    public ResponseEntity<CommentDto> addComment(@PathVariable Long userId, @PathVariable Long eventId,
                                                 @RequestBody @Valid NewCommentDto comment) {

        return new ResponseEntity<>(commentService.addComment(userId, eventId, comment), HttpStatus.CREATED);
    }

    @PatchMapping("/comments/{commentId}")
    public ResponseEntity<CommentDto> updateComment(@PathVariable Long userId, @PathVariable Long commentId,
                                                    @RequestBody @Valid NewCommentDto comment) {

        return new ResponseEntity<>(commentService.updateComment(userId, commentId, comment), HttpStatus.OK);
    }

    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long userId, @PathVariable Long commentId) {

        commentService.deleteComment(userId, commentId);
    }
}
