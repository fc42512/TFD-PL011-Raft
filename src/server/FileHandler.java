/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import com.sun.org.apache.bcel.internal.util.Objects;
import common.OperationType;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

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

    public void readLogFile() {
        String filename = FILE_PATH_WINDOWS + LOG_FILE_NAME + server.getServerID() + FILE_TYPE;
        LogEntry logEntry;
        try {
            FileReader reader = new FileReader(filename);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] splittedLine = line.split(";");
                logEntry = new LogEntry(Integer.valueOf(splittedLine[0]), Integer.valueOf(splittedLine[1]), OperationType.valueOf(splittedLine[2]), splittedLine[3],
                        splittedLine[4], splittedLine[5], Integer.valueOf(splittedLine[6]), Integer.valueOf(splittedLine[7]), Boolean.valueOf(splittedLine[8]), Boolean.valueOf(splittedLine[9]));
                server.getLog().add(logEntry);
            }
            bufferedReader.close();
        } catch (IOException iOException) {
            System.out.println("Falha ao ler o ficheiro de LOG " + filename + " ou o ficheiro ainda não existe!");
        }
    }

    public void readSnapshotFile() {
        String filename = FILE_PATH_WINDOWS + SNAPSHOT_FILE_NAME + server.getServerID() + FILE_TYPE;
        boolean isFirstLine = true;
        try {
            FileReader reader = new FileReader(filename);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line;
            String[] splittedLine;
            while ((line = bufferedReader.readLine()) != null) {
                if (isFirstLine) {
                    splittedLine = line.split(";");
                    server.setLastApplied(Integer.valueOf(splittedLine[0]));
                    server.setCurrentTerm(Integer.valueOf(splittedLine[1]));
                    server.setID_MESSAGE(Integer.valueOf(splittedLine[2]));
                    isFirstLine = false;
                } else {
                    splittedLine = line.split(";");
                    server.getKeyValueStore().put(splittedLine[0], splittedLine[1]);
                }
            }
            bufferedReader.close();
        } catch (IOException iOException) {
            System.out.println("Falha ao ler o ficheiro de SNAPSHOT " + filename + " ou o ficheiro ainda não existe!");
        }
    }

}
