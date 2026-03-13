package com.arcosprojetos.service;

import com.arcosprojetos.dao.ComponenteDAO;
import com.arcosprojetos.dao.jpa.*;
import com.arcosprojetos.model.*;
import com.arcosprojetos.util.JPAUtil;
import java.util.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceException;

/**
 *
 * @author Artur S Costa
 */
public class Servico {
    private final ComponenteDAO componenteDAO;
      
    private final FamiliaDAOJPA familiaDAO = new FamiliaDAOJPA();
    private final CategoriaDAOJPA categoriaDAO = new CategoriaDAOJPA();
    private final ClasseDAOJPA classeDAO = new ClasseDAOJPA();
    private final TipoDAOJPA tipoDAO = new TipoDAOJPA();
    private final SubtipoDAOJPA subtipoDAO = new SubtipoDAOJPA();

    public Servico() {
        this(new ComponenteDAOJPA());
    }

    public Servico(ComponenteDAO componenteDAO) {
        this.componenteDAO = Objects.requireNonNull(componenteDAO, "componenteDAO não pode ser null");
    }

    public List<Componente> listarTodos() {
        List<Componente> lista = componenteDAO.listarTodos();
        if (lista == null) return List.of();
        return lista;
    }
    
    public void salvar(Componente c) {
        componenteDAO.salvar(c);
    }
    
    public void atualizar(Componente c) {
        componenteDAO.atualizar(c);
    }
    
    public void excluirPorCodigo(String codigo) {
        componenteDAO.excluirPorCodigo(codigo);
    }
    
    public String gerarCodigo(Componente comp){
        if (comp == null) throw new IllegalArgumentException("Componente nulo.");

        Subtipo st = comp.getSubtipo();
        if (st == null || st.getTipo() == null || st.getTipo().getClasse() == null
                || st.getTipo().getClasse().getCategoria() == null
                || st.getTipo().getClasse().getCategoria().getFamilia() == null) {
            throw new IllegalArgumentException("Hierarquia incompleta para gerar código.");
        }

        Familia fam = st.getTipo().getClasse().getCategoria().getFamilia();
        Categoria cat = st.getTipo().getClasse().getCategoria();
        Classe cl = st.getTipo().getClasse();

        if (fam.getIdFamilia() == null) throw new IllegalArgumentException("idFamilia nulo.");
        if (cl.getIdClasse() == null) throw new IllegalArgumentException("idClasse nulo.");

        String ff = String.format("%02d", fam.getIdFamilia());
        String ccc = cat.getCodigoCategoria();
        if (ccc == null || ccc.length() != 3) {
            throw new IllegalStateException("Código da Categoria inválido: " + cat.getNomeCategoria());
        }
        String clcl = String.format("%02d", cl.getIdClasse());

        String prefixo = ff + ccc + clcl + "-";

        int seq = proximoSequencial(prefixo);
        return prefixo + String.format("%03d", seq);
    }  
   
    private int proximoSequencial(String prefixo) {
        String maxCodigo = componenteDAO.buscarMaiorCodigoPorPrefixo(prefixo);

        int max = 0;
        if (maxCodigo != null && maxCodigo.length() >= prefixo.length() + 3) {
            String nnn = maxCodigo.substring(prefixo.length(), prefixo.length() + 3);
            
            try { max = Integer.parseInt(nnn); } 
            
            catch (NumberFormatException ignored) {}
        }

        if (max >= 999) throw new IllegalStateException("Sequencial esgotado para " + prefixo);
        return max + 1;
    }
    
    public List<Familia> listarFamiliasDisponiveis() {
        return familiaDAO.listarTodasOrdenadasPorNome();
    }

    public List<Categoria> listarCategoriasDisponiveis(Familia familia) {
        if (familia == null || familia.getIdFamilia() == null) return new ArrayList<>();
        return categoriaDAO.listarPorFamiliaOrdenadasPorNome(familia.getIdFamilia());
    }

    public List<Classe> listarClassesDisponiveis(Categoria categoria) {
        if (categoria == null || categoria.getIdCategoria() == null) return new ArrayList<>();
        return classeDAO.listarPorCategoriaOrdenadasPorNome(categoria.getIdCategoria());
    }

    public List<Tipo> listarTiposDisponiveis(Classe classe) {
        if (classe == null || classe.getIdClasse() == null) return new ArrayList<>();
        return tipoDAO.listarPorClasseOrdenadosPorNome(classe.getIdClasse());
    }

    public List<Subtipo> listarSubtiposDisponiveis(Tipo tipo) {
        if (tipo == null || tipo.getIdTipo() == null) return new ArrayList<>();
        return subtipoDAO.listarPorTipoOrdenadosPorNome(tipo.getIdTipo());
    }
    
    private String norm(String s) {
        if (s == null) return "";
        return s.trim().replaceAll("\\s+", " ");
    }
    
    public Subtipo resolverOuCriarHierarquiaPorTexto(String nomeFamilia, String nomeCategoria, String nomeClasse, String nomeTipo, String nomeSubtipo){
        String fam = norm(nomeFamilia);
        String cat = norm(nomeCategoria);
        String cla = norm(nomeClasse);
        String tip = norm(nomeTipo);
        String sub = norm(nomeSubtipo);

        if (fam.isEmpty() || cat.isEmpty() || cla.isEmpty() || tip.isEmpty() || sub.isEmpty()) {
            throw new IllegalArgumentException("Hierarquia incompleta. Preencha Família, Categoria, Classe, Tipo e Subtipo.");
        }
        
        EntityManager em = JPAUtil.getEntityManager();
        
        try {
            em.getTransaction().begin();

            Familia familia = FamiliaPorNome(em, fam);
                if (familia == null) {
                    familia = new Familia();
                    familia.setNomeFamilia(fam);
                    em.persist(familia);
                    em.flush();
                }

            Categoria categoria = CategoriaPorNomeEFamilia(em, cat, familia.getIdFamilia());
                if (categoria == null) {
                    categoria = new Categoria();
                    categoria.setNomeCategoria(cat);
                    categoria.setCodigoCategoria(GerarCodigoServico.gerarCodigoCategoriaUnico(em, cat, familia.getIdFamilia()));
                    categoria.setFamilia(familia);
                    em.persist(categoria);
                    em.flush();
                }

            Classe classe = ClassePorNomeECategoria(em, cla, categoria.getIdCategoria());
                if (classe == null) {
                    classe = new Classe();
                    classe.setNomeClasse(cla);
                    classe.setCategoria(categoria);
                    em.persist(classe);
                    em.flush();
                }

            Tipo tipo = TipoPorNomeEClasse(em, tip, classe.getIdClasse());
                if (tipo == null) {
                    tipo = new Tipo();
                    tipo.setNomeTipo(tip);
                    tipo.setClasse(classe);
                    em.persist(tipo);
                    em.flush();
                }

            Subtipo subtipo = SubtipoPorNomeETipo(em, sub, tipo.getIdTipo());
                if (subtipo == null) {
                    subtipo = new Subtipo();
                    subtipo.setNomeSubtipo(sub);
                    subtipo.setTipo(tipo);
                    em.persist(subtipo);
                    em.flush();
                }

            em.getTransaction().commit();
            return subtipo;
            
        } catch (PersistenceException pe) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw pe;
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw ex;
        } finally {
            em.close();
        }
    }
        
    private Familia FamiliaPorNome(EntityManager em, String nomeFamilia) {
        try {
            return em.createQuery("select f from Familia f where lower(f.nomeFamilia) = :nome", Familia.class)
                .setParameter("nome", nomeFamilia.toLowerCase()).setMaxResults(1).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    private Categoria CategoriaPorNomeEFamilia(EntityManager em, String nomeCategoria, Long idFamilia) {
        try {
            return em.createQuery("select c from Categoria c where c.familia.idFamilia = :idFam and lower(c.nomeCategoria) = :nome",Categoria.class)
                .setParameter("idFam", idFamilia).setParameter("nome", nomeCategoria.toLowerCase())
                .setMaxResults(1).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    private Classe ClassePorNomeECategoria(EntityManager em, String nomeClasse, Long idCategoria) {
        try {
            return em.createQuery("select cl from Classe cl where cl.categoria.idCategoria = :idCat and lower(cl.nomeClasse) = :nome",Classe.class)
                .setParameter("idCat", idCategoria).setParameter("nome", nomeClasse.toLowerCase())
                .setMaxResults(1).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    private Tipo TipoPorNomeEClasse(EntityManager em, String nomeTipo, Long idClasse) {
        try {
            return em.createQuery("select t from Tipo t where t.classe.idClasse = :idCl and lower(t.nomeTipo) = :nome",Tipo.class)
                .setParameter("idCl", idClasse).setParameter("nome", nomeTipo.toLowerCase())
                .setMaxResults(1).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    private Subtipo SubtipoPorNomeETipo(EntityManager em, String nomeSubtipo, Long idTipo) {
        try {
            return em.createQuery("select st from Subtipo st where st.tipo.idTipo = :idTipo and lower(st.nomeSubtipo) = :nome",Subtipo.class)
                .setParameter("idTipo", idTipo).setParameter("nome", nomeSubtipo.toLowerCase())
                .setMaxResults(1).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

}
