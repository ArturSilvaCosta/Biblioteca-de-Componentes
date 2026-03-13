package com.arcosprojetos.dao.jpa;

import com.arcosprojetos.util.JPAUtil;
import com.arcosprojetos.model.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;

/**
 *
 * @author Artur S Costa
 */
public class UsuarioDAOJPA {

    public Usuario validarLogin(String login, String senha) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT u FROM Usuario u WHERE u.login = :login AND u.senha = :senha", Usuario.class)
                    .setParameter("login", login)
                    .setParameter("senha", senha)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }
}
