package com.arcosprojetos.util;

import com.arcosprojetos.model.Componente;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Artur S Costa
 */
public final class ComponenteFiltro {
    
    private ComponenteFiltro(){}
       
    public static final class Criterio {
        public final Long idFamilia;
        public final Long idCategoria;
        public final Long idClasse;
        public final Long idTipo;
        public final Long idSubtipo;
        public final boolean somenteCad;
        public final String termoBuscaLower;
    
        public Criterio(Long idFamilia, Long idCategoria, Long idClasse, Long idTipo, Long idSubtipo,boolean somenteCad, String termoBuscaLower) {
            this.idFamilia = idFamilia;
            this.idCategoria = idCategoria;
            this.idClasse = idClasse;
            this.idTipo = idTipo;
            this.idSubtipo = idSubtipo;
            this.somenteCad = somenteCad;
            this.termoBuscaLower = (termoBuscaLower == null || termoBuscaLower.isBlank()) ? null : termoBuscaLower.trim().toLowerCase();
        }
    }
    
    private static Long getIdFamilia(Componente c) {
        try {
            return c.getSubtipo().getTipo().getClasse().getCategoria().getFamilia().getIdFamilia();
        } catch (Exception e) {
            return null;
        }
    }
    private static Long getIdCategoria(Componente c) {
        try {
            return c.getSubtipo().getTipo().getClasse().getCategoria().getIdCategoria();
        } catch (Exception e) {
            return null;
        }
    }
    private static Long getIdClasse(Componente c) {
        try {
            return c.getSubtipo().getTipo().getClasse().getIdClasse();
        } catch (Exception e) {
            return null;
        }
    }
    private static Long getIdTipo(Componente c) {
        try {
            return c.getSubtipo().getTipo().getIdTipo();
        } catch (Exception e) {
            return null;
        }
    }
    private static Long getIdSubtipo(Componente c) {
        try {
            return c.getSubtipo().getIdSubtipo();
        } catch (Exception e) {
            return null;
        }
    }
        
    private static List<Componente> filtrarPorIdFamilia(List<Componente> base, Long idFamilia) {
        List<Componente> saida = new ArrayList<>();
        for (Componente c : base) {
            Long id = getIdFamilia(c);
            if (id != null && Objects.equals(id, idFamilia)) saida.add(c);
        }
        return saida;
    }
    private static List<Componente> filtrarPorIdCategoria(List<Componente> base, Long idCategoria) {
        List<Componente> saida = new ArrayList<>();
        for (Componente c : base) {
            Long id = getIdCategoria(c);
            if (id != null && Objects.equals(id, idCategoria)) saida.add(c);
        }
        return saida;
    }
    private static List<Componente> filtrarPorIdClasse(List<Componente> base, Long idClasse) {
        List<Componente> saida = new ArrayList<>();
        for (Componente c : base) {
            Long id = getIdClasse(c);
            if (id != null && Objects.equals(id, idClasse)) saida.add(c);
        }
        return saida;
    }
    private static List<Componente> filtrarPorIdTipo(List<Componente> base, Long idTipo) {
        List<Componente> saida = new ArrayList<>();
        for (Componente c : base) {
            Long id = getIdTipo(c);
            if (id != null && Objects.equals(id, idTipo)) saida.add(c);
        }
        return saida;
    }
    private static List<Componente> filtrarPorIdSubtipo(List<Componente> base, Long idSubtipo) {
        List<Componente> saida = new ArrayList<>();
        for (Componente c : base) {
            Long id = getIdSubtipo(c);
            if (id != null && Objects.equals(id, idSubtipo)) saida.add(c);
        }
        return saida;
    }
   
    public static List<Componente> aplicar(List<Componente> base, Criterio c) {
        List<Componente> lista = (base == null) ? new ArrayList<>() : new ArrayList<>(base);
        lista = filtrarHierarquia(lista, c);
        lista = filtrarCad(lista, c.somenteCad);
        lista = filtrarTexto(lista, c.termoBuscaLower);
        return lista;
    }

    private static List<Componente> filtrarHierarquia(List<Componente> base, Criterio c) {
        if (c == null) return base;

        if (c.idSubtipo != null) return filtrarPorIdSubtipo(base, c.idSubtipo);
        if (c.idTipo != null)    return filtrarPorIdTipo(base, c.idTipo);
        if (c.idClasse != null)  return filtrarPorIdClasse(base, c.idClasse);
        if (c.idCategoria != null) return filtrarPorIdCategoria(base, c.idCategoria);
        if (c.idFamilia != null) return filtrarPorIdFamilia(base, c.idFamilia);

        return base;
    }

    private static List<Componente> filtrarCad(List<Componente> base, boolean somenteCad) {
        if (!somenteCad) return base;

        List<Componente> out = new ArrayList<>();
        for (Componente c : base) {
            if (c != null && c.isCAD()) out.add(c);
        }
        return out;
    }

    private static List<Componente> filtrarTexto(List<Componente> base, String qLower) {
        if (qLower == null || qLower.isBlank()) return base;

        List<Componente> saida = new ArrayList<>();
        for (Componente c : base) {
            if (c == null) continue;

            if (contem(c.getCodigo(), qLower)
                || contem(c.getNomeComponente(), qLower)
                || contem(c.getFabricante(), qLower)
                || contem(c.getModelo(), qLower)) {
                saida.add(c);
            }
        }
        return saida;
    }

    private static boolean contem(String campo, String qLower) {
        return campo != null && campo.toLowerCase().contains(qLower);
    }

}