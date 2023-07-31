package ru.skypro.homework.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.*;
import ru.skypro.homework.entity.Ads;
import ru.skypro.homework.entity.Comment;
import ru.skypro.homework.entity.User;
import ru.skypro.homework.exception.AdsNotFoundException;
import ru.skypro.homework.exception.UserWithEmailNotFoundException;
import ru.skypro.homework.mapper.AdsMapper;
import ru.skypro.homework.mapper.CommentMapper;
import ru.skypro.homework.repository.AdsRepository;
import ru.skypro.homework.repository.CommentRepository;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.ImageService;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdsServiceImplTest {

    @Mock
    private AdsRepository adsRepository;
    @Mock
    private AdsMapper adsMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ImageService imageService;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private CommentMapper commentMapper;
    @InjectMocks
    private AdsServiceImpl adsService;
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetAllAds() {

        Ads ads1 = new Ads();
        ads1.setId(1);
        ads1.setTitle("Ad 1");

        Ads ads2 = new Ads();
        ads2.setId(2);
        ads2.setTitle("Ad 2");

        List<Ads> adsList = Arrays.asList(ads1, ads2);

        AdsDto adsDto1 = new AdsDto();
        adsDto1.setTitle("Ad 1");

        AdsDto adsDto2 = new AdsDto();
        adsDto2.setTitle("Ad 2");

        List<AdsDto> adsDtoList = Arrays.asList(adsDto1, adsDto2);

        ResponseWrapperAds expectedResponse = new ResponseWrapperAds();
        expectedResponse.setCount(adsList.size());
        expectedResponse.setResults(adsDtoList);

        when(adsRepository.findAll()).thenReturn(adsList);
        when(adsMapper.toDtos(adsList)).thenReturn(adsDtoList);

        ResponseWrapperAds response = adsService.getAllAds();

        assertEquals(expectedResponse.getCount(), response.getCount());
        assertEquals(expectedResponse.getResults(), response.getResults());

        verify(adsRepository, times(1)).findAll();
        verify(adsMapper, times(1)).toDtos(adsList);
    }

    @Test
    void testGetAdsMeWithEmailFound() {
        String email = "test@example.com";
        User user = new User();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        List<Ads> adsList = new ArrayList<>();
        AdsDto adsDto = new AdsDto();
        List<AdsDto> adsDtoList = Arrays.asList(adsDto);
        when(adsRepository.findByUser(user)).thenReturn(adsList);
        when(adsMapper.toDtos(adsList)).thenReturn(adsDtoList);

        ResponseWrapperAds responseWrapperAds = adsService.getAdsMe(email);

        assertEquals(adsDtoList, responseWrapperAds.getResults());
        assertEquals(adsList.size(), responseWrapperAds.getCount());

        verify(userRepository).findByEmail(email);
        verify(adsRepository).findByUser(user);
        verify(adsMapper).toDtos(adsList);
    }

    @Test
    void testGetAdsMeWithEmailNotFound() {
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UserWithEmailNotFoundException.class, () -> adsService.getAdsMe(email));

        verify(userRepository).findByEmail(email);
        verifyNoInteractions(adsRepository);
        verifyNoInteractions(adsMapper);
    }

    @Test
    void testAddAd() {
        String email = "test@example.com";
        CreateAds createAds = new CreateAds();
        User user = new User();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        MockMultipartFile mockImage = new MockMultipartFile(
                "image", "test-image.png", "image/png", new byte[0]
        );
        when(imageService.saveImage(mockImage, "/ads")).thenReturn("path/to/test-image.png");
        Ads ads = new Ads();
        AdsDto adsDto = new AdsDto();
        when(adsMapper.toAdsFromCreateAds(createAds)).thenReturn(ads);
        when(adsMapper.toAdsDto(ads)).thenReturn(adsDto);
        AdsDto result = adsService.addAd(createAds, email, mockImage);
        assertEquals(adsDto, result);
        verify(userRepository).findByEmail(email);
        verify(imageService).saveImage(mockImage, "/ads");
        verify(adsRepository).save(ads);
        verify(adsMapper).toAdsFromCreateAds(createAds);
        verify(adsMapper).toAdsDto(ads);
    }

    @Test
    void testAddAdWithEmailNotFound() {
        String email = "nonexistent@example.com";
        CreateAds createAds = new CreateAds();
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        MockMultipartFile mockImage = new MockMultipartFile(
                "image", "test-image.png", "image/png", new byte[0]
        );
        assertThrows(UserWithEmailNotFoundException.class, () -> adsService.addAd(createAds, email, mockImage));
        verify(userRepository).findByEmail(email);
        verifyNoInteractions(imageService);
        verifyNoInteractions(adsRepository);
    }

    @Test
    void testGetAdsByIdFound() {
        Integer id = 123;
        Ads ads = new Ads();
        when(adsRepository.findById(id)).thenReturn(Optional.of(ads));
        FullAds fullAds = new FullAds();
        when(adsMapper.toFullAds(ads)).thenReturn(fullAds);
        FullAds result = adsService.getAds(id);
        assertEquals(fullAds, result);
        verify(adsRepository).findById(id);
        verify(adsMapper).toFullAds(ads);
    }

    @Test
    void testGetAdsByIdNotFound() {
        Integer id = 123;
        when(adsRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(AdsNotFoundException.class, () -> adsService.getAds(id));
        verify(adsRepository).findById(id);
        verifyNoInteractions(adsMapper);
    }
    @Test
    void testRemoveAd() {
        Integer id = 123;
        Ads ads = new Ads();
        when(adsRepository.findById(id)).thenReturn(Optional.of(ads));

        doNothing().when(commentRepository).deleteAllByAds_Id(id);

        doNothing().when(imageService).deleteFileIfNotNull(anyString());

        adsService.removeAd(id);

        verify(commentRepository).deleteAllByAds_Id(id);
        verify(adsRepository).findById(id);
        verify(imageService).deleteFileIfNotNull(ads.getImage());
        verify(adsRepository).delete(ads);
    }

    @Test
    void testUpdateAds() {
        Integer id = 123;
        CreateAds createAds = new CreateAds();
        Ads ads = new Ads();
        when(adsRepository.findById(id)).thenReturn(Optional.of(ads));
        doNothing().when(adsMapper).updateAds(createAds, ads);
        AdsDto updatedAdsDto = new AdsDto();
        when(adsMapper.toAdsDto(ads)).thenReturn(updatedAdsDto);
        AdsDto result = adsService.updateAds(createAds, id);
        assertEquals(updatedAdsDto, result);
        verify(adsRepository).findById(id);
        verify(adsMapper).updateAds(createAds, ads);
        verify(adsRepository).save(ads);
        verify(adsMapper).toAdsDto(ads);
    }

    @Test
    void testGetComments() {
        Integer id = 123;
        List<Comment> commentList = new ArrayList<>();
        Comment comment1 = new Comment();
        Comment comment2 = new Comment();
        commentList.add(comment1);
        commentList.add(comment2);
        when(commentRepository.findAllByAdsId(id)).thenReturn(commentList);
        List<CommentDto> commentDtoList = new ArrayList<>();
        CommentDto commentDto1 = new CommentDto();
        CommentDto commentDto2 = new CommentDto();
        commentDtoList.add(commentDto1);
        commentDtoList.add(commentDto2);
        when(commentMapper.toListDto(commentList)).thenReturn(commentDtoList);
        ResponseWrapperComment responseWrapperComment = new ResponseWrapperComment();
        responseWrapperComment.setResults(commentDtoList);
        responseWrapperComment.setCount(commentDtoList.size());
        ResponseWrapperComment result = adsService.getComments(id);
        assertEquals(responseWrapperComment, result);
        verify(commentRepository).findAllByAdsId(id);
        verify(commentMapper).toListDto(commentList);
    }

    @Test
    void testAddComment() {
        Integer id = 123;
        CreateComment createComment = new CreateComment();
        String email = "test@example.com";
        Ads ads = new Ads();
        when(adsRepository.findById(id)).thenReturn(Optional.of(ads));
        Comment comment = new Comment();
        when(commentMapper.toCommentFromCreateComment(createComment)).thenReturn(comment);
        LocalDateTime now = LocalDateTime.now();
        comment.setCreatedAt(now);
        User user = new User();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        CommentDto commentDto = new CommentDto();
        when(commentMapper.toCommentDtoFromComment(comment)).thenReturn(commentDto);

        CommentDto result = adsService.addComment(id, createComment, email);

        assertEquals(commentDto, result);

        verify(adsRepository).findById(id);
        verify(commentMapper).toCommentFromCreateComment(createComment);
        verify(commentMapper).toCommentDtoFromComment(comment);
        verify(userRepository).findByEmail(email);
        verify(commentRepository).save(comment);
    }

    @Test
    void testDeleteComment() {
        Integer adId = 123;
        Integer id = 456;

        adsService.deleteComment(adId, id);

        verify(commentRepository).deleteByAdsIdAndId(adId, id);
    }
    @Test
    void testUpdateComment() {
        Integer adId = 123;
        Integer id = 456;
        CreateComment createComment = new CreateComment();
        Comment comment = new Comment();
        when(commentRepository.findCommentByIdAndAds_Id(id, adId)).thenReturn(Optional.of(comment));
        when(commentMapper.toCommentDtoFromComment(comment)).thenReturn(new CommentDto());

        CommentDto result = adsService.updateComment(adId, id, createComment);

        assertNotNull(result);
        verify(commentRepository).findCommentByIdAndAds_Id(id, adId);
        verify(commentMapper).toCommentDtoFromComment(comment);
        verify(commentRepository).save(comment);
    }

    @Test
    void testUpdateAdsImage() {
        Integer id = 123;
        MultipartFile image = new MockMultipartFile("test.jpg", new byte[0]);
        Ads ads = new Ads();
        when(adsRepository.findById(id)).thenReturn(Optional.of(ads));

        adsService.updateAdsImage(id, image);

        verify(adsRepository).findById(id);
        verify(imageService).deleteFileIfNotNull(ads.getImage());
        verify(imageService).saveImage(image, "/ads");
        verify(adsRepository).save(ads);
    }
    @Test
    void testGetCommentDto() {
        Integer adId = 123;
        Integer id = 456;
        Comment comment = new Comment();
        when(commentRepository.findCommentByIdAndAds_Id(id, adId)).thenReturn(Optional.of(comment));
        when(commentMapper.toCommentDtoFromComment(comment)).thenReturn(new CommentDto());

        CommentDto result = adsService.getCommentDto(adId, id);

        assertNotNull(result);
        verify(commentRepository).findCommentByIdAndAds_Id(id, adId);
        verify(commentMapper).toCommentDtoFromComment(comment);
    }
    @Test
    void testGetUserNameOfComment() {
        Integer id = 123;
        Comment comment = new Comment();
        User user = new User();
        user.setEmail("test@example.com");
        comment.setUser(user);
        when(commentRepository.findById(id)).thenReturn(Optional.of(comment));

        String result = adsService.getUserNameOfComment(id);

        assertEquals("test@example.com", result);
        verify(commentRepository).findById(id);
    }
}
