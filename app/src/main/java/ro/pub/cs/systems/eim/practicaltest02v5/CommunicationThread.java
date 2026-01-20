package ro.pub.cs.systems.eim.practicaltest02v5;

import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

// Importuri HTTP
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.impl.client.BasicResponseHandler;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import org.json.JSONObject;

public class CommunicationThread extends Thread {
    private ServerThread serverThread;
    private Socket socket;

    // URL-ul real care merge (sau pune-l pe cel din PDF daca vrei)
    private final String URL_TIMP = "http://time.now/developer/api/timezone/UTC";

    public CommunicationThread(ServerThread serverThread, Socket socket) {
        this.serverThread = serverThread;
        this.socket = socket;
    }

    // --- METODA CARE IA TIMPUL DE PE NET ---
    private long iaTimpulDePeNet() {
        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(URL_TIMP);
            String response = httpClient.execute(httpGet, new BasicResponseHandler());

            // Parsam JSON-ul primit
            JSONObject json = new JSONObject(response);
            // API-ul WorldTime returneaza "unixtime" (secunde), noi vrem milisecunde
            long unixTime = json.getLong("unixtime");
            return unixTime * 1000;

        } catch (Exception e) {
            Log.e("PracticalTest02", "Eroare HTTP Time: " + e.getMessage());
            // FALLBACK: Daca nu merge netul, luam timpul local ca sa nu crape
            return System.currentTimeMillis();
        }
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

                // 1. Luam timpul de pe net
                Log.i("PracticalTest02", "Luam timpul pentru PUT...");
                long netTime = iaTimpulDePeNet();

                // 2. Salvam in server cu tot cu timp
                serverThread.setData(key, value, netTime);
                result = "Data saved: " + key + " @ " + netTime;

            } else if ("get".equals(command) && parts.length >= 2) {
                String key = parts[1];
                ServerThread.DataModel data = serverThread.getData(key);

                if (data != null) {
                    // 1. Luam timpul curent de pe net pentru comparatie
                    Log.i("PracticalTest02", "Luam timpul pentru GET...");
                    long netTimeNow = iaTimpulDePeNet();

                    // 2. Verificam diferenta (1 minut = 60000 ms, 10 sec = 10000 ms)
                    // Subiectul zice 1 minut
                    if (netTimeNow - data.timestamp > 60000) {
                        result = "none (expired)";
                    } else {
                        result = data.value;
                    }
                } else {
                    result = "none (missing)";
                }
            } else {
                result = "Invalid command";
            }

            pw.println(result);
            socket.close();

        } catch (Exception e) {
            Log.e("PracticalTest02", "Error: " + e.getMessage());
        }
    }
}