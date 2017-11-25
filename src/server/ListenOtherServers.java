/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author TFD-GRUPO11-17/18
 */
public class ListenOtherServers implements Runnable {

    private Server server;
    private ServerSocket serverSocket;
    private boolean stopListenOtherServers;

    public ListenOtherServers(Server server, ServerSocket serverSocket) {
        this.server = server;
        this.serverSocket = serverSocket;
        this.stopListenOtherServers = false;
    }

    @Override
    public void run() {
        try {
            while (!stopListenOtherServers) {

                Socket socketToServer = this.serverSocket.accept();

                if (!stopListenOtherServers) {
                    ProcessServer ps = new ProcessServer(server, socketToServer);
                    server.setProcessServer(ps);
                    new Thread(ps).start();
                } else if (socketToServer != null) {
                    socketToServer.close();
                }
            }

        } catch (IOException ex) {
            System.err.println(server.getServerID() + " - Erro no estabelecimento da ligação com o líder \n" + ex.getLocalizedMessage());
        }
    }
    
    public void stopListenOtherServers() {
        this.stopListenOtherServers = true;
        try {
            new Socket(serverSocket.getInetAddress(), serverSocket.getLocalPort()).close();
        } catch (IOException e) {

        }
    }
}
