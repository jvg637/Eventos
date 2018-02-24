package org.example.eventos.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by jvg63 on 24/02/2018.
 */

public class Comun {
    public static void mostrarDialogo(final Context context
            , final String mensaje) {
        Intent intent = new Intent(context, Dialogo.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("mensaje", mensaje);
        context.startActivity(intent);
    }

    public static void mostrarDialogo2(final Context context
            , final String mensaje, String evento) {
        Intent intent = new Intent(context, Dialogo.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("mensaje", mensaje);
        intent.putExtra("evento", evento);
        intent.putExtra("detalle", "si");
        context.startActivity(intent);
    }


    //    static final String URL_SERVIDOR = "http://cursoandroid.hol.es/notificaciones/";
    public static final String URL_SERVIDOR = "http://eventosjvg.esy.es/";
    public static String ID_PROYECTO = "eventos-eae83";
    public static String API_KEY = "AAAAIDy5Iuo:APA91bE3QEymE6zURi353tEw0kYAp_DBOIPWl7R5ft2KwSvFoQi_-c46VErwX8WkfgtAmz-S8VoiKA-ozPROcvUx1RzvIoBGsOOppcKTHTXi4MBsp8W21CHcng3nkD-0Qhy34qqaDuJ0";


    String idRegistro = "";

    public static class registrarDispositivoEnServidorWebTask
            extends AsyncTask<Void, Void, String> {
        String response = "error";
        Context contexto;
        String idRegistroTarea = "";

        public void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... arg0) {
            try {
                Uri.Builder constructorParametros = new Uri.Builder()
                        .appendQueryParameter("iddevice", idRegistroTarea)
                        .appendQueryParameter("idapp", ID_PROYECTO);
                String parametros =
                        constructorParametros.build().getEncodedQuery();
                String url = URL_SERVIDOR + "registrar.php";
                URL direccion = new URL(url);
                HttpURLConnection conexion = (HttpURLConnection)
                        direccion.openConnection();
                conexion.setRequestMethod("POST");
                conexion.setRequestProperty("Accept-Language", "UTF-8");
                conexion.setDoOutput(true);
                OutputStreamWriter outputStreamWriter = new
                        OutputStreamWriter(conexion.getOutputStream());
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
        }
    }

    public static void guardarIdRegistro(Context context, String idRegistro) {
        registrarDispositivoEnServidorWebTask tarea =
                new registrarDispositivoEnServidorWebTask();
        tarea.contexto = context;
        tarea.idRegistroTarea = idRegistro;
        tarea.execute();
    }

    public static void eliminarIdRegistro(Context context) {
        desregistrarDispositivoEnServidorWebTask tarea =
                new desregistrarDispositivoEnServidorWebTask();
        tarea.contexto = context;
        tarea.idRegistroTarea = FirebaseInstanceId.getInstance().getToken();
        tarea.execute();
    }

    public static class desregistrarDispositivoEnServidorWebTask
            extends AsyncTask<Void, Void, String> {
        String response = "error";
        Context contexto;
        String idRegistroTarea;

        public void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... arg0) {
            try {
                Uri.Builder constructorParametros = new Uri.Builder()
                        .appendQueryParameter("iddevice", idRegistroTarea)
                        .appendQueryParameter("idapp", ID_PROYECTO);
                String parametros =
                        constructorParametros.build().getEncodedQuery();
                String url = URL_SERVIDOR + "desregistrar.php";
                URL direccion = new URL(url);
                HttpURLConnection conexion = (HttpURLConnection)
                        direccion.openConnection();
                conexion.setRequestMethod("POST");
                conexion.setRequestProperty("Accept-Language", "UTF-8");
                conexion.setDoOutput(true);
                OutputStreamWriter outputStreamWriter = new
                        OutputStreamWriter(conexion.getOutputStream());
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
        }
    }

    public static FirebaseStorage storage;
    public static StorageReference storageRef;

    public static StorageReference getStorageReference() {
        return storageRef;
    }
}

