package org.example.eventos.actividades;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import org.example.eventos.BuildConfig;
import org.example.eventos.R;

import static org.example.eventos.util.Comun.mFirebaseRemoteConfig;
import static org.example.eventos.util.Comun.mostrarDialogo;


public class SplashScreen extends AppCompatActivity {

    static TextView txtAdvertencia;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        txtAdvertencia = (TextView) findViewById(R.id.txtAdvertencia);

        compruebaRegion();

    }

    public void compruebaRegion() {
        actualizaRemoteConfig();
    }

    private void actualizaRemoteConfig() {
        FirebaseRemoteConfigSettings configSettings =
                new FirebaseRemoteConfigSettings
                        .Builder()
                        .setDeveloperModeEnabled(BuildConfig.DEBUG)
                        .build();

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);

        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_default);

//        long cacheExpiration = 3600;
        long cacheExpiration = 0;

        mFirebaseRemoteConfig.fetch(cacheExpiration).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Boolean regionDesarrollo;
                mFirebaseRemoteConfig.activateFetched();
                regionDesarrollo = mFirebaseRemoteConfig.getBoolean("pais");

                txtAdvertencia.setVisibility(View.VISIBLE);
                if (regionDesarrollo != null && regionDesarrollo) {
                    txtAdvertencia.setText("Esta aplicación muestra información sobre eventos");
                } else {
                    txtAdvertencia.setText("Esta aplicación muestra eventos de España");
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(SplashScreen.this.getBaseContext(), "No se ha podido obtener la región (País)", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void continuar(View view) {
        Intent i = new Intent(this, ActividadPrincipal.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.keySet().size() > 5) {
            String evento = "";
            evento = "Evento: " + extras.getString("evento") + "\n";
            evento = evento + "Día: " + extras.getString("dia") + "\n";
            evento = evento + "Ciudad: " + extras.getString("ciudad") + "\n";
            evento = evento + "Comentario: " + extras.getString("comentario");
            mostrarDialogo(getApplicationContext(), evento);
            for (String key : extras.keySet()) {
                getIntent().removeExtra(key);
            }
            extras = null;
        }
    }
}