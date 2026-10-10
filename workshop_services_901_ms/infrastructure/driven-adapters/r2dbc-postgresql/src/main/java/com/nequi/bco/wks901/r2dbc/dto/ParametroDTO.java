package com.nequi.bco.wks901.r2dbc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ParametroDTO {
    private String idParametro;
    private String valor;
    private String descripcion;
}
