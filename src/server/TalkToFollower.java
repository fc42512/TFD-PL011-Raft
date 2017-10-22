/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author João
 */
public class TalkToFollower implements Runnable {

    private Server server;
    private int followerPort;
    private AppendEntry appendEntry;
    private AppendEntry response;

    public TalkToFollower(Server server, int followerPort, AppendEntry appendEntry) {
        this.server = server;
        this.followerPort = followerPort;
        this.appendEntry = appendEntry;
        this.response = null;
    }

    @Override
    public void run() {
        try {
            Socket socket = new Socket("localhost", followerPort);
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(appendEntry);
            oos.flush();
            System.out.println("Enviado para o follower...");

            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            response = (AppendEntry) ois.readObject();
            if (response != null) {
                server.getServerQueue().add(response);
                System.out.println("Enviado para o líder de novo...");
            }
            oos.close();
            ois.close();
            socket.close();
        } 
        catch (IOException ex) {
            System.err.println("O follower contactado pelo líder " + server.getLeaderID() + " não está disponível! \n" + ex.getLocalizedMessage());
        } 
        catch (ClassNotFoundException ex) {
            System.err.println("Erro na conversão da classe \n" + ex.getLocalizedMessage());

        }
    }
}
