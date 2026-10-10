package com.nequi.bco.wks901.model.parametro.gateways;

import com.nequi.bco.wks901.model.parametro.Parametro;
import reactor.core.publisher.Mono;

public interface ParametroRepository {

    Mono<Parametro> getParametro(String idParametro);
}
