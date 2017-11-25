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
    private final static String LOG_FILE_NAME = "log.csv";
    private final static String SNAPSHOT_FILE_NAME = "snapshot.csv";

    public void writeToFile(String text, boolean isSnapshot) {
        String filename;
        if (isSnapshot) {
            filename = FILE_PATH_WINDOWS + SNAPSHOT_FILE_NAME;
        } else {
            filename = FILE_PATH_WINDOWS + LOG_FILE_NAME;
        }
        try {
            File file = new File(filename);
            FileWriter writer = new FileWriter(filename, true);
            BufferedWriter bufferedWriter = new BufferedWriter(writer);
//            if (file.exists()) {
                bufferedWriter.write(text);
                bufferedWriter.newLine();
                bufferedWriter.flush();
                bufferedWriter.close();
//            }
//            else{
//                
//            }
        } catch (IOException iOException) {
            System.out.println("Falha ao escrever no ficheiro " + filename);
        }
    }

}
