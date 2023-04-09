package ru.practicum.user.service;

import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;

import java.util.List;

public interface UserService {

    List<UserDto> getUsers(Long[] ids, Integer from, Integer size);

    UserDto addUser(NewUserRequest user);

    void deleteUser(Long userId);
}
