package org.example.eventos.actividades;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.example.eventos.EventosAplicacion;
import org.example.eventos.R;
import org.example.eventos.util.DialogoConfirmacion;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.example.eventos.util.Comun.acercaDe;
import static org.example.eventos.util.Comun.getStorageReference;
import static org.example.eventos.util.Comun.mostrarDialogo;

import com.google.firebase.perf.FirebasePerformance;
import com.google.firebase.perf.metrics.Trace;


/**
 * Created by jvg63 on 18/02/2018.
 */

public class EventoDetalles extends AppCompatActivity {
    TextView txtEvento, txtFecha, txtCiudad;
    ImageView imgImagen;
    String evento;
    CollectionReference registros;
    Trace mTrace;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.evento_detalles);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        txtEvento = (TextView) findViewById(R.id.txtEvento);
        txtFecha = (TextView) findViewById(R.id.txtFecha);
        txtCiudad = (TextView) findViewById(R.id.txtCiudad);
        imgImagen = (ImageView) findViewById(R.id.imgImagen);
        Bundle extras = getIntent().getExtras();
        evento = extras.getString("evento");
        if (evento==null) {
            android.net.Uri url = getIntent().getData();
            evento= url.getQueryParameter("evento");
        }

        ((EventosAplicacion) getApplication()).getFirebaseAnalytics().setUserProperty("evento_detalle", evento);

        registros = FirebaseFirestore.getInstance().collection("eventos");
        if (!evento.isEmpty()) {
            registros.document(evento).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        if (task.getResult().exists()) {
                            txtEvento.setText(task.getResult().get("evento").toString());
                            txtCiudad.setText(task.getResult().get("ciudad").toString());
                            txtFecha.setText(task.getResult().get("fecha").toString());

                            Object img = task.getResult().get("imagen");
                            if (img != null && !img.toString().isEmpty())
                                new DownloadImageTask((ImageView) imgImagen).execute(task.getResult().get("imagen").toString());
                        } else {
                            mostrarDialogo(getApplicationContext(), "Error! Evento no Existe!");
                            finish();

                        }
                    }
                }
            });
        }

        mTrace =
                FirebasePerformance.getInstance().newTrace("trace_EventoDetalles");
    }

    @Override
    protected void onResume(){
        super.onResume();
        mTrace.start();
    }
    @Override
    protected void onStop(){
        super.onStop();
        mTrace.stop();
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mImagen = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mImagen = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (mImagen != null)
                hayImagen = true;
            else
                hayImagen = false;

            return mImagen;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }

    }

    final int SOLICITUD_SUBIR_PUTDATA = 0;
    final int SOLICITUD_SUBIR_PUTSTREAM = 1;
    final int SOLICITUD_SUBIR_PUTFILE = 2;
    final int SOLICITUD_SELECCION_STREAM = 100;
    final int SOLICITUD_SELECCION_PUTFILE = 101;

    private ProgressDialog progresoSubida;
    Boolean subiendoDatos = false;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detalles, menu);

        if (acercaDe!=null && !acercaDe) {
            menu.removeItem(R.id.action_acercaDe);
        }
        return true;
    }

    boolean hayImagen = false;


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        View vista = (View) findViewById(android.R.id.content);
        int id = item.getItemId();

        FirebaseAnalytics mFirebaseAnalytics = ((EventosAplicacion) getApplication()).getFirebaseAnalytics();

        Bundle bundle = new Bundle();
        switch (id) {
            case R.id.action_putData:

                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "subir_imagen");
                mFirebaseAnalytics.logEvent("menus", bundle);


                if (hayImagen) {
                    subirAFirebaseStorage(SOLICITUD_SUBIR_PUTDATA, null);
                } else {
                    mostrarDialogo(getApplicationContext(), "Actualmente no hay cargada ninguna imagen!");
                }
                break;
            case R.id.action_streamData:
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "subir_stream");
                mFirebaseAnalytics.logEvent("menus", bundle);

                seleccionarFotografiaDispositivo(vista, SOLICITUD_SELECCION_STREAM);
                break;
            case R.id.action_putFile:

                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "subir_fichero");
                mFirebaseAnalytics.logEvent("menus", bundle);
                seleccionarFotografiaDispositivo(vista, SOLICITUD_SELECCION_PUTFILE);
                break;
            case R.id.action_getFile:
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "descargar_fichero");
                mFirebaseAnalytics.logEvent("menus", bundle);

                descargarDeFirebaseStorage(evento);
                break;
            case R.id.action_fotografiasDrive:
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "fotografias_drive");
                mFirebaseAnalytics.logEvent("menus", bundle);

                Intent intent = new Intent(getBaseContext(), FotografiasDrive.class);
                intent.putExtra("evento", evento);
                startActivity(intent);
                break;
            case R.id.action_deleteFile:
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "borrar_imagen");
                mFirebaseAnalytics.logEvent("menus", bundle);

                if (hayImagen) {
                    borrarDeFirebaseStorage();
                } else {
                    mostrarDialogo(getApplicationContext(), "Actualmente no hay cargada ninguna imagen!");
                }
                break;
            case R.id.action_acercaDe:
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "acerca_de");
                mFirebaseAnalytics.logEvent("menus", bundle);
                Intent intentWeb = new Intent(getBaseContext(), EventosWeb.class);
                intentWeb.putExtra("evento", evento);
                startActivity(intentWeb);
                break;
            case R.id.action_publicarFacebook:
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "publicar_facebook");
                mFirebaseAnalytics.logEvent("menus", bundle);
                Intent intentFb = new Intent(getBaseContext(), EventosFacebook.class);
                intentFb.putExtra("evento", evento);
                startActivity(intentFb);
                break;
            case R.id.action_publicarTwitter:
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "publicar_twitter");
                mFirebaseAnalytics.logEvent("menus", bundle);
                Intent intentTw = new Intent(getBaseContext(), EventosTwitter.class);
                intentTw.putExtra("evento", evento);
                startActivity(intentTw);
                break;

            case R.id.action_publicarInstagram:
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "publicar_instagram");
                mFirebaseAnalytics.logEvent("menus", bundle);
                Intent intentInstagram;
                intentInstagram = new Intent(getBaseContext(), EventosInstagram.class);
                intentInstagram.putExtra("evento", evento);
                startActivity(intentInstagram);
                break;
            case R.id.action_publicarWhatsapp:
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "publicar_whatsapp");
                mFirebaseAnalytics.logEvent("menus", bundle);
                Intent intentWhatsapp= new Intent(getBaseContext(), EventosWhatsapp.class);
                intentWhatsapp.putExtra("evento", evento);
                startActivity(intentWhatsapp);
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    final int SOLICITUD_ELIMINAR_PORTADA_FIREBASE = 103;


    private void borrarDeFirebaseStorage() {
        if (hayImagen) {

            Intent i = new Intent(this, DialogoConfirmacion.class);
            startActivityForResult(i, SOLICITUD_ELIMINAR_PORTADA_FIREBASE);


        } else {
            mostrarDialogo(getApplicationContext(), "No hay imagen de portada!");
        }
    }


    private void solicitudEliminarFirebase() {
        try {

            progresoSubida = ProgressDialog.show(EventoDetalles.this, "Espere ...", "Eliminando Fichero  ...", true);
            StorageReference referenciaImagen = getStorageReference().child(evento);

            referenciaImagen.delete().addOnSuccessListener(new OnSuccessListener<Void>() {

                @Override
                public void onSuccess(Void aVoid) {
                    Map<String, Object> datos = new HashMap<>();
                    datos.put("imagen", null);

                    /// Corregir update
                    FirebaseFirestore.getInstance().collection("eventos").document(evento).update(datos).addOnCompleteListener((new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful() && task.getException() == null) {
                                hideProgress();
                                mostrarDialogo(getApplicationContext(), "Imagen eliminada correctamente.");
                                imgImagen.setImageDrawable(null);
                                hayImagen = false;
                            } else {
                                hideProgress();
                                mostrarDialogo(getApplicationContext(), "Ha ocurrido un error al borrar la imagen en FireStore.");
                            }
                        }
                    }));

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    hideProgress();
                    mostrarDialogo(getApplicationContext(), "Ha ocurrido un error al borrar la imagen.");
                }
            });

        } catch (Exception e) {
            mostrarDialogo(getApplicationContext(), e.toString());
        }
    }


    private void descargarDeFirebaseStorage(String fichero) {

        StorageReference referenciaFichero = getStorageReference().child(fichero);
        File rootPath = new File(Environment.getExternalStorageDirectory(), "Eventos");
        if (!rootPath.exists()) {
            rootPath.mkdirs();
        }
        final File localFile = new File(rootPath, evento + ".jpg");
        referenciaFichero.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                mostrarDialogo(getApplicationContext(), "Fichero descargado con éxito: " + localFile.toString());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                mostrarDialogo(getApplicationContext(), "Error al descargar el fichero.");
            }
        });
    }

    public void seleccionarFotografiaDispositivo(View v, Integer solicitud) {
        Intent seleccionFotografiaIntent = new Intent(Intent.ACTION_PICK);
        seleccionFotografiaIntent.setType("image/*");
        startActivityForResult(seleccionFotografiaIntent, solicitud);
    }

    public void subirAFirebaseStorage(Integer opcion, String ficheroDispositivo) {

        progresoSubida = new
                ProgressDialog(EventoDetalles.this);
        progresoSubida.setTitle("Subiendo...");
        progresoSubida.setMessage("Espere...");
        progresoSubida.setCancelable(true);
        progresoSubida.setCanceledOnTouchOutside(false);


        String fichero = evento;
        imagenRef = getStorageReference().child(fichero);

        try {
            switch (opcion) {
                case SOLICITUD_SUBIR_PUTDATA:
                    imgImagen.setDrawingCacheEnabled(true);
                    imgImagen.buildDrawingCache();
                    Bitmap bitmap = imgImagen.getDrawingCache();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] data = baos.toByteArray();
                    uploadTask = imagenRef.putBytes(data);
                    break;
                case SOLICITUD_SUBIR_PUTSTREAM:
                    InputStream stream = new FileInputStream(new File(ficheroDispositivo));
                    uploadTask = imagenRef.putStream(stream);
                    break;
                case SOLICITUD_SUBIR_PUTFILE:
                    Uri file = Uri.fromFile(new File(ficheroDispositivo));
                    uploadTask = imagenRef.putFile(file);
                    break;


            }


            uploadTask.addOnFailureListener(onFailureListener).addOnSuccessListener(onSuccessListenerUpload).addOnProgressListener(onProgressListenerUpload).addOnPausedListener(onPausedListenerUpload);

            progresoSubida.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    uploadTask.cancel();
                    progresoSubida.hide();
                }
            });
//            progresoSubida.show();
            showProgress();
        } catch (
                IOException e)

        {
            mostrarDialogo(getApplicationContext(), e.toString());
        }
    }

    private OnSuccessListener<UploadTask.TaskSnapshot> onSuccessListenerUpload = new OnSuccessListener<UploadTask.TaskSnapshot>() {
        @Override
        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
            //UploadTask exito
            Map<String, Object> datos = new HashMap<>();
            datos.put("imagen", taskSnapshot.getDownloadUrl().toString());
            /// Corregir update
            FirebaseFirestore.getInstance().collection("eventos").document(evento).update(datos);
            new DownloadImageTask((ImageView) imgImagen).execute(taskSnapshot.getDownloadUrl().toString());
//                    progresoSubida.dismiss();
            hideProgress();
            subiendoDatos = false;
            hayImagen = true;
            mostrarDialogo(getApplicationContext(), "Imagen subida correctamente.");
        }
    };
    private OnProgressListener<UploadTask.TaskSnapshot> onProgressListenerUpload = new OnProgressListener<UploadTask.TaskSnapshot>() {
        @Override
        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
            //UploadTask progreso
            if (!subiendoDatos) {
//                        progresoSubida.show();
                showProgress();
                subiendoDatos = true;
            } else {
                if (taskSnapshot.getTotalByteCount() > 0 && progresoSubida != null) {
                    progresoSubida.setMessage("Espere... " + String.valueOf(100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount()) + "%");
                }
            }
        }
    };
    private OnFailureListener onFailureListener = new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception exception) {
            //UploadTask error
            subiendoDatos = false;
            mostrarDialogo(getApplicationContext(), "Ha ocurrido un error al subir la imagen o el usuario ha cancelado la subida.");
        }
    };
    private OnPausedListener<UploadTask.TaskSnapshot> onPausedListenerUpload = new OnPausedListener<UploadTask.TaskSnapshot>() {
        @Override
        public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
            //UploadTask pausa
            subiendoDatos = false;
            mostrarDialogo(getApplicationContext(), "La subida ha sido pausada.");
        }
    };


    private void showProgress() {
        if (progresoSubida != null)
            progresoSubida.show();

    }

    private void hideProgress() {
        if (progresoSubida != null) {
            progresoSubida.dismiss();
            progresoSubida = null;
        }

    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode,
                                    final Intent data) {
        Uri ficheroSeleccionado;
        Cursor cursor;
        String rutaImagen;
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case SOLICITUD_SELECCION_STREAM:
                    ficheroSeleccionado = data.getData();
                    String[] proyeccionStream = {MediaStore.Images.Media.DATA};
                    cursor = getContentResolver().query(ficheroSeleccionado, proyeccionStream, null, null, null);
                    cursor.moveToFirst();
                    rutaImagen = cursor.getString(cursor.getColumnIndex(proyeccionStream[0]));
                    cursor.close();
                    subirAFirebaseStorage(SOLICITUD_SUBIR_PUTSTREAM, rutaImagen);
                    break;
                case SOLICITUD_SELECCION_PUTFILE:
                    ficheroSeleccionado = data.getData();
                    String[] proyeccionFile = {MediaStore.Images.Media.DATA};
                    cursor = getContentResolver().query(ficheroSeleccionado, proyeccionFile, null, null, null);
                    cursor.moveToFirst();
                    rutaImagen = cursor.getString(cursor.getColumnIndex(proyeccionFile[0]));
                    cursor.close();
                    subirAFirebaseStorage(SOLICITUD_SUBIR_PUTFILE, rutaImagen);
                    break;
                case SOLICITUD_ELIMINAR_PORTADA_FIREBASE:
                    solicitudEliminarFirebase();
                    break;
            }
        }
    }

    static UploadTask uploadTask = null;
    StorageReference imagenRef;


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (imagenRef != null) {
            outState.putString("EXTRA_STORAGE_REFERENCE_KEY", imagenRef.toString());
        }
        Log.d("EventosDetalle", "onSaveInstaceState:");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        final String stringRef = savedInstanceState.getString("EXTRA_STORAGE_REFERENCE_KEY");
        if (stringRef == null) {
            return;
        }
        imagenRef = FirebaseStorage.getInstance().getReferenceFromUrl(stringRef);
        List<UploadTask> tasks = imagenRef.getActiveUploadTasks();
        for (UploadTask task : tasks) {
            task.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    upload_error(exception);
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    upload_exito(taskSnapshot);
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    upload_progreso(taskSnapshot);
                }

            }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                    upload_pausa(taskSnapshot);
                }
            });
        }
    }


    private void upload_error(Exception exception) {
        subiendoDatos = false;
        mostrarDialogo(getApplicationContext(), "Ha ocurrido un error al subir la imagen o el usuario ha  cancelado la subida.");
    }

    private void upload_exito(UploadTask.TaskSnapshot taskSnapshot) {
        Map<String, Object> datos = new HashMap<>();
        datos.put("imagen", taskSnapshot.getDownloadUrl().toString());
        FirebaseFirestore.getInstance().collection("eventos").document(evento).update(datos);
        new DownloadImageTask((ImageView) imgImagen).execute(taskSnapshot.getDownloadUrl().toString());
//        progresoSubida.dismiss();
        hideProgress();
        subiendoDatos = false;
        mostrarDialogo(getApplicationContext(), "Imagen subida correctamente.");
    }

    private void upload_progreso(UploadTask.TaskSnapshot taskSnapshot) {
        if (!subiendoDatos) {
            progresoSubida = new ProgressDialog(EventoDetalles.this);
            progresoSubida.setTitle("Subiendo...");
            progresoSubida.setMessage("Espere...");
            progresoSubida.setCancelable(true);
            progresoSubida.setCanceledOnTouchOutside(false);
            progresoSubida.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    uploadTask.cancel();
                }
            });
//            progresoSubida.show();
            showProgress();
            subiendoDatos = true;
        } else if (taskSnapshot.getTotalByteCount() > 0)
            progresoSubida.setMessage("Espere... " + String.valueOf(100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount()) + "%");
    }

    private void upload_pausa(UploadTask.TaskSnapshot taskSnapshot) {
        subiendoDatos = false;
        mostrarDialogo(getApplicationContext(), "La subida ha sido pausada.");
    }

//    final int SOLICITUD_FOTOGRAFIAS_DRIVE = 102;

    @Override
    protected void onDestroy() {
        hideProgress();
        if (uploadTask != null) {
            uploadTask.removeOnSuccessListener(onSuccessListenerUpload);
            uploadTask.removeOnFailureListener(onFailureListener);
            uploadTask.removeOnPausedListener(onPausedListenerUpload);
            uploadTask.removeOnProgressListener(onProgressListenerUpload);

        }
        super.onDestroy();
    }
}