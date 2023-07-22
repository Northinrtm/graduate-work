package ru.skypro.homework.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.skypro.homework.dto.Register;
import ru.skypro.homework.dto.Role;
import ru.skypro.homework.entity.User;
import ru.skypro.homework.mapper.UserMapper;
import ru.skypro.homework.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;
    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLogin_Success() {
        String userName = "user@example.com";
        String password = "password";

        User user = new User();
        user.setPassword("encodedPassword");

        when(userRepository.findByEmail(userName)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, user.getPassword())).thenReturn(true);

        assertTrue(authService.login(userName, password));
    }

    @Test
    public void testLogin_UserNotFound() {
        String userName = "user@example.com";
        String password = "password";

        when(userRepository.findByEmail(userName)).thenReturn(Optional.empty());

        assertFalse(authService.login(userName, password));
    }

    @Test
    public void testLogin_PasswordNotMatches() {
        String userName = "user@example.com";
        String password = "password";

        User user = new User();
        user.setPassword("encodedPassword");

        when(userRepository.findByEmail(userName)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, user.getPassword())).thenReturn(false);

        assertFalse(authService.login(userName, password));
    }

    @Test
    public void testRegister_Success() {
        String userName = "user@example.com";
        String password = "password";

        Register register = new Register();
        register.setUsername(userName);
        register.setPassword(password);

        User user = new User();
        user.setEmail(userName);

        Role role = Role.USER;

        when(userRepository.findByEmail(userName)).thenReturn(Optional.empty());
        when(userMapper.toUser(register)).thenReturn(user);

        assertTrue(authService.register(register, role));
        verify(userRepository, times(1)).save(any());
    }

    @Test
    public void testRegister_UserAlreadyExists() {
        String userName = "user@example.com";
        String password = "password";

        Register register = new Register();
        register.setUsername(userName);
        register.setPassword(password);

        Role role = Role.USER;

        User existingUser = new User();

        when(userRepository.findByEmail(userName)).thenReturn(Optional.of(existingUser));

        assertFalse(authService.register(register, role));
        verify(userRepository, never()).save(any());
    }
}