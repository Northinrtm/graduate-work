package ru.skypro.homework.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.*;
import ru.skypro.homework.entity.Ads;
import ru.skypro.homework.entity.Comment;
import ru.skypro.homework.entity.User;
import ru.skypro.homework.exception.AdsNotFoundException;
import ru.skypro.homework.exception.CommentNotFoundException;
import ru.skypro.homework.exception.UserWithEmailNotFoundException;
import ru.skypro.homework.mapper.AdsMapper;
import ru.skypro.homework.mapper.CommentMapper;
import ru.skypro.homework.repository.AdsRepository;
import ru.skypro.homework.repository.CommentRepository;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.AdsService;
import ru.skypro.homework.service.ImageService;

import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdsServiceImpl implements AdsService {

    private final AdsRepository adsRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final ImageService imageService;
    private final AdsMapper adsMapper;
    private final CommentMapper commentMapper;

    /**
     * Получить список всех объявлений.
     * использует методы {@link AdsRepository#findAll()} и {@link AdsMapper#toDtos(List)}
     *
     * @return Объект {@link ResponseWrapperAds} с оберткой содержащей список объявлений и статус ответа.
     */
    @Override
    public ResponseWrapperAds getAllAds() {
        List<Ads> adsList = adsRepository.findAll();
        List<AdsDto> adsDtoList = adsMapper.toDtos(adsList);
        ResponseWrapperAds responseWrapperAds = new ResponseWrapperAds();
        responseWrapperAds.setCount(adsList.size());
        responseWrapperAds.setResults(adsDtoList);
        return responseWrapperAds;
    }

    /**
     * Получает список объявлений, принадлежащих пользователю с указанным адресом электронной почты.
     * Использует методы {@link AdsRepository#findByUser(User)} и {@link AdsMapper#toDtos(List)}.
     *
     * @param email Адрес электронной почты пользователя, для которого нужно получить объявления.
     * @return Объект {@link ResponseWrapperAds} с оберткой, содержащей список DTO объявлений и статус ответа.
     * @throws UserWithEmailNotFoundException Если пользователя с указанным адресом электронной почты не найдено.
     */
    @Override
    public ResponseWrapperAds getAdsMe(String email) {
        List<Ads> adsList = adsRepository.findByUser(userRepository.findByEmail(email)
                .orElseThrow(() -> new UserWithEmailNotFoundException(email)));
        List<AdsDto> adsDtoList = adsMapper.toDtos(adsList);
        ResponseWrapperAds responseWrapperAds = new ResponseWrapperAds();
        responseWrapperAds.setResults(adsDtoList);
        responseWrapperAds.setCount(adsList.size());
        return responseWrapperAds;
    }

    /**
     * Добавляет новое объявление в базу данных.
     * Использует методы {@link AdsMapper#toAdsFromCreateAds(CreateAds)},
     * {@link UserRepository#findByEmail(String)},
     * {@link ImageService#saveImage(MultipartFile, String)} и {@link AdsRepository#save(Object)}.
     *
     * @param createAds Объект CreateAds, содержащий информацию для создания нового объявления.
     * @param email     Адрес электронной почты пользователя, который будет ассоциирован с добавляемым объявлением.
     * @param image     Объект MultipartFile с изображением для объявления.
     * @return Объект AdsDto, содержащий информацию о добавленном объявлении.
     * @throws UserWithEmailNotFoundException Если пользователя с указанным адресом электронной почты не найдено.
     */
    @Override
    public AdsDto addAd(CreateAds createAds, String email, MultipartFile image) {
        Ads ads = adsMapper.toAdsFromCreateAds(createAds);
        ads.setUser(userRepository.findByEmail(email)
                .orElseThrow(() -> new UserWithEmailNotFoundException(email)));
        ads.setImage(imageService.saveImage(image, "/ads"));
        adsRepository.save(ads);
        return adsMapper.toAdsDto(ads);
    }

    /**
     * Получает полную информацию об объявлении по его идентификатору.
     * Использует методы {@link AdsRepository#findById(Object)} и {@link AdsMapper#toFullAds(Ads)}.
     *
     * @param id Идентификатор объявления, для которого нужно получить полную информацию.
     * @return Объект типа FullAds, содержащий полную информацию об объявлении.
     * @throws AdsNotFoundException Если объявление с указанным идентификатором не найдено.
     */
    @Override
    public FullAds getAds(Integer id) {
        Ads ads = adsRepository.findById(id)
                .orElseThrow(() -> new AdsNotFoundException("Ads not found by id: " + id));
        return adsMapper.toFullAds(ads);
    }

    /**
     * Удаляет объявление по его идентификатору.
     * Метод помечен аннотацией {@link org.springframework.transaction.annotation.Transactional},
     * что обозначает транзакционное выполнение данного метода.
     * <p>
     * Использует методы:
     * {@link CommentRepository#deleteAllByAds_Id(Integer)},
     * {@link AdsRepository#findById(Object)},
     * {@link ImageService#deleteFileIfNotNull(String)} и
     * {@link AdsRepository#delete(Object)}.
     *
     * @param id Идентификатор объявления, которое нужно удалить.
     * @throws AdsNotFoundException Если объявление с указанным идентификатором не найдено.
     */
    @Transactional
    @Override
    public void removeAd(Integer id) {
        commentRepository.deleteAllByAds_Id(id);
        Ads ads = adsRepository.findById(id)
                .orElseThrow(() -> new AdsNotFoundException("Ads not found by id: " + id));
        imageService.deleteFileIfNotNull(ads.getImage());
        log.trace("Removed Ads with id: ", id);
        adsRepository.delete(ads);
    }


    /**
     * Обновляет информацию об объявлении по его идентификатору.
     * Использует методы:
     * {@link AdsRepository#findById(Object)},
     * {@link AdsMapper#updateAds(CreateAds, Ads)},
     * {@link AdsRepository#save(Object)},
     * {@link AdsMapper#toAdsDto(Ads)}.
     *
     * @param createAds Объект CreateAds с обновленными данными для объявления.
     * @param id        Идентификатор объявления, которое нужно обновить.
     * @return Объект AdsDto, содержащий обновленную информацию об объявлении.
     * @throws AdsNotFoundException Если объявление с указанным идентификатором не найдено.
     */
    @Override
    public AdsDto updateAds(CreateAds createAds, Integer id) {
        Ads ads = adsRepository.findById(id)
                .orElseThrow(() -> new AdsNotFoundException("Ads not found by id: " + id));
        adsMapper.updateAds(createAds, ads);
        adsRepository.save(ads);
        log.trace("Updated Ads with id: ", id);
        return adsMapper.toAdsDto(ads);
    }

    /**
     * Получает список комментариев к объявлению по его идентификатору.
     * Использует методы:
     * {@link CommentRepository#findAllByAdsId(Integer)},
     * {@link CommentMapper#toListDto(List)}.
     *
     * @param id Идентификатор объявления, для которого нужно получить комментарии.
     * @return Объект ResponseWrapperComment с оберткой, содержащей список DTO комментариев и количество комментариев.
     */
    @Override
    public ResponseWrapperComment getComments(Integer id) {
        List<Comment> commentList = commentRepository.findAllByAdsId(id);
        List<CommentDto> commentDtos = commentMapper.toListDto(commentList);
        ResponseWrapperComment responseWrapperComment = new ResponseWrapperComment();
        responseWrapperComment.setResults(commentDtos);
        responseWrapperComment.setCount(commentDtos.size());
        return responseWrapperComment;
    }

    /**
     * Добавляет новый комментарий к объявлению.
     * Использует методы:
     * {@link AdsRepository#findById(Object)},
     * {@link CommentMapper#toCommentFromCreateComment(CreateComment)},
     * {@link CommentRepository#save(Object)},
     * {@link Comment#getId()} и
     * {@link CommentMapper#toCommentDtoFromComment(Comment)}.
     *
     * @param id            Идентификатор объявления, к которому нужно добавить комментарий.
     * @param createComment Объект CreateComment с информацией о новом комментарии.
     * @param email         Адрес электронной почты пользователя, оставляющего комментарий.
     * @return Объект CommentDto, содержащий информацию о добавленном комментарии.
     * @throws AdsNotFoundException           Если объявление с указанным идентификатором не найдено.
     * @throws UserWithEmailNotFoundException Если пользователя с указанным адресом электронной почты не найдено.
     */
    @Override
    public CommentDto addComment(Integer id, CreateComment createComment, String email) {
        Ads ads = adsRepository.findById(id)
                .orElseThrow(() -> new AdsNotFoundException("Ads not found"));
        Comment comment = commentMapper.toCommentFromCreateComment(createComment);
        comment.setAds(ads);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUser(userRepository.findByEmail(email).get());
        commentRepository.save(comment);
        log.trace("Added comment with id: ", comment.getId());
        return commentMapper.toCommentDtoFromComment(comment);
    }


    /**
     * Удаляет комментарий по идентификаторам объявления и комментария.
     * Использует методы:
     * {@link CommentRepository#deleteByAdsIdAndId(Integer, Integer)}
     *
     * @param adId Идентификатор объявления, к которому привязан комментарий.
     * @param id   Идентификатор комментария, который нужно удалить.
     */
    @Override
    @Transactional
    public void deleteComment(Integer adId, Integer id) {
        commentRepository.deleteByAdsIdAndId(adId, id);
        log.trace("Deleted comment with id: ", id);
    }

    /**
     * Обновляет текст комментария по идентификаторам объявления и комментария.
     * <p>
     * Использует методы:
     * {@link CommentRepository#findCommentByIdAndAds_Id(Integer, Integer)},
     * {@link CommentRepository#save(Object)},
     * {@link CommentMapper#toCommentDtoFromComment(Comment)}.
     *
     * @param adId          Идентификатор объявления, к которому привязан комментарий.
     * @param id            Идентификатор комментария, который нужно обновить.
     * @param createComment Объект CreateComment с обновленными данными для комментария.
     * @return Объект CommentDto, содержащий обновленную информацию о комментарии.
     * @throws CommentNotFoundException Если комментарий с указанными идентификаторами не найден.
     */
    @Override
    public CommentDto updateComment(Integer adId, Integer id, CreateComment createComment) {
        Comment comment = commentRepository.findCommentByIdAndAds_Id(id, adId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found"));
        comment.setText(createComment.getText());
        commentRepository.save(comment);
        log.trace("Updated comment with id: ", id);
        return commentMapper.toCommentDtoFromComment(comment);
    }

    /**
     * Обновляет изображение объявления по его идентификатору.
     * <p>
     * Использует методы:
     * {@link AdsRepository#findById(Object)},
     * {@link ImageService#deleteFileIfNotNull(String)},
     * {@link ImageService#saveImage(MultipartFile, String)} и
     * {@link AdsRepository#save(Object)}.
     *
     * @param id    Идентификатор объявления, для которого нужно обновить изображение.
     * @param image Объект MultipartFile с новым изображением объявления.
     * @throws AdsNotFoundException Если объявление с указанным идентификатором не найдено.
     */
    @Override
    public void updateAdsImage(Integer id, MultipartFile image) {
        Ads ads = adsRepository.findById(id)
                .orElseThrow(() -> new AdsNotFoundException("Ads not found"));
        imageService.deleteFileIfNotNull(ads.getImage());
        ads.setImage(imageService.saveImage(image, "/ads"));
        adsRepository.save(ads);
    }

    /**
     * Получает изображение по его имени.
     * <p>
     * Использует метод {@link ImageService#getImage(String)} для получения изображения по имени.
     *
     * @param name Имя изображения, которое нужно получить.
     * @return Массив байтов, представляющий изображение.
     * @throws IOException Если произошла ошибка при получении изображения.
     */
    @Override
    public byte[] getImage(String name) throws IOException {
        return imageService.getImage(name);
    }

    /**
     * Получает объект CommentDto по идентификаторам объявления и комментария.
     * <p>
     * Использует методы:
     * {@link CommentRepository#findCommentByIdAndAds_Id(Integer, Integer)} и
     * {@link CommentMapper#toCommentDtoFromComment(Comment)}.
     *
     * @param adId Идентификатор объявления, к которому привязан комментарий.
     * @param id   Идентификатор комментария, который нужно получить.
     * @return Объект CommentDto, содержащий информацию о комментарии.
     * @throws CommentNotFoundException Если комментарий с указанными идентификаторами не найден.
     */
    @Override
    public CommentDto getCommentDto(Integer adId, Integer id) {
        Comment comment = commentRepository.findCommentByIdAndAds_Id(id, adId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found"));
        return commentMapper.toCommentDtoFromComment(comment);
    }

    public String getUserNameOfComment(Integer id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found"))
                .getUser().getEmail();
    }
}
