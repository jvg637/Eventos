package org.example.eventos.actividades;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.appinvite.AppInviteInvitationResult;
import com.google.android.gms.appinvite.AppInviteReferral;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.storage.FirebaseStorage;

import org.example.eventos.BuildConfig;
import org.example.eventos.EventosAplicacion;
import org.example.eventos.R;
import org.example.eventos.modelo.Evento;
import org.example.eventos.util.Comun;

import static org.example.eventos.modelo.EventosFirestore.EVENTOS;
import static org.example.eventos.util.Comun.mFirebaseRemoteConfig;
import static org.example.eventos.util.Comun.mostrarDialogo;
import static org.example.eventos.util.Comun.storage;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

public class ActividadPrincipal extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private AdaptadorEventos adaptador;

    private GoogleApiClient mGoogleApiClient;
    private static final int REQUEST_INVITE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_principal);
        Fabric.with(this, new Crashlytics());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

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

//        // Expose
//        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
//        StrictMode.setVmPolicy(builder.build());

        ActivityCompat.requestPermissions(ActividadPrincipal.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(AppInvite.API)
                .enableAutoManage(this, this)
                .build();

        boolean autoLaunchDeepLink = true;
        AppInvite.AppInviteApi.getInvitation(mGoogleApiClient, this,
                autoLaunchDeepLink)
                .setResultCallback(
                        new ResultCallback<AppInviteInvitationResult>() {
                            @Override
                            public void onResult(AppInviteInvitationResult result) {
                                if (result.getStatus().isSuccess()) {
                                    Intent intent = result.getInvitationIntent();
                                    String deepLink = AppInviteReferral.getDeepLink(intent);
                                    String invitationId = AppInviteReferral
                                            .getInvitationId(intent);
                                    android.net.Uri url = Uri.parse(deepLink);
                                    String descuento = url.getQueryParameter("descuento");
                                    mostrarDialogo(getApplicationContext(),
                                            "Tienes un descuento del " + descuento
                                                    + "% gracias a la invitación: " + invitationId);
                                }
                            }
                        });

        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings =
                new FirebaseRemoteConfigSettings
                        .Builder()
                        .setDeveloperModeEnabled(BuildConfig.DEBUG)
                        .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);

        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_default);

//        long cacheExpiration = 3600;
        long cacheExpiration = 0;
        mFirebaseRemoteConfig.fetch(cacheExpiration)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mFirebaseRemoteConfig.activateFetched();
                        getColorFondo();
                        getAcercaDe();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Comun.colorFondo=mFirebaseRemoteConfig.getString("color_fondo");
                        Comun.acercaDe=mFirebaseRemoteConfig.getBoolean("acerca_de");
                    }
                });
    }

    private void getColorFondo() {
        Comun.colorFondo = mFirebaseRemoteConfig.getString("color_fondo");
    }
    private void getAcercaDe() {
        Comun.acercaDe = mFirebaseRemoteConfig.getBoolean("acerca_de");
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
                ActivityCompat.requestPermissions(ActividadPrincipal.this, new String[]{android.Manifest.permission.ACCESS_NETWORK_STATE}, 4);

                return;
            }

            case 4: {
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(ActividadPrincipal.this, "Permiso denegado para conocer el estado de la red.", Toast.LENGTH_SHORT).show();
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
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.ITEM_NAME,
                    "suscripciones");
            ((EventosAplicacion) getApplication()).getFirebaseAnalytics().logEvent("menus", bundle);

            Intent intent = new Intent(getBaseContext(), Temas.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_enviar_evento) {
            Intent intent = new Intent(getBaseContext(), EnviarEvento.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_share_photo) {
            Intent intent = new Intent(getBaseContext(), SharedPhotosDrive.class);
            intent.putExtra("accion", "carpeta_compartirda");
            startActivity(intent);
            return true;
        } else if (id == R.id.action_share_reto_estoy_aqui) {
            Intent intent = new Intent(getBaseContext(), SharedPhotosDrive.class);
            intent.putExtra("accion", "reto_estoy_aqui");
            startActivity(intent);
            return true;
        } else if (id == R.id.action_invitar) {
            invitar();
        } else if (id == R.id.action_error) {
            Crashlytics.getInstance().crash();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void invitar() {
        Intent intent = new AppInviteInvitation.IntentBuilder(getString(
                R.string.invitation_title))
                .setMessage(getString(R.string.invitation_message))
                .setDeepLink(Uri.parse(getString(R.string.invitation_deep_link)))
                .setCustomImage(Uri.parse(getString(R.string.invitation_custom_image)))
                .setCallToActionText(getString(R.string.invitation_cta))
                .build();
        startActivityForResult(intent, REQUEST_INVITE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
            } else {
                Toast.makeText(this, "Error al enviar la invitación",
                        Toast.LENGTH_LONG);
            }
        }
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


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Error al enviar la invitación", Toast.LENGTH_LONG);
    }
}
