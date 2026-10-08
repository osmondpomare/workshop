package com.nequi.bco.wks901.jpa.service.impl;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.nequi.bco.wks901.jpa.exception.EnumJPAException;
import com.nequi.bco.wks901.jpa.exception.JPAException;
import com.nequi.bco.wks901.jpa.model.Parametro;
import com.nequi.bco.wks901.jpa.services.ParameterJPAService;
import com.nequi.bco.wks901.jpa.util.ConstantJPA;

public class ParameterJPAServiceIMPL implements ParameterJPAService {

    private static ParameterJPAServiceIMPL instance;

    private ParameterJPAServiceIMPL() {
    }

    public static synchronized ParameterJPAServiceIMPL getInstance() {
        if (null == instance) {
            instance = new ParameterJPAServiceIMPL();
        }
        return instance;
    }

    public Parametro getParametro(String idParametro, EntityManager em) throws JPAException {
        try {
            CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
            CriteriaQuery<Parametro> criteriaQuery = criteriaBuilder.createQuery(Parametro.class);
            Root<Parametro> from = criteriaQuery.from(Parametro.class);
            criteriaQuery.select(from);
            criteriaQuery.where(criteriaBuilder.equal(from.get(ConstantJPA.COLUMN_ID_PARAMETRO), idParametro));
            TypedQuery<Parametro> typedQuery = em.createQuery(criteriaQuery);
            return typedQuery.getSingleResult();
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            throw new JPAException(EnumJPAException.DB_ERROR,
                    this.getClass().getName() + ConstantJPA.METHOD_GET_PARAMETRO, e);
        }
    }
}
