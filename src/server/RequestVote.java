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

/**
 *
 * @author João
 */
public class RequestVote implements Runnable {

    private Server server;
    private int otherServerPort;
    private AppendEntry requestVote;
    private boolean isFinished;

    public RequestVote(Server server, int otherServerPort, AppendEntry requestVote) {
        this.server = server;
        this.otherServerPort = otherServerPort;
        this.requestVote = requestVote;
        this.isFinished = false;
    }

    @Override
    public void run() {
        Socket socket = null;
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;

        while (!isFinished) {
            try {
                socket = new Socket("localhost", otherServerPort);
                oos = new ObjectOutputStream(socket.getOutputStream());
                oos.writeObject(requestVote);
                oos.flush();
                System.out.println("Enviado o pedido de voto para o outro servidor...");

                ois = new ObjectInputStream(socket.getInputStream());
                AppendEntry response = (AppendEntry) ois.readObject();
                if (response != null) {
                    server.getServerQueue().add(response);
                    System.out.println("Enviado para o candidato de novo...");
                }

                oos.close();
                ois.close();
                socket.close();
                isFinished = true;

            } catch (IOException ex) {
                System.err.println("O servidor contactado pelo " + server.getState() + " " + server.getServerID() + " não está disponível! \n" + ex.getLocalizedMessage());
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
}
