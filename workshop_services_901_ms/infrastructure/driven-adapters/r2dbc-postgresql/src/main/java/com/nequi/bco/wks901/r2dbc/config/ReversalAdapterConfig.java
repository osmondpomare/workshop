package com.nequi.bco.wks901.r2dbc.config;

import com.nequi.bco.wks901.model.parametro.gateways.ParametroRepository;
import com.nequi.bco.wks901.model.reverso.gateways.ReversoRepository;
import com.nequi.bco.wks901.r2dbc.adapter.ParametroAdapter;
import com.nequi.bco.wks901.r2dbc.adapter.ReversoAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.r2dbc.core.DatabaseClient;

@Configuration
public class ReversalAdapterConfig {

    @Bean
    public ReversoRepository reversoRepository(DatabaseClient databaseClient) {
        return new ReversoAdapter(databaseClient);
    }

    @Bean
    public ParametroRepository parametroRepository(DatabaseClient databaseClient) {
        return new ParametroAdapter(databaseClient);
    }
}
