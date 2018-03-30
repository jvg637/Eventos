package org.example.eventos.actividades;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.example.eventos.R;

/**
 * Created by jvg63 on 30/03/2018.
 */

public class EventosWhatsapp extends AppCompatActivity {

    public void click_publicar_whatsapp(View view) {
        EditText text = ((EditText) findViewById(R.id.txt_whatsapp));
        if (text.getText().toString().isEmpty()) {
            text.setError("No hay texto a enviar introducido...");
        } else {
            enviaMensajeWhatsApp(elEvento + ": " + text.getText().toString());
        }
    }


    public void enviaMensajeWhatsApp(String msj) {
        PackageManager pm = getPackageManager();
        try {
            Intent waIntent = new Intent(Intent.ACTION_SEND);
            waIntent.setType("text/plain");
            PackageInfo info = pm.getPackageInfo("com.whatsapp", PackageManager.GET_META_DATA);
            waIntent.setPackage("com.whatsapp");
            waIntent.putExtra(Intent.EXTRA_TEXT, msj);
            startActivity(Intent.createChooser(waIntent, "Compartir con:"));
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(this, "WhatsApp no esta instalado!", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.evento_detalles_whatsapp);

        elEvento = getIntent().getStringExtra("evento");
        if (elEvento==null) {
            elEvento="";
        }
    }

    String elEvento;
}
