package ru.skypro.homework.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.*;
import ru.skypro.homework.entity.Ads;
import ru.skypro.homework.entity.Comment;
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

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdsServiceImpl implements AdsService {

    private final AdsRepository adsRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final ImageService imageService;

    @Override
    public ResponseWrapperAds getAllAds() {
        List<Ads> adsList = adsRepository.findAll();
        List<AdsDto> adsDtoList = AdsMapper.INSTANCE.toDtos(adsList);
        ResponseWrapperAds responseWrapperAds = new ResponseWrapperAds();
        responseWrapperAds.setCount(adsList.size());
        responseWrapperAds.setResults(adsDtoList);
        return responseWrapperAds;
    }

    @Override
    public ResponseWrapperAds getAdsMe(String email) {
        List<Ads> adsList = adsRepository.findByUser(userRepository.findByEmail(email).get());
        List<AdsDto> adsDtoList = AdsMapper.INSTANCE.toDtos(adsList);
        ResponseWrapperAds responseWrapperAds = new ResponseWrapperAds();
        responseWrapperAds.setResults(adsDtoList);
        responseWrapperAds.setCount(adsList.size());
        return responseWrapperAds;
    }

    @Override
    public AdsDto addAd(CreateAds createAds, String email, MultipartFile image) {
        Ads ads = AdsMapper.INSTANCE.toAdsFromCreateAds(createAds);
        ads.setUser(userRepository.findByEmail(email)
                .orElseThrow(() -> new UserWithEmailNotFoundException(email)));
        String name = "ad" + ads.getId();
        ads.setImage(imageService.saveImage(image,name));
        adsRepository.save(ads);
        return AdsMapper.INSTANCE.toAdsDto(ads);
    }

    @Override
    public FullAds getAds(Integer id) {
        Ads ads = adsRepository.findById(id)
                .orElseThrow(() -> new AdsNotFoundException("Ads not found by id: " + id));
        return AdsMapper.INSTANCE.toFullAds(ads);
    }

    @Override
    public void removeAd(Integer id) {
        Ads ads = adsRepository.findById(id)
                .orElseThrow(() -> new AdsNotFoundException("Ads not found by id: " + id));
        adsRepository.delete(ads);
    }

    @Override
    public AdsDto updateAds(CreateAds createAds, Integer id) {
        Ads ads = adsRepository.findById(id)
                .orElseThrow(() -> new AdsNotFoundException("Ads not found by id: " + id));
        System.out.println(ads);
        AdsMapper.INSTANCE.updateAds(createAds, ads);
        System.out.println(ads);
        adsRepository.save(ads);
        return AdsMapper.INSTANCE.toAdsDto(ads);
    }

    @Override
    public ResponseWrapperComment getComments(Integer id) {
        List<Comment> commentList = commentRepository.findAllByAdsId(id);
        List<CommentDto> commentDtos = CommentMapper.INSTANCE.toListDto(commentList);
        ResponseWrapperComment responseWrapperComment = new ResponseWrapperComment();
        responseWrapperComment.setResults(commentDtos);
        responseWrapperComment.setCount(commentDtos.size());
        return responseWrapperComment;
    }

    @Override
    public CommentDto addComment(Integer id, CreateComment createComment, String email) {
        Ads ads = adsRepository.findById(id).get();
        Comment comment = CommentMapper.INSTANCE.toCommentFromCreateComment(createComment);
        System.out.println(ads);
        comment.setAds(ads);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUser(userRepository.findByEmail(email).get());
        commentRepository.save(comment);
        System.out.println(comment);
        return CommentMapper.INSTANCE.toCommentDtoFromComment(comment);
    }

    @Override
    public void deleteComment(Integer adId, Integer id) {
        commentRepository.deleteByIdAndAds_Id(id, adId);
    }

    @Override
    public CommentDto updateComment(Integer adId, Integer id, CommentDto commentDto) {
        Comment comment = commentRepository.findCommentByIdAndAds_Id(id, adId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found"));
        comment.setText(commentDto.getText());
        commentRepository.save(comment);
        return CommentMapper.INSTANCE.toCommentDtoFromComment(comment);
    }

    @Override
    public void updateAdsImage(Integer id, MultipartFile image) {
        Ads ads = adsRepository.findById(id)
                .orElseThrow(() -> new AdsNotFoundException("Ads not found"));
        ads.setImage(imageService.saveImage(image,"qweqeqew"));
        adsRepository.save(ads);
    }
}
