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
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author João
 */
public class TalkToFollower implements Runnable {

    private Server server;
    private int otherServerPort;
    private AppendEntry response;
    private LinkedBlockingQueue<AppendEntry> appendEntriesToSend;
    private boolean connectionAlive;

    public TalkToFollower(Server server, int otherServerPort) {
        this.server = server;
        this.otherServerPort = otherServerPort;
        this.response = null;
        this.appendEntriesToSend = new LinkedBlockingQueue<>();
        this.connectionAlive = false;

    }

    @Override
    public void run() {
        Socket socket = null;
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        while (!connectionAlive) {
            try {
                socket = new Socket("localhost", otherServerPort);
                connectionAlive = true;
                while (connectionAlive) {
                    if (!appendEntriesToSend.isEmpty()) {
                        oos = new ObjectOutputStream(socket.getOutputStream());
                        oos.writeObject(appendEntriesToSend.remove());
                        oos.flush();
                        System.out.println("Enviado para o follower...");

                        if (socket.isConnected()) {
                            ois = new ObjectInputStream(socket.getInputStream());
                            response = (AppendEntry) ois.readObject();
                            if (response != null) {
                                server.getServerQueue().add(response);
                                System.out.println("Enviado para o líder de novo...");
                            }
                        } else {
                            oos.close();
                            ois.close();
                            socket.close();
                            connectionAlive = false;
                            appendEntriesToSend.clear();
                        }
                    }
                }

            } catch (IOException ex) {
                System.err.println("O servidor contactado pelo " + server.getState() + " " + server.getServerID() + " não está disponível! \n" + ex.getLocalizedMessage());
                connectionAlive = false;
                appendEntriesToSend.clear();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex1) {
                    System.err.println("O servidor contactado pelo " + server.getState() + " " + server.getServerID() + " não está disponível! Estou à espera!!!\n" + ex1.getLocalizedMessage());
                }
            } catch (ClassNotFoundException ex) {
                System.err.println("Erro na conversão da classe \n" + ex.getLocalizedMessage());
            }
        }
    }

    public void storeAppendEntryInQueue(AppendEntry a) {
        this.appendEntriesToSend.add(a);
    }
}
