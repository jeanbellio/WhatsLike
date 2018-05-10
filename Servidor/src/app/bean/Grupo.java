/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package app.bean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author 10084678
 */
public class Grupo implements Serializable{
    
    private String nome;
    private ArrayList<Contato> contatosGrupo = new ArrayList<Contato>();

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public ArrayList<Contato> getContatosGrupo() {
        return contatosGrupo;
    }

    public void setContatosGrupo(ArrayList<Contato> contatosGrupo) {
        this.contatosGrupo = contatosGrupo;
    }
    
    
}
