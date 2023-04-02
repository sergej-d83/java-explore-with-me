package ru.practicum.event.dto;

import ru.practicum.category.Category;
import ru.practicum.category.CategoryMapper;
import ru.practicum.event.Event;
import ru.practicum.event.status.EventStatus;
import ru.practicum.user.User;
import ru.practicum.user.UserMapper;

import java.time.LocalDateTime;

public class EventMapper {

    public static Event toEvent(NewEventDto eventDto) {

        Event event = new Event();

        event.setAnnotation(eventDto.getAnnotation());
        event.setCreatedOn(LocalDateTime.now());
        event.setDescription(eventDto.getDescription());
        event.setEventDate(eventDto.getEventDate());
        event.setLocation(eventDto.getLocation());
        event.setIsPaid(eventDto.getIsPaid());
        event.setParticipantLimit(eventDto.getParticipantLimit());
        event.setRequestModeration(eventDto.getRequestModeration());
        event.setState(EventStatus.PENDING);
        event.setTitle(eventDto.getTitle());

        return event;
    }

    public static EventShortDto toEventShortDto(Event event) {

        EventShortDto eventShortDto = new EventShortDto();

        eventShortDto.setId(event.getId());
        eventShortDto.setAnnotation(event.getAnnotation());
        eventShortDto.setCategoryDto(CategoryMapper.toCategoryDto(event.getCategory()));
        eventShortDto.setConfirmedRequests((long) event.getConfirmedRequests().size());
        eventShortDto.setEventDate(event.getEventDate());
        eventShortDto.setInitiator(UserMapper.toUserShortDto(event.getInitiator()));
        eventShortDto.setIsPaid(event.getIsPaid());
        eventShortDto.setTitle(event.getTitle());

        return eventShortDto;
    }

    public static EventFullDto toEventFullDto(Event event) {

        EventFullDto fullDto = new EventFullDto();

        fullDto.setId(event.getId());
        fullDto.setAnnotation(event.getAnnotation());
        fullDto.setCategoryDto(CategoryMapper.toCategoryDto(event.getCategory()));
        fullDto.setConfirmedRequests((long) event.getConfirmedRequests().size());
        fullDto.setCreatedOn(event.getCreatedOn());
        fullDto.setDescription(event.getDescription());
        fullDto.setEventDate(event.getEventDate());
        fullDto.setInitiator(UserMapper.toUserShortDto(event.getInitiator()));
        fullDto.setLocation(event.getLocation());
        fullDto.setParticipantLimit(event.getParticipantLimit());
        fullDto.setPublishedOn(event.getPublishedOn());
        fullDto.setRequestModeration(event.getRequestModeration());
        fullDto.setState(event.getState());
        fullDto.setTitle(event.getTitle());

        return fullDto;
    }
}
