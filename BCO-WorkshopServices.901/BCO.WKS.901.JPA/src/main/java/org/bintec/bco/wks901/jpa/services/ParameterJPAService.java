package org.bintec.bco.wks901.jpa.services;

import javax.persistence.EntityManager;

import org.bintec.bco.wks901.jpa.exception.JPAException;
import org.bintec.bco.wks901.jpa.model.Parametro;

public interface ParameterJPAService {

    Parametro getParametro(String idParametro, EntityManager em) throws JPAException;
}
