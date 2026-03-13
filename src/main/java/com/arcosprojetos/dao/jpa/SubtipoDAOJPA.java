package com.arcosprojetos.dao.jpa;

import com.arcosprojetos.model.Subtipo;
import com.arcosprojetos.util.JPAUtil;
import jakarta.persistence.EntityManager;
import java.util.List;

/**
 *
 * @author Artur S Costa
 */
public class SubtipoDAOJPA {
    public List<Subtipo> listarPorTipoOrdenadosPorNome(Long idTipo) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                "select s from Subtipo s " +
                "where s.tipo.idTipo = :id " +
                "order by lower(s.nomeSubtipo)", Subtipo.class
            ).setParameter("id", idTipo)
             .getResultList();
        } finally {
            em.close();
        }
    }
}
