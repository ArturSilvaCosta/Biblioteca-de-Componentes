package com.arcosprojetos.model;

import jakarta.persistence.*;

/**
 *
 * @author Artur S Costa
 */
@Entity
@Table(name = "tipo")
public class Tipo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo")
    private Long idTipo;

    @Column(name = "nome_tipo", nullable = false, length = 100)
    private String nomeTipo;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_classe", nullable = false)
    private Classe classe;
    
    public Tipo() {}

    public void setIdTipo(Long idTipo) {
        this.idTipo = idTipo;
    }

    public void setNomeTipo(String nomeTipo) {
        this.nomeTipo = nomeTipo;
    }

    public void setClasse(Classe classe) {
        this.classe = classe;
    }

    public Long getIdTipo() {
        return idTipo;
    }

    public String getNomeTipo() {
        return nomeTipo;
    }

    public Classe getClasse() {
        return classe;
    }

    
}
