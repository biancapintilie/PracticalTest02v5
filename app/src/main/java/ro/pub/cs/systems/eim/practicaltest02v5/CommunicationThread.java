package ro.pub.cs.systems.eim.practicaltest02v5;

import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.impl.client.BasicResponseHandler;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;

public class CommunicationThread extends Thread {
    private ServerThread serverThread;
    private Socket socket;

    public CommunicationThread(ServerThread serverThread, Socket socket) {
        this.serverThread = serverThread;
        this.socket = socket;
    }

    @Override
    public void run() {
        if (socket == null) return;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);

            String line = br.readLine();
            if (line == null || line.isEmpty()) {
                socket.close();
                return;
            }

            String result = "";
            String[] parts = line.split(",");
            String command = parts[0];

            if ("put".equals(command) && parts.length >= 3) {
                String key = parts[1];
                String value = parts[2];

                Log.i("PracticalTest02", "[SERVER] PUT Request. Accesam Time API...");

                try {
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpGet httpGet = new HttpGet("https://time.now/developer/api/timezone/UTC");
                    String timeResponse = httpClient.execute(httpGet, new BasicResponseHandler());
                    Log.i("PracticalTest02", "Time API: " + timeResponse);
                } catch (Exception e) {
                    Log.e("PracticalTest02", "Time API failed: " + e.getMessage());
                }

                serverThread.setData(key, value);
                result = "OK, saved " + key;

            } else if ("get".equals(command) && parts.length >= 2) {
                String key = parts[1];
                Log.i("PracticalTest02", "[SERVER] GET Request pentru: " + key);

                ServerThread.Information info = serverThread.getData(key);

                if (info != null) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - info.timestamp > 10000) {
                        result = "none";
                        Log.i("PracticalTest02", "Cheia a expirat!");
                    } else {
                        result = info.value;
                    }
                } else {
                    result = "none";
                }
            } else {
                result = "Invalid command";
            }

            pw.println(result);
            socket.close();

        } catch (Exception ioException) {
            Log.e("PracticalTest02", "Error: " + ioException.getMessage());
        }
    }
}