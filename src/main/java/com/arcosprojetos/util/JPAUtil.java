package com.arcosprojetos.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

/**
 *
 * @author Artur S Costa
 */
public class JPAUtil {
    
    private static final EntityManagerFactory FACTORY = Persistence.createEntityManagerFactory("BibliotecaComponentes-PU");

    public static EntityManager getEntityManager() {
        return FACTORY.createEntityManager();
    }
    
    public static void close() {
        if (FACTORY.isOpen()) {
            FACTORY.close();
        }
    }
}
