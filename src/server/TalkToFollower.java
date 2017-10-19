/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import common.Message;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 *
 * @author João
 */
public class TalkToFollower implements Runnable {

    private Server server;
    private int followerPort;
    private boolean connectionEstablished;
    private int ID_REQUEST;
    private Message request;
    private Message response;

    public TalkToFollower(Server server, int followerPort) {
        this.server = server;
        this.followerPort = followerPort;
        this.connectionEstablished = false;
        response = null;
        this.ID_REQUEST = 0;
    }

    @Override
    public void run() {

        while (!connectionEstablished) {
            try {
                Socket socket = new Socket("localhost", followerPort);
                connectionEstablished = true;

                while (true) {
                    setAppendEntries();
                    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                    oos.writeObject(request);
                    oos.flush();

                    ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                    response = (Message) ois.readObject();
                    if (response != null) {
                        System.out.println(response.getId() + " " + response.getContent());
                    }
                }
            } catch (IOException ex) {
                System.err.println("O follower contactado pelo líder " + server.getLeaderID() + " não está disponível! \n" + ex.getLocalizedMessage());
                connectionEstablished = false;
            } catch (ClassNotFoundException ex) {
                System.err.println("Erro na conversão da classe \n" + ex.getLocalizedMessage());
            }
        }
    }

    private void setAppendEntries() {
        if (response == null) {
            request = new Message("SR" + server.getServerID() + "-RQ" + ID_REQUEST, request.getSource(), "", "");
        } else {
            ID_REQUEST++;
            request = new Message("SR" + server.getServerID() + "-RQ" + ID_REQUEST, request.getSource(), "", "");
        }
    }
}
