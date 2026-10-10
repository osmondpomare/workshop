package com.nequi.bco.wks901.r2dbc.mapper;

import com.nequi.bco.wks901.model.reverso.Reverso;
import com.nequi.bco.wks901.r2dbc.dto.ReversoDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ReversoMapper {

    ReversoMapper MAPPER = Mappers.getMapper(ReversoMapper.class);

    Reverso dtoToDomain(ReversoDTO dto);

    ReversoDTO domainToDto(Reverso domain);
}
