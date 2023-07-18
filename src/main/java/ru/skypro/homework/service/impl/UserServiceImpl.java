package ru.skypro.homework.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.NewPassword;
import ru.skypro.homework.dto.UserDto;
import ru.skypro.homework.entity.User;
import ru.skypro.homework.exception.UserNotFoundException;
import ru.skypro.homework.exception.UserWithEmailNotFoundException;
import ru.skypro.homework.mapper.UserMapper;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.ImageService;
import ru.skypro.homework.service.UserService;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final ImageService imageService;

    @Override
    public boolean setPassword(NewPassword newPassword, String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (encoder.matches(newPassword.getCurrentPassword(), user.getPassword())) {
                user.setPassword(encoder.encode(newPassword.getNewPassword()));
                userRepository.save(user);
                log.trace("Update password for user with login: " + email);
                return true;
            }
        }
        return false;
    }

    @Override
    public UserDto getUser(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserWithEmailNotFoundException(email));
        log.trace("Get user with login: " + email);
        return UserMapper.INSTANCE.toUserDto(user);
    }

    @Override
    public UserDto updateUser(UserDto userDto, String email) {
        User user = UserMapper.INSTANCE.toUserFromUserDto(userDto);
        userRepository.save(user);
        log.trace("Update user with email: " + email);
        return UserMapper.INSTANCE.toUserDto(user);
    }

    @Override
    public void updateAvatar(MultipartFile image, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserWithEmailNotFoundException(email));
        String name = "user" + user.getId();
        imageService.saveImage(image,name);
        user.setImage("/users/image/" + name);
        userRepository.save(user);
    }

    @Override
    public byte[] getImage(String name) throws IOException {
        return imageService.getImage(name);
    }
}
