package org.bintec.bco.wks901.jpa.services;

import javax.persistence.EntityManager;

import org.bintec.bco.wks901.jpa.exception.JPAException;
import org.bintec.bco.wks901.jpa.model.Reverso;

public interface ReversoJPAService {

    Reverso saveReverso(Reverso reverso, EntityManager em) throws JPAException;

    Reverso findByMessageId(String idMensaje, EntityManager em) throws JPAException;

    Reverso updateEstado(String idReverso, String estado, String usuario, EntityManager em) throws JPAException;
}
