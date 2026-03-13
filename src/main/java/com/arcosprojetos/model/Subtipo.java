package com.arcosprojetos.model;

import jakarta.persistence.*;

/**
 *
 * @author Artur S Costa
 */
@Entity
@Table(name = "subtipo")
public class Subtipo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_subtipo")
    private Long idSubtipo;

    @Column(name = "nome_subtipo", nullable = false, length = 100)
    private String nomeSubtipo;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo", nullable = false)
    private Tipo tipo;
    
    public Subtipo() {}

    public void setIdSubtipo(Long idSubtipo) {
        this.idSubtipo = idSubtipo;
    }

    public void setNomeSubtipo(String nomeSubtipo) {
        this.nomeSubtipo = nomeSubtipo;
    }

    public void setTipo(Tipo tipo) {
        this.tipo = tipo;
    }

    public Long getIdSubtipo() {
        return idSubtipo;
    }

    public String getNomeSubtipo() {
        return nomeSubtipo;
    }

    public Tipo getTipo() {
        return tipo;
    }

    
}
