package com.arcosprojetos.service;

import jakarta.persistence.EntityManager;
import java.text.Normalizer;
import static java.text.Normalizer.normalize;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Artur S Costa
 */
public class GerarCodigoServico {
    
    private static String sigla(String s) {
        if (s == null) return "XXX";
        String n = normalize(s.trim(), Normalizer.Form.NFD);
        n = n.replaceAll("\\p{M}", "");
        n = n.replaceAll("[^A-Za-z]", "");
        n = n.toUpperCase();

        if (n.length() >= 3) return n.substring(0, 3);
        if (n.length() == 2) return n + "X";
        if (n.length() == 1) return n + "XX";
        return "XXX";
    }
    
    private static String norm(String s) {
        if (s == null) return "";
        return s.trim().replaceAll("\\s+", " ");
    }
    
    public static String gerarCodigoCategoriaUnico(EntityManager em, String nomeCategoria, Long idFamilia) {
        List<String> candidatos = new ArrayList<>();

        String base = sigla(nomeCategoria);
        candidatos.add(base);

        String[] palavras = norm(nomeCategoria).split("\\s+");
        if (palavras.length >= 2) {
            String p1 = limparLetras(palavras[0]);
            String p2 = limparLetras(palavras[1]);

            if (p1.length() >= 1 && p2.length() >= 2) candidatos.add((p1.substring(0,1) + p2.substring(0,2)).toUpperCase());
            if (p1.length() >= 2 && p2.length() >= 1) candidatos.add((p1.substring(0,2) + p2.substring(0,1)).toUpperCase());
            if (p1.length() >= 1 && p2.length() >= 1) candidatos.add((p1.substring(0,1) + p2.substring(0,1) + ultimoDigito(p2)).toUpperCase());
        }
        if (palavras.length >= 3) {
            String a = limparLetras(palavras[0]);
            String b = limparLetras(palavras[1]);
            String c = limparLetras(palavras[2]);
            if (!a.isEmpty() && !b.isEmpty() && !c.isEmpty()) {
                candidatos.add(("" + a.charAt(0) + b.charAt(0) + c.charAt(0)).toUpperCase());
            }
        }

        for (String cod : candidatos) {
            cod = pad3(cod);
            if (!cod.isEmpty() && !existeCodigoCategoriaNaFamilia(em, idFamilia, cod)) {
                return cod;
            }
        }

        String prefix2 = pad2(limparLetras(sigla(nomeCategoria)).substring(0,2));
        String sufixos = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        for (int i = 0; i < sufixos.length(); i++) {
            String cod = (prefix2 + sufixos.charAt(i));
            if (!existeCodigoCategoriaNaFamilia(em, idFamilia, cod)) {
                return cod;
            }
        }
        
        throw new IllegalStateException(
            "Não foi possível gerar código único para a categoria '" + nomeCategoria +
            "' na família ID " + idFamilia
        );
    }

    private static boolean existeCodigoCategoriaNaFamilia(EntityManager em, Long idFamilia, String codigo) {
        Long count = em.createQuery("select count(c) from Categoria c " +
            "where c.familia.idFamilia = :idFam and c.codigoCategoria = :cod", Long.class)
                .setParameter("idFam", idFamilia).setParameter("cod", codigo).getSingleResult();
        return count != null && count > 0;
    }

    private static String limparLetras(String s) {
        if (s == null) return "";
        String n = java.text.Normalizer.normalize(s.trim(), java.text.Normalizer.Form.NFD);
        n = n.replaceAll("\\p{M}", "");
        n = n.replaceAll("[^A-Za-z]", "");
        return n.toUpperCase();
    }

    private static String pad3(String s) {
        if (s == null) return "";
        s = limparLetras(s);
        if (s.length() >= 3) return s.substring(0,3);
        if (s.length() == 2) return s + "X";
        if (s.length() == 1) return s + "XX";
        return "XXX";
    }

    private static String pad2(String s) {
        if (s == null) return "XX";
        s = limparLetras(s);
        if (s.length() >= 2) return s.substring(0,2);
        if (s.length() == 1) return s + "X";
        return "XX";
    }

    private static char ultimoDigito(String s) {
        if (s == null || s.isEmpty()) return 'X';
        return s.charAt(s.length() - 1);
    } 
}
