/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.Objects;

/**
 *
 * @author TFD-GRUPO11-17/18
 */
public class ProcessServer implements Runnable {

    private Server server;
    private Socket socket;
    private boolean stopProcessServer;

    public ProcessServer(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;
        this.stopProcessServer = false;
    }

    @Override
    public void run() {
        try {
            while (!stopProcessServer) {

                ObjectInputStream dis = new ObjectInputStream(socket.getInputStream());
                if (!stopProcessServer) {
                    AppendEntry ae = (AppendEntry) dis.readObject();
                    processAppendEntry(ae, socket);//executa o método que processa a AppendEntry de outro servidor
                    System.out.println("Recebida AppendEntry do " + ae.getLeaderId() +" - " + ae.getType() + ". Sou o " + server.getServerID());
                }
            }
            socket.close();

        } catch (IOException ex) {
            System.err.println(server.getServerID() + " - Erro no estabelecimento da ligação com o líder \n" + ex.getLocalizedMessage());

        } catch (ClassNotFoundException ex) {
            System.err.println("Erro na conversão da classe Request\n" + ex.getLocalizedMessage());
        }

    }

    private void processAppendEntry(AppendEntry ae, Socket socketToServer) {
        if (ae != null) {
            if (Objects.equals(server.getState(), "LEADER")) {
                if (Objects.equals(ae.getType(), "REQUESTVOTE")) {
                    server.getServerQueue().add(ae);
                }
            } else {
                server.getServerQueue().add(ae);
            }
            server.addServersSockets(ae.getLeaderId(), socketToServer);
        }
    }

    public void stopProcessServer() {
        this.stopProcessServer = true;
    }
}
