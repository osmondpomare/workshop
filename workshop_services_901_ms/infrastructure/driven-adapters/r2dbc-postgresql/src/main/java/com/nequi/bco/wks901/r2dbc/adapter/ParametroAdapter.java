package com.nequi.bco.wks901.r2dbc.adapter;

import com.nequi.bco.wks901.model.enums.TechnicalMessage;
import com.nequi.bco.wks901.model.exceptions.TechnicalException;
import com.nequi.bco.wks901.model.parametro.Parametro;
import com.nequi.bco.wks901.model.parametro.gateways.ParametroRepository;
import com.nequi.bco.wks901.r2dbc.dto.ParametroDTO;
import com.nequi.bco.wks901.r2dbc.mapper.ParametroMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;

import java.util.function.Predicate;

/**
 * R2DBC adapter for the PARAMETRO table. Replaces the legacy ParameterJPAServiceIMPL
 * (criteria query by idParametro) with reactive native SQL.
 */
@Slf4j
@RequiredArgsConstructor
public class ParametroAdapter implements ParametroRepository {

    private final DatabaseClient databaseClient;

    private static final String SELECT_BY_ID_SQL = """
            SELECT ID_PARAMETRO, VALOR, DESCRIPCION
            FROM PARAMETRO
            WHERE ID_PARAMETRO = :idParametro
            """;

    @Override
    public Mono<Parametro> getParametro(String idParametro) {
        return Mono.defer(() -> databaseClient.sql(SELECT_BY_ID_SQL)
                        .bind("idParametro", idParametro)
                        .map((row, metadata) -> ParametroDTO.builder()
                                .idParametro(row.get("ID_PARAMETRO", String.class))
                                .valor(row.get("VALOR", String.class))
                                .descripcion(row.get("DESCRIPCION", String.class))
                                .build())
                        .one()
                        .map(ParametroMapper.MAPPER::dtoToDomain))
                .doOnError(error -> log.error("DB getParametro error", error))
                .onErrorMap(Predicate.not(TechnicalException.class::isInstance),
                        ex -> new TechnicalException(ex, TechnicalMessage.DATABASE_ERROR));
    }
}
