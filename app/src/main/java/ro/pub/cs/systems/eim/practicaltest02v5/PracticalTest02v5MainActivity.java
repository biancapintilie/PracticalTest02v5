package ro.pub.cs.systems.eim.practicaltest02v5;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class PracticalTest02v5MainActivity extends AppCompatActivity {

    // UI Elements
    private EditText serverPortEditText;
    private Button connectButton;

    private EditText clientAddressEditText;
    private EditText clientPortEditText;
    private EditText commandEditText;
    private Button executeButton;

    private TextView resultTextView;

    private ServerThread serverThread = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practical_test02v5_main);

        // Initializare View-uri
        serverPortEditText = findViewById(R.id.server_port_edit_text);
        connectButton = findViewById(R.id.connect_button);

        clientAddressEditText = findViewById(R.id.client_address_edit_text);
        clientPortEditText = findViewById(R.id.client_port_edit_text);
        commandEditText = findViewById(R.id.command_edit_text);
        executeButton = findViewById(R.id.execute_button);

        resultTextView = findViewById(R.id.result_text_view);

        // Buton Start Server
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String port = serverPortEditText.getText().toString();
                if (port.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Port lipsa!", Toast.LENGTH_SHORT).show();
                    return;
                }
                serverThread = new ServerThread(Integer.parseInt(port));
                serverThread.start();
                Toast.makeText(getApplicationContext(), "Server pornit!", Toast.LENGTH_SHORT).show();
            }
        });

        // Buton Execute Client
        executeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String address = clientAddressEditText.getText().toString();
                String port = clientPortEditText.getText().toString();
                String command = commandEditText.getText().toString();

                if (address.isEmpty() || port.isEmpty() || command.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Completeaza campurile!", Toast.LENGTH_SHORT).show();
                    return;
                }

                ClientThread clientThread = new ClientThread(
                        address, Integer.parseInt(port), command, resultTextView
                );
                clientThread.start();
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (serverThread != null) {
            serverThread.stopThread();
        }
        super.onDestroy();
    }
}