package app.frame;

import app.bean.Contato;
import app.bean.Grupo;
import app.bean.WhatsMessage;
import app.bean.WhatsMessage.Action;
import app.crypto.Crypto;
import app.service.ClienteService;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import sun.applet.Main;

public class ClienteFrame extends javax.swing.JFrame {

    private Socket socket;
    private WhatsMessage message;
    private ClienteService service;
    private Crypto crypto = new Crypto();

    private ArrayList<Contato> contAux = new ArrayList<>();
    private ArrayList<Grupo> gruAux = new ArrayList<>();

    private ArrayList<WhatsMessage> mensagensEnviadasRecebidas = new ArrayList<>();

    private Log log = new Log();

    public ClienteFrame() {
        initComponents();

    }

    private void mostraConversaGrupo() {
        txtAreaReceive.setText("");
        String proprioUsuario = txtName.getText();
        StringBuilder menssagens = new StringBuilder();
        String grupoClicado = listGrupo.getSelectedValue();
        for (WhatsMessage wMessage : mensagensEnviadasRecebidas) {
            if (wMessage.getGrupos().getNome().equals(grupoClicado)) {
                if (wMessage.getName().equals(proprioUsuario)) {
                    menssagens.append("Você disse: " + wMessage.getText());
                } else {
                    menssagens.append(wMessage.getName() + " disse: " + wMessage.getText());
                }
                menssagens.append("\n");
            }
        }
        txtAreaReceive.setText(menssagens.toString());
    }

    private class ListenerSocket implements Runnable {

        private ObjectInputStream input;

        public ListenerSocket(Socket socket) {
            try {
                this.input = new ObjectInputStream(socket.getInputStream());
            } catch (IOException ex) {
                System.out.println(ex);
            }
        }

        @Override
        public void run() {
            WhatsMessage message = null;
            try {
                while ((message = (WhatsMessage) input.readObject()) != null) {
                    Action action = message.getAction();

                    if (action.equals(Action.CONNECT)) {
                        connected(message);
                    } else if (action.equals(Action.DISCONNECT)) {
                        disconnected();
                        socket.close();
                    } else if (action.equals(Action.SEND_ONE)) {
                        System.out.println("::: " + message.getText() + " :::");
                        receive(message);
                    } else if (action.equals(Action.USERS_ONLINE)) {
                        refreshOnlines(message);
                    } else if (action.equals(Action.USERS_CONTACTS)) {
                        refreshContatos(message);
                        refreshOnlines(message);
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(ClienteFrame.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(ClienteFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void connected(WhatsMessage message) {
        if (message.getText().equals("NO")) {
            this.txtName.setText("");
            JOptionPane.showMessageDialog(this, "Conexão não realizada!\nTente novamente com um novo nome.");
            return;
        }

        this.message = message;
        this.btnConnectar.setEnabled(false);
        this.txtName.setEditable(false);

        this.btnSair.setEnabled(true);
        this.txtAreaSend.setEnabled(true);
        this.txtAreaReceive.setEnabled(true);
        this.btnEnviar.setEnabled(true);
        this.btnLimpar.setEnabled(true);

        this.btnAddContato.setEnabled(true);
        this.nomeContatoAdd.setEnabled(true);

        this.btnAddGrupo.setEnabled(true);
        this.nomeGrupoAdd.setEnabled(true);

        this.comboBoxGrupos.setEnabled(true);
        this.comboBoxContatos.setEnabled(true);
        this.btnAddContatoGrupo.setEnabled(true);
        this.log = new Log(this.txtName.getText());

        /* teste
        Set<String> names = new HashSet<String>();
        if(this.message.getContatos().size() > 0){
            for(Contato con : this.message.getContatos()){
                names.add(con.getNome());
            }
            String[] array = (String[]) names.toArray(new String[names.size()]);
            listContatos.setListData(array);
        }*/
        JOptionPane.showMessageDialog(this, "Conectado!");
        if (message.getOfflineMessages().size() > 0) {
            StringBuilder allMessages = new StringBuilder("Mensagens recebidas offline: \n");
            for (WhatsMessage offlineMessage : message.getOfflineMessages()) {
                allMessages.append(offlineMessage.getName()).append(" disse: ").append(offlineMessage.getText()).append("\n");
                message.setText(offlineMessage.getText());
                message.setNameReserved(offlineMessage.getName());
                receive(message);
                log.leArquivo(message.getName(), message.getNameReserved());
            }
            JOptionPane.showMessageDialog(this, allMessages.toString());
        }
    }

    private void disconnected() {

        this.btnConnectar.setEnabled(true);
        this.txtName.setEditable(true);

        this.btnSair.setEnabled(false);
        this.txtAreaSend.setEnabled(false);
        this.txtAreaReceive.setEnabled(false);
        this.btnEnviar.setEnabled(false);
        this.btnLimpar.setEnabled(false);

        this.btnAddContato.setEnabled(false);
        this.nomeContatoAdd.setEnabled(false);

        this.btnAddGrupo.setEnabled(false);
        this.nomeGrupoAdd.setEnabled(false);

        this.comboBoxGrupos.setEnabled(false);
        this.comboBoxContatos.setEnabled(false);
        this.btnAddContatoGrupo.setEnabled(false);

        this.txtAreaReceive.setText("");
        this.txtAreaSend.setText("");

        JOptionPane.showMessageDialog(this, "Desconectado!");
    }

//    private void receive(WhatsMessage message) {
//        this.txtAreaReceive.append(message.getName() + " diz: " + message.getText() + "\n");
//    }
    private void receive(WhatsMessage message) {
        if(message.getText().length() == 32){
            System.out.println("app.frame.ClienteFrame.receive()");
        }
            incluiOuEditaGrupo();
        if (message.getGrupos().getNome()!= null && !message.getGrupos().getNome().equals("")) {
            message.getGrupos().getContatosGrupo().forEach(cont -> {
                if (!contAux.contains(cont) && cont.getNome().equals(txtName.getName())) {
                    contAux.add(cont);
                    refreshContatos(message);
                }
            });
            if(!gruAux.contains(message.getGrupos())){
                gruAux.add(message.getGrupos());
                refreshGrupos(message);
            }
        }
        if (message.getText().equals("^") && !this.txtAreaReceive.getText().endsWith("^\n")) {
            this.txtAreaReceive.append("^");
        } else if (message.getText().equals("^^") && !this.txtAreaReceive.getText().endsWith("^^\n")) {
            this.txtAreaReceive.append("^\n");
        } else if (this.txtAreaReceive.getText().endsWith("^^\n")){
        } else {
            byte[] msgEnc = crypto.toHex(message.getText());
            byte[] msg = crypto.decode(msgEnc);
            String mensagemRecebida = new String(msg).trim();
            message.setText(mensagemRecebida);
            System.out.println("GRAVOU AQUI RECEIVE");
            log.gravaNoArquivoReceive(message.getNameReserved(), message.getName(), mensagemRecebida);
            enviarRecebimento(message);
            this.txtAreaReceive.setText(log.leArquivo(message.getNameReserved(), message.getName()));
        }
    }

    private void incluiOuEditaGrupo() {
        if (this.message.getGrupos().getNome() != null) {
            Grupo grupo = gruAux.stream().filter(cont -> cont.getNome().equals(this.message.getGrupos())).findAny().get();
            if (grupo != null) {
                grupo.setContatosGrupo(this.message.getGrupos().getContatosGrupo());
            } else {
                gruAux.add(this.message.getGrupos());
            }
        }
    }

    private void enviarRecebimento(WhatsMessage message) {

        String text = this.txtAreaSend.getText();

        String name = this.message.getName();
        this.message = new WhatsMessage();

        if (this.listContatos.getSelectedIndex() > -1) {
            //Confere se tem alguém selecionado se nao envia para todos
            this.message.setNameReserved((String) this.listContatos.getSelectedValue());
            this.message.setAction(Action.SEND_ONE);
        } else {
            this.message.setAction(Action.SEND_ALL);
        }
        //Envia mensagem de recebido para o usuário
        this.message.setName(name);
        this.message.setText("^");
        this.service.send(this.message);
    }

    private void enviarConfirmLeitura(WhatsMessage message) {
        String text = this.txtAreaSend.getText();
        String name = this.message.getName();

        this.message = new WhatsMessage();

        //Confere se tem alguém selecionado se nao envia para todos
        if (this.listContatos.getSelectedIndex() > -1) {
            this.message.setNameReserved((String) this.listContatos.getSelectedValue());
            this.message.setAction(Action.SEND_ONE);

            this.message.setName(name);
            this.message.setText("^^");
            this.service.send(this.message);
            //this.listContatos.clearSelection();
        } else {
            this.message.setAction(Action.SEND_ALL);
        }

        //Envia mensagem de recebido para o usuário
    }

    /*private void enviarRecebimento(WhatsMessage message) { 
       
	// this.txtAreaSend.getText();
	
        //nome de quem está enviando
        String name = message.getName();
        
        //nome de quem está recebendo
        String nameReserved = message.getNameReserved();
	
	//this.message = new WhatsMessage();
        
	//Confere se tem alguém selecionado se nao envia para todos
	/*if (this.listContatos.getSelectedIndex() > -1) {
		this.message.setNameReserved((String) this.listContatos.getSelectedValue());
		this.message.setAction(Action.SEND_ONE);
		this.listContatos.clearSelection();
	} else {
		this.message.setAction(Action.SEND_ALL);
	}*/
    //Envia mensagem de recebido para o usuário
    //this.message.setName(name);
    ////message.setText("^");
    //if (this.listContatos.getSelectedIndex() > -1) {
    //this.message.setNameReserved((String) this.listContatos.getSelectedValue());
    ////message.setAction(Action.SEND_ONE);
    //}
    ////this.service.send(this.message);
    // }*/
    private void refreshOnlines(WhatsMessage message) {
        System.out.println(message.getSetOnlines().toString());

        Set<String> namesOnlines = message.getSetOnlines();
        //Set<String> namesContatos = message.getSetContatos();
        Set<String> names = new HashSet<String>();//message.getSetOnlines();//new HashSet<String>();// = new AbstractSet<String>;

        for (String ons : namesOnlines) {
            for (Contato con : contAux) {
                if (ons.equalsIgnoreCase(con.getNome())) {
                    names.add(ons);
                }
            }
        }

        names.remove(message.getName());

        String[] array = (String[]) names.toArray(new String[names.size()]);

        this.listOnlines.setListData(array);
        this.listOnlines.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.listOnlines.setLayoutOrientation(JList.VERTICAL);
    }

    private void refreshContatos(WhatsMessage message) {
        System.out.println(message.getSetContatos().toString());

        Set<String> names = new HashSet<String>();//message.getSetContatos();

        //names.remove(message.getName());
        Contato contato = new Contato();
        for (Contato con : contAux) {//message.getContatos()){
            names.add(con.getNome());
            //teste
            this.message.getContatos().add(con);
        }

        String[] array = (String[]) names.toArray(new String[names.size()]);

        this.listContatos.setListData(array);
        this.listContatos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.listContatos.setLayoutOrientation(JList.VERTICAL);

        this.message.setAction(Action.USERS_ONLINE);
        this.service.send(message);
    }

    private void refreshGrupos(WhatsMessage message) {
        System.out.println(message.getGrupos().toString());
        Set<String> names = new HashSet<String>();

        for (Grupo gru : gruAux) {
            names.add(gru.getNome());
        }

        String[] array = (String[]) names.toArray(new String[names.size()]);

        this.listGrupo.setListData(array);
        this.listGrupo.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.listGrupo.setLayoutOrientation(JList.VERTICAL);
    }

    private void refreshContatosGrupo() {

        Set<String> names = new HashSet<String>();

        //String teste = this.listGrupo.getSelectedValue().toString();
        //System.out.println(teste);
        for (Grupo gru : gruAux) {
            for (Contato con : gru.getContatosGrupo()) {
                names.add(con.getNome());
            }
        }

//        String[] array = (String[]) names.toArray(new String[names.size()]);
//        this.listContatosGrupo.setListData(array);
        //this.listGrupo.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.listContatosGrupo.setLayoutOrientation(JList.VERTICAL);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        txtName = new javax.swing.JTextField();
        btnConnectar = new javax.swing.JButton();
        btnSair = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        listOnlines = new javax.swing.JList();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtAreaReceive = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtAreaSend = new javax.swing.JTextArea();
        btnEnviar = new javax.swing.JButton();
        btnLimpar = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        listGrupo = new javax.swing.JList<>();
        nomeContatoAdd = new javax.swing.JTextField();
        btnAddContato = new javax.swing.JToggleButton();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        listContatos = new javax.swing.JList<>();
        jLabel1 = new javax.swing.JLabel();
        nomeGrupoAdd = new javax.swing.JTextField();
        btnAddGrupo = new javax.swing.JToggleButton();
        jLabel2 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        listContatosGrupo = new javax.swing.JList<>();
        btnAddContatoGrupo = new javax.swing.JToggleButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        btnAddContatoGrupo1 = new javax.swing.JToggleButton();
        comboBoxContatos = new javax.swing.JComboBox<>();
        comboBoxGrupos = new javax.swing.JComboBox<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Conectar"));

        txtName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtNameActionPerformed(evt);
            }
        });

        btnConnectar.setText("Entrar");
        btnConnectar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConnectarActionPerformed(evt);
            }
        });

        btnSair.setText("Sair");
        btnSair.setEnabled(false);
        btnSair.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSairActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(txtName)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnConnectar)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSair)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(btnConnectar)
                .addComponent(btnSair))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Onlines"));

        listOnlines.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                listOnlinesInputMethodTextChanged(evt);
            }
        });
        jScrollPane3.setViewportView(listOnlines);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        txtAreaReceive.setEditable(false);
        txtAreaReceive.setColumns(20);
        txtAreaReceive.setRows(5);
        txtAreaReceive.setEnabled(false);
        jScrollPane1.setViewportView(txtAreaReceive);

        txtAreaSend.setColumns(20);
        txtAreaSend.setRows(5);
        txtAreaSend.setEnabled(false);
        txtAreaSend.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                txtAreaSendFocusGained(evt);
            }
        });
        jScrollPane2.setViewportView(txtAreaSend);

        btnEnviar.setText("Enviar");
        btnEnviar.setEnabled(false);
        btnEnviar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEnviarActionPerformed(evt);
            }
        });

        btnLimpar.setText("Limpar");
        btnLimpar.setEnabled(false);
        btnLimpar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLimparActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(btnLimpar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(btnEnviar, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(9, 9, 9))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 246, Short.MAX_VALUE)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING))
                        .addGap(0, 9, Short.MAX_VALUE))))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnEnviar)
                    .addComponent(btnLimpar))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Grupo"));

        listGrupo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                listGrupoKeyPressed(evt);
            }
        });
        listGrupo.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                listGrupoValueChanged(evt);
            }
        });
        jScrollPane4.setViewportView(listGrupo);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE)
                .addContainerGap())
        );

        nomeContatoAdd.setEnabled(false);
        nomeContatoAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nomeContatoAddActionPerformed(evt);
            }
        });

        btnAddContato.setEnabled(false);
        btnAddContato.setLabel("Adicionar");
        btnAddContato.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddContatoActionPerformed(evt);
            }
        });

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Contatos"));

        listContatos.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                listContatosValueChanged(evt);
            }
        });
        jScrollPane5.setViewportView(listContatos);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 90, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE)
                .addContainerGap())
        );

        jLabel1.setText("Nome do Contato");

        nomeGrupoAdd.setEnabled(false);
        nomeGrupoAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nomeGrupoAddActionPerformed(evt);
            }
        });

        btnAddGrupo.setEnabled(false);
        btnAddGrupo.setLabel("Adicionar");
        btnAddGrupo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddGrupoActionPerformed(evt);
            }
        });

        jLabel2.setText("Nome do Grupo");

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("Contatos do Grupo"));

        listContatosGrupo.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                listContatosGrupoInputMethodTextChanged(evt);
            }
        });
        jScrollPane6.setViewportView(listContatosGrupo);

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 105, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE)
                .addContainerGap())
        );

        btnAddContatoGrupo.setText("Add no Grupo");
        btnAddContatoGrupo.setEnabled(false);
        btnAddContatoGrupo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddContatoGrupoActionPerformed(evt);
            }
        });

        jLabel3.setText("Nome do Contato");

        jLabel4.setText("Nome do Grupo");

        btnAddContatoGrupo1.setText("Mostrar Contatos");
        btnAddContatoGrupo1.setEnabled(false);
        btnAddContatoGrupo1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddContatoGrupo1ActionPerformed(evt);
            }
        });

        comboBoxGrupos.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                comboBoxGruposItemStateChanged(evt);
            }
        });
        comboBoxGrupos.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                comboBoxGruposInputMethodTextChanged(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(13, 13, 13)
                                .addComponent(btnAddContato))
                            .addComponent(nomeContatoAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addComponent(jLabel1)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(nomeGrupoAdd, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addComponent(btnAddGrupo))
                            .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(24, 24, 24)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(btnAddContatoGrupo1, javax.swing.GroupLayout.DEFAULT_SIZE, 221, Short.MAX_VALUE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, Short.MAX_VALUE))))
                            .addGroup(layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnAddContatoGrupo))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(comboBoxContatos, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(37, 37, 37)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(comboBoxGrupos, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel4)
                                        .addComponent(comboBoxGrupos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel2)
                                        .addComponent(jLabel1)))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(nomeGrupoAdd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabel3)
                                            .addComponent(comboBoxContatos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(btnAddGrupo)
                                            .addComponent(btnAddContatoGrupo))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(7, 7, 7)
                                        .addComponent(nomeContatoAdd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(btnAddContato)
                                        .addGap(0, 0, Short.MAX_VALUE)))))
                        .addGap(14, 14, 14)
                        .addComponent(btnAddContatoGrupo1)))
                .addContainerGap())
        );

        jPanel5.getAccessibleContext().setAccessibleName("Grupos");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnConnectarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConnectarActionPerformed
        String name = this.txtName.getText().trim();

        if (!name.isEmpty()) {
            this.message = new WhatsMessage();
            this.message.setAction(Action.CONNECT);
            this.message.setName(name);

            this.service = new ClienteService();
            this.socket = this.service.connect();

            new Thread(new ListenerSocket(this.socket)).start();

            this.service.send(message);
        }
    }//GEN-LAST:event_btnConnectarActionPerformed

    private void btnSairActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSairActionPerformed
        WhatsMessage message = new WhatsMessage();
        message.setName(this.message.getName());
        message.setAction(Action.DISCONNECT);
        this.service.send(message);
        //this.listContatos.removeAll();
        //this.listOnlines.removeAll();
        //this.listGrupo.removeAll();
        //this.listContatosGrupo.removeAll();
        disconnected();
    }//GEN-LAST:event_btnSairActionPerformed

    private void btnLimparActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLimparActionPerformed
        this.txtAreaSend.setText("");
    }//GEN-LAST:event_btnLimparActionPerformed

    private void btnEnviarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEnviarActionPerformed
        String text = this.txtAreaSend.getText();
        String name = this.message.getName();
        String nameReserved = (String) this.listContatos.getSelectedValue();

        byte[] enc = crypto.encode(crypto.nullPadString(text).getBytes());
        String msgParaEnviar = crypto.fromHex(enc);

        Grupo grupoSelecionado = new Grupo();

        if (gruAux != null && gruAux.size() > 0) {
            grupoSelecionado = gruAux.stream().filter(gru -> gru.getNome().equals(listGrupo.getSelectedValue())).findFirst().get();
        }

        this.message = new WhatsMessage();

        if (!text.isEmpty()) {

            if (this.listContatos.getSelectedIndex() > -1) {
                this.message.setNameReserved((String) this.listContatos.getSelectedValue());
                this.message.setAction(Action.SEND_ONE);

                this.message.setName(name);
                this.message.setText(msgParaEnviar);
                //this.message.setNameReserved((String) this.listContatos.getSelectedValue());

                log.gravaNoArquivo(name, nameReserved, text);
                this.txtAreaReceive.setText(log.leArquivo(name, nameReserved));
                this.service.send(this.message);
                //mensagensEnviadasRecebidas.add(this.message);
            } else {
                this.message.setGrupos(grupoSelecionado);
                this.message.setAction(Action.SEND_ALL);

                this.message.setName(this.txtName.getText());
                this.message.setText(msgParaEnviar);
                //this.message.setNameReserved((String) this.listContatos.getSelectedValue());

                log.gravaNoArquivoGrupo(this.message.getName(), grupoSelecionado.getNome(), text);
                this.txtAreaReceive.setText(log.leArquivoGrupo(grupoSelecionado.getNome()));
                this.service.send(this.message);
                //mensagensEnviadasRecebidas.add(this.message);
            }
        }

        /*if (!text.isEmpty()) {
            this.message.setName(name);
            this.message.setText(text);
            this.message.setNameReserved((String) this.listContatos.getSelectedValue());

            //this.txtAreaReceive.append("Você disse: " + text + "\n");
            log.gravaNoArquivo(name, nameReserved, text);
            this.txtAreaReceive.setText(log.leArquivo(name, nameReserved));
            this.service.send(this.message);
            mensagensEnviadasRecebidas.add(this.message);
        }*/
        this.txtAreaSend.setText("");
    }//GEN-LAST:event_btnEnviarActionPerformed

    private void nomeContatoAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nomeContatoAddActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_nomeContatoAddActionPerformed

    private void txtNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtNameActionPerformed

    private void btnAddContatoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddContatoActionPerformed
        String name = this.nomeContatoAdd.getText();
        //System.out.println(this.listOnlines.toString() + " - " + this.listOnlines);
        if (!name.isEmpty()) {
            System.out.println("nome para add: " + name);
            Contato contato = new Contato();
            contato.setNome(name);
            //message.getContatos().add(contato);
            contAux.add(contato);
            refreshContatos(message);
            //metodo antigo
            /*message.getSetContatos().add(name);*/
            this.message.setAction(Action.USERS_ONLINE);
            this.service.send(message);
            this.nomeContatoAdd.setText("");
            refreshListContatosByGrupo();
        }
    }//GEN-LAST:event_btnAddContatoActionPerformed

    private void nomeGrupoAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nomeGrupoAddActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_nomeGrupoAddActionPerformed

    private void btnAddGrupoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddGrupoActionPerformed
        String name = this.nomeGrupoAdd.getText();
        if (!name.isEmpty()) {
            System.out.println("nome do grupo para add: " + name);
            Grupo grupo = new Grupo();
            grupo.setNome(name);
            gruAux.add(grupo);
            //message.getGrupos().add(grupo);
            refreshGrupos(message);
            comboBoxGrupos.addItem(name);
            this.nomeGrupoAdd.setText("");
        }
    }//GEN-LAST:event_btnAddGrupoActionPerformed

    private void btnAddContatoGrupoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddContatoGrupoActionPerformed
        String nomeContato = this.comboBoxContatos.getSelectedItem().toString();
        String nomeGrupo = this.comboBoxGrupos.getSelectedItem().toString();
        if (!nomeContato.isEmpty() || !nomeGrupo.isEmpty()) {
            Contato contato = contAux.stream().filter(cont -> cont.getNome().equals(nomeContato)).findAny().get();
            Grupo grupo = gruAux.stream().filter(cont -> cont.getNome().equals(nomeGrupo)).findAny().get();
            if (contato != null && grupo != null) {
                grupo.getContatosGrupo().add(contato);
            }
        }
        refreshContatosGrupo();
        refreshListContatosByGrupo();
    }//GEN-LAST:event_btnAddContatoGrupoActionPerformed

    private void listGrupoKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_listGrupoKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_listGrupoKeyPressed

    private void btnAddContatoGrupo1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddContatoGrupo1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnAddContatoGrupo1ActionPerformed

    private void listGrupoValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_listGrupoValueChanged
        listContatos.clearSelection();
        listContatosGrupo.clearSelection();
        listOnlines.clearSelection();
        String grupoClicado = listGrupo.getSelectedValue();

        Grupo grupo = gruAux.stream().filter(cont -> cont.getNome().equals(grupoClicado)).findAny().get();
        ArrayList<String> contactsNames = new ArrayList<>();
        grupo.getContatosGrupo().forEach(cont -> contactsNames.add(cont.getNome()));
        String[] str = new String[contactsNames.size()];
        this.listContatosGrupo.setListData(contactsNames.toArray(str));

        //mostraConversaGrupo();
        //teste
        String nameReserved = (String) this.listContatos.getSelectedValue();
        String name = this.message.getName();
        this.txtAreaReceive.setText(log.leArquivoGrupo(grupoClicado));
    }//GEN-LAST:event_listGrupoValueChanged
    private void comboBoxGruposItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_comboBoxGruposItemStateChanged
        String grupoSelecionado = comboBoxGrupos.getSelectedItem().toString();
        if (grupoSelecionado != null && !grupoSelecionado.isEmpty()) {
            refreshListContatosByGrupo();
        }
    }//GEN-LAST:event_comboBoxGruposItemStateChanged
    private void listContatosValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_listContatosValueChanged
        listContatosGrupo.clearSelection();
        listGrupo.clearSelection();
        listOnlines.clearSelection();
        String nameReserved = (String) this.listContatos.getSelectedValue();
        String name = this.message.getName();
        this.txtAreaReceive.setText(log.leArquivo(name, nameReserved));
    }//GEN-LAST:event_listContatosValueChanged
    private void listOnlinesInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_listOnlinesInputMethodTextChanged
        listContatos.clearSelection();
        listContatosGrupo.clearSelection();
        listGrupo.clearSelection();
    }//GEN-LAST:event_listOnlinesInputMethodTextChanged
    private void listContatosGrupoInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_listContatosGrupoInputMethodTextChanged

        listContatos.clearSelection();
        listGrupo.clearSelection();
        listOnlines.clearSelection();
    }//GEN-LAST:event_listContatosGrupoInputMethodTextChanged

    private void refreshListContatosByGrupo() {
        if (comboBoxGrupos.getSelectedItem() != null) {
            String grupoSelecionado = comboBoxGrupos.getSelectedItem().toString();
            comboBoxContatos.removeAllItems();
            Grupo grupo = gruAux.stream().filter(cont -> cont.getNome().equals(grupoSelecionado)).findAny().get();
            for (Contato cont : contAux) {
                if (!grupo.getContatosGrupo().stream().anyMatch(con -> con.getNome().equals(cont.getNome()))) {
                    comboBoxContatos.addItem(cont.getNome());
                }
            }
        }
    }

    private void comboBoxGruposInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
    }

    private void txtAreaSendFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtAreaSendFocusGained
        //if(message.getName().equals(this.message.getName())){
        this.message.getName();
        message.getName();
        enviarConfirmLeitura(message);
        //}
    }//GEN-LAST:event_txtAreaSendFocusGained
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton btnAddContato;
    private javax.swing.JToggleButton btnAddContatoGrupo;
    private javax.swing.JToggleButton btnAddContatoGrupo1;
    private javax.swing.JToggleButton btnAddGrupo;
    private javax.swing.JButton btnConnectar;
    private javax.swing.JButton btnEnviar;
    private javax.swing.JButton btnLimpar;
    private javax.swing.JButton btnSair;
    private javax.swing.JComboBox<String> comboBoxContatos;
    private javax.swing.JComboBox<String> comboBoxGrupos;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JList<String> listContatos;
    private javax.swing.JList<String> listContatosGrupo;
    private javax.swing.JList<String> listGrupo;
    private javax.swing.JList listOnlines;
    private javax.swing.JTextField nomeContatoAdd;
    private javax.swing.JTextField nomeGrupoAdd;
    private javax.swing.JTextArea txtAreaReceive;
    private javax.swing.JTextArea txtAreaSend;
    private javax.swing.JTextField txtName;
    // End of variables declaration//GEN-END:variables
}
