package com.arcosprojetos.model;

import jakarta.persistence.*;

/**
 *
 * @author Artur S Costa
 */
@Entity
@Table(name = "familia")
public class Familia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_familia")
    private Long idFamilia;

    @Column(name = "nome_familia", nullable = false, length = 100)
    private String nomeFamilia;
  
    public Familia() {}

    public void setIdFamilia(Long idFamilia) {
        this.idFamilia = idFamilia;
    }

    public void setNomeFamilia(String nomeFamilia) {
        this.nomeFamilia = nomeFamilia;
    }

    public Long getIdFamilia() {
        return idFamilia;
    }

    public String getNomeFamilia() {
        return nomeFamilia;
    }

}
