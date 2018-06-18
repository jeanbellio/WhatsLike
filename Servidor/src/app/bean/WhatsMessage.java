
package app.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class WhatsMessage implements Serializable {
    
    private String name;
    private String text;
    private String nameReserved;
    private Set<String> setOnlines = new HashSet<String>();
    private Action action;
    private Set<String> setContatos = new HashSet<String>();
    private ArrayList<Grupo> grupos = new ArrayList<Grupo>();
    private ArrayList<Contato> contatos = new ArrayList<Contato>();
    private ArrayList<Contato> contatosAux = new ArrayList<Contato>();
    private List<WhatsMessage> offlineMessages = new ArrayList<>(); 
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getNameReserved() {
        return nameReserved;
    }

    public void setNameReserved(String nameReserved) {
        this.nameReserved = nameReserved;
    }

    public Set<String> getSetOnlines() {
        return setOnlines;
    }

    public void setSetOnlines(Set<String> setOnlines) {
        this.setOnlines = setOnlines;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public Set<String> getSetContatos() {
        return setContatos;
    }

    public void setSetContatos(Set<String> setContatos) {
        this.setContatos = setContatos;
    }

    public ArrayList<Grupo> getGrupos() {
        return grupos;
    }

    public void setGrupos(ArrayList<Grupo> grupos) {
        this.grupos = grupos;
    }

    public ArrayList<Contato> getContatos() {
        return contatos;
    }

    public void setContatos(ArrayList<Contato> contatos) {
        this.contatos = contatos;
    }

    public ArrayList<Contato> getContatosAux() {
        return contatosAux;
    }

    public void setContatosAux(ArrayList<Contato> contatosAux) {
        this.contatosAux = contatosAux;
    }
        
    public enum Action {
        CONNECT, DISCONNECT, SEND_ONE, SEND_ALL, USERS_ONLINE, USERS_CONTACTS, ADD_GROUP, ADD_CONTACT_GROUP 
    }

    public List<WhatsMessage> getOfflineMessages() {
        return offlineMessages;
    }

    public void setOfflineMessages(List<WhatsMessage> offlineMessages) {
        this.offlineMessages = offlineMessages;
    }

}
