package ru.skypro.homework.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.AdsDto;
import ru.skypro.homework.dto.CreateAds;
import ru.skypro.homework.dto.FullAds;
import ru.skypro.homework.dto.ResponseWrapperAds;
import ru.skypro.homework.entity.Ads;
import ru.skypro.homework.entity.User;
import ru.skypro.homework.exception.AdsNotFoundException;
import ru.skypro.homework.exception.UserWithEmailNotFoundException;
import ru.skypro.homework.mapper.AdsMapper;
import ru.skypro.homework.repository.AdsRepository;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.ImageService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
    @InjectMocks
    private AdsServiceImpl adsService;
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllAds() {

        List<Ads> mockAdsList = Collections.singletonList(new Ads());
        when(adsRepository.findAll()).thenReturn(mockAdsList);

        List<AdsDto> mockAdsDtoList = Collections.singletonList(new AdsDto());
        when(adsMapper.toDtos(mockAdsList)).thenReturn(mockAdsDtoList);

        ResponseWrapperAds responseWrapperAds = adsService.getAllAds();

        assertEquals(mockAdsDtoList, responseWrapperAds.getResults());
        assertEquals(mockAdsList.size(), responseWrapperAds.getCount());
    }

    @Test
    void testGetAdsMe_Success() {
        String userEmail = "user@example.com";

        User mockUser = new User();
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(mockUser));

        List<Ads> mockAdsList = Collections.singletonList(new Ads());
        when(adsRepository.findByUser(mockUser)).thenReturn(mockAdsList);

        List<AdsDto> mockAdsDtoList = Collections.singletonList(new AdsDto());
        when(adsMapper.toDtos(mockAdsList)).thenReturn(mockAdsDtoList);

        ResponseWrapperAds responseWrapperAds = adsService.getAdsMe(userEmail);

        assertEquals(mockAdsDtoList, responseWrapperAds.getResults());
        assertEquals(mockAdsList.size(), responseWrapperAds.getCount());
    }

    @Test
    void testAddAd_Success() throws Exception {

        CreateAds createAds = new CreateAds();
        String email = "user@example.com";
        MultipartFile image = new MockMultipartFile("test.jpg", new byte[]{});

        User user = new User();
        Ads ads = new Ads();
        AdsDto adsDto = new AdsDto();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        when(adsMapper.toAdsFromCreateAds(createAds)).thenReturn(ads);
        when(adsMapper.toAdsDto(ads)).thenReturn(adsDto);

        when(imageService.saveImage(image, "/ads")).thenReturn("/ads/test.jpg");

        AdsDto result = adsService.addAd(createAds, email, image);

        verify(userRepository).findByEmail(email);
        verify(adsMapper).toAdsFromCreateAds(createAds);
        verify(adsRepository).save(ads);
        verify(imageService).saveImage(image, "/ads");
        verify(adsMapper).toAdsDto(ads);

        assertNotNull(result);
        assertEquals(adsDto, result);
    }

    @Test
    void testAddAd_UserNotFound() {

        CreateAds createAds = new CreateAds();
        String email = "nonexistent_user@example.com";
        MultipartFile image = new MockMultipartFile("test.jpg", new byte[]{});

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UserWithEmailNotFoundException.class,
                () -> adsService.addAd(createAds, email, image));

        verify(userRepository).findByEmail(email);
        verifyNoInteractions(adsMapper, adsRepository, imageService);
    }

    @Test
    void testAddAd_ImageServiceError() throws Exception {

        CreateAds createAds = new CreateAds();
        String email = "user@example.com";
        MultipartFile image = new MockMultipartFile("test.jpg", new byte[]{});

        User user = new User();
        Ads ads = new Ads();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        when(adsMapper.toAdsFromCreateAds(createAds)).thenReturn(ads);

        when(imageService.saveImage(image, "/ads")).thenThrow(new Exception("Error saving image"));

        assertThrows(Exception.class,
                () -> adsService.addAd(createAds, email, image));

        verify(userRepository).findByEmail(email);
        verify(adsMapper).toAdsFromCreateAds(createAds);
        verifyNoMoreInteractions(adsMapper, adsRepository);
        verify(imageService).saveImage(image, "/ads");
    }

    @Test
    void testGetAds_Success() {
        Integer id = 1;
        Ads ads = new Ads();
        FullAds fullAds = new FullAds();
        when(adsRepository.findById(id)).thenReturn(Optional.of(ads));
        when(adsMapper.toFullAds(ads)).thenReturn(fullAds);
        FullAds result = adsService.getAds(id);
        assertNotNull(result);
        assertEquals(fullAds, result);
    }

    @Test
    void testGetAds_AdsNotFound() {
        Integer id = 1;
        when(adsRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(AdsNotFoundException.class,
                () -> adsService.getAds(id));
        verify(adsRepository).findById(id);
        verifyNoInteractions(adsMapper);
    }
}