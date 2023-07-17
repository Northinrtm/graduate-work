package ru.skypro.homework.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.skypro.homework.dto.NewPassword;
import ru.skypro.homework.dto.Register;
import ru.skypro.homework.dto.Role;
import ru.skypro.homework.entity.User;
import ru.skypro.homework.mapper.UserMapper;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.AuthService;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    @Override
    public boolean login(String userName, String password) {
        Optional<User> optionalUser = userRepository.findByEmail(userName);
        if (optionalUser.isEmpty()) {
            return false;
        }
        return encoder.matches(password,optionalUser.get().getPassword());
    }

    @Override
    public boolean register(Register register, Role role) {
        if (userRepository.findByEmail(register.getUsername()).isPresent()) {
            return false;
        }
        User user = UserMapper.INSTANCE.toUser(register);
        user.setEmail(user.getEmail().toLowerCase());
        user.setPassword(encoder.encode(user.getPassword()));
        user.setRole(role);
        userRepository.save(user);
        return true;
    }
}
