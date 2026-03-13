package com.arcosprojetos.dao.jpa;

import com.arcosprojetos.model.Categoria;
import com.arcosprojetos.util.JPAUtil;
import jakarta.persistence.EntityManager;
import java.util.List;

/**
 *
 * @author Artur S Costa
 */
public class CategoriaDAOJPA {
    public List<Categoria> listarPorFamiliaOrdenadasPorNome(Long idFamilia) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                "select c from Categoria c " +
                "where c.familia.idFamilia = :id " +
                "order by lower(c.nomeCategoria)", Categoria.class
            ).setParameter("id", idFamilia)
             .getResultList();
        } finally {
            em.close();
        }
    } 
}
