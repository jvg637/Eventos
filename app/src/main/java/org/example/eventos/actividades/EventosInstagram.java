package org.example.eventos.actividades;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import org.example.eventos.R;
import org.example.eventos.actividades.redessociales.Instagram;
import org.example.eventos.modelo.instagram.RespuestaGetTags;
import org.example.eventos.modelo.instagram.RespuestaGetUsuarioInstagram;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

import static org.example.eventos.util.Comun.mostrarDialogo;

/**
 * Created by jvg63 on 30/03/2018.
 */

public class EventosInstagram extends AppCompatActivity {

    private static final int SELECCIONAR_FOTO = 10000;
    private String elTokenDeInstagram = null;
    private String elEvento = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.evento_detalles_instagram);
        elEvento = getIntent().getStringExtra("evento");
        if (elEvento == null) {
            elEvento = "";
        }
        abrirDialogoAutenticacion();
    }

    private void abrirDialogoAutenticacion() {
        DialogoAutenticacionInstagram di = new DialogoAutenticacionInstagram(this,
                new DialogoAutenticacionInstagram.EscuchadorAutenticacion() {
                    @Override
                    public void alRecibidrElToken(String auth_token) {
                        elTokenDeInstagram = auth_token;
                        Log.d("cuandrav", " TOKEN RECIBIDO =  " + elTokenDeInstagram);
                        mostrarDialogo(EventosInstagram.this, " TOKEN RECIBIDO =  " + elTokenDeInstagram);

                    }
                }

        );
        di.setContentView(R.layout.dialogo_autenticacion_instagram);
        di.setCancelable(true);
        di.show();

    } // ()

    public void boton_prueba1Pulsado(View quien) {
        Log.d("cuandrav", "boton_prueba1Pulsado() ");
        if (elTokenDeInstagram != null)
            this.obtenerIdentidadUsuarioInstagram();
        else {
            mostrarDialogo(EventosInstagram.this, " Debe realizar login Primero");

        }

    } // ()


    public void boton_prueba2Pulsado(View quien) {
        Log.d("cuandrav", "boton_prueba2Pulsado() ");

        this.buscarEtiqueta("cangur");

        //this.buscarEtiqueta("felizviernes");
    }

    public void logoff(View view) {
        WebView webView = new WebView(this);
        webView.loadUrl("https://instagram.com/accounts/logout/");
    }


    public interface InstagramRESTInterfaz {

        @GET("v1/users/self")
        Call<RespuestaGetUsuarioInstagram> getUsuario(@Query("access_token") String access_token);

        @GET("v1/tags/{tag_name}")
        Call<RespuestaGetTags> getTagFotos(@Path("tag_name") String tag_name,
                                           @Query("access_token") String access_token);

    }

    private void obtenerIdentidadUsuarioInstagram() {

        Call<RespuestaGetUsuarioInstagram> llamada = new Retrofit.Builder()
                .baseUrl(getString(R.string.INSTAGRAM_API_URL))
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(Instagram.InstagramRESTInterfaz.class).getUsuario(elTokenDeInstagram);

        llamada.enqueue(new Callback<RespuestaGetUsuarioInstagram>() {
                            @Override
                            public void onResponse(Call<RespuestaGetUsuarioInstagram> call, Response<RespuestaGetUsuarioInstagram> response) {

                                Log.d("cuandrav", "respuesta a la llamada ! ");
                                Log.d("cuandrav", "Respuesta. nombre completo = " + response.body().getData().getFullName());

                                mostrarDialogo(EventosInstagram.this, " Nombre completo =  " + response.body().getData().getFullName());

                            } // onResponse

                            @Override
                            public void onFailure(Call<RespuestaGetUsuarioInstagram> call, Throwable t) {
                                Log.d("cuandrav", "fallo en respuesta a la llamada ! :" + t.getMessage());


                            } // onFailure
                        } // new Callback
        ); // enqueue


    }

    private void buscarEtiqueta(String etiqueta) {

        Call<RespuestaGetTags> llamada = new Retrofit.Builder()
                .baseUrl(getString(R.string.INSTAGRAM_API_URL))
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(Instagram.InstagramRESTInterfaz.class).getTagFotos(etiqueta, elTokenDeInstagram);


        llamada.enqueue(new Callback<RespuestaGetTags>() {
                            @Override
                            public void onResponse(Call<RespuestaGetTags> call, Response<RespuestaGetTags> response) {

                                Log.d("cuandrav", "respuesta a la llamada ! ");
                                Log.d("cuandrav", "Respuesta. Body a string = " + response.body().getData().toString());


                                Log.d("cuandrav", "Respuesta. nombre = " + response.body().getData().getName());
                                Log.d("cuandrav", "Respuesta. cuantos media = " + response.body().getData().getMediaCount());


                            } // onResponse

                            @Override
                            public void onFailure(Call<RespuestaGetTags> call, Throwable t) {
                                Log.d("cuandrav", "fallo en respuesta a la llamada ! :" + t.getMessage());


                            } // onFailure
                        } // new Callback
        ); // enqueue

    }


    public void click_publicar_imagen_instagram(View view) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/jpg");
        startActivityForResult(photoPickerIntent, SELECCIONAR_FOTO);
    }

    private void enviarImagen_async(Uri uriFoto) {
        PackageManager pm = getPackageManager();

        try {
            PackageInfo info = pm.getPackageInfo("com.instagram.android", PackageManager.GET_META_DATA);
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setPackage("com.instagram.android");
            shareIntent.setType("image/jpeg");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uriFoto);
            shareIntent.putExtra(Intent.EXTRA_TEXT, "texto extra");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "tema extra");
            startActivity(shareIntent);
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(this, "Instagram no esta instalado!", Toast.LENGTH_SHORT)
                    .show();
        }

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECCIONAR_FOTO) {
            if (resultCode == RESULT_OK) {
                this.enviarImagen_async(data.getData()); // le doy un URI

            }
        }
    }


} // class
