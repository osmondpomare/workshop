package com.nequi.bco.wks901.jpa.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "REVERSO")
@NamedQueries({
        @NamedQuery(name = "Reverso.findByMessageId", query = "SELECT r FROM Reverso r WHERE r.idMensaje = :idMensaje") })
public class Reverso implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID_REVERSO", length = 36)
    private String idReverso;

    @Column(name = "ID_MENSAJE", length = 64, nullable = false, unique = true)
    private String idMensaje;

    @Column(name = "ID_TRANSACCION", length = 64, nullable = false)
    private String idTransaccion;

    @Column(name = "NUMERO_CELULAR", length = 20, nullable = false)
    private String numeroCelular;

    @Column(name = "VALOR", precision = 18, scale = 2, nullable = false)
    private BigDecimal valor;

    @Column(name = "MONEDA", length = 3, nullable = false)
    private String moneda;

    @Column(name = "TASA_CAMBIO", precision = 18, scale = 6)
    private BigDecimal tasaCambio;

    @Column(name = "VALOR_COP", precision = 18, scale = 2)
    private BigDecimal valorCop;

    @Column(name = "MOTIVO", length = 255)
    private String motivo;

    @Column(name = "ESTADO", length = 20, nullable = false)
    private String estado;

    @Column(name = "CANAL", length = 20)
    private String canal;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "FECHA_CREACION", nullable = false)
    private Date fechaCreacion;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "FECHA_MODIFICACION")
    private Date fechaModificacion;

    @Column(name = "USUARIO_CREACION", length = 50)
    private String usuarioCreacion;

    @Column(name = "USUARIO_MODIFICACION", length = 50)
    private String usuarioModificacion;

    public String getIdReverso() {
        return idReverso;
    }

    public void setIdReverso(String idReverso) {
        this.idReverso = idReverso;
    }

    public String getIdMensaje() {
        return idMensaje;
    }

    public void setIdMensaje(String idMensaje) {
        this.idMensaje = idMensaje;
    }

    public String getIdTransaccion() {
        return idTransaccion;
    }

    public void setIdTransaccion(String idTransaccion) {
        this.idTransaccion = idTransaccion;
    }

    public String getNumeroCelular() {
        return numeroCelular;
    }

    public void setNumeroCelular(String numeroCelular) {
        this.numeroCelular = numeroCelular;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    public BigDecimal getTasaCambio() {
        return tasaCambio;
    }

    public void setTasaCambio(BigDecimal tasaCambio) {
        this.tasaCambio = tasaCambio;
    }

    public BigDecimal getValorCop() {
        return valorCop;
    }

    public void setValorCop(BigDecimal valorCop) {
        this.valorCop = valorCop;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getCanal() {
        return canal;
    }

    public void setCanal(String canal) {
        this.canal = canal;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Date getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(Date fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    public String getUsuarioCreacion() {
        return usuarioCreacion;
    }

    public void setUsuarioCreacion(String usuarioCreacion) {
        this.usuarioCreacion = usuarioCreacion;
    }

    public String getUsuarioModificacion() {
        return usuarioModificacion;
    }

    public void setUsuarioModificacion(String usuarioModificacion) {
        this.usuarioModificacion = usuarioModificacion;
    }
}
