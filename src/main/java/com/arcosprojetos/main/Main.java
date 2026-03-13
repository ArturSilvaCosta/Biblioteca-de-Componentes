package com.arcosprojetos.main;

import com.arcosprojetos.ui.TelaLogin;

/**
 * Classe principal para inicializar o programa e definir a tela inicial (GUI).
 * @author Artur S Costa
 */
public class Main {

    public static void main(String[] args) {
    
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } 
        catch (Exception e) {}

        java.awt.EventQueue.invokeLater(() -> {
            TelaLogin telaLogin = new TelaLogin();
            telaLogin.setVisible(true);
            telaLogin.setLocationRelativeTo(null);
        });
        
    }
    
      
}