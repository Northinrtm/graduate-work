package ru.skypro.homework.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
@Api(tags = "Users", description = "API для работы с пользователями")
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
    @ApiOperation(value = "Обновление пароля пользователя", response = NewPassword.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Пароль успешно обновлен"),
            @ApiResponse(code = 401, message = "Пользователь не аутентифицирован")
    })
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
    @ApiOperation(value = "Получить информацию о текущем пользователе", response = UserDto.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Информация о пользователе успешно получена"),
            @ApiResponse(code = 401, message = "Пользователь не аутентифицирован")
    })
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
    @ApiOperation(value = "Обновить информацию о текущем пользователе", response = UserDto.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Информация о пользователе успешно обновлена"),
            @ApiResponse(code = 401, message = "Пользователь не аутентифицирован"),
            @ApiResponse(code = 404, message = "Пользователь не найден")
    })
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
    @ApiOperation(value = "Обновить изображение пользователя")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Изображение пользователя успешно обновлено"),
            @ApiResponse(code = 401, message = "Пользователь не аутентифицирован"),
            @ApiResponse(code = 404, message = "Пользователь не найден")
    })
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
    @ApiOperation(value = "Получить изображение пользователя по его имени")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Изображение пользователя успешно получено"),
            @ApiResponse(code = 404, message = "Изображение пользователя не найдено")
    })
    @GetMapping(value = "/image/{name}", produces = {MediaType.IMAGE_PNG_VALUE})
    public byte[] getImages(@PathVariable String name) throws IOException {
        return userService.getImage(name);
    }
}
