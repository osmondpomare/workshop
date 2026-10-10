package com.nequi.bco.wks901.r2dbc.mapper;

import com.nequi.bco.wks901.model.parametro.Parametro;
import com.nequi.bco.wks901.r2dbc.dto.ParametroDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ParametroMapper {

    ParametroMapper MAPPER = Mappers.getMapper(ParametroMapper.class);

    Parametro dtoToDomain(ParametroDTO dto);

    ParametroDTO domainToDto(Parametro domain);
}
