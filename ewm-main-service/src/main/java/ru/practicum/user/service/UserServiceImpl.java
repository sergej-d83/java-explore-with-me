package ru.practicum.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.User;
import ru.practicum.user.UserMapper;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public List<UserDto> getUsers(Long[] ids, Integer from, Integer size) {

        Pageable pageable = PageRequest.of((from / size), size);

        if (ids != null && ids.length > 0) {
            List<Long> userIds = List.of(ids);
            return UserMapper.toListOfUserDto(userRepository.findByIdIn(userIds, pageable));
        }
        return null;
    }

    @Override
    public UserDto addUser(NewUserRequest newUser) {

        Optional<User> user = userRepository.findByEmail(newUser.getEmail());
        if (user.isPresent()) {
            throw new ConflictException("Пользователь с такой электронной почтой уже существует. " + newUser.getEmail());
        }

        return UserMapper.toUserDto(userRepository.save(UserMapper.toUser(newUser)));
    }

    @Override
    public void deleteUser(Long userId) {
        userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь под номером " + userId + " не найден."));
        userRepository.deleteById(userId);
    }
}
