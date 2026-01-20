package ro.pub.cs.systems.eim.practicaltest02v5;

import android.util.Log;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class ServerThread extends Thread {
    private ServerSocket serverSocket;

    // --- CLASA INTERNA PENTRU A TINE VALOAREA + TIMPUL ---
    public static class DataModel {
        public String value;
        public long timestamp;

        public DataModel(String value, long timestamp) {
            this.value = value;
            this.timestamp = timestamp;
        }
    }

    // HashMap-ul tine acum DataModel, nu String simplu
    private HashMap<String, DataModel> data = new HashMap<>();

    public ServerThread(int port) {
        try {
            this.serverSocket = new ServerSocket(port);
        } catch (IOException ioException) {
            Log.e("PracticalTest02", "An exception has occurred: " + ioException.getMessage());
        }
    }

    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Socket socket = serverSocket.accept();
                CommunicationThread communicationThread = new CommunicationThread(this, socket);
                communicationThread.start();
            }
        } catch (IOException ioException) {
            Log.e("PracticalTest02", "An exception has occurred: " + ioException.getMessage());
        }
    }

    // --- METODELE SINCRONIZATE PENTRU PUT SI GET ---

    // Adauga un parametru "timestamp"
    public synchronized void setData(String key, String value, long timestamp) {
        DataModel info = new DataModel(value, timestamp);
        this.data.put(key, info);
    }

    public synchronized DataModel getData(String key) {
        return this.data.get(key);
    }

    public void stopThread() {
        interrupt();
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ioException) {
                Log.e("PracticalTest02", "An exception has occurred: " + ioException.getMessage());
            }
        }
    }
}