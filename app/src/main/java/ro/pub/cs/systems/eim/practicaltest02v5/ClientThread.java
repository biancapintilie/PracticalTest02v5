package ro.pub.cs.systems.eim.practicaltest02v5;

import android.util.Log;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientThread extends Thread {
    private String address;
    private int port;
    private String command;
    private TextView resultTextView;
    private Socket socket;

    public ClientThread(String address, int port, String command, TextView resultTextView) {
        this.address = address;
        this.port = port;
        this.command = command;
        this.resultTextView = resultTextView;
    }

    @Override
    public void run() {
        try {
            socket = new Socket(address, port);

            PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
            pw.println(command);

            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String result = br.readLine();

            final String finalizedResult = result;
            resultTextView.post(new Runnable() {
                @Override
                public void run() {
                    resultTextView.setText(finalizedResult);
                }
            });

        } catch (IOException ioException) {
            Log.e("PracticalTest02v5", "Error Client: " + ioException.getMessage());
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ioException) {
                    Log.e("PracticalTest02v5", "Error Closing: " + ioException.getMessage());
                }
            }
        }
    }
}