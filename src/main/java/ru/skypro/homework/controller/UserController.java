package ru.skypro.homework.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.NewPassword;
import ru.skypro.homework.dto.UserDto;
import ru.skypro.homework.service.UserService;

import java.io.IOException;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    /**
     * Установить новый пароль для пользователя.
     *
     * @param newPassword    Объект {@link NewPassword} с новым паролем.
     * @param authentication Объект {@link Authentication} с информацией об аутентифицированном пользователе.
     * @return Объект {@link ResponseEntity} с объектом {@link NewPassword} и статусом ответа.
     * @see UserService#setPassword(NewPassword, String)
     */
    @PostMapping("/set_password")
    public ResponseEntity<NewPassword> setPassword(@RequestBody NewPassword newPassword,
                                                   Authentication authentication) {
        userService.setPassword(newPassword, authentication.getName());
        return ResponseEntity.ok(newPassword);
    }

    /**
     * Получить информацию о текущем пользователе.
     *
     * @param authentication Объект {@link Authentication} с информацией об аутентифицированном пользователе.
     * @return Объект {@link ResponseEntity} с объектом {@link UserDto} и статусом ответа.
     * @see UserService#getUser(String)
     */
    @GetMapping("/me")
    public ResponseEntity<UserDto> getUser(Authentication authentication) {
        return ResponseEntity.ok(userService.getUser(authentication.getName()));
    }

    /**
     * Обновить информацию о текущем пользователе.
     *
     * @param userDto        Объект {@link UserDto} с обновленными данными пользователя.
     * @param authentication Объект {@link Authentication} с информацией об аутентифицированном пользователе.
     * @return Объект {@link ResponseEntity} с обновленным объектом {@link UserDto} и статусом ответа.
     * @see UserService#updateUser(UserDto, String)
     */
    @PatchMapping("/me")
    public ResponseEntity<UserDto> updateUser(@RequestBody UserDto userDto,
                                              Authentication authentication) {
        return ResponseEntity.ok(userService.updateUser(userDto, authentication.getName()));
    }

    /**
     * Обновить аватар пользователя.
     *
     * @param image          Объект {@link MultipartFile} с новым изображением для аватара.
     * @param authentication Объект {@link Authentication} с информацией об аутентифицированном пользователе.
     * @return Объект {@link ResponseEntity} с пустым телом ответа и статусом NO_CONTENT в случае успешного обновления.
     * @see UserService#updateAvatar(MultipartFile, String)
     */
    @PatchMapping(value = "/me/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateUserImage(@RequestParam MultipartFile image,
                                             Authentication authentication) {
        userService.updateAvatar(image, authentication.getName());
        return ResponseEntity.ok(HttpStatus.NO_CONTENT);
    }

    /**
     * Получить изображение пользователя по его имени.
     *
     * @param name Имя изображения пользователя, которое нужно получить.
     * @return Массив байтов с содержимым изображения в формате PNG.
     * @throws IOException Исключение, возникающее при ошибке чтения изображения.
     * @see UserService#getImage(String)
     */
    @GetMapping(value = "/image/{name}", produces = {MediaType.IMAGE_PNG_VALUE})
    public byte[] getImages(@PathVariable String name) throws IOException {
        return userService.getImage(name);
    }
}
