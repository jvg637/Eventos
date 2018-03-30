package org.example.eventos.util;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by jvg63 on 25/02/2017.
 */

public class DialogoConfirmacion extends AppCompatActivity {
    AlertDialog alertDialog = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Mensaje:");

        Intent datos = getIntent();
        String tipoMensaje = "";

        if (datos.getExtras() != null) {

            tipoMensaje = datos.getExtras().getString("tipoMensaje", "");

        }

        String textoMensaje = tipoMensaje.isEmpty() ? "¿Desea eliminar la portada del evento?" : "“Con el fin de mejorar la aplicación, te pedimos que participes en el envío\n" +
                "automático de errores a nuestros servidores. ¿Estás de acuerdo?”";

        alertDialog.setCancelable(false);
        alertDialog.setMessage(textoMensaje);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, ("Aceptar"), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_OK, returnIntent);
                finish();

            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, ("Cancelar"), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_CANCELED, returnIntent);
                finish();

            }
        });
        alertDialog.show();
    }


}
