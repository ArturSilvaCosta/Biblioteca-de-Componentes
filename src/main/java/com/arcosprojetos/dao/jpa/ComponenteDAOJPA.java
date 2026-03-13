package com.arcosprojetos.dao.jpa;

import com.arcosprojetos.util.JPAUtil;
import com.arcosprojetos.dao.ComponenteDAO;
import com.arcosprojetos.model.Componente;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import java.util.List;

/**
 *
 * @author Artur S Costa
 */
public class ComponenteDAOJPA implements ComponenteDAO {
    
    @Override
    public List<Componente> listarTodos() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                "select c from Componente c " +
                "join fetch c.subtipo st " +
                "join fetch st.tipo t " +
                "join fetch t.classe cl " +
                "join fetch cl.categoria cat " +
                "join fetch cat.familia fam " +
                "order by c.nomeComponente", Componente.class).getResultList();
        } finally {
            em.close();
        }
    }
    
    public Componente buscarPorId(Long id) {
        if (id == null) return null;

        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Componente.class, id);
            
        } finally {
            em.close();
        }
    } 
   
    public void excluirPorId(Long id) {
        if (id == null) return;

        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Componente c = em.find(Componente.class, id);
            if (c != null) em.remove(c);
            em.getTransaction().commit();
            
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
            
        } finally {
            em.close();
        }
    }

    @Override
    public void excluirPorCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) return;

        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Componente c = em.createQuery("select c from Componente c where c.codigo = :codigo", Componente.class)
                .setParameter("codigo", codigo).getSingleResult();
            em.remove(c);
            em.getTransaction().commit();
            
        } catch (NoResultException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
            
        } finally {
            em.close();
        }
    }
    
    @Override
    public void salvar(Componente c) {
        if (c == null) return;

        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(c);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }
    
    @Override
    public void atualizar(Componente c) {
        if (c == null) return;

        EntityManager em = JPAUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(c);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }
    
    @Override
    public String buscarMaiorCodigoPorPrefixo(String prefixo) {
        if (prefixo == null) return null;

        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("select max(c.codigo) from Componente c where c.codigo like :p", String.class)
                .setParameter("p", prefixo + "%").getSingleResult();
            
        } finally {
            em.close();
        }
    }
    
    public Componente buscarPorIdComAtributos(Long idComponente) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                "select distinct c from Componente c join fetch c.subtipo st " +
                "join fetch st.tipo t join fetch t.classe cl " +
                "join fetch cl.categoria cat join fetch cat.familia fam " +
                "left join fetch c.atributosTecnicos where c.idComponente = :id", 
                    Componente.class).setParameter("id", idComponente).getSingleResult();
        } finally {
            em.close();
        }
    }
    
}
