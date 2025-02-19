package ru.skypro.homework.controller;

import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.*;
import ru.skypro.homework.service.AdsService;

import java.io.IOException;

@Slf4j
@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequiredArgsConstructor
@RequestMapping("/ads")
@Api(tags = "Ads", description = "API для управления объявлениями")
public class AdsController {

    private final AdsService adsService;

    /**
     * Получить список всех объявлений.
     *
     * @return Объект {@link ResponseEntity} с оберткой {@link ResponseWrapperAds}, содержащей список объявлений и статус ответа.
     * @see AdsService#getAllAds()
     */
    @ApiOperation(value = "Получить список всех объявлений", response = ResponseWrapperAds.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно получен список объявлений"),
            @ApiResponse(code = 500, message = "Ошибка сервера")
    })
    @GetMapping
    public ResponseEntity<ResponseWrapperAds> getAllAds() {
        return ResponseEntity.ok(adsService.getAllAds());
    }

    /**
     * Получить список объявлений пользователя, выполнившего аутентификацию.
     *
     * @param authentication Объект {@link Authentication} с информацией об аутентифицированном пользователе.
     * @return Объект {@link ResponseEntity} с оберткой {@link ResponseWrapperAds}, содержащей список объявлений пользователя и статус ответа.
     * @see AdsService#getAdsMe(String)
     */
    @ApiOperation(value = "Получить список объявлений пользователя", response = ResponseWrapperAds.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно получен список объявлений пользователя"),
            @ApiResponse(code = 401, message = "Пользователь не аутентифицирован"),
            @ApiResponse(code = 500, message = "Ошибка сервера")
    })
    @GetMapping("/me")
    public ResponseEntity<ResponseWrapperAds> getAdsMe(Authentication authentication) {
        return ResponseEntity.ok(adsService.getAdsMe(authentication.getName()));
    }

    /**
     * Добавить новое объявление.
     *
     * @param authentication Объект {@link Authentication} с информацией об аутентифицированном пользователе.
     * @param createAds      Объект {@link CreateAds} с данными нового объявления.
     * @param image          Объект {@link MultipartFile} с изображением объявления.
     * @return Объект {@link ResponseEntity} с созданным объявлением и статусом ответа.
     * @see AdsService#addAd(CreateAds, String, MultipartFile)
     */
    @ApiOperation(value = "Добавить новое объявление", response = AdsDto.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "properties", dataType = "string", paramType = "form", value = "JSON-строка с данными нового объявления"),
            @ApiImplicitParam(name = "image", dataType = "file", paramType = "form", value = "Изображение объявления", required = true)
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AdsDto> addAd(Authentication authentication,
                                        @RequestPart("properties") CreateAds createAds,
                                        @RequestPart("image") MultipartFile image) {
        return ResponseEntity.ok(adsService.addAd(createAds, authentication.getName(), image));
    }

    /**
     * Получить объявление по его идентификатору.
     *
     * @param id Идентификатор объявления, который нужно получить.
     * @return Объект {@link ResponseEntity} с найденным объявлением и статусом ответа.
     * @see AdsService#getAds(Integer)
     */
    @ApiOperation(value = "Получить объявление по его идентификатору", response = FullAds.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно получено объявление"),
            @ApiResponse(code = 404, message = "Объявление не найдено"),
            @ApiResponse(code = 500, message = "Ошибка сервера")
    })
    @GetMapping("/{id}")
    public ResponseEntity<FullAds> getAds(@PathVariable Integer id) {
        return ResponseEntity.ok(adsService.getAds(id));
    }

    /**
     * Удалить объявление по его идентификатору.
     *
     * @param id Идентификатор объявления, которое нужно удалить.
     * @return Объект {@link ResponseEntity} с пустым телом ответа и статусом NO_CONTENT в случае успешного удаления.
     * @see AdsService#removeAd(Integer)
     */
    @ApiOperation(value = "Удалить объявление по его идентификатору", response = ResponseEntity.class)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Успешно удалено"),
            @ApiResponse(code = 401, message = "Неавторизованный запрос"),
            @ApiResponse(code = 403, message = "Запрещено")
    })
    @PreAuthorize("hasRole('ADMIN') or @adsServiceImpl.getAds(#id).getEmail() == authentication.principal.username")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeAd(@PathVariable Integer id) {
        adsService.removeAd(id);
        return ResponseEntity.ok(HttpStatus.NO_CONTENT);
    }

    /**
     * Обновить объявление по его идентификатору.
     *
     * @param createAds Объект {@link CreateAds} с обновленными данными для объявления.
     * @param id        Идентификатор объявления, которое нужно обновить.
     * @return Объект {@link ResponseEntity} с обновленным объявлением и статусом ответа.
     * @see AdsService#updateAds(CreateAds, Integer)
     */
    @ApiOperation(value = "Обновить объявление по его идентификатору", response = AdsDto.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно обновлено"),
            @ApiResponse(code = 400, message = "Некорректный запрос"),
            @ApiResponse(code = 401, message = "Неавторизованный запрос"),
            @ApiResponse(code = 403, message = "Запрещено"),
            @ApiResponse(code = 404, message = "Объявление не найдено")
    })
    @PreAuthorize("hasRole('ADMIN') or @adsServiceImpl.getAds(#id).getEmail()==authentication.principal.username")
    @PatchMapping("/{id}")
    public ResponseEntity<AdsDto> updateAds(@RequestBody CreateAds createAds,
                                            @PathVariable Integer id) {
        return ResponseEntity.ok(adsService.updateAds(createAds, id));
    }

    /**
     * Получить список комментариев объявления по его идентификатору.
     *
     * @param id Идентификатор объявления, для которого нужно получить комментарии.
     * @return Объект {@link ResponseEntity} с оберткой {@link ResponseWrapperComment}, содержащей список комментариев и статус ответа.
     * @see AdsService#getComments(Integer)
     */
    @ApiOperation(value = "Получить список комментариев объявления по его идентификатору", response = ResponseWrapperComment.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно получено"),
            @ApiResponse(code = 400, message = "Некорректный запрос"),
            @ApiResponse(code = 401, message = "Неавторизованный запрос"),
            @ApiResponse(code = 403, message = "Запрещено"),
            @ApiResponse(code = 404, message = "Объявление не найдено")
    })
    @GetMapping("/{id}/comments")
    public ResponseEntity<ResponseWrapperComment> getComments(@PathVariable Integer id) {
        return ResponseEntity.ok(adsService.getComments(id));
    }

    /**
     * Добавить комментарий объявления по его идентификатору.
     *
     * @param id             Идентификатор объявления, для которого нужно добавить комментарий.
     * @param createComment  Объект {@link CreateComment} с данными нового комментария.
     * @param authentication Объект {@link Authentication} с информацией об аутентифицированном пользователе.
     * @return Объект {@link ResponseEntity} с созданным комментарием и статусом ответа.
     * @see AdsService#addComment(Integer, CreateComment, String)
     */
    @ApiOperation(value = "Добавить комментарий объявления по его идентификатору", response = CommentDto.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешно создано"),
            @ApiResponse(code = 400, message = "Некорректный запрос"),
            @ApiResponse(code = 401, message = "Неавторизованный запрос"),
            @ApiResponse(code = 403, message = "Запрещено"),
            @ApiResponse(code = 404, message = "Объявление не найдено")
    })
    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentDto> addComment(@PathVariable Integer id,
                                                 @RequestBody CreateComment createComment,
                                                 Authentication authentication) {
        return ResponseEntity.ok(adsService.addComment(id, createComment, authentication.getName()));
    }

    /**
     * Удалить комментарий объявления по его идентификаторам.
     *
     * @param adId      Идентификатор объявления, для которого нужно удалить комментарий.
     * @param commentId Идентификатор комментария, который нужно удалить.
     * @return Объект {@link ResponseEntity} с пустым телом ответа и статусом NO_CONTENT в случае успешного удаления.
     * @see AdsService#deleteComment(Integer, Integer)
     */
    @ApiOperation(value = "Удалить комментарий объявления по его идентификаторам", response = ResponseEntity.class)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Успешное удаление"),
            @ApiResponse(code = 401, message = "Неавторизованный запрос"),
            @ApiResponse(code = 403, message = "Запрещено"),
            @ApiResponse(code = 404, message = "Объявление или комментарий не найдены")
    })
    @PreAuthorize("hasRole('ADMIN') or " +
            "@adsServiceImpl.getUserNameOfComment(#commentId)==authentication.principal.username")
    @DeleteMapping("/{adId}/comments/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Integer adId, @PathVariable Integer commentId) {
        adsService.deleteComment(adId, commentId);
        return ResponseEntity.ok(HttpStatus.NO_CONTENT);
    }

    /**
     * Обновить комментарий объявления по его идентификаторам.
     *
     * @param adId          Идентификатор объявления, для которого нужно обновить комментарий.
     * @param commentId     Идентификатор комментария, который нужно обновить.
     * @param createComment Объект {@link CreateComment} с обновленными данными для комментария.
     * @return Объект {@link ResponseEntity} с обновленным комментарием и статусом ответа.
     * @see AdsService#updateComment(Integer, Integer, CreateComment)
     */
    @ApiOperation(value = "Обновить комментарий объявления по его идентификаторам", response = CommentDto.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешное обновление комментария"),
            @ApiResponse(code = 401, message = "Неавторизованный запрос"),
            @ApiResponse(code = 403, message = "Запрещено"),
            @ApiResponse(code = 404, message = "Объявление или комментарий не найдены")
    })
    @PreAuthorize("hasRole('ADMIN') or " +
            "@adsServiceImpl.getUserNameOfComment(#commentId)==authentication.principal.username")
    @PatchMapping("/{adId}/comments/{commentId}")
    public ResponseEntity<CommentDto> updateComment(@PathVariable Integer adId,
                                                    @PathVariable Integer commentId,
                                                    @RequestBody CreateComment createComment) {
        return ResponseEntity.ok(adsService.updateComment(adId, commentId, createComment));
    }

    /**
     * Обновить изображение объявления по его идентификатору.
     *
     * @param id    Идентификатор объявления, для которого нужно обновить изображение.
     * @param image Объект {@link MultipartFile} с новым изображением для объявления.
     * @return Объект {@link ResponseEntity} с пустым телом ответа и статусом NO_CONTENT в случае успешного обновления.
     * @see AdsService#updateAdsImage(Integer, MultipartFile)
     */
    @ApiOperation(value = "Обновить изображение объявления по его идентификатору", response = void.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешное обновление изображения"),
            @ApiResponse(code = 401, message = "Неавторизованный запрос"),
            @ApiResponse(code = 403, message = "Запрещено"),
            @ApiResponse(code = 404, message = "Объявление не найдено")
    })
    @PatchMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateAdsImage(@PathVariable Integer id, @RequestParam MultipartFile image) {
        adsService.updateAdsImage(id, image);
        return ResponseEntity.ok(HttpStatus.NO_CONTENT);
    }

    /**
     * Получить изображение по его имени.
     *
     * @param name Имя изображения, которое нужно получить.
     * @return Массив байтов с содержимым изображения в формате PNG.
     * @throws IOException Исключение, возникающее при ошибке чтения изображения.
     * @see AdsService#getImage(String)
     */
    @ApiOperation(value = "Получить изображение по его имени", response = byte[].class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Успешное получение изображения"),
            @ApiResponse(code = 404, message = "Изображение не найдено")
    })
    @GetMapping(value = "/image/{name}", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] getImages(@PathVariable String name) throws IOException {
        return adsService.getImage(name);
    }
}
