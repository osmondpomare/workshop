package com.nequi.bco.wks901.r2dbc.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReversoDTO {
    private String idReverso;
    private String idMensaje;
    private String idTransaccion;
    private String numeroCelular;
    private BigDecimal valor;
    private String moneda;
    private BigDecimal tasaCambio;
    private BigDecimal valorCop;
    private String motivo;
    private String estado;
    private String canal;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    private String usuarioCreacion;
    private String usuarioModificacion;
}
