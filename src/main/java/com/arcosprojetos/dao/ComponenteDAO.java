package com.arcosprojetos.dao;

import com.arcosprojetos.model.Componente;
import java.util.List;

/**
 *
 * @author Artur S Costa
 */
public interface ComponenteDAO {
    List<Componente> listarTodos();
    void salvar(Componente c);
    void atualizar(Componente c);
    void excluirPorCodigo(String codigo);
    String buscarMaiorCodigoPorPrefixo(String prefixo);
}
