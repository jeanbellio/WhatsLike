package app.frame;


import java.io.BufferedWriter;
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

    public void gravaNoArquivo(String user, String message) {
        dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        date = new Date();
        String dataAtual = dateFormat.format(date);
        
        try {
            arq = new FileWriter(name + "-" + dataAtual + ".txt", true);
            out = new BufferedWriter(arq);
            out.write(user + " diz: " + message + "\n");
            out.flush();
            out.close();

        } catch (IOException ex) {
            System.out.println("Falha ao gravar arquivo");
        }
    }

}
