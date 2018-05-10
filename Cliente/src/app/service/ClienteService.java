
package app.service;

import app.bean.WhatsMessage;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ClienteService {
    
    private Socket socket;
    private ObjectOutputStream output;
    
    public Socket connect() {
        try {
            //this.socket = new Socket("10.32.148.14", 5555);
            this.socket = new Socket("localhost", 5555);

            this.output = new ObjectOutputStream(socket.getOutputStream());
        } catch (UnknownHostException ex) {
            System.out.println(ex);
        } catch (IOException ex) {
            System.out.println(ex);
        }
        
        return socket;
    }
    
    public void send(WhatsMessage message) {
        try {
            output.writeObject(message);
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }
}
