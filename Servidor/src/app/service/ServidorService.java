
package app.service;

import app.bean.Contato;
import app.bean.Grupo;
import app.bean.WhatsMessage;
import app.bean.WhatsMessage.Action;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ServidorService {

    private ServerSocket serverSocket;
    private Socket socket;
    private Map<String, ObjectOutputStream> mapOnlines = new HashMap<String, ObjectOutputStream>();
    private Map<String, List<WhatsMessage>> messagesDidNotSend = new HashMap<>(); 

    public ServidorService() {
        try {
            serverSocket = new ServerSocket(5555);
            if (!serverSocket.isBound()){
//            serverSocket.bind(new InetSocketAddress("10.32.148.91", 5555));
           serverSocket.bind(new InetSocketAddress("localhost", 5555));
           // serverSocket.bind(new InetSocketAddress("10.32.148.14", 5555));
            }
            System.out.println("Servidor on!");

            while (true) {
                socket = serverSocket.accept();

                new Thread(new ListenerSocket(socket)).start();
            }

        } catch (IOException ex) {
            Logger.getLogger(ServidorService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private class ListenerSocket implements Runnable {

        private ObjectOutputStream output;
        private ObjectInputStream input;

        public ListenerSocket(Socket socket) {
            try {
                this.output = new ObjectOutputStream(socket.getOutputStream());
                this.input = new ObjectInputStream (socket.getInputStream());
            } catch (IOException ex) {
                Logger.getLogger(ServidorService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        @Override
        public void run() {
            WhatsMessage message = null;
            try {
                while ((message = (WhatsMessage) input.readObject()) != null) {
                     Action action = message.getAction();
                    if (action.equals(Action.CONNECT)) {
                        boolean isConnect = connect(message, output);
                        if (isConnect) {
                            mapOnlines.put(message.getName(), output);
                            sendOnlines();
                        }
                    } else if (action.equals(Action.DISCONNECT)) {
                        disconnect(message, output);
                        sendOnlines();
                        return;
                    } else if (action.equals(Action.SEND_ONE)) {
                        sendOne(message);
                    } else if (action.equals(Action.SEND_ALL)) {
                        sendAll(message);
                    } else if (action.equals(Action.USERS_ONLINE)) {
                        sendOnlines();
                    } else if (action.equals(Action.USERS_CONTACTS)) {
                        sendOnlines(message);
                    }
                }
            } catch (IOException ex) {
                WhatsMessage cm = new WhatsMessage();
                cm.setName(message.getName());
                disconnect(cm, output);
                sendOnlines();
                System.out.println(message.getName() + " deixou o chat!");
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(ServidorService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private boolean connect(WhatsMessage message, ObjectOutputStream output) {
        if(messagesDidNotSend.containsKey(message.getName())){
            message.setOfflineMessages(messagesDidNotSend.get(message.getName()));
            messagesDidNotSend.remove(message.getName());
        }
        if (mapOnlines.isEmpty()) {
            message.setText("YES");
            send(message, output);
            return true;
        }

        if (mapOnlines.containsKey(message.getName())) {
            message.setText("NO");
            send(message, output);
            return false;
        } else {
            message.setText("YES");
            send(message, output);
            return true;
        }
    }

    private void disconnect(WhatsMessage message, ObjectOutputStream output) {
        mapOnlines.remove(message.getName());

        message.setText(" Até logo!");

        message.setAction(Action.SEND_ONE);

        sendAll(message);

        System.out.println("User " + message.getName() + " saiu da sala" );
    }

    private void send(WhatsMessage message, ObjectOutputStream output) {
        try {
            output.writeObject(message);
        } catch (IOException ex) {
            Logger.getLogger(ServidorService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void sendOne(WhatsMessage message) {
        for (Map.Entry<String, ObjectOutputStream> kv : mapOnlines.entrySet()) {
            if (kv.getKey().equals(message.getNameReserved())) {
                try {
                    kv.getValue().writeObject(message);
                    return;
                } catch (IOException ex) {
                    Logger.getLogger(ServidorService.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        if(messagesDidNotSend.containsKey(message.getNameReserved())){ 
                messagesDidNotSend.get(message.getNameReserved()).add(message); 
        } else { 
            List<WhatsMessage> messages = new ArrayList<>(); 
            messages.add(message); 
            messagesDidNotSend.put(message.getNameReserved(), messages); 
        } 
    }

    private void sendAll(WhatsMessage message) {
        Set<String> contatos = new HashSet<String>();
        Grupo gru = message.getGrupos();
            for(Contato con : gru.getContatosGrupo()){
                //contatos.add(con.getNome());
            
                for (Map.Entry<String, ObjectOutputStream> kv : mapOnlines.entrySet()) {
                    if(kv.getKey().equals(con.getNome())){
                        if (!kv.getKey().equals(message.getName())) {
                            message.setAction(Action.SEND_ONE);
                            try {
                                kv.getValue().writeObject(message);
                            } catch (IOException ex) {
                                Logger.getLogger(ServidorService.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                }
            }
    }

    private void sendOnlines() {
        Set<String> setNames = new HashSet<String>();
        for (Map.Entry<String, ObjectOutputStream> kv : mapOnlines.entrySet()) {
            setNames.add(kv.getKey());
        }

        WhatsMessage message = new WhatsMessage();
        message.setAction(Action.USERS_ONLINE);
        message.setSetOnlines(setNames);

        for (Map.Entry<String, ObjectOutputStream> kv : mapOnlines.entrySet()) {
            message.setName(kv.getKey());
            try {
                kv.getValue().writeObject(message);
            } catch (IOException ex) {
                Logger.getLogger(ServidorService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    //teste
    private void sendOnlines(WhatsMessage message) {
        Set<String> setNames = new HashSet<String>();
        for (Map.Entry<String, ObjectOutputStream> kv : mapOnlines.entrySet()) {
            setNames.add(kv.getKey());
        }
        
        message.setAction(Action.USERS_CONTACTS);
        message.setSetOnlines(setNames);

        for (Map.Entry<String, ObjectOutputStream> kv : mapOnlines.entrySet()) {
            message.setName(kv.getKey());
            try {
                kv.getValue().writeObject(message);
            } catch (IOException ex) {
                Logger.getLogger(ServidorService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
