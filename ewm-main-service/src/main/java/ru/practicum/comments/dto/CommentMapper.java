package ru.practicum.comments.dto;

import ru.practicum.comments.Comment;
import ru.practicum.user.UserMapper;

public class CommentMapper {

    public static CommentDto toCommentDto(Comment comment) {

        CommentDto commentDto = new CommentDto();

        commentDto.setId(comment.getId());
        commentDto.setText(comment.getText());
        commentDto.setAuthor(UserMapper.toUserShortDto(comment.getAuthor()));
        commentDto.setEventId(comment.getEvent().getId());
        commentDto.setCreatedOn(comment.getCreatedOn());

        return commentDto;
    }
}
