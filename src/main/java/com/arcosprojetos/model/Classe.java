package com.arcosprojetos.model;

import jakarta.persistence.*;

/**
 *
 * @author Artur S Costa
 */
@Entity
@Table(name = "classe")
public class Classe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_classe")
    private Long idClasse;

    @Column(name = "nome_classe", nullable = false, length = 100)
    private String nomeClasse;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;
    
    public Classe() {}

    public void setIdClasse(Long idClasse) {
        this.idClasse = idClasse;
    }

    public void setNomeClasse(String nomeClasse) {
        this.nomeClasse = nomeClasse;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public Long getIdClasse() {
        return idClasse;
    }

    public String getNomeClasse() {
        return nomeClasse;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    
}
