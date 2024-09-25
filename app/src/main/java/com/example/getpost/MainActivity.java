package com.example.getpost;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private TextView tvResult;
    private EditText editTextUserName;
    private EditText editTextPassword;
    private Button buttonSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvResult = findViewById(R.id.tvResult);
        Button btnGet = findViewById(R.id.btnGet);

        editTextUserName = findViewById(R.id.editTextUserName);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonSubmit = findViewById(R.id.buttonSubmit);

        // Dirección para el emulador
        String baseUrl = "http://10.0.2.2:3001/posts"; // Cambia esto según tu configuración

        // Configurar el botón para realizar la solicitud GET
        btnGet.setOnClickListener(v -> new HttpGetRequest(MainActivity.this).execute(baseUrl));

        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = editTextUserName.getText().toString();
                String password = editTextPassword.getText().toString();
                hacerPostRequest(username, password);
            }
        });
    }

    // Clase interna estática para manejar las solicitudes GET
    private static class HttpGetRequest extends AsyncTask<String, Void, String> {
        private final MainActivity activity;

        // Constructor para pasar la referencia de la actividad
        HttpGetRequest(MainActivity activity) {
            this.activity = activity;
        }

        @Override
        protected String doInBackground(String... params) {
            String response = "";
            try {
                URL url = new URL(params[0]); // Utilizar la URL pasada como parámetro
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Accept", "application/json");
                connection.connect();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuilder content = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }
                    in.close();
                    response = content.toString();
                } else {
                    response = "Error: " + responseCode;
                }

                connection.disconnect();
            } catch (Exception e) {
                Log.e("HttpGetRequest", "Exception: " + e.getMessage(), e);
                response = "Exception: " + e.getMessage();
            }

            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d("HttpGetRequest", "Response: " + result);
            // Actualizar el TextView en la actividad
            activity.runOnUiThread(() -> activity.tvResult.setText(result != null ? result : "Error al obtener datos."));
        }
    }


    private void hacerPostRequest(String username, String password) {
        new PostTask().execute(username, password);
    }

    private class PostTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String username = params[0];
            String password = params[1];
            String apiUrl = "http://10.0.2.2:3001/posts"; // Cambia a la URL de tu Mockoon

            try {
                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                // Crear el JSON de entrada
                String jsonInputString = String.format("{\"username\":\"%s\", \"password\":\"%s\"}", username, password);

                // Enviar el JSON al servidor
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                // Leer la respuesta
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                return response.toString();

            } catch (Exception e) {
                Log.e("PostTask", "Error: " + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d("PostTask", "Response: " + result);
            // Mostrar la respuesta en un Toast
            if (result != null) {
                Toast.makeText(MainActivity.this, result, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MainActivity.this, "Error en la solicitud", Toast.LENGTH_SHORT).show();
            }
        }
    }

}