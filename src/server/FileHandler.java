/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author TFD-GRUPO11-17/18
 */
public class FileHandler {

    private final static String FILE_PATH_WINDOWS = "F:\\Programação\\TFD-011-Raft\\";
    private final static String FILE_PATH_LINUX = "";
    private final static String LOG_FILE_NAME = "log";
    private final static String SNAPSHOT_FILE_NAME = "snapshot";
    private final static String FILE_TYPE = ".csv";

    private Server server;

    public FileHandler(Server server) {
        this.server = server;
    }

    public void writeToFile(String text, boolean isSnapshot) {
        String filename;
        if (isSnapshot) {
            filename = FILE_PATH_WINDOWS + SNAPSHOT_FILE_NAME + server.getServerID() + FILE_TYPE;
        } else {
            filename = FILE_PATH_WINDOWS + LOG_FILE_NAME + server.getServerID() + FILE_TYPE;
        }
        try {
            File file = new File(filename);
            FileWriter writer = new FileWriter(filename, true);
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
//            if (!file.exists()) {
//                bufferedWriter.write("TERM;INDEX;OPERATION_TYPE;KEY;VALUE;ID_MESSAGE;SOURCE;MAJORITY;COMMITED;SENT_TO_CLIENT\n");
//                bufferedWriter.newLine();
//                
//            }
            bufferedWriter.write(text);
            bufferedWriter.newLine();
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException iOException) {
            System.out.println("Falha ao escrever no ficheiro " + filename);
        }
    }

}
