package com.nequi.bco.wks901.r2dbc.adapter;

import com.nequi.bco.wks901.model.enums.TechnicalMessage;
import com.nequi.bco.wks901.model.exceptions.TechnicalException;
import com.nequi.bco.wks901.model.reverso.Reverso;
import com.nequi.bco.wks901.model.reverso.gateways.ReversoRepository;
import com.nequi.bco.wks901.r2dbc.dto.ReversoDTO;
import com.nequi.bco.wks901.r2dbc.mapper.ReversoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.function.Predicate;

/**
 * R2DBC adapter for the REVERSO table. Replaces the legacy ReversoJPAServiceIMPL
 * (EntityManager persist/merge/named-query) with reactive native SQL.
 */
@Slf4j
@RequiredArgsConstructor
public class ReversoAdapter implements ReversoRepository {

    private final DatabaseClient databaseClient;

    private static final String INSERT_SQL = """
            INSERT INTO REVERSO
                (ID_REVERSO, ID_MENSAJE, ID_TRANSACCION, NUMERO_CELULAR, VALOR, MONEDA,
                 TASA_CAMBIO, VALOR_COP, MOTIVO, ESTADO, CANAL, FECHA_CREACION, USUARIO_CREACION)
            VALUES
                (:idReverso, :idMensaje, :idTransaccion, :numeroCelular, :valor, :moneda,
                 :tasaCambio, :valorCop, :motivo, :estado, :canal, :fechaCreacion, :usuarioCreacion)
            """;

    private static final String SELECT_BY_MESSAGE_ID_SQL = """
            SELECT ID_REVERSO, ID_MENSAJE, ID_TRANSACCION, NUMERO_CELULAR, VALOR, MONEDA,
                   TASA_CAMBIO, VALOR_COP, MOTIVO, ESTADO, CANAL, FECHA_CREACION,
                   FECHA_MODIFICACION, USUARIO_CREACION, USUARIO_MODIFICACION
            FROM REVERSO
            WHERE ID_MENSAJE = :idMensaje
            """;

    private static final String SELECT_BY_ID_SQL = """
            SELECT ID_REVERSO, ID_MENSAJE, ID_TRANSACCION, NUMERO_CELULAR, VALOR, MONEDA,
                   TASA_CAMBIO, VALOR_COP, MOTIVO, ESTADO, CANAL, FECHA_CREACION,
                   FECHA_MODIFICACION, USUARIO_CREACION, USUARIO_MODIFICACION
            FROM REVERSO
            WHERE ID_REVERSO = :idReverso
            """;

    private static final String UPDATE_ESTADO_SQL = """
            UPDATE REVERSO
            SET ESTADO = :estado, FECHA_MODIFICACION = :fechaModificacion, USUARIO_MODIFICACION = :usuario
            WHERE ID_REVERSO = :idReverso
            """;

    @Override
    public Mono<Reverso> save(Reverso reverso) {
        return Mono.defer(() -> databaseClient.sql(INSERT_SQL)
                        .bind("idReverso", reverso.getIdReverso())
                        .bind("idMensaje", reverso.getIdMensaje())
                        .bind("idTransaccion", reverso.getIdTransaccion())
                        .bind("numeroCelular", reverso.getNumeroCelular())
                        .bind("valor", reverso.getValor())
                        .bind("moneda", reverso.getMoneda())
                        .bind("tasaCambio", reverso.getTasaCambio())
                        .bind("valorCop", reverso.getValorCop())
                        .bind("motivo", reverso.getMotivo() == null ? "" : reverso.getMotivo())
                        .bind("estado", reverso.getEstado())
                        .bind("canal", reverso.getCanal() == null ? "" : reverso.getCanal())
                        .bind("fechaCreacion", reverso.getFechaCreacion())
                        .bind("usuarioCreacion", reverso.getUsuarioCreacion())
                        .fetch()
                        .rowsUpdated()
                        .thenReturn(reverso))
                .doOnError(error -> log.error("DB save reverso error", error))
                .onErrorMap(Predicate.not(TechnicalException.class::isInstance),
                        ex -> new TechnicalException(ex, TechnicalMessage.DATABASE_ERROR));
    }

    @Override
    public Mono<Reverso> findByMessageId(String idMensaje) {
        return Mono.defer(() -> databaseClient.sql(SELECT_BY_MESSAGE_ID_SQL)
                        .bind("idMensaje", idMensaje)
                        .map((row, metadata) -> mapRow(row))
                        .one()
                        .map(ReversoMapper.MAPPER::dtoToDomain))
                .doOnError(error -> log.error("DB findByMessageId error", error))
                .onErrorMap(Predicate.not(TechnicalException.class::isInstance),
                        ex -> new TechnicalException(ex, TechnicalMessage.DATABASE_ERROR));
    }

    @Override
    public Mono<Reverso> updateEstado(String idReverso, String estado, String usuario) {
        return Mono.defer(() -> databaseClient.sql(UPDATE_ESTADO_SQL)
                        .bind("estado", estado)
                        .bind("fechaModificacion", LocalDateTime.now())
                        .bind("usuario", usuario)
                        .bind("idReverso", idReverso)
                        .fetch()
                        .rowsUpdated()
                        .flatMap(updated -> updated == 0
                                ? Mono.error(new TechnicalException(TechnicalMessage.DATABASE_ERROR))
                                : findById(idReverso)))
                .doOnError(error -> log.error("DB updateEstado error", error))
                .onErrorMap(Predicate.not(TechnicalException.class::isInstance),
                        ex -> new TechnicalException(ex, TechnicalMessage.DATABASE_ERROR));
    }

    private Mono<Reverso> findById(String idReverso) {
        return databaseClient.sql(SELECT_BY_ID_SQL)
                .bind("idReverso", idReverso)
                .map((row, metadata) -> mapRow(row))
                .one()
                .map(ReversoMapper.MAPPER::dtoToDomain);
    }

    private ReversoDTO mapRow(io.r2dbc.spi.Row row) {
        return ReversoDTO.builder()
                .idReverso(row.get("ID_REVERSO", String.class))
                .idMensaje(row.get("ID_MENSAJE", String.class))
                .idTransaccion(row.get("ID_TRANSACCION", String.class))
                .numeroCelular(row.get("NUMERO_CELULAR", String.class))
                .valor(row.get("VALOR", BigDecimal.class))
                .moneda(row.get("MONEDA", String.class))
                .tasaCambio(row.get("TASA_CAMBIO", BigDecimal.class))
                .valorCop(row.get("VALOR_COP", BigDecimal.class))
                .motivo(row.get("MOTIVO", String.class))
                .estado(row.get("ESTADO", String.class))
                .canal(row.get("CANAL", String.class))
                .fechaCreacion(row.get("FECHA_CREACION", LocalDateTime.class))
                .fechaModificacion(row.get("FECHA_MODIFICACION", LocalDateTime.class))
                .usuarioCreacion(row.get("USUARIO_CREACION", String.class))
                .usuarioModificacion(row.get("USUARIO_MODIFICACION", String.class))
                .build();
    }
}
