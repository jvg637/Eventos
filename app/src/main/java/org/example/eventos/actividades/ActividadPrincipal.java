package org.example.eventos.actividades;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;

import org.example.eventos.R;
import org.example.eventos.modelo.Evento;
import org.example.eventos.util.Comun;

import static org.example.eventos.modelo.EventosFirestore.EVENTOS;
import static org.example.eventos.util.Comun.mostrarDialogo;
import static org.example.eventos.util.Comun.storage;

public class ActividadPrincipal extends AppCompatActivity {
    private AdaptadorEventos adaptador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_principal);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

//        crearEventos();
        Query query = FirebaseFirestore.getInstance()
                .collection(EVENTOS)
                .limit(50);
        FirestoreRecyclerOptions<Evento> opciones = new FirestoreRecyclerOptions
                .Builder<Evento>().setQuery(query, Evento.class).build();
        adaptador = new AdaptadorEventos(opciones);
        final RecyclerView recyclerView = findViewById(R.id.reciclerViewEventos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adaptador);

        final SharedPreferences preferencias =
                getApplicationContext().getSharedPreferences("Temas",
                        Context.MODE_PRIVATE);
        if (preferencias.getBoolean("Inicializado", false) == false) {
            final SharedPreferences prefs =
                    getApplicationContext().getSharedPreferences(
                            "Temas", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("Inicializado", true);
            editor.commit();
            FirebaseMessaging.getInstance().subscribeToTopic("Todos");
        }

        adaptador.setOnItemClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                int position = recyclerView.getChildAdapterPosition(view);
                Evento currentItem = (Evento) adaptador.getItem(position);
                String idEvento = adaptador.getSnapshots().getSnapshot(position).getId();
                Context context = getAppContext();
                Intent intent = new Intent(context, EventoDetalles.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("evento", idEvento);
                context.startActivity(intent);
            }
        });

        Comun.storage = FirebaseStorage.getInstance();
        Comun.storageRef = storage.getReferenceFromUrl("gs://eventos-eae83.appspot.com");

        // Expose
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        ActivityCompat.requestPermissions(ActividadPrincipal.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(ActividadPrincipal.this, "Permiso denegado para mantener escribir en el almacenamiento.", Toast.LENGTH_SHORT).show();
                }

                ActivityCompat.requestPermissions(ActividadPrincipal.this, new String[]{android.Manifest.permission.CAMERA}, 2);
                return;
            }

            case 2: {
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(ActividadPrincipal.this, "Permiso denegado para acceder a la camara", Toast.LENGTH_SHORT).show();
                }
                ActivityCompat.requestPermissions(ActividadPrincipal.this, new String[]{android.Manifest.permission.GET_ACCOUNTS}, 3);
                return;
            }

            case 3: {
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(ActividadPrincipal.this, "Permiso denegado para acceder a las cuentas", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_actividad_principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_temas) {
            Intent intent = new Intent(getBaseContext(), Temas.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_enviar_evento) {
            Intent intent = new Intent(getBaseContext(), EnviarEvento.class);
            startActivity(intent);
            return true;
        }else if (id == R.id.action_share_photo) {
            Intent intent = new Intent(getBaseContext(), ShareFotoDrive.class);
            intent.putExtra("accion", "carpeta_compartirda");
            startActivity(intent);
            return true;
        } else if (id == R.id.action_share_reto_estoy_aqui) {
            Intent intent = new Intent(getBaseContext(), ShareFotoDrive.class);
            intent.putExtra("accion", "reto_estoy_aqui");
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        Bundle extras = getIntent().getExtras();
//        if (getIntent().hasExtra("body")) {
//            mostrarDialogo(this, extras.getString("body"));
//            extras.remove("body");
//        }
//    }

    @Override
    protected void onResume() {
        super.onResume();
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.keySet().size() > 4) {
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

    @Override
    public void onStart() {
        super.onStart();
        adaptador.startListening();
        current = this;
    }

    @Override
    public void onStop() {
        super.onStop();
        adaptador.stopListening();
    }

    private static ActividadPrincipal current;

    public static ActividadPrincipal getCurrentContext() {
        return current;
    }

    public static Context getAppContext() {
        return ActividadPrincipal.getCurrentContext();
    }


}
