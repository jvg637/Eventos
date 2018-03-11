package org.example.eventos.actividades;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import org.example.eventos.BuildConfig;
import org.example.eventos.R;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by jvg63 on 19/02/2017.
 */

public class SharedPhotosDrive extends AppCompatActivity {
    //    public TextView mDisplay;
    static WebView mDisplay;
    String evento;
    private String idCarpeta;
    ////https://drive.google.com/drive/folders/0B8h9K5NvlyIAOGNfNTF0ZHhhd3M?usp=sharing
    ///https://drive.google.com/open?id=0B8h9K5NvlyIALUtGZ3VaY2k5WGs

    String accion;

    @Override
    protected void onDestroy() {
//        unregisterReceiver(mHandleMessageReceiver);
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fotografias_drive);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        registerReceiver(mHandleMessageReceiver, new IntentFilter(DISPLAY_MESSAGE_ACTION));
//        mDisplay = (TextView) findViewById(R.id.display);
        mDisplay = (WebView) findViewById(R.id.display);
        mDisplay.getSettings().setJavaScriptEnabled(true);
        mDisplay.getSettings().setBuiltInZoomControls(false);
//        mDisplay.loadUrl("file:///android_asset/fotografias.html");
        mDisplay.loadUrl("https://eventos-eae83.firebaseapp.com/fotografias.html");

        credencial = GoogleAccountCredential.usingOAuth2(this, Arrays.asList(DriveScopes.DRIVE));

        SharedPreferences prefs = getSharedPreferences("Preferencias", Context.MODE_PRIVATE);
        nombreCuenta = prefs.getString("nombreCuenta", null);

//        noAutoriza = prefs.getBoolean("noAutoriza", false);


        Bundle extras = getIntent().getExtras();
        accion = extras.getString("accion");
        if (accion.equals("reto_estoy_aqui")) {
            // Ruta de Reto
            idCarpeta = "1moTqIoGBmKmLxfhPBYwPIwqM3YHlSzuJ";
            setTitle("Reto estoy aqui!!!");

        } else {
            // Ruta compartida
            idCarpeta = "1xWZbwOyLYkc27XwO7TV-43pkJZZ01blL";
            setTitle("Subir fot. carp. compartida");
        }

//        if (!noAutoriza) {
        if (nombreCuenta == null) {
            PedirCredenciales();
        } else {
            credencial.setSelectedAccountName(nombreCuenta);
            servicio = obtenerServicioDrive(credencial);
            listarFicheros();
        }
//        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

//    private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            boolean inicializa = intent.getExtras().getBoolean("inicializa", false);
//            String nuevoMensaje = intent.getExtras().getString("mensaje");
////            if (!inicializa)
////                mDisplay.append(nuevoMensaje + "\n");
////            else
////                mDisplay.setText("");
//        }
//    };
//
//    static void mostrarTexto(Context contexto, String mensaje) {
//        mostrarTexto(contexto, mensaje, false);
//    }

//    static void mostrarTexto(Context contexto, String mensaje, boolean inicializa) {
//        Intent intent = new Intent(DISPLAY_MESSAGE_ACTION);
//        intent.putExtra("mensaje", mensaje);
//        intent.putExtra("inicializa", inicializa);
//        contexto.sendBroadcast(intent);
//    }


    private void PedirCredenciales() {
        if (nombreCuenta == null) {
            startActivityForResult(credencial.newChooseAccountIntent(), SOLICITUD_SELECCION_CUENTA);
        }
    }

    private Drive obtenerServicioDrive(GoogleAccountCredential credencial) {
        return new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credencial).build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_drive, menu);
        return true;
    }


    private String getRealPathFromURI(Uri contentURI) {
        String result = null;

        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);

        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            if (cursor.moveToFirst()) {
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                result = cursor.getString(idx);
            }
            cursor.close();
        }
        return result;
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {

            case SOLICITUD_SELECCIONAR_FOTOGRAFIA:
                if (resultCode == Activity.RESULT_OK) {
                    Uri ficheroSeleccionado = data.getData();
                    String[] proyeccion = {MediaStore.Images.Media.DATA};
                    Cursor cursor = managedQuery(ficheroSeleccionado, proyeccion, null, null, null);
                    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    cursor.moveToFirst();
                    uriFichero = Uri.fromFile(new java.io.File(cursor.getString(column_index)));
                    guardarFicheroEnDrive(this.findViewById(android.R.id.content));
                }
                break;


            case SOLICITUD_SELECCION_CUENTA:
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                    nombreCuenta = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (nombreCuenta != null) {
                        credencial.setSelectedAccountName(nombreCuenta);
                        servicio = obtenerServicioDrive(credencial);
                        SharedPreferences prefs = getSharedPreferences("Preferencias", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("nombreCuenta", nombreCuenta);
                        editor.commit();

                        listarFicheros();
                    }
                }
                break;
            case SOLICITUD_HACER_FOTOGRAFIA:
                if (resultCode == Activity.RESULT_OK) {
                    guardarFicheroEnDrive(this.findViewById(android.R.id.content));
                }
                break;


            case SOLICITUD_AUTORIZACION:
                if (resultCode == Activity.RESULT_OK) {
                    guardarFicheroEnDrive(this.findViewById(android.R.id.content));
                } else {
                    noAutoriza = true;
                    SharedPreferences prefs = getSharedPreferences("Preferencias", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("noAutoriza", true);
                    editor.commit();
                    mostrarMensaje(this, "El usuario no autoriza usar Google Drive");
                }
                break;

        }
    }

    private void guardarFicheroEnDrive(final View view) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mostrarCarga(SharedPhotosDrive.this, "Subiendo imagen...");
                    java.io.File ficheroJava = new java.io.File(uriFichero.getPath());
                    FileContent contenido = new FileContent("image/jpeg", ficheroJava);
                    File ficheroDrive = new File();


                    if (accion.equals("reto_estoy_aqui")) {
                        ficheroDrive.setName("VILCHES GUTIERREZ, JORGE.jpg");

                    } else {
                        // Ruta compartida
                        ficheroDrive.setName(ficheroJava.getName());
                    }


                    ficheroDrive.setMimeType("image/jpeg");
                    ficheroDrive.setParents(Collections.singletonList(idCarpeta));

                    Log.d("traza", "test " + credencial.getSelectedAccountName());
                    File ficheroSubido = servicio.files().create(ficheroDrive, contenido).setFields("id").execute();
                    if (ficheroSubido.getId() != null) {
                        mostrarMensaje(SharedPhotosDrive.this, "¡Foto subida!");
                        listarFicheros();
                    }
                    ocultarCarga(SharedPhotosDrive.this);
                } catch (UserRecoverableAuthIOException e) {
                    ocultarCarga(SharedPhotosDrive.this);
                    startActivityForResult(e.getIntent(), SOLICITUD_AUTORIZACION);
                    //mostrarMensaje(SharedPhotosDrive.this, "¡La carpeta compartida no tiene permisos!: " + idCarpeta);
                } catch (IOException e) {
                    mostrarMensaje(SharedPhotosDrive.this, "Error;" + e.getMessage());
                    ocultarCarga(SharedPhotosDrive.this);
                    e.printStackTrace();
                }
            }
        }

        );
        t.start();

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        View vista = (View) findViewById(android.R.id.content);
        int id = item.getItemId();
        switch (id) {
            case R.id.action_camara:
                if (!noAutoriza) {
                    hacerFoto(vista);
                }
                break;
            case R.id.action_galeria:
                if (!noAutoriza) {
                    seleccionarFoto(vista);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    static Drive servicio = null;
    static GoogleAccountCredential credencial = null;
    static String nombreCuenta = null;

    private static Handler manejador = new Handler();
    private static Handler carga = new Handler();
    private static ProgressDialog dialogo;
    private Boolean noAutoriza = false;


    static void mostrarMensaje(final Context context, final String mensaje) {
        manejador.post(new Runnable() {
            public void run() {
                Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show();
            }
        });
    }

    static void mostrarCarga(final Context context, final String mensaje) {
        carga.post(new Runnable() {
            public void run() {
                dialogo = new ProgressDialog(context);
                dialogo.setMessage(mensaje);
                dialogo.setCanceledOnTouchOutside(false);
                dialogo.show();
            }
        });
    }

    static void ocultarCarga(final Context context) {
        carga.post(new Runnable() {
            public void run() {
                dialogo.dismiss();
            }
        });
    }

    static final int SOLICITUD_SELECCION_CUENTA = 1;
    static final int SOLICITUD_AUTORIZACION = 2;
    static final int SOLICITUD_SELECCIONAR_FOTOGRAFIA = 3;
    static final int SOLICITUD_HACER_FOTOGRAFIA = 4;
    private static Uri uriFichero;

    private static final String DISPLAY_MESSAGE_ACTION = "org.example.eventos.DISPLAY_MESSAGE2";

    public void listarFicheros() {
        if (nombreCuenta == null) {
            mostrarMensaje(this, "Debes seleccionar una cuenta de Google Drive");
        } else {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        mostrarCarga(SharedPhotosDrive.this, "Listando archivos...");
                        vaciarLista(getBaseContext());

                        FileList ficheros = servicio.files().list().setQ("'" + idCarpeta + "' in parents").setFields("*").execute();
//                  mostrarTexto(getBaseContext(),"", true);
                        List<File> files = ficheros.getFiles();
                        Log.d("ELEMENTOS", "" + ficheros.getFiles().size());
                        for (File fichero : files) {
//                        mostrarTexto(getBaseContext(), fichero.getOriginalFilename());
                            addItem(SharedPhotosDrive.this, fichero.getOriginalFilename(), fichero.getThumbnailLink());
                        }
                        mostrarMensaje(SharedPhotosDrive.this, "¡Archivos listados!");
                        ocultarCarga(SharedPhotosDrive.this);
                    } catch (UserRecoverableAuthIOException e) {
                        ocultarCarga(SharedPhotosDrive.this);
                        startActivityForResult(e.getIntent(), SOLICITUD_AUTORIZACION);
                    } catch (IOException e) {
                        mostrarMensaje(SharedPhotosDrive.this, "Error;" + e.getMessage());
                        ocultarCarga(SharedPhotosDrive.this);
                        e.printStackTrace();
                    }
                }
            });
            t.start();
        }
    }

    static void addItem(final Context context, final String fichero, final String imagen) {
        carga.post(new Runnable() {
            public void run() {
                mDisplay.loadUrl("javascript:add(\"" + fichero + "\",\"" + imagen + "\");");
            }
        });
    }

    static void vaciarLista(final Context context) {
        carga.post(new Runnable() {
            public void run() {
                mDisplay.loadUrl("javascript:vaciar()");
            }
        });
    }

    public void hacerFoto(View v) {
        if (nombreCuenta == null) {
            mostrarMensaje(this, "Debes seleccionar una cuenta de Google Drive");
        } else {
            Intent takePictureIntent =
                    new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                java.io.File ficheroFoto = null;
                try {
                    ficheroFoto = crearFicheroImagen();
                    if (ficheroFoto != null) {
                        Uri fichero = FileProvider.getUriForFile(
                                SharedPhotosDrive.this,
                                BuildConfig.APPLICATION_ID + ".provider",
                                ficheroFoto);
                        uriFichero =
                                Uri.parse("content://" + ficheroFoto.getAbsolutePath());
                        takePictureIntent.putExtra(
                                MediaStore.EXTRA_OUTPUT, fichero);
                        startActivityForResult(takePictureIntent,
                                SOLICITUD_HACER_FOTOGRAFIA);
                    }
                } catch (IOException ex) {
                    return;
                }
            }
        }
    }

    private java.io.File crearFicheroImagen() throws IOException {
        String tiempo = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        String nombreFichero = "JPEG_" + tiempo + "_";
        java.io.File dirAlmacenaje =
                new java.io.File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DCIM), "Camera");
        java.io.File ficheroImagen = java.io.File.createTempFile(
                nombreFichero,
                ".jpg", dirAlmacenaje);
        return ficheroImagen;
    }

    public void seleccionarFoto(View v) {
        if (nombreCuenta == null) {
            mostrarMensaje(this, "Debes seleccionar una cuenta de Google Drive");
        } else {
            Intent seleccionFotografiaIntent = new Intent();
            seleccionFotografiaIntent.setType("image/*");
            seleccionFotografiaIntent.setAction(Intent.ACTION_PICK);
            startActivityForResult(Intent.createChooser(seleccionFotografiaIntent, "Seleccionar fotografía"), SOLICITUD_SELECCIONAR_FOTOGRAFIA);
        }
    }

}
