package com.arcosprojetos.dao.jpa;

import com.arcosprojetos.model.Tipo;
import com.arcosprojetos.util.JPAUtil;
import jakarta.persistence.EntityManager;
import java.util.List;

/**
 *
 * @author Artur S Costa
 */
public class TipoDAOJPA {
    public List<Tipo> listarPorClasseOrdenadosPorNome(Long idClasse) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                "select t from Tipo t " +
                "where t.classe.idClasse = :id " +
                "order by lower(t.nomeTipo)", Tipo.class
            ).setParameter("id", idClasse)
             .getResultList();
        } finally {
            em.close();
        }
    }
}
