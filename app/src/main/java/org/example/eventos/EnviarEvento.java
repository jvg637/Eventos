package org.example.eventos;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.example.eventos.Comun.API_KEY;
import static org.example.eventos.Comun.ID_PROYECTO;
import static org.example.eventos.Comun.URL_SERVIDOR;


/**
 * Created by jvg63 on 19/02/2017.
 */
public class EnviarEvento extends AppCompatActivity {
    EditText mensaje;
    Button enviar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.enviar_evento);
        mensaje = findViewById(R.id.mensaje);
        enviar = findViewById(R.id.enviar);
        enviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enviaMsg();
            }
        });

    }

    public void enviaMsg() {
      
            EnviarMensajeEnServidorWebTask tarea = new EnviarMensajeEnServidorWebTask();
            tarea.contexto = getBaseContext();
            tarea.mensaje = mensaje.getText().toString();
            tarea.execute();
        }


    private  static class EnviarMensajeEnServidorWebTask extends AsyncTask<Void, Void, String> {
       String response = "error";
        Context contexto;
        String mensaje = "";

        public void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... arg0) {
            try {
                Uri.Builder constructorParametros = new Uri.Builder().appendQueryParameter("mensaje", mensaje).
                        appendQueryParameter("idapp", ID_PROYECTO).appendQueryParameter("apiKey", API_KEY);
                String parametros = constructorParametros.build().getEncodedQuery();
                String url = URL_SERVIDOR +  "notificar.php";
                URL direccion = new URL(url);
                HttpURLConnection conexion = (HttpURLConnection) direccion.openConnection();
                conexion.setRequestMethod("POST");
                conexion.setRequestProperty("Accept-Language", "UTF-8");
                conexion.setDoOutput(true);
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(conexion.getOutputStream());
                outputStreamWriter.write(parametros.toString());
                outputStreamWriter.flush();
                int respuesta = conexion.getResponseCode();
                if (respuesta == 200) {
                    response = "ok";
                } else {
                    response = "error";
                }
            } catch (IOException e) {
                response = "error";
            }
            return response;
        }

        public void onPostExecute(String res) {
            if (res == "ok") {

                Toast.makeText(contexto, "Mensaje Enviado Correctamente!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
