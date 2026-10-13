package org.bintec.bco.wks901.sec.bean.persistence;

import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.bintec.bco.wks901.jpa.exception.JPAException;
import org.bintec.bco.wks901.jpa.model.Parametro;
import org.bintec.bco.wks901.jpa.model.Reverso;
import org.bintec.bco.wks901.jpa.service.impl.ParameterJPAServiceIMPL;
import org.bintec.bco.wks901.jpa.service.impl.ReversoJPAServiceIMPL;
import org.bintec.bco.wks901.jpa.services.ParameterJPAService;
import org.bintec.bco.wks901.jpa.services.ReversoJPAService;
import org.bintec.bco.wks901.sec.util.ConstantWKS901;

@Stateless(name = "ReversoPersistenceBean")
@LocalBean
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class ReversoPersistenceBean {

    @PersistenceContext(unitName = ConstantWKS901.PERSISTENCE_UNIT)
    private EntityManager em;

    private ReversoJPAService reversoJPAService;

    private ParameterJPAService parameterJPAService;

    @PostConstruct
    void init() {
        this.reversoJPAService = ReversoJPAServiceIMPL.getInstance();
        this.parameterJPAService = ParameterJPAServiceIMPL.getInstance();
    }

    public Reverso saveReverso(Reverso reverso) throws JPAException {
        return reversoJPAService.saveReverso(reverso, em);
    }

    public Reverso findByMessageId(String messageId) throws JPAException {
        return reversoJPAService.findByMessageId(messageId, em);
    }

    public Reverso updateEstado(String idReverso, String estado) throws JPAException {
        return reversoJPAService.updateEstado(idReverso, estado, currentUser(), em);
    }

    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public String getParametroValor(String idParametro, String defaultValue) throws JPAException {
        Parametro parametro = parameterJPAService.getParametro(idParametro, em);
        if (null == parametro || null == parametro.getValor() || parametro.getValor().trim().isEmpty()) {
            return defaultValue;
        }
        return parametro.getValor().trim();
    }

    public static String currentUser() {
        String user = System.getenv(ConstantWKS901.CURRENT_USER_ENV);
        if (null == user || user.trim().isEmpty()) {
            return ConstantWKS901.DEFAULT_USER;
        }
        return user.trim();
    }
}
