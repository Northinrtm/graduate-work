package ru.skypro.homework.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import ru.skypro.homework.dto.AdsDto;
import ru.skypro.homework.dto.CreateAds;
import ru.skypro.homework.dto.FullAds;
import ru.skypro.homework.entity.Ads;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AdsMapper {

    AdsMapper INSTANCE = Mappers.getMapper(AdsMapper.class);

    Ads toAdsFromCreateAds(CreateAds createAds);

    @Mapping(target = "author", source = "user.id")
    @Mapping(target = "pk", source = "id")
    AdsDto toAdsDto(Ads ads);

    List<AdsDto> toDtos(List<Ads> adsList);

    @Mapping(target = "pk", source = "id")
    FullAds toFullAds(Ads ads);

    void updateAds(CreateAds createAds, @MappingTarget Ads ads);
}
