package org.bintec.bco.wks901.jpa.model;

import java.io.Serializable;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Cacheable(true)
@Table(name = "PARAMETRO")
public class Parametro implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID_PARAMETRO", length = 60)
    private String idParametro;

    @Column(name = "VALOR", length = 255, nullable = false)
    private String valor;

    @Column(name = "DESCRIPCION", length = 255)
    private String descripcion;

    public String getIdParametro() {
        return idParametro;
    }

    public void setIdParametro(String idParametro) {
        this.idParametro = idParametro;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
