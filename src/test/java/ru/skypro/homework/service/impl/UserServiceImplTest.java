package ru.skypro.homework.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.NewPassword;
import ru.skypro.homework.dto.UserDto;
import ru.skypro.homework.entity.User;
import ru.skypro.homework.exception.UserNotFoundException;
import ru.skypro.homework.exception.UserWithEmailNotFoundException;
import ru.skypro.homework.mapper.UserMapper;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.ImageService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder encoder;
    @Mock
    private UserMapper userMapper;
    @Mock
    private ImageService imageService;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSetPasswordValid() {
        String email = "test@example.com";
        NewPassword newPassword = new NewPassword();
        newPassword.setCurrentPassword("oldPassword");
        newPassword.setNewPassword("newPassword");
        User user = new User();
        user.setEmail(email);
        user.setPassword("oldPasswordEncoded");
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(encoder.matches(newPassword.getCurrentPassword(), user.getPassword())).thenReturn(true);
        when(encoder.encode(newPassword.getNewPassword())).thenReturn("newPasswordEncoded");

        boolean result = userService.setPassword(newPassword, email);

        assertTrue(result);
        assertEquals("newPasswordEncoded", user.getPassword());
        verify(userRepository).save(user);
        verify(encoder).encode(newPassword.getNewPassword());
    }

    @Test
    void testSetPasswordInvalidUser() {
        String email = "test@example.com";
        NewPassword newPassword = new NewPassword();
        newPassword.setCurrentPassword("oldPassword");
        newPassword.setNewPassword("newPassword");
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        boolean result = userService.setPassword(newPassword, email);

        assertFalse(result);
        verify(userRepository).findByEmail(email);
        verifyNoInteractions(encoder);
    }

    @Test
    void testSetPasswordInvalidPassword() {
        String email = "test@example.com";
        NewPassword newPassword = new NewPassword();
        newPassword.setCurrentPassword("oldPassword");
        newPassword.setNewPassword("newPassword");
        User user = new User();
        user.setEmail(email);
        user.setPassword("wrongPasswordEncoded");
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(encoder.matches(newPassword.getCurrentPassword(), user.getPassword())).thenReturn(false);

        boolean result = userService.setPassword(newPassword, email);

        assertFalse(result);
        verify(userRepository).findByEmail(email);
        verify(encoder).matches(newPassword.getCurrentPassword(), user.getPassword());
        verifyNoMoreInteractions(encoder);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void testGetUser() {
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);
        UserDto userDto = new UserDto();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userMapper.toUserDto(user)).thenReturn(userDto);

        UserDto result = userService.getUser(email);

        assertNotNull(result);
        assertEquals(userDto, result);
        verify(userRepository).findByEmail(email);
        verify(userMapper).toUserDto(user);
    }

    @Test
    void testGetUserNotFound() {
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UserWithEmailNotFoundException.class, () -> userService.getUser(email));

        verify(userRepository).findByEmail(email);
        verifyNoInteractions(userMapper);
    }

    @Test
    void testUpdateUser() {
        String email = "test@example.com";
        UserDto userDto = new UserDto();
        userDto.setEmail(email);
        User user = new User();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userMapper.toUserDto(user)).thenReturn(userDto);

        UserDto result = userService.updateUser(userDto, email);

        assertNotNull(result);
        assertEquals(userDto, result);
        verify(userRepository).findByEmail(email);
        verify(userMapper).updateUserFromUserDto(userDto, user);
        verify(userRepository).save(user);
        verify(userMapper).toUserDto(user);
    }

    @Test
    void testUpdateUserNotFound() {
        String email = "nonexistent@example.com";
        UserDto userDto = new UserDto();
        userDto.setEmail(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.updateUser(userDto, email));

        verify(userRepository).findByEmail(email);
        verifyNoInteractions(userMapper);
        verifyNoMoreInteractions(userRepository);
    }
    @Test
    void testUpdateAvatar() {
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);
        MultipartFile image = mock(MultipartFile.class);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        userService.updateAvatar(image, email);

        verify(userRepository).findByEmail(email);
        verify(imageService).deleteFileIfNotNull(user.getImage());
        verify(imageService).saveImage(image, "/users");
        verify(userRepository).save(user);
    }

    @Test
    void testUpdateAvatarNotFound() {
        String email = "nonexistent@example.com";
        MultipartFile image = mock(MultipartFile.class);

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UserWithEmailNotFoundException.class, () -> userService.updateAvatar(image, email));

        verify(userRepository).findByEmail(email);
        verifyNoInteractions(imageService);
        verifyNoMoreInteractions(userRepository);
    }
}