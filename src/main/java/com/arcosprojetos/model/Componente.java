package com.arcosprojetos.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Artur S Costa
 */
@Entity
@Table(name = "componente")
public class Componente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_componente")
    private Long idComponente;

    @Column(name = "nome_componente", nullable = false)
    private String nomeComponente;
    
    @Column(name = "codigo", nullable = false, unique = true)
    private String codigo;
    
    @Column(name = "fabricante", length = 100)
    private String fabricante;

    @Column(name = "modelo", length = 100)
    private String modelo;

    @Column(name = "diretorio", length = 255)
    private String diretorio;

    @Column(name = "cad", nullable = false)
    private boolean cad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_subtipo", nullable = false)
    private Subtipo subtipo;

    @OneToMany(mappedBy = "componente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AtributoTecnico> atributosTecnicos = new ArrayList<>();
    
    @Override
    public String toString() {
        return nomeComponente + " (" + codigo + ")";
    }
    
    public Componente() {}

    public void setNomeComponente(String nomeComponente) {
        this.nomeComponente = nomeComponente;
    }

    public void setFabricante(String fabricante) {
        this.fabricante = fabricante;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public void setDiretorio(String diretorio) {
        this.diretorio = diretorio;
    }

    public void setCAD(boolean cad) {
        this.cad = cad;
    }

    public void setSubtipo(Subtipo subtipo) {
        this.subtipo = subtipo;
    }

    public void setAtributosTecnicos(List<AtributoTecnico> atributosTecnicos) {
        this.atributosTecnicos.clear();
        if (atributosTecnicos != null) {
            atributosTecnicos.forEach(this::addAtributoTecnico);
        }
    }

    public Long getIdComponente() {
        return idComponente;
    }

    public String getNomeComponente() {
        return nomeComponente;
    }

    public String getFabricante() {
        return fabricante;
    }

    public String getModelo() {
        return modelo;
    }

    public String getDiretorio() {
        return diretorio;
    }

    public boolean isCAD() {
        return cad;
    }

    public Subtipo getSubtipo() {
        return subtipo;
    }

    public List<AtributoTecnico> getAtributosTecnicos() {
        return atributosTecnicos;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
    
    public void addAtributoTecnico(AtributoTecnico atributo) {
        atributosTecnicos.add(atributo);
        atributo.setComponente(this);
    }

    public void removeAtributoTecnico(AtributoTecnico atributo) {
        atributosTecnicos.remove(atributo);
        atributo.setComponente(null);
    }

}
