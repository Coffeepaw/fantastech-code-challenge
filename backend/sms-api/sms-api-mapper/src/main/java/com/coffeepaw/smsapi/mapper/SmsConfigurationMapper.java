package com.coffeepaw.smsapi.mapper;

import com.coffeepaw.smsapi.model.SmsConfiguration;
import com.coffeepaw.smsapi.model.dto.SmsConfigurationDto;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SmsConfigurationMapper {
    SmsConfigurationMapper INSTANCE = Mappers.getMapper(SmsConfigurationMapper.class);

    SmsConfigurationDto toDto(SmsConfiguration entity);

    SmsConfiguration toEntity(SmsConfigurationDto dto);
}