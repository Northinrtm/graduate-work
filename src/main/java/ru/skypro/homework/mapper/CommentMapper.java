package ru.skypro.homework.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ru.skypro.homework.dto.CommentDto;
import ru.skypro.homework.dto.CreateComment;
import ru.skypro.homework.entity.Comment;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    CommentMapper INSTANCE = Mappers.getMapper(CommentMapper.class);

    List<CommentDto> toListDto(List<Comment> commentList);

    Comment toCommentFromCreateComment(CreateComment createComment);

    @Mapping(source = "id", target = "pk")
    CommentDto toCommentDtoFromComment(Comment comment);
}
