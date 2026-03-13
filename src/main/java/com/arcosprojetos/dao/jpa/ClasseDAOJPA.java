package com.arcosprojetos.dao.jpa;

import com.arcosprojetos.model.Classe;
import com.arcosprojetos.util.JPAUtil;
import jakarta.persistence.EntityManager;
import java.util.List;

/**
 *
 * @author Artur S Costa
 */
public class ClasseDAOJPA {
    public List<Classe> listarPorCategoriaOrdenadasPorNome(Long idCategoria) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                "select c from Classe c " +
                "where c.categoria.idCategoria = :id " +
                "order by lower(c.nomeClasse)", Classe.class
            ).setParameter("id", idCategoria)
             .getResultList();
        } finally {
            em.close();
        }
    }
}
