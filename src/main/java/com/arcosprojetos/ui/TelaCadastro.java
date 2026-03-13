package com.arcosprojetos.ui;

import com.arcosprojetos.model.*;
import com.arcosprojetos.service.Servico;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Artur S Costa
 */
public class TelaCadastro extends javax.swing.JFrame {
  
    private final String[] colAtributos = {"Atributo", "Valor", "Unidade"};

    private final DefaultTableModel modeloAtributos = new DefaultTableModel(colAtributos, 0) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return true;
        }
    };
    
    private final Servico servico = new Servico();
    
    private final Map<String, Familia> mapaFamilias = new LinkedHashMap<>();
    private final Map<String, Categoria> mapaCategorias = new LinkedHashMap<>();
    private final Map<String, Classe> mapaClasses = new LinkedHashMap<>();
    private final Map<String, Tipo> mapaTipos = new LinkedHashMap<>();
    private final Map<String, Subtipo> mapaSubtipos = new LinkedHashMap<>();

    private boolean inicializando = true;
    private boolean preenchendoCombos = false;
    
    private final Runnable onSalvar;
       
    public enum ModoTelaCadastro {NOVO,EDITAR}
    
    private ModoTelaCadastro modo = ModoTelaCadastro.NOVO;
    private Componente componenteEmEdicao = null;
    
    public TelaCadastro() {
        this(null);
    }
    
    public TelaCadastro(Runnable onSalvar) {
        this.onSalvar = onSalvar;
        initComponents();
        inicializarTela();
        configurarModoNovo();
    }

    public TelaCadastro(Componente componente, Runnable onSalvar) {
        this.onSalvar = onSalvar;
        initComponents();
        inicializarTela();
        configurarModoEditar(componente);
    }

    private void inicializarTela(){
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        tabelaAtributos.setModel(modeloAtributos);
        modeloAtributos.addRow(new Object[]{"", "", ""});
        
        selectFamilia.setEnabled(true);
        selectCategoria.setEnabled(false);
        selectClasse.setEnabled(false);
        selectTipo.setEnabled(false);
        selectSubtipo.setEnabled(false);

        carregarSelectFamilia();

        inicializando = false;
    }
    
    private void configurarModoNovo() {
        modo = ModoTelaCadastro.NOVO;
        componenteEmEdicao = null;

        botaoLimparExcluir.setText("LIMPAR");

        botaoSalvar.setText("CRIAR COMPONENTE");
    }
    
    private void configurarModoEditar(Componente comp) {
        modo = ModoTelaCadastro.EDITAR;
        componenteEmEdicao = comp;

        botaoLimparExcluir.setText("EXCLUIR");

        botaoSalvar.setText("SALVAR ALTERAÇÕES");

        carregarDadosDoComponente(comp);
    }
    
    private List<AtributoTecnico> coletarAtributos() {
        List<AtributoTecnico> lista = new ArrayList<>();

        for (int i = 0; i < modeloAtributos.getRowCount(); i++) {
            String nome  = String.valueOf(modeloAtributos.getValueAt(i, 0)).trim();
            String valor = String.valueOf(modeloAtributos.getValueAt(i, 1)).trim();
            String unid  = String.valueOf(modeloAtributos.getValueAt(i, 2)).trim();

            if (nome.isEmpty() || valor.isEmpty() || unid.isEmpty()) {
                if (nome.isEmpty() && valor.isEmpty() && unid.isEmpty()) {
                    continue;
                }

                int resp = JOptionPane.showConfirmDialog(this,
                        "A linha " + (i + 1) + " da tabela de atributos está incompleta.\n"
                        + "Deseja ignorar essa linha e continuar?","Atributo Técnico Incompleto!",
                        JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);

                if (resp == JOptionPane.YES_OPTION) continue;
                else return null;
            }

            AtributoTecnico atributo = new AtributoTecnico();
            atributo.setNomeAtributo(nome);
            atributo.setValorAtributo(valor);
            atributo.setUnidadeMedida(unid);
            lista.add(atributo);
        }

        return lista;
    }
    
    private boolean validarCamposDoComponente(){
        if(txtNome.getText().trim().isEmpty()){
            JOptionPane.showMessageDialog(
                    this,"Informe o Nome do componente","Dados incompletos",JOptionPane.ERROR_MESSAGE
            );
            txtNome.requestFocusInWindow();
            return false;
        }
        if(txtFabricante.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,"Informe o Fabricante do componente.","Dados incompletos",JOptionPane.ERROR_MESSAGE
            );
            txtFabricante.requestFocusInWindow();
            return false;
        }

        if(txtModelo.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,"Informe o Modelo do componente.","Dados incompletos",JOptionPane.ERROR_MESSAGE
            );
            txtModelo.requestFocusInWindow();
            return false;
        }
        return true;
    }
    
    private void carregarSelectFamilia() {
        preenchendoCombos = true;
        try {
            mapaFamilias.clear();
            selectFamilia.removeAllItems();
            selectFamilia.addItem("Selecionar Família");

            for (Familia f : servico.listarFamiliasDisponiveis()) {
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
                return;
            }

            for (Categoria c : servico.listarCategoriasDisponiveis(familia)) {
                if (c == null || c.getNomeCategoria() == null) continue;
                selectCategoria.addItem(c.getNomeCategoria());
                mapaCategorias.put(c.getNomeCategoria(), c);
            }

            selectCategoria.setSelectedIndex(0);
            selectCategoria.setEnabled(true);

        } finally {
            preenchendoCombos = false;
        }
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
                return;
            }

            for (Classe cl : servico.listarClassesDisponiveis(categoria)) {
                if (cl == null || cl.getNomeClasse() == null) continue;
                selectClasse.addItem(cl.getNomeClasse());
                mapaClasses.put(cl.getNomeClasse(), cl);
            }

            selectClasse.setSelectedIndex(0);
            selectClasse.setEnabled(true);

        } finally {
            preenchendoCombos = false;
        }
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
                return;
            }

            for (Tipo t : servico.listarTiposDisponiveis(classe)) {
                if (t == null || t.getNomeTipo() == null) continue;
                selectTipo.addItem(t.getNomeTipo());
                mapaTipos.put(t.getNomeTipo(), t);
            }

            selectTipo.setSelectedIndex(0);
            selectTipo.setEnabled(true);

        } finally {
            preenchendoCombos = false;
        }
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

            for (Subtipo st : servico.listarSubtiposDisponiveis(tipo)) {
                if (st == null || st.getNomeSubtipo() == null) continue;
                selectSubtipo.addItem(st.getNomeSubtipo());
                mapaSubtipos.put(st.getNomeSubtipo(), st);
            }

            selectSubtipo.setSelectedIndex(0);
            selectSubtipo.setEnabled(true);

        } finally {
            preenchendoCombos = false;
        }
    }
    
    private void limparSelectCategoria() {
        mapaCategorias.clear();
        selectCategoria.removeAllItems();
        selectCategoria.addItem("Selecionar Categoria");
        selectCategoria.setSelectedIndex(0);
        selectCategoria.setEnabled(false);
    }
    private void limparSelectClasse() {
        mapaClasses.clear();
        selectClasse.removeAllItems();
        selectClasse.addItem("Selecionar Classe");
        selectClasse.setSelectedIndex(0);
        selectClasse.setEnabled(false);
    }
    private void limparSelectTipo() {
        mapaTipos.clear();
        selectTipo.removeAllItems();
        selectTipo.addItem("Selecionar Tipo");
        selectTipo.setSelectedIndex(0);
        selectTipo.setEnabled(false);
    }
    private void limparSelectSubtipo() {
        mapaSubtipos.clear();
        selectSubtipo.removeAllItems();
        selectSubtipo.addItem("Selecionar Subtipo");
        selectSubtipo.setSelectedIndex(0);
        selectSubtipo.setEnabled(false);
    }
    
    private String norm(String s) {
        if (s == null) return "";
        return s.trim().replaceAll("\\s+", " ");
    }
    private boolean validarHierarquia() {

    if (norm(txtFamilia.getText()).isEmpty()) {
        JOptionPane.showMessageDialog(this,"Informe a Família do componente.","Dados incompletos",JOptionPane.ERROR_MESSAGE);
        txtFamilia.requestFocusInWindow();
        return false;
    }

    if (norm(txtCategoria.getText()).isEmpty()) {
        JOptionPane.showMessageDialog(this,"Informe a Categoria do componente.","Dados incompletos",JOptionPane.ERROR_MESSAGE);
        txtCategoria.requestFocusInWindow();
        return false;
    }

    if (norm(txtClasse.getText()).isEmpty()) {
        JOptionPane.showMessageDialog(this,"Informe a Classe do componente.","Dados incompletos",JOptionPane.ERROR_MESSAGE);
        txtClasse.requestFocusInWindow();
        return false;
    }

    if (norm(txtTipo.getText()).isEmpty()) {
        JOptionPane.showMessageDialog(this,"Informe o Tipo do componente.","Dados incompletos",JOptionPane.ERROR_MESSAGE);
        txtTipo.requestFocusInWindow();
        return false;
    }

    if (norm(txtSubtipo.getText()).isEmpty()) {
        JOptionPane.showMessageDialog(this,"Informe o Subtipo do componente.","Dados incompletos",JOptionPane.ERROR_MESSAGE);
        txtSubtipo.requestFocusInWindow();
        return false;
    }

    return true;
}
    
    private void limparCampos(){
        txtFamilia.setText("");
        txtNome.setText("");
        txtFabricante.setText("");
        txtModelo.setText("");
        modeloAtributos.setRowCount(0);
        modeloAtributos.addRow(new Object[]{"", "", ""});
        possuiCADNao.setSelected(true);
        
        inicializando=true;
        try{
            limparSelectSubtipo();
            limparSelectTipo();
            limparSelectClasse();
            limparSelectCategoria();
            carregarSelectFamilia();
        }finally{
            inicializando=false;
        }
    }
    
    private void carregarDadosDoComponente(Componente comp) {
        if (comp == null) return;

        txtNome.setText(comp.getNomeComponente());
        txtFabricante.setText(comp.getFabricante());
        txtModelo.setText(comp.getModelo());

        if (comp.isCAD()) {
            possuiCADSim.setSelected(true);
        } else {
            possuiCADNao.setSelected(true);
        }

        Subtipo st = comp.getSubtipo();
        if (st != null && st.getTipo() != null && st.getTipo().getClasse() != null
            && st.getTipo().getClasse().getCategoria() != null
            && st.getTipo().getClasse().getCategoria().getFamilia() != null) {

                Familia f = st.getTipo().getClasse().getCategoria().getFamilia();
                Categoria cat = st.getTipo().getClasse().getCategoria();
                Classe cl = st.getTipo().getClasse();
                Tipo t = st.getTipo();

                txtFamilia.setText(f.getNomeFamilia());
                txtCategoria.setText(cat.getNomeCategoria());
                txtClasse.setText(cl.getNomeClasse());
                txtTipo.setText(t.getNomeTipo());
                txtSubtipo.setText(st.getNomeSubtipo());
            }

        modeloAtributos.setRowCount(0);
        if (comp.getAtributosTecnicos() != null && !comp.getAtributosTecnicos().isEmpty()) {
            for (AtributoTecnico a : comp.getAtributosTecnicos()) {
                modeloAtributos.addRow(new Object[]{
                    a.getNomeAtributo(),
                    a.getValorAtributo(),
                    a.getUnidadeMedida()
                });
            }
        } else {
            modeloAtributos.addRow(new Object[]{"", "", ""});
        }
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
        jPanel3 = new javax.swing.JPanel();
        possuiCADNao = new javax.swing.JCheckBox();
        possuiCADSim = new javax.swing.JCheckBox();
        jPanel4 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtNome = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtFabricante = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtModelo = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabelaAtributos = new javax.swing.JTable();
        botaoAddAtributo = new javax.swing.JButton();
        botaoRemoverAtributo = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        txtFamilia = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        txtCategoria = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        txtClasse = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        txtTipo = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        txtSubtipo = new javax.swing.JTextField();
        selectFamilia = new javax.swing.JComboBox<>();
        selectCategoria = new javax.swing.JComboBox<>();
        selectClasse = new javax.swing.JComboBox<>();
        selectTipo = new javax.swing.JComboBox<>();
        selectSubtipo = new javax.swing.JComboBox<>();
        botaoSalvar = new javax.swing.JButton();
        botaoLimparExcluir = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Possui CAD?", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("MuseoSansCyrl-700", 0, 14), new java.awt.Color(32, 43, 203))); // NOI18N

        buttonGroup1.add(possuiCADNao);
        possuiCADNao.setFont(new java.awt.Font("MuseoSansCyrl-700", 0, 16)); // NOI18N
        possuiCADNao.setForeground(new java.awt.Color(47, 81, 221));
        possuiCADNao.setSelected(true);
        possuiCADNao.setText("NÃO");
        possuiCADNao.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        possuiCADNao.setVerticalTextPosition(javax.swing.SwingConstants.TOP);

        buttonGroup1.add(possuiCADSim);
        possuiCADSim.setFont(new java.awt.Font("MuseoSansCyrl-700", 0, 16)); // NOI18N
        possuiCADSim.setForeground(new java.awt.Color(47, 81, 221));
        possuiCADSim.setText("SIM");
        possuiCADSim.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        possuiCADSim.setVerticalTextPosition(javax.swing.SwingConstants.TOP);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(possuiCADNao, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(possuiCADSim, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(possuiCADSim)
                    .addComponent(possuiCADNao))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Preencha as informações do componente", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("MuseoSansCyrl-700", 0, 18))); // NOI18N

        jLabel1.setFont(new java.awt.Font("MuseoSansCyrl-700", 0, 14)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(32, 43, 203));
        jLabel1.setText("Nome do componente");

        txtNome.setFont(new java.awt.Font("MuseoSansCyrl-300", 0, 16)); // NOI18N
        txtNome.setForeground(new java.awt.Color(47, 81, 221));

        jLabel2.setFont(new java.awt.Font("MuseoSansCyrl-700", 0, 14)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(32, 43, 203));
        jLabel2.setText("Fabricante");

        txtFabricante.setFont(new java.awt.Font("MuseoSansCyrl-300", 0, 16)); // NOI18N
        txtFabricante.setForeground(new java.awt.Color(47, 81, 221));

        jLabel3.setFont(new java.awt.Font("MuseoSansCyrl-700", 0, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(32, 43, 203));
        jLabel3.setText("Modelo");

        txtModelo.setFont(new java.awt.Font("MuseoSansCyrl-300", 0, 16)); // NOI18N
        txtModelo.setForeground(new java.awt.Color(47, 81, 221));

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Atributos Técnicos", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("MuseoSansCyrl-700", 0, 14), new java.awt.Color(32, 43, 203))); // NOI18N

        tabelaAtributos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3"
            }
        ));
        jScrollPane1.setViewportView(tabelaAtributos);

        botaoAddAtributo.setText("Adicionar Linha");
        botaoAddAtributo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoAddAtributoActionPerformed(evt);
            }
        });

        botaoRemoverAtributo.setText("Remover Linha");
        botaoRemoverAtributo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoRemoverAtributoActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 304, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(botaoAddAtributo)
                    .addComponent(botaoRemoverAtributo, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(16, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(41, 41, 41)
                .addComponent(botaoAddAtributo, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28)
                .addComponent(botaoRemoverAtributo, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(txtFabricante)
                    .addComponent(txtNome)
                    .addComponent(txtModelo, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtNome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtFabricante, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(txtModelo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Preencha ou selecione a Hierarquia de Categorização", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("MuseoSansCyrl-700", 0, 18))); // NOI18N

        jLabel4.setFont(new java.awt.Font("MuseoSansCyrl-700", 0, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(32, 43, 203));
        jLabel4.setText("Família");

        txtFamilia.setFont(new java.awt.Font("MuseoSansCyrl-300", 0, 16)); // NOI18N
        txtFamilia.setForeground(new java.awt.Color(47, 81, 221));

        jLabel5.setFont(new java.awt.Font("MuseoSansCyrl-700", 0, 14)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(32, 43, 203));
        jLabel5.setText("Categoria");

        txtCategoria.setFont(new java.awt.Font("MuseoSansCyrl-300", 0, 16)); // NOI18N
        txtCategoria.setForeground(new java.awt.Color(47, 81, 221));

        jLabel6.setFont(new java.awt.Font("MuseoSansCyrl-700", 0, 14)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(32, 43, 203));
        jLabel6.setText("Classe");

        txtClasse.setFont(new java.awt.Font("MuseoSansCyrl-300", 0, 16)); // NOI18N
        txtClasse.setForeground(new java.awt.Color(47, 81, 221));

        jLabel7.setFont(new java.awt.Font("MuseoSansCyrl-700", 0, 14)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(32, 43, 203));
        jLabel7.setText("Tipo");

        txtTipo.setFont(new java.awt.Font("MuseoSansCyrl-300", 0, 16)); // NOI18N
        txtTipo.setForeground(new java.awt.Color(47, 81, 221));

        jLabel8.setFont(new java.awt.Font("MuseoSansCyrl-700", 0, 14)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(32, 43, 203));
        jLabel8.setText("Subtipo");

        txtSubtipo.setFont(new java.awt.Font("MuseoSansCyrl-300", 0, 16)); // NOI18N
        txtSubtipo.setForeground(new java.awt.Color(47, 81, 221));

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

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5)
                            .addComponent(jLabel6)
                            .addComponent(jLabel7)
                            .addComponent(jLabel8))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(txtSubtipo)
                            .addComponent(txtTipo)
                            .addComponent(txtClasse)
                            .addComponent(txtCategoria)
                            .addComponent(txtFamilia))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(selectFamilia, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(selectCategoria, 0, 190, Short.MAX_VALUE)
                            .addComponent(selectClasse, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(selectTipo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(selectSubtipo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addGap(7, 7, 7))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtFamilia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(selectFamilia, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtCategoria, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(selectCategoria, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtClasse, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(selectClasse, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtTipo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(selectTipo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtSubtipo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(selectSubtipo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        botaoSalvar.setText("SALVAR ALTERAÇÕES");
        botaoSalvar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoSalvarActionPerformed(evt);
            }
        });

        botaoLimparExcluir.setText("LIMPAR");
        botaoLimparExcluir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                botaoLimparExcluirActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(botaoLimparExcluir, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 34, Short.MAX_VALUE)
                        .addComponent(botaoSalvar, javax.swing.GroupLayout.PREFERRED_SIZE, 166, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(23, 23, 23))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(9, 9, 9)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(botaoLimparExcluir, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(botaoSalvar, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(6, 6, 6)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void selectFamiliaItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_selectFamiliaItemStateChanged
        if (inicializando|| preenchendoCombos) return;
        if (evt.getStateChange() != ItemEvent.SELECTED) return;

        String selecionado = (String) selectFamilia.getSelectedItem();

        limparSelectCategoria();
        limparSelectClasse();
        limparSelectTipo();
        limparSelectSubtipo();

        if (selecionado == null || selecionado.equals("Selecionar Família")) {
            txtFamilia.setText("");
            return;
        }
        
        txtFamilia.setText(selecionado);
                
            Familia familiaSelecionada = mapaFamilias.get(selecionado);
            carregarSelectCategoria(familiaSelecionada);       
    }//GEN-LAST:event_selectFamiliaItemStateChanged

    private void selectCategoriaItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_selectCategoriaItemStateChanged
        if (inicializando|| preenchendoCombos) return;
        if (evt.getStateChange() != ItemEvent.SELECTED) return;

        String selecionado = (String) selectCategoria.getSelectedItem();

        limparSelectClasse();
        limparSelectTipo();
        limparSelectSubtipo();

        if (selecionado == null || selecionado.equals("Selecionar Categoria")) {
            txtCategoria.setText("");
            return;
        }
        
        txtCategoria.setText(selecionado);
                
        Categoria categoriaSelecionada = mapaCategorias.get(selecionado);
        carregarSelectClasse(categoriaSelecionada);
    }//GEN-LAST:event_selectCategoriaItemStateChanged

    private void selectClasseItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_selectClasseItemStateChanged
        if (inicializando|| preenchendoCombos) return;
        if (evt.getStateChange() != ItemEvent.SELECTED) return;

        String selecionado = (String) selectClasse.getSelectedItem();

        limparSelectTipo();
        limparSelectSubtipo();

        if (selecionado == null || selecionado.equals("Selecionar Classe")) {
            txtClasse.setText("");
            return;
        }
        
        txtClasse.setText(selecionado);
                
        Classe classeSelecionada = mapaClasses.get(selecionado);
        carregarSelectTipo(classeSelecionada);
    }//GEN-LAST:event_selectClasseItemStateChanged

    private void selectTipoItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_selectTipoItemStateChanged
        if (inicializando|| preenchendoCombos) return;
        if (evt.getStateChange() != ItemEvent.SELECTED) return;

        String selecionado = (String) selectTipo.getSelectedItem();

        limparSelectSubtipo();

        if (selecionado == null || selecionado.equals("Selecionar Tipo")) {
            txtTipo.setText("");
            return;
        }
        
        txtTipo.setText(selecionado);
                
        Tipo tipoSelecionado = mapaTipos.get(selecionado);
        carregarSelectSubtipo(tipoSelecionado);
    }//GEN-LAST:event_selectTipoItemStateChanged

    private void selectSubtipoItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_selectSubtipoItemStateChanged
        if (inicializando|| preenchendoCombos) return;
        if (evt.getStateChange() != ItemEvent.SELECTED) return;

        String selecionado = (String) selectSubtipo.getSelectedItem();

        if (selecionado == null || selecionado.equals("Selecionar Subtipo")) {
            txtSubtipo.setText("");
            return;
        }
        
        txtSubtipo.setText(selecionado);
    }//GEN-LAST:event_selectSubtipoItemStateChanged

    private void botaoAddAtributoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botaoAddAtributoActionPerformed
            modeloAtributos.addRow(new Object[]{"", "", ""});
    }//GEN-LAST:event_botaoAddAtributoActionPerformed

    private void botaoRemoverAtributoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botaoRemoverAtributoActionPerformed
        int row = tabelaAtributos.getSelectedRow();
        if (row >= 0) {modeloAtributos.removeRow(row);}
    }//GEN-LAST:event_botaoRemoverAtributoActionPerformed

    private void botaoSalvarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botaoSalvarActionPerformed
        if(!validarCamposDoComponente()){
            return;
        }
        if (!validarHierarquia()) {
        return;
        }
        
        Componente comp;

        if (modo == ModoTelaCadastro.EDITAR && componenteEmEdicao != null) {
            comp = componenteEmEdicao;
        } else {
            comp = new Componente();
        }
        
        comp.setNomeComponente(txtNome.getText().trim());
        comp.setFabricante(txtFabricante.getText().trim());
        comp.setModelo(txtModelo.getText().trim());
        
        try{        
            Subtipo st = servico.resolverOuCriarHierarquiaPorTexto(
                    txtFamilia.getText().trim(),
                    txtCategoria.getText().trim(),
                    txtClasse.getText().trim(),
                    txtTipo.getText().trim(),
                    txtSubtipo.getText().trim()
            );
                
            comp.setSubtipo(st);
            
            comp.setCAD(possuiCADSim.isSelected());
            
            if (modo == ModoTelaCadastro.NOVO || comp.getCodigo() == null || comp.getCodigo().isBlank()) {
                comp.setCodigo(servico.gerarCodigo(comp));
            }

            List<AtributoTecnico> atributos = coletarAtributos();
            if(atributos==null) return;
            comp.getAtributosTecnicos().clear();
            comp.setAtributosTecnicos(atributos);
        
            if (modo == ModoTelaCadastro.NOVO) servico.salvar(comp);
            else servico.atualizar(comp);
        
            String msg = (modo==ModoTelaCadastro.NOVO)
                    ? "Componente cadastrado com sucesso!" 
                    : "Componente atualizado com sucesso!";
            JOptionPane.showMessageDialog(this,msg,"Sucesso",JOptionPane.INFORMATION_MESSAGE);
        
            if (onSalvar != null) onSalvar.run();
            dispose();
        }catch (Exception e){
            JOptionPane.showMessageDialog(this,"Erro ao salvar:\n"+e.getMessage(),"Erro!",JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_botaoSalvarActionPerformed

    private void botaoLimparExcluirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botaoLimparExcluirActionPerformed
        if (modo == ModoTelaCadastro.NOVO){
            limparCampos();
            return;
        }
        if (modo == ModoTelaCadastro.EDITAR){
            if (componenteEmEdicao == null || componenteEmEdicao.getCodigo() == null) {
                JOptionPane.showMessageDialog(this, "Componente inválido para exclusão.");
                return;
            }

            int resp = JOptionPane.showConfirmDialog(
                    this,
                    "Deseja realmente excluir o componente " + componenteEmEdicao.getCodigo() + "?",
                    "Confirmar exclusão",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (resp != JOptionPane.YES_OPTION) return;

            servico.excluirPorCodigo(componenteEmEdicao.getCodigo());

            if (onSalvar != null) onSalvar.run();
            dispose();
        }
    }//GEN-LAST:event_botaoLimparExcluirActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton botaoAddAtributo;
    private javax.swing.JButton botaoLimparExcluir;
    private javax.swing.JButton botaoRemoverAtributo;
    private javax.swing.JButton botaoSalvar;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JCheckBox possuiCADNao;
    private javax.swing.JCheckBox possuiCADSim;
    private javax.swing.JComboBox<String> selectCategoria;
    private javax.swing.JComboBox<String> selectClasse;
    private javax.swing.JComboBox<String> selectFamilia;
    private javax.swing.JComboBox<String> selectSubtipo;
    private javax.swing.JComboBox<String> selectTipo;
    private javax.swing.JTable tabelaAtributos;
    private javax.swing.JTextField txtCategoria;
    private javax.swing.JTextField txtClasse;
    private javax.swing.JTextField txtFabricante;
    private javax.swing.JTextField txtFamilia;
    private javax.swing.JTextField txtModelo;
    private javax.swing.JTextField txtNome;
    private javax.swing.JTextField txtSubtipo;
    private javax.swing.JTextField txtTipo;
    // End of variables declaration//GEN-END:variables
}
