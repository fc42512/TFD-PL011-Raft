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
 * @author TFD-GRUPO11-17/18
 */
public class TalkToFollower implements Runnable {

    private Server server;
    private String[] otherServerAddress;
    private AppendEntry response;
    private LinkedBlockingQueue<AppendEntry> appendEntriesToSend;
    private boolean connectionAlive;
    private boolean stopTalkToFollower;

    public TalkToFollower(Server server, String[] otherServerAddress) {
        this.server = server;
        this.otherServerAddress = otherServerAddress;
        this.response = null;
        this.appendEntriesToSend = new LinkedBlockingQueue<>();
        this.connectionAlive = false;
        this.stopTalkToFollower = false;
    }

    @Override
    public void run() {
        Socket socket = null;
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        while (!connectionAlive && !stopTalkToFollower) {
            try {
                socket = new Socket(otherServerAddress[0], Integer.parseInt(otherServerAddress[1]));
                connectionAlive = true;
                while (connectionAlive && !stopTalkToFollower) {
                    if (!appendEntriesToSend.isEmpty()) {
                        oos = new ObjectOutputStream(socket.getOutputStream());
                        oos.writeObject(appendEntriesToSend.remove());
                        oos.flush();
                        System.out.println("Enviado para o follower...");

                        if (socket.isConnected()) {
                            ois = new ObjectInputStream(socket.getInputStream());
                            response = (AppendEntry) ois.readObject();
                            if (response != null && !stopTalkToFollower) {
                                server.getServerQueue().add(response);
                                System.out.println("Recebido pelo líder vindo do Follower...");
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
                try {
                    Thread.sleep(1000);
                    LinkedBlockingQueue<AppendEntry> newAppendEntriesToSend = new LinkedBlockingQueue<>();
                    AppendEntry ae;
                    for(int i=0; i<appendEntriesToSend.size(); i++){
                        ae = appendEntriesToSend.remove();
                        if(ae.getType() == "APPENDENTRY"){
                            newAppendEntriesToSend.add(ae);
                        }
                    }
                    appendEntriesToSend = newAppendEntriesToSend;
                } catch (InterruptedException ex1) {
                    System.err.println("O servidor contactado pelo " + server.getState() + " " + server.getServerID() + " não está disponível! Estou à espera!!!\n" + ex1.getLocalizedMessage());
                }
            } catch (ClassNotFoundException ex) {
                System.err.println("Erro na conversão da classe \n" + ex.getLocalizedMessage());
            }
        }
//        try {
//            socket.close();
//        } catch (IOException ex) {
//            System.err.println("Erro no fecho do Socket do TalkToFollower \n" + ex.getLocalizedMessage());
//        }
    }

    public void storeAppendEntryInQueue(AppendEntry a) {
        this.appendEntriesToSend.add(a);
    }
    
    public void stopTalkToFollower(){
        this.stopTalkToFollower = true;
    }
}
