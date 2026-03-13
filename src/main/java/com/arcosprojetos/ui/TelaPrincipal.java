package com.arcosprojetos.ui;

import com.arcosprojetos.dao.jpa.ComponenteDAOJPA;
import com.arcosprojetos.model.*;
import com.arcosprojetos.service.Servico;
import com.arcosprojetos.util.ComponenteFiltro;
import com.arcosprojetos.util.SessaoUsuario;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.*;
import javax.swing.Timer;

/**
 *
 * @author Artur S Costa
 */
public class TelaPrincipal extends javax.swing.JFrame {

    private final String[] tableColumns = {"CAD", "Código", "Componente", "Fabricante", "Modelo","Detalhes"};
    
    DefaultTableModel tabelaModelo = new DefaultTableModel(tableColumns, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column ==5;
        }
};
  
    private final Servico novoServico = new Servico();
    
    private final Map<String, Familia> mapaFamilias = new LinkedHashMap<>();
    private final Map<String, Categoria> mapaCategorias = new LinkedHashMap<>();
    private final Map<String, Classe> mapaClasses = new LinkedHashMap<>();
    private final Map<String, Tipo> mapaTipos = new LinkedHashMap<>();
    private final Map<String, Subtipo> mapaSubtipos = new LinkedHashMap<>();
    
    private boolean inicializando = true;
    private boolean preenchendoCombos = false;
    private Timer debouncePesquisa;
    
    private static final String PLACEHOLDER_PESQUISA ="Pesquisar por código, nome, fabricante ou modelo.";
    private boolean placeholderAtivo = true;
    private boolean mudandoPlaceholderPesquisa = false;
    
    private List<Componente> cacheExibido = new ArrayList<>();
    
    private List<Componente> cacheTodos = new ArrayList<>();
    

    public TelaPrincipal() {
        inicializando = true;
        initComponents();
        
        configurarAcesso();   
        configurarTabela();
        recarregarCache();
        renderizarTabela(cacheTodos);
        carregarSelectFamilia();
        configurarPesquisaDinamica();
        configurarPlaceholderPesquisa();
        configurarTags();
        
        SwingUtilities.invokeLater(() -> {tabelaComponentes.requestFocusInWindow();});
        
        inicializando = false;
    }   
    
    private void configurarAcesso(){
        Usuario usuario = SessaoUsuario.getUsuarioLogado();
        menuSessaoUsuario.setText(usuario.getNome()+" - Acesso: "+usuario.getNivelAcesso());
        if ("consulta".equalsIgnoreCase(usuario.getNivelAcesso())){
            menuOpcoes.setEnabled(false);
        }
    }
    
    private void configurarTabela(){
        tabelaComponentes.setModel(tabelaModelo);

        tabelaComponentes.getColumnModel().getColumn(0).setPreferredWidth(40);  // CAD
        tabelaComponentes.getColumnModel().getColumn(1).setPreferredWidth(90);  // Código
        tabelaComponentes.getColumnModel().getColumn(2).setPreferredWidth(220); // Nome
        tabelaComponentes.getColumnModel().getColumn(3).setPreferredWidth(140); // Fabricante
        tabelaComponentes.getColumnModel().getColumn(4).setPreferredWidth(120); // Modelo
        int colArquivos=5;
        tabelaComponentes.getColumnModel().getColumn(colArquivos).setCellRenderer(new ButtonRenderer());
        tabelaComponentes.getColumnModel().getColumn(colArquivos).setCellEditor(new ButtonEditor()); 
    }
    
    private void recarregarCache() {
        List<Componente> base = novoServico.listarTodos();
        cacheTodos = (base == null) ? new ArrayList<>() : new ArrayList<>(base);
    }
    
    private Long getIdSelecionadoFamilia() {
        String sel = (String) selectFamilia.getSelectedItem();
        if (sel == null || sel.equals("Selecionar Família")) return null;
        Familia f = mapaFamilias.get(sel);
        return (f != null) ? f.getIdFamilia() : null;
    }
    private Long getIdSelecionadoCategoria() {
        String sel = (String) selectCategoria.getSelectedItem();
        if (sel == null || sel.equals("Selecionar Categoria")) return null;
        Categoria c = mapaCategorias.get(sel);
        return (c != null) ? c.getIdCategoria() : null;
    }
    private Long getIdSelecionadoClasse() {
        String sel = (String) selectClasse.getSelectedItem();
        if (sel == null || sel.equals("Selecionar Classe")) return null;
        Classe c = mapaClasses.get(sel);
        return (c != null) ? c.getIdClasse() : null;
    }
    private Long getIdSelecionadoTipo() {
        String sel = (String) selectTipo.getSelectedItem();
        if (sel == null || sel.equals("Selecionar Tipo")) return null;
        Tipo t = mapaTipos.get(sel);
        return (t != null) ? t.getIdTipo() : null;
    }
    private Long getIdSelecionadoSubtipo() {
        String sel = (String) selectSubtipo.getSelectedItem();
        if (sel == null || sel.equals("Selecionar Subtipo")) return null;
        Subtipo s = mapaSubtipos.get(sel);
        return (s != null) ? s.getIdSubtipo() : null;
    }
    
    private void aplicarFiltrosAtuais() {
        // ids selecionados
        Long idFamilia = getIdSelecionadoFamilia();
        Long idCategoria = getIdSelecionadoCategoria();
        Long idClasse = getIdSelecionadoClasse();
        Long idTipo = getIdSelecionadoTipo();
        Long idSubtipo = getIdSelecionadoSubtipo();

        // Somente CAD?
        boolean somenteCad = checkSomenteCAD.isSelected();

        //Pesquisa dinâmica de texto
        String termo = txtPesquisa.getText();
        boolean aplicarTexto = termo != null && !termo.trim().isEmpty() 
                && !termo.equals(PLACEHOLDER_PESQUISA)&& !placeholderAtivo;
        String termoLower = aplicarTexto ? termo.trim().toLowerCase() : null;

        var criterio = new ComponenteFiltro.Criterio(idFamilia, idCategoria, idClasse, idTipo, idSubtipo, somenteCad, termoLower);

        List<Componente> filtrada = ComponenteFiltro.aplicar(cacheTodos, criterio);
        renderizarTabela(filtrada);
    }
    
    private void renderizarTabela(List<Componente> lista) {
        tabelaModelo.setRowCount(0);

        List<Componente> atual = (lista == null) ? new ArrayList<>() : new ArrayList<>(lista);
        cacheExibido = atual;

        for (Componente c : atual) {
            tabelaModelo.addRow(new Object[]{
                c.isCAD(),
                c.getCodigo(),
                c.getNomeComponente(),
                c.getFabricante(),
                c.getModelo(),
                "Abrir..."
            });
        }
    }

    private void carregarSelectFamilia() {
        preenchendoCombos = true;
        try {

            mapaFamilias.clear();
            selectFamilia.removeAllItems();
            selectFamilia.addItem("Selecionar Família");

            List<Familia> familias = novoServico.listarFamiliasDisponiveis();

            if (familias == null || familias.isEmpty()) {
                selectFamilia.setSelectedIndex(0);
                selectFamilia.setEnabled(true);
                return;
            }

            for (Familia f : familias) {
                if (f == null || f.getNomeFamilia() == null) continue;
                selectFamilia.addItem(f.getNomeFamilia());
                mapaFamilias.put(f.getNomeFamilia(), f);
            }

            selectFamilia.setSelectedIndex(0);
            selectFamilia.setEnabled(true);
            
        } finally {
            preenchendoCombos = false;
        }
    }
    private void carregarSelectCategoria(Familia familia) {
        preenchendoCombos = true;
        try {

            mapaCategorias.clear();
            selectCategoria.removeAllItems();
            selectCategoria.addItem("Selecionar Categoria");

            if (familia == null) {
                selectCategoria.setSelectedIndex(0);
                selectCategoria.setEnabled(false);
                return;}

            for (Categoria c : novoServico.listarCategoriasDisponiveis(familia)) {
                selectCategoria.addItem(c.getNomeCategoria());
                mapaCategorias.put(c.getNomeCategoria(), c);
            }

            selectCategoria.setSelectedIndex(0);
            selectCategoria.setEnabled(true);
           
        } finally {
            preenchendoCombos = false;}
    }
    private void carregarSelectClasse(Categoria categoria) {
        preenchendoCombos = true;
        try {
            mapaClasses.clear();
            selectClasse.removeAllItems();
            selectClasse.addItem("Selecionar Classe");

            if (categoria == null) {
                selectClasse.setSelectedIndex(0);
                selectClasse.setEnabled(false);
                return;}

            for (Classe cl : novoServico.listarClassesDisponiveis(categoria)) {
                selectClasse.addItem(cl.getNomeClasse());
                mapaClasses.put(cl.getNomeClasse(), cl);
            }

            selectClasse.setSelectedIndex(0);
            selectClasse.setEnabled(true);
                        
        } finally {
            preenchendoCombos = false;}
    }
    private void carregarSelectTipo(Classe classe) {
        preenchendoCombos = true;
        try {
            mapaTipos.clear();
            selectTipo.removeAllItems();
            selectTipo.addItem("Selecionar Tipo");

            if (classe == null) {
                selectTipo.setSelectedIndex(0);
                selectTipo.setEnabled(false);
                return;}

            for (Tipo t : novoServico.listarTiposDisponiveis(classe)) {
                selectTipo.addItem(t.getNomeTipo());
                mapaTipos.put(t.getNomeTipo(), t);
            }

            selectTipo.setSelectedIndex(0);
            selectTipo.setEnabled(true);
                        
        } finally {
            preenchendoCombos = false;}
    }
    private void carregarSelectSubtipo(Tipo tipo) {
        preenchendoCombos = true;
        try {
            mapaSubtipos.clear();
            selectSubtipo.removeAllItems();
            selectSubtipo.addItem("Selecionar Subtipo");

            if (tipo == null) {
                selectSubtipo.setSelectedIndex(0);
                selectSubtipo.setEnabled(false);
                return;
            }

            for (Subtipo st : novoServico.listarSubtiposDisponiveis(tipo)) {
                selectSubtipo.addItem(st.getNomeSubtipo());
                mapaSubtipos.put(st.getNomeSubtipo(), st);
            }

            selectSubtipo.setSelectedIndex(0);
            selectSubtipo.setEnabled(true);
                        
        } finally {
            preenchendoCombos = false;}
    }

    private void resetCombo(JComboBox<String> combo, Map<String, ?> mapa, String placeholder, boolean enabled, JComponent tag) {
        mapa.clear();
        combo.removeAllItems();
        combo.addItem(placeholder);
        combo.setSelectedIndex(0);
        combo.setEnabled(enabled);
        if (tag != null) tag.setVisible(false);
    }
    
    private void limparSelectFamilia() {
        resetCombo(selectFamilia, mapaFamilias, "Selecionar Família", true, tagFamilia);
    }
    private void limparSelectCategoria() {
        resetCombo(selectCategoria, mapaCategorias, "Selecionar Categoria", false, tagCategoria);
    }
    private void limparSelectClasse() {
        resetCombo(selectClasse, mapaClasses, "Selecionar Classe", false, tagClasse);
    }
    private void limparSelectTipo() {
        resetCombo(selectTipo, mapaTipos, "Selecionar Tipo", false, tagTipo);
    }
    private void limparSelectSubtipo() {
        resetCombo(selectSubtipo, mapaSubtipos, "Selecionar Subtipo", false, tagSubtipo);
    }
    
    private enum Nivel { FAMILIA, CATEGORIA, CLASSE, TIPO, SUBTIPO }
    
    private void limparAbaixo(Nivel nivel) {
        switch (nivel) {
            case FAMILIA -> {
                limparSelectCategoria();
                limparSelectClasse();
                limparSelectTipo();
                limparSelectSubtipo();
            }
            case CATEGORIA -> {
                limparSelectClasse();
                limparSelectTipo();
                limparSelectSubtipo();
            }
            case CLASSE -> {
                limparSelectTipo();
                limparSelectSubtipo();
            }
            case TIPO -> {
                limparSelectSubtipo();
            }
            case SUBTIPO -> {}
        }
    }  
    
    private void carregarProximoCombo(Nivel nivel) {
        switch (nivel) {

            case FAMILIA -> {
                String sel = (String) selectFamilia.getSelectedItem();
                if (sel == null || sel.equals("Selecionar Família")) return;

                Familia fam = mapaFamilias.get(sel);
                if (fam != null) carregarSelectCategoria(fam);
            }

            case CATEGORIA -> {
                String sel = (String) selectCategoria.getSelectedItem();
                if (sel == null || sel.equals("Selecionar Categoria")) return;

                Categoria cat = mapaCategorias.get(sel);
                if (cat != null) carregarSelectClasse(cat);
            }

            case CLASSE -> {
                String sel = (String) selectClasse.getSelectedItem();
                if (sel == null || sel.equals("Selecionar Classe")) return;

                Classe cl = mapaClasses.get(sel);
                if (cl != null) carregarSelectTipo(cl);
            }

            case TIPO -> {
                String sel = (String) selectTipo.getSelectedItem();
                if (sel == null || sel.equals("Selecionar Tipo")) return;

                Tipo t = mapaTipos.get(sel);
                if (t != null) carregarSelectSubtipo(t);
            }

            case SUBTIPO -> {}
        }
    }

    private void configurarPesquisaDinamica() {
        debouncePesquisa = new javax.swing.Timer(200, e -> aplicarFiltrosAtuais());
        debouncePesquisa.setRepeats(false);

        txtPesquisa.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void changed() {
                if (inicializando || preenchendoCombos || mudandoPlaceholderPesquisa) return;
                debouncePesquisa.restart();
            }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { changed(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { changed(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { changed(); }
        });
    }

    private void configurarPlaceholderPesquisa() {
        if (txtPesquisa.getText() == null || txtPesquisa.getText().trim().isEmpty()) {
            txtPesquisa.setText(PLACEHOLDER_PESQUISA);
            placeholderAtivo = true;
        } else {
            placeholderAtivo = txtPesquisa.getText().equals(PLACEHOLDER_PESQUISA);
        }
    }

    private void configurarTags() {
        tagFamilia.setVisible(false);
        tagCategoria.setVisible(false);
        tagClasse.setVisible(false);
        tagTipo.setVisible(false);
        tagSubtipo.setVisible(false);
    }
    
    public void atualizarTela(){
        recarregarCache();
        carregarSelectFamilia();
        aplicarFiltrosAtuais();
    }
    
    private Componente getComponenteSelecionado() {
        int linhaSelecionada = tabelaComponentes.getSelectedRow();
        if (linhaSelecionada < 0) return null;

        int ComponenteLinha = tabelaComponentes.convertRowIndexToModel(linhaSelecionada);
        if (ComponenteLinha < 0 || ComponenteLinha >= cacheExibido.size()) return null;

        return cacheExibido.get(ComponenteLinha);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        UIDetalhesComponente = new javax.swing.JDialog();
        jPanel5 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButton4 = new javax.swing.JButton();
        jInternalFrame1 = new javax.swing.JInternalFrame();
        jMenuBar1 = new javax.swing.JMenuBar();
        menuOpcoes = new javax.swing.JMenu();
        CadastrarComponente = new javax.swing.JMenuItem();
        botaoEditarExcluir = new javax.swing.JMenuItem();
        menuNomeSistema = new javax.swing.JMenu();
        menuSessaoUsuario = new javax.swing.JMenu();
        txtPesquisa = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        selectFamilia = new javax.swing.JComboBox<>();
        selectCategoria = new javax.swing.JComboBox<>();
        selectClasse = new javax.swing.JComboBox<>();
        selectTipo = new javax.swing.JComboBox<>();
        selectSubtipo = new javax.swing.JComboBox<>();
        jPanel3 = new javax.swing.JPanel();
        checkCADNao = new javax.swing.JCheckBox();
        checkSomenteCAD = new javax.swing.JCheckBox();
        jPanel4 = new javax.swing.JPanel();
        tagFamilia = new javax.swing.JButton();
        tagCategoria = new javax.swing.JButton();
        tagClasse = new javax.swing.JButton();
        tagTipo = new javax.swing.JButton();
        tagSubtipo = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabelaComponentes = new javax.swing.JTable();
        botaoLimpar = new javax.swing.JButton();

        jButton1.setText("jButton1");

        jButton2.setText("jButton2");

        jButton3.setText("jButton3");

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane2.setViewportView(jTable1);

        jButton4.setText("jButton4");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton1)
                .addGap(18, 18, 18)
                .addComponent(jButton2)
                .addGap(18, 18, 18)
                .addComponent(jButton3)
                .addGap(18, 18, 18)
                .addComponent(jButton4)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 375, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 13, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2)
                    .addComponent(jButton3)
                    .addComponent(jButton4))
                .addGap(25, 25, 25))
        );

        javax.swing.GroupLayout UIDetalhesComponenteLayout = new javax.swing.GroupLayout(UIDetalhesComponente.getContentPane());
        UIDetalhesComponente.getContentPane().setLayout(UIDetalhesComponenteLayout);
        UIDetalhesComponenteLayout.setHorizontalGroup(
            UIDetalhesComponenteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(UIDetalhesComponenteLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        UIDetalhesComponenteLayout.setVerticalGroup(
            UIDetalhesComponenteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(UIDetalhesComponenteLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jInternalFrame1.setBorder(null);
        jInternalFrame1.setVisible(true);

        jMenuBar1.setBackground(new java.awt.Color(242, 242, 242));
        jMenuBar1.setForeground(new java.awt.Color(32, 43, 203));
        jMenuBar1.setFont(new java.awt.Font("MuseoSansCyrl-900", 0, 24)); // NOI18N

        menuOpcoes.setText("Opções");
        menuOpcoes.setBorderPainted(false);
        menuOpcoes.setContentAreaFilled(false);
        menuOpcoes.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        menuOpcoes.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);

        CadastrarComponente.setText("Cadastrar Componentes");
        CadastrarComponente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CadastrarComponenteActionPerformed(evt);
            }
        });
        menuOpcoes.add(CadastrarComponente);

        botaoEditarExcluir.setText("Editar/Excluir Componente");
        botaoEditarExcluir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoEditarExcluirActionPerformed(evt);
            }
        });
        menuOpcoes.add(botaoEditarExcluir);

        jMenuBar1.add(menuOpcoes);

        menuNomeSistema.setBorder(null);
        menuNomeSistema.setForeground(new java.awt.Color(32, 43, 203));
        menuNomeSistema.setText("BIBLIOTECA DE COMPONENTES");
        menuNomeSistema.setBorderPainted(false);
        menuNomeSistema.setContentAreaFilled(false);
        menuNomeSistema.setFocusable(false);
        menuNomeSistema.setFont(new java.awt.Font("MuseoSansCyrl-900", 0, 18)); // NOI18N
        menuNomeSistema.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        menuNomeSistema.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        menuNomeSistema.setMaximumSize(new java.awt.Dimension(1150, 32767));
        menuNomeSistema.setPreferredSize(new java.awt.Dimension(679, 22));
        jMenuBar1.add(menuNomeSistema);

        menuSessaoUsuario.setText("User");
        menuSessaoUsuario.setBorderPainted(false);
        menuSessaoUsuario.setContentAreaFilled(false);
        menuSessaoUsuario.setFocusable(false);
        menuSessaoUsuario.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        menuSessaoUsuario.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        jMenuBar1.add(menuSessaoUsuario);

        jInternalFrame1.setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout jInternalFrame1Layout = new javax.swing.GroupLayout(jInternalFrame1.getContentPane());
        jInternalFrame1.getContentPane().setLayout(jInternalFrame1Layout);
        jInternalFrame1Layout.setHorizontalGroup(
            jInternalFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jInternalFrame1Layout.setVerticalGroup(
            jInternalFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        txtPesquisa.setFont(new java.awt.Font("MuseoSansCyrl-700", 0, 16)); // NOI18N
        txtPesquisa.setForeground(new java.awt.Color(47, 81, 221));
        txtPesquisa.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtPesquisaFocusGained(evt);
            }
            public void focusLost(java.awt.event.FocusEvent evt) {
                txtPesquisaFocusLost(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("MuseoSansCyrl-700", 0, 16)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(32, 43, 203));
        jLabel1.setText("FILTRAR RESULTADO");

        selectFamilia.setFont(new java.awt.Font("MuseoSansCyrl-700", 0, 14)); // NOI18N
        selectFamilia.setForeground(new java.awt.Color(47, 81, 221));
        selectFamilia.setMaximumRowCount(999);
        selectFamilia.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Selecionar Família", "Item 2", "Item 3", "Item 4" }));
        selectFamilia.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                selectFamiliaItemStateChanged(evt);
            }
        });

        selectCategoria.setFont(new java.awt.Font("MuseoSansCyrl-700", 0, 14)); // NOI18N
        selectCategoria.setForeground(new java.awt.Color(47, 81, 221));
        selectCategoria.setMaximumRowCount(999);
        selectCategoria.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Selecionar Categoria", "Item 2", "Item 3", "Item 4" }));
        selectCategoria.setEnabled(false);
        selectCategoria.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                selectCategoriaItemStateChanged(evt);
            }
        });

        selectClasse.setFont(new java.awt.Font("MuseoSansCyrl-700", 0, 14)); // NOI18N
        selectClasse.setForeground(new java.awt.Color(47, 81, 221));
        selectClasse.setMaximumRowCount(999);
        selectClasse.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Selecionar Classe", "Item 2", "Item 3", "Item 4" }));
        selectClasse.setEnabled(false);
        selectClasse.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                selectClasseItemStateChanged(evt);
            }
        });

        selectTipo.setFont(new java.awt.Font("MuseoSansCyrl-700", 0, 14)); // NOI18N
        selectTipo.setForeground(new java.awt.Color(47, 81, 221));
        selectTipo.setMaximumRowCount(999);
        selectTipo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Selecionar Tipo", "Item 2", "Item 3", "Item 4" }));
        selectTipo.setEnabled(false);
        selectTipo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                selectTipoItemStateChanged(evt);
            }
        });

        selectSubtipo.setFont(new java.awt.Font("MuseoSansCyrl-700", 0, 14)); // NOI18N
        selectSubtipo.setForeground(new java.awt.Color(47, 81, 221));
        selectSubtipo.setMaximumRowCount(999);
        selectSubtipo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Selecionar Subtipo", "Item 2", "Item 3", "Item 4" }));
        selectSubtipo.setEnabled(false);
        selectSubtipo.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                selectSubtipoItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(selectFamilia, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(selectCategoria, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(selectClasse, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(selectTipo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(selectSubtipo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addComponent(jLabel1)
                .addContainerGap(37, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addGap(12, 12, 12)
                .addComponent(selectFamilia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(selectCategoria, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(selectClasse, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(selectTipo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(selectSubtipo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "SOMENTE CAD?", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("MuseoSansCyrl-700", 0, 18), new java.awt.Color(32, 43, 203))); // NOI18N

        buttonGroup1.add(checkCADNao);
        checkCADNao.setFont(new java.awt.Font("MuseoSansCyrl-700", 0, 16)); // NOI18N
        checkCADNao.setForeground(new java.awt.Color(47, 81, 221));
        checkCADNao.setSelected(true);
        checkCADNao.setText("NÃO");
        checkCADNao.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        checkCADNao.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        checkCADNao.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkCADNaoActionPerformed(evt);
            }
        });

        buttonGroup1.add(checkSomenteCAD);
        checkSomenteCAD.setFont(new java.awt.Font("MuseoSansCyrl-700", 0, 16)); // NOI18N
        checkSomenteCAD.setForeground(new java.awt.Color(47, 81, 221));
        checkSomenteCAD.setText("SIM");
        checkSomenteCAD.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        checkSomenteCAD.setVerticalTextPosition(javax.swing.SwingConstants.TOP);
        checkSomenteCAD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkSomenteCADActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(checkCADNao, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(checkSomenteCAD, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(32, 32, 32))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(checkSomenteCAD)
                    .addComponent(checkCADNao))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "FILTROS APLICADOS", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP, new java.awt.Font("MuseoSansCyrl-700", 0, 18), new java.awt.Color(32, 43, 203))); // NOI18N

        tagFamilia.setBackground(new java.awt.Color(47, 119, 225));
        tagFamilia.setFont(new java.awt.Font("MuseoSansCyrl-300", 0, 12)); // NOI18N
        tagFamilia.setForeground(new java.awt.Color(255, 255, 255));
        tagFamilia.setText("x    Família");
        tagFamilia.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        tagFamilia.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tagFamiliaActionPerformed(evt);
            }
        });

        tagCategoria.setBackground(new java.awt.Color(47, 119, 225));
        tagCategoria.setFont(new java.awt.Font("MuseoSansCyrl-300", 0, 12)); // NOI18N
        tagCategoria.setForeground(new java.awt.Color(255, 255, 255));
        tagCategoria.setText("x    Categoria");
        tagCategoria.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        tagCategoria.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tagCategoriaActionPerformed(evt);
            }
        });

        tagClasse.setBackground(new java.awt.Color(47, 119, 225));
        tagClasse.setFont(new java.awt.Font("MuseoSansCyrl-300", 0, 12)); // NOI18N
        tagClasse.setForeground(new java.awt.Color(255, 255, 255));
        tagClasse.setText("x    Classe");
        tagClasse.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        tagClasse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tagClasseActionPerformed(evt);
            }
        });

        tagTipo.setBackground(new java.awt.Color(47, 119, 225));
        tagTipo.setFont(new java.awt.Font("MuseoSansCyrl-300", 0, 12)); // NOI18N
        tagTipo.setForeground(new java.awt.Color(255, 255, 255));
        tagTipo.setText("x    Tipo");
        tagTipo.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        tagTipo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tagTipoActionPerformed(evt);
            }
        });

        tagSubtipo.setBackground(new java.awt.Color(47, 119, 225));
        tagSubtipo.setFont(new java.awt.Font("MuseoSansCyrl-300", 0, 12)); // NOI18N
        tagSubtipo.setForeground(new java.awt.Color(255, 255, 255));
        tagSubtipo.setText("x    Subtipo");
        tagSubtipo.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        tagSubtipo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tagSubtipoActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tagFamilia, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(tagCategoria, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(tagClasse, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(tagTipo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(tagSubtipo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(tagFamilia)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(tagCategoria)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(tagClasse)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(tagTipo)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(tagSubtipo)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tabelaComponentes.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {}
            },
            new String [] {

            }
        ));
        jScrollPane1.setViewportView(tabelaComponentes);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 995, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 521, Short.MAX_VALUE)
        );

        botaoLimpar.setFont(new java.awt.Font("MuseoSansCyrl-700", 0, 14)); // NOI18N
        botaoLimpar.setForeground(new java.awt.Color(47, 81, 221));
        botaoLimpar.setText("Limpar pesquisa");
        botaoLimpar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoLimparActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jInternalFrame1)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(txtPesquisa)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(botaoLimpar))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 14, Short.MAX_VALUE)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(17, 17, 17))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jInternalFrame1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtPesquisa, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(botaoLimpar, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void selectFamiliaItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_selectFamiliaItemStateChanged
        if (inicializando|| preenchendoCombos) return;
        if (evt.getStateChange() != ItemEvent.SELECTED) return;
        
        String selecionado = (String) selectFamilia.getSelectedItem();
        
        preenchendoCombos = true;
        try {
            limparAbaixo(Nivel.FAMILIA);
            tagFamilia.setVisible(false);
            if (selectFamilia.getSelectedIndex()!=0){
                tagFamilia.setText(selecionado);
                tagFamilia.setVisible(true);
            }
            carregarProximoCombo(Nivel.FAMILIA);
        } finally {
            preenchendoCombos = false;
        }
        aplicarFiltrosAtuais();

    }//GEN-LAST:event_selectFamiliaItemStateChanged

    private void selectCategoriaItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_selectCategoriaItemStateChanged
        if (inicializando|| preenchendoCombos) return;
        if (evt.getStateChange() != ItemEvent.SELECTED) return;

        String selecionado = (String) selectCategoria.getSelectedItem();

        preenchendoCombos = true;
            try {
                limparAbaixo(Nivel.CATEGORIA);
                tagCategoria.setVisible(false);
                if (selectCategoria.getSelectedIndex()!=0){
                    tagCategoria.setText(selecionado);
                    tagCategoria.setVisible(true);
                }
                carregarProximoCombo(Nivel.CATEGORIA);
            } finally {
                preenchendoCombos = false;
            }
            aplicarFiltrosAtuais();
    }//GEN-LAST:event_selectCategoriaItemStateChanged

    private void selectClasseItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_selectClasseItemStateChanged
        if (inicializando|| preenchendoCombos) return;
        if (evt.getStateChange() != ItemEvent.SELECTED) return;

        String selecionado = (String) selectClasse.getSelectedItem();

        preenchendoCombos = true;
        try {
            limparAbaixo(Nivel.CLASSE);
            tagClasse.setVisible(false);
            if (selectClasse.getSelectedIndex()!=0){
                tagClasse.setText(selecionado);
                tagClasse.setVisible(true);
            }
            carregarProximoCombo(Nivel.CLASSE);
        } finally {
            preenchendoCombos = false;
        }
        aplicarFiltrosAtuais();
    }//GEN-LAST:event_selectClasseItemStateChanged

    private void selectTipoItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_selectTipoItemStateChanged
        if (inicializando|| preenchendoCombos) return;
        if (evt.getStateChange() != ItemEvent.SELECTED) return;

        String selecionado = (String) selectTipo.getSelectedItem();
        
        preenchendoCombos = true;
        try {
            limparAbaixo(Nivel.TIPO);
            tagTipo.setVisible(false);
            if (selectTipo.getSelectedIndex()!=0){            
                tagTipo.setText(selecionado);
                tagTipo.setVisible(true);
            }
            carregarProximoCombo(Nivel.TIPO);
        } finally {
            preenchendoCombos = false;
        }
        aplicarFiltrosAtuais();
    }//GEN-LAST:event_selectTipoItemStateChanged

    private void selectSubtipoItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_selectSubtipoItemStateChanged
        if (inicializando|| preenchendoCombos) return;
        if (evt.getStateChange() != ItemEvent.SELECTED) return;

        String selecionado = (String) selectSubtipo.getSelectedItem();
        
        preenchendoCombos = true;
        try {
            limparAbaixo(Nivel.SUBTIPO);
            tagSubtipo.setVisible(false);
            if (selectSubtipo.getSelectedIndex()!=0){   
                tagSubtipo.setText(selecionado);
                tagSubtipo.setVisible(true);
            }
            carregarProximoCombo(Nivel.SUBTIPO);
        } finally {
            preenchendoCombos = false;
        }
        aplicarFiltrosAtuais();
    }//GEN-LAST:event_selectSubtipoItemStateChanged

    private void checkSomenteCADActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkSomenteCADActionPerformed
        if (inicializando || preenchendoCombos) return;
        aplicarFiltrosAtuais();
    }//GEN-LAST:event_checkSomenteCADActionPerformed

    private void checkCADNaoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkCADNaoActionPerformed
        if (inicializando || preenchendoCombos) return;
        aplicarFiltrosAtuais();
    }//GEN-LAST:event_checkCADNaoActionPerformed

    private void txtPesquisaFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtPesquisaFocusGained
        if (!placeholderAtivo) return;

        mudandoPlaceholderPesquisa = true;
        try {
            txtPesquisa.setText("");
            placeholderAtivo = false;
        } finally {
            mudandoPlaceholderPesquisa = false;
        }
    }//GEN-LAST:event_txtPesquisaFocusGained

    private void txtPesquisaFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtPesquisaFocusLost
            if (txtPesquisa.getText() != null && !txtPesquisa.getText().trim().isEmpty()) return;

            mudandoPlaceholderPesquisa = true;
            try {
                txtPesquisa.setText(PLACEHOLDER_PESQUISA);
                placeholderAtivo = true;
            } finally {
                mudandoPlaceholderPesquisa = false;
            }
    }//GEN-LAST:event_txtPesquisaFocusLost

    private void botaoLimparActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botaoLimparActionPerformed
        txtPesquisa.setText(PLACEHOLDER_PESQUISA);
        placeholderAtivo = true;
        mudandoPlaceholderPesquisa = false;
        SwingUtilities.invokeLater(() -> {tabelaComponentes.requestFocusInWindow();});
        aplicarFiltrosAtuais();
    }//GEN-LAST:event_botaoLimparActionPerformed

    private void tagSubtipoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tagSubtipoActionPerformed
        if(preenchendoCombos)return;
        preenchendoCombos=true;
                       
        try {
            selectSubtipo.setSelectedIndex(0);
            tagSubtipo.setVisible(false);
            limparAbaixo(Nivel.SUBTIPO);
            carregarProximoCombo(Nivel.TIPO);
            
        } finally {
            preenchendoCombos = false;
        }
        aplicarFiltrosAtuais();
        SwingUtilities.invokeLater(() -> {tabelaComponentes.requestFocusInWindow();});
    }//GEN-LAST:event_tagSubtipoActionPerformed

    private void tagTipoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tagTipoActionPerformed
        if(preenchendoCombos)return;
        preenchendoCombos=true;
                       
        try {
            selectTipo.setSelectedIndex(0);
            tagTipo.setVisible(false);
            limparAbaixo(Nivel.TIPO);
            carregarProximoCombo(Nivel.CLASSE);
            
        } finally {
            preenchendoCombos = false;
        }
        aplicarFiltrosAtuais();
        SwingUtilities.invokeLater(() -> {tabelaComponentes.requestFocusInWindow();});
    }//GEN-LAST:event_tagTipoActionPerformed

    private void tagClasseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tagClasseActionPerformed
        if(preenchendoCombos)return;
        preenchendoCombos=true;
                       
        try {
            selectClasse.setSelectedIndex(0);
            tagClasse.setVisible(false);
            limparAbaixo(Nivel.CLASSE);
            carregarProximoCombo(Nivel.CATEGORIA);
            
        } finally {
            preenchendoCombos = false;
        }
        aplicarFiltrosAtuais();
        SwingUtilities.invokeLater(() -> {tabelaComponentes.requestFocusInWindow();});
    }//GEN-LAST:event_tagClasseActionPerformed

    private void tagCategoriaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tagCategoriaActionPerformed
        if(preenchendoCombos)return;
        preenchendoCombos=true;
                       
        try {
            selectCategoria.setSelectedIndex(0);
            tagCategoria.setVisible(false);
            limparAbaixo(Nivel.CATEGORIA);
            carregarProximoCombo(Nivel.FAMILIA);
            
        } finally {
            preenchendoCombos = false;
        }
        aplicarFiltrosAtuais();
        SwingUtilities.invokeLater(() -> {tabelaComponentes.requestFocusInWindow();});
    }//GEN-LAST:event_tagCategoriaActionPerformed

    private void tagFamiliaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tagFamiliaActionPerformed
        if(preenchendoCombos)return;
        preenchendoCombos=true;
                       
        try {
            selectFamilia.setSelectedIndex(0);
            tagFamilia.setVisible(false);
            limparAbaixo(Nivel.FAMILIA);
   
        } finally {
            preenchendoCombos = false;
        }
        aplicarFiltrosAtuais();
        SwingUtilities.invokeLater(() -> {tabelaComponentes.requestFocusInWindow();});
    }//GEN-LAST:event_tagFamiliaActionPerformed

    private void CadastrarComponenteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CadastrarComponenteActionPerformed
        TelaCadastro tela = new TelaCadastro(() -> atualizarTela());
        tela.setLocationRelativeTo(this);
        tela.setVisible(true);
    }//GEN-LAST:event_CadastrarComponenteActionPerformed

    private void botaoEditarExcluirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botaoEditarExcluirActionPerformed
        Componente selecionado = getComponenteSelecionado();
        if (selecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um componente na tabela.");
            return;
        }
        
        ComponenteDAOJPA dao = new ComponenteDAOJPA();
        Componente completo = dao.buscarPorIdComAtributos(selecionado.getIdComponente());

        TelaCadastro tela = new TelaCadastro(completo, () -> atualizarTela());
        tela.setLocationRelativeTo(this);
        tela.setVisible(true);
    }//GEN-LAST:event_botaoEditarExcluirActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem CadastrarComponente;
    private javax.swing.JDialog UIDetalhesComponente;
    private javax.swing.JMenuItem botaoEditarExcluir;
    private javax.swing.JButton botaoLimpar;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JCheckBox checkCADNao;
    private javax.swing.JCheckBox checkSomenteCAD;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JInternalFrame jInternalFrame1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTable1;
    private javax.swing.JMenu menuNomeSistema;
    private javax.swing.JMenu menuOpcoes;
    private javax.swing.JMenu menuSessaoUsuario;
    private javax.swing.JComboBox<String> selectCategoria;
    private javax.swing.JComboBox<String> selectClasse;
    private javax.swing.JComboBox<String> selectFamilia;
    private javax.swing.JComboBox<String> selectSubtipo;
    private javax.swing.JComboBox<String> selectTipo;
    private javax.swing.JTable tabelaComponentes;
    private javax.swing.JButton tagCategoria;
    private javax.swing.JButton tagClasse;
    private javax.swing.JButton tagFamilia;
    private javax.swing.JButton tagSubtipo;
    private javax.swing.JButton tagTipo;
    private javax.swing.JTextField txtPesquisa;
    // End of variables declaration//GEN-END:variables

    private class ButtonRenderer extends JButton implements TableCellRenderer {

        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int col) {

            setText(value == null ? "Abrir..." : value.toString());
            return this;
        }
    }  

    private class ButtonEditor extends AbstractCellEditor implements TableCellEditor {

        private final JButton button = new JButton("Abrir...");
        private int row;

        public ButtonEditor() {
            button.addActionListener(e -> {
                try{
                    Componente compSelecionado = getComponenteSelecionado();
                
                    if (compSelecionado == null) return;

                    Long id = compSelecionado.getIdComponente();

                    ComponenteDAOJPA dao = new ComponenteDAOJPA();
                    Componente compCompleto = dao.buscarPorIdComAtributos(id);

                    TelaDetalhes detalhes = new TelaDetalhes(compCompleto);
                    detalhes.setLocationRelativeTo(TelaPrincipal.this);
                    detalhes.setVisible(true);
                }catch(Exception d){
                    JOptionPane.showMessageDialog(TelaPrincipal.this, "Não foi possível localizar o componente",
                        "Erro", JOptionPane.ERROR_MESSAGE);
                }
                
                fireEditingStopped();
            });
        }

        @Override
        public Object getCellEditorValue() {
            return "Abrir...";
        }

        @Override
        public Component getTableCellEditorComponent(
            JTable table, Object value,
            boolean isSelected, int row, int column) {
                this.row=row;
                button.setText("Abrir...");
                return button;
            }
    }

}
