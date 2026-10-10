package com.nequi.bco.wks901.model.reverso;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Domain model representing a transaction reversal (backout) persisted in table REVERSO.
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Reverso {

    String idReverso;
    String idMensaje;
    String idTransaccion;
    String numeroCelular;
    BigDecimal valor;
    String moneda;
    BigDecimal tasaCambio;
    BigDecimal valorCop;
    String motivo;
    String estado;
    String canal;
    LocalDateTime fechaCreacion;
    LocalDateTime fechaModificacion;
    String usuarioCreacion;
    String usuarioModificacion;
}
