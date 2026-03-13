package com.arcosprojetos.dao.jpa;

import com.arcosprojetos.model.Familia;
import com.arcosprojetos.util.JPAUtil;
import jakarta.persistence.EntityManager;
import java.util.List;

/**
 *
 * @author Artur S Costa
 */
public class FamiliaDAOJPA {
    public List<Familia> listarTodasOrdenadasPorNome() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                "select f from Familia f order by lower(f.nomeFamilia)", Familia.class
            ).getResultList();
        } finally {
            em.close();
        }
    }
}
