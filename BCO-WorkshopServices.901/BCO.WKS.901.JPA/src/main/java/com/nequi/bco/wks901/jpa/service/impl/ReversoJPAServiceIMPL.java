package com.nequi.bco.wks901.jpa.service.impl;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.nequi.bco.wks901.jpa.exception.EnumJPAException;
import com.nequi.bco.wks901.jpa.exception.JPAException;
import com.nequi.bco.wks901.jpa.model.Reverso;
import com.nequi.bco.wks901.jpa.services.ReversoJPAService;
import com.nequi.bco.wks901.jpa.util.ConstantJPA;

public class ReversoJPAServiceIMPL implements ReversoJPAService {

    private static ReversoJPAServiceIMPL instance;

    private ReversoJPAServiceIMPL() {
    }

    public static synchronized ReversoJPAServiceIMPL getInstance() {
        if (null == instance) {
            instance = new ReversoJPAServiceIMPL();
        }
        return instance;
    }

    public Reverso saveReverso(Reverso reverso, EntityManager em) throws JPAException {
        try {
            em.persist(reverso);
            em.flush();
            return reverso;
        } catch (Exception e) {
            throw new JPAException(EnumJPAException.DB_ERROR,
                    this.getClass().getName() + ConstantJPA.METHOD_SAVE_REVERSO, e);
        }
    }

    public Reverso findByMessageId(String idMensaje, EntityManager em) throws JPAException {
        try {
            TypedQuery<Reverso> query = em.createNamedQuery(ConstantJPA.QUERY_REVERSO_BY_MESSAGE_ID, Reverso.class);
            query.setParameter(ConstantJPA.PARAM_ID_MENSAJE, idMensaje);
            List<Reverso> resultado = query.getResultList();
            if (null == resultado || resultado.isEmpty()) {
                return null;
            }
            return resultado.get(0);
        } catch (Exception e) {
            throw new JPAException(EnumJPAException.DB_ERROR,
                    this.getClass().getName() + ConstantJPA.METHOD_FIND_REVERSO_BY_MESSAGE_ID, e);
        }
    }

    public Reverso updateEstado(String idReverso, String estado, String usuario, EntityManager em)
            throws JPAException {
        Reverso reverso;
        try {
            reverso = em.find(Reverso.class, idReverso);
        } catch (Exception e) {
            throw new JPAException(EnumJPAException.DB_ERROR,
                    this.getClass().getName() + ConstantJPA.METHOD_UPDATE_ESTADO, e);
        }
        if (null == reverso) {
            throw new JPAException(EnumJPAException.NOT_FOUND,
                    this.getClass().getName() + ConstantJPA.METHOD_UPDATE_ESTADO, null);
        }
        try {
            reverso.setEstado(estado);
            reverso.setFechaModificacion(new Date());
            reverso.setUsuarioModificacion(usuario);
            return em.merge(reverso);
        } catch (Exception e) {
            throw new JPAException(EnumJPAException.DB_ERROR,
                    this.getClass().getName() + ConstantJPA.METHOD_UPDATE_ESTADO, e);
        }
    }
}
