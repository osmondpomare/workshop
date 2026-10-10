package com.nequi.bco.wks901.model.parametro;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Domain model representing a configuration parameter persisted in table PARAMETRO.
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Parametro {

    String idParametro;
    String valor;
    String descripcion;
}
