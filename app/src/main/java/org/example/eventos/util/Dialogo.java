package org.example.eventos.util;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import org.example.eventos.actividades.EventoDetalles;

/**
 * Created by jvg63 on 17/02/2018.
 */
public class Dialogo extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle extras = getIntent().getExtras();
        if (getIntent().hasExtra("mensaje")) {
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Mensaje:");
            alertDialog.setMessage(extras.getString("mensaje"));

            if (extras.getString("detalle", "").isEmpty()) {

                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "CERRAR",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                finish();
                            }
                        });
            } else {
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                Intent intent = new Intent(Dialogo.this, EventoDetalles.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra("evento", extras.getString("evento"));
                                Dialogo.this.startActivity(intent);
                                finish();
                            }
                        });
            }
            alertDialog.show();
            extras.remove("mensaje");
        }
    }
}
