package app.frame;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Jean
 */
public class Log {

    private DateFormat dateFormat;
    private Date date;
    private String dataAtual;
    private FileWriter arq;
    private PrintWriter gravarArq;
    private BufferedWriter out = null;
    private String name;
    
    public Log(String name) {
        this.name = name;
    }

    Log() {
    }

    public void gravaNoArquivo(String user, String nameReserved, String message) {
        dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        date = new Date();
        String dataAtual = dateFormat.format(date);
        
        try {
            arq = new FileWriter(name + "-" + nameReserved + ".txt", true);
            out = new BufferedWriter(arq);
            out.write(name + " diz: " + message);
            out.write("\n");
            out.flush();
            out.close();

        } catch (IOException ex) {
            System.out.println("Falha ao gravar arquivo");
        }
    }
    
    public void gravaNoArquivoReceive(String user, String nameReserved, String message) {
        dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        date = new Date();
        String dataAtual = dateFormat.format(date);
        
        try {
            arq = new FileWriter(name + "-" + nameReserved + ".txt", true);
            out = new BufferedWriter(arq);
            out.write(nameReserved + " diz: " + message);
            out.write("\n");
            out.flush();
            out.close();

        } catch (IOException ex) {
            System.out.println("Falha ao gravar arquivo");
        }
    }

    public String leArquivo(String name, String nameReserved){
        String conversa = "";
        try{            
            BufferedReader br = new BufferedReader(new FileReader(name + "-" + nameReserved + ".txt"));
            StringBuilder conv = new StringBuilder();
            while(br.ready()){
                //String linha = br.readLine();
                //System.out.println(linha);
                conv.append(br.readLine());
                conv.append("\n");
            }
            br.close();
            conversa = conv.toString();
        }catch(IOException ioe){
            System.out.println("não há histórico de conversa com este contato.");
            //ioe.printStackTrace();
        }
        return conversa;
    }
    
    public void gravaNoArquivoGrupo(String user, String nameGrupo, String message) {
        dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        date = new Date();
        String dataAtual = dateFormat.format(date);
        
        try {
            arq = new FileWriter(nameGrupo + ".txt", true);
            out = new BufferedWriter(arq);
            out.write(user + " diz: " + message);
            out.write("\n");
            out.flush();
            out.close();

        } catch (IOException ex) {
            System.out.println("Falha ao gravar arquivo");
        }
    }
    
    public String leArquivoGrupo(String name){
        String conversa = "";
        try{            
            BufferedReader br = new BufferedReader(new FileReader(name + ".txt"));
            StringBuilder conv = new StringBuilder();
            while(br.ready()){
                //String linha = br.readLine();
                //System.out.println(linha);
                conv.append(br.readLine());
                conv.append("\n");
            }
            br.close();
            conversa = conv.toString();
        }catch(IOException ioe){
            System.out.println("não há histórico de conversa deste grupo.");
            //ioe.printStackTrace();
        }
        return conversa;
    }
    
}
