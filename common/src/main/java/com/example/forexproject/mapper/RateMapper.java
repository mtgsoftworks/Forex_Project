package com.example.forexproject.mapper;

import org.mapstruct.Mapper;
import com.example.forexproject.model.Rate;
import com.example.forexproject.dto.RateDto;

/**
 * MapStruct mapper for converting between Rate entity and RateDto.
 */
@Mapper(componentModel = "spring")
public interface RateMapper {

    RateDto toDto(Rate rate);

    Rate toModel(RateDto dto);
}
