package ro.pub.cs.systems.eim.practicaltest02v5;

import android.util.Log;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class ServerThread extends Thread {
    private ServerSocket serverSocket;

    public static class Information {
        public String value;
        public long timestamp;

        public Information(String value, long timestamp) {
            this.value = value;
            this.timestamp = timestamp;
        }
    }

    private HashMap<String, Information> data = new HashMap<>();

    public ServerThread(int port) {
        try {
            this.serverSocket = new ServerSocket(port);
        } catch (IOException ioException) {
            Log.e("PracticalTest02", "Error: " + ioException.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Socket socket = serverSocket.accept();
                CommunicationThread communicationThread = new CommunicationThread(this, socket);
                communicationThread.start();
            }
        } catch (IOException ioException) {
            Log.e("PracticalTest02", "Error: " + ioException.getMessage());
        }
    }

    public synchronized void setData(String key, String value) {
        long currentTime = System.currentTimeMillis();
        Information info = new Information(value, currentTime);
        this.data.put(key, info);
    }

    public synchronized Information getData(String key) {
        return this.data.get(key);
    }

    public void stopThread() {
        interrupt();
        if (serverSocket != null) {
            try { serverSocket.close(); } catch (IOException e) {}
        }
    }
}