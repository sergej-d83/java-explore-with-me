package ru.practicum.comments.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.dto.NewCommentDto;
import ru.practicum.comments.service.CommentService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/users/{userId}/events/{eventId}/comments")
    public ResponseEntity<CommentDto> addComment(@PathVariable Long userId, @PathVariable Long eventId,
                                                 @RequestBody @Valid NewCommentDto comment) {

        return new ResponseEntity<>(commentService.addComment(userId, eventId, comment), HttpStatus.CREATED);
    }

    @GetMapping("/comments/{commentId}")
    public ResponseEntity<CommentDto> getComment(@PathVariable Long commentId) {

        return new ResponseEntity<>(commentService.getComment(commentId), HttpStatus.OK);
    }

    @GetMapping("/comments")
    public ResponseEntity<List<CommentDto>> getComments(@RequestParam Long eventId) {

        return new ResponseEntity<>(commentService.getComments(eventId), HttpStatus.OK);
    }

    @PatchMapping("/users/{userId}/comments/{commentId}")
    public ResponseEntity<CommentDto> updateComment(@PathVariable Long userId, @PathVariable Long commentId,
                                                    @RequestBody @Valid NewCommentDto comment) {

        return new ResponseEntity<>(commentService.updateComment(userId, commentId, comment), HttpStatus.OK);
    }

    @DeleteMapping("/users/{userId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long userId, @PathVariable Long commentId) {

        commentService.deleteComment(userId, commentId);
    }
}
