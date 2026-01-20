package ro.pub.cs.systems.eim.practicaltest02v5;

import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientThread extends Thread {
    private String address;
    private int port;
    private String p1, p2, p3; // Parametrii generici
    private TextView resultTextView;

    public ClientThread(String address, int port, String p1, String p2, String p3, TextView resultTextView) {
        this.address = address;
        this.port = port;
        this.p1 = p1; this.p2 = p2; this.p3 = p3;
        this.resultTextView = resultTextView;
    }

    @Override
    public void run() {
        try {
            Socket socket = new Socket(address, port);
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);

            // Trimitem datele
            pw.println(p1);
            pw.println(p2);
            pw.println(p3);

            // Citim raspunsul
            String response = br.readLine();

            // Afisam (Text + Toast)
            if (response != null) {
                final String finalResp = response;
                resultTextView.post(() -> {
                    resultTextView.setText(finalResp);
                    Toast.makeText(resultTextView.getContext(), finalResp, Toast.LENGTH_LONG).show();
                });
            }
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}