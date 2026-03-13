package com.arcosprojetos.util;

import com.arcosprojetos.model.Usuario;

/**
 * Armazena o usuário logado na memória do app 
 * @author Artur S Costa
 */
public class SessaoUsuario {
    private static Usuario usuarioLogado;

    public static void setUsuarioLogado(Usuario usuario) {usuarioLogado = usuario;}

    public static Usuario getUsuarioLogado() {return usuarioLogado;}

    public static void encerrarSessao() {usuarioLogado = null;}
    
    public static String getNivelAcesso() {return usuarioLogado.getNivelAcesso();}
}
