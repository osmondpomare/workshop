package com.nequi.bco.wks901.model.reverso.gateways;

import com.nequi.bco.wks901.model.reverso.Reverso;
import reactor.core.publisher.Mono;

public interface ReversoRepository {

    Mono<Reverso> save(Reverso reverso);

    Mono<Reverso> findByMessageId(String idMensaje);

    Mono<Reverso> updateEstado(String idReverso, String estado, String usuario);
}
