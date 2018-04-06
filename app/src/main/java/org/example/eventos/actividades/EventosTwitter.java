package org.example.eventos.actividades;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.Media;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.MediaService;
import com.twitter.sdk.android.core.services.StatusesService;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;

import org.example.eventos.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import okhttp3.MediaType;
import retrofit.mime.TypedFile;
import retrofit2.Call;

/**
 * Created by jvg63 on 24/03/2018.
 */

public class EventosTwitter extends AppCompatActivity {

    private static final int SELECCIONAR_FOTO = 10000;
    private static final int SELECCIONAR_FOTO_TWITTER_API = 10001;
    private TextView textoConElMensaje;
    private TextView textoConElMensajeComposer;

    private TextView elTextoDeBienvenida;
    private final Activity THIS = this;
    private Button botonEnviarATwitter;
    private Button botonEnviarImagenATwitter;
    private com.twitter.sdk.android.core.identity.TwitterLoginButton botonLoginTwitter;
    private String elEvento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        elEvento = getIntent().getStringExtra("evento");
        if (elEvento == null) {
            elEvento = "";
        }
        Log.d("cuandrav.onCreate()", " .onCreate() llamado");
        // cosas de Facebook
        // inicializar FacebookSDK
        // 2018: no hace falta, se puede borrar:
        // http:stackoverflow.com/questions/41904350/facebooksdk-sdkinitializegetapplicationcontextdeprecated
        // FacebookSdk.sdkInitialize(this.getApplicationContext());
        // pongo el contenido visual de la actividad (hacer antes que findViewById()
        // y después de inicializar FacebookSDK)
        //
        this.setContentView(R.layout.evento_detalles_twitter);

        TwitterAuthConfig authConfig = new TwitterAuthConfig(getString(R.string.com_twitter_sdk_android_CONSUMER_KEY), getString(R.string.com_twitter_sdk_android_CONSUMER_SECRET));
        TwitterConfig config = new TwitterConfig.Builder(this)
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(authConfig)
                .debug(true)
                .build();
        Twitter.initialize(config);

        elTextoDeBienvenida = (TextView) findViewById(
                R.id.elTextoDeBienvenida);
        textoConElMensaje = (TextView) findViewById(R.id.txt_mensajeFB);
        botonEnviarATwitter = (Button) findViewById(R.id.boton_EnviarATwitter);
        botonEnviarImagenATwitter = (Button) findViewById(R.id.boton_EnviarATwitterImagen);
        textoConElMensajeComposer = (TextView) findViewById(R.id.txt_mensajeComposer);
        botonLoginTwitter = findViewById(R.id.twitter_login_button);
        botonLoginTwitter.setEnabled(true);
        botonLoginTwitter.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                Toast.makeText(THIS, "Autenticado en twitter: " + result.data.getUserName(),
                        Toast.LENGTH_LONG).show();


                elTextoDeBienvenida.setText("Bienvenido 2018: " + result.data.getUserName());
                habilitaBotones(true);


            }

            @Override
            public void failure(TwitterException e) {
                Toast.makeText(THIS, "Fallo en autentificación: " +
                        e.getMessage(), Toast.LENGTH_LONG).show();
                habilitaBotones(false);
            }

        });
        habilitaBotones(false);
    }

    private void habilitaBotones(boolean habilita) {
        if (habilita) {
            textoConElMensaje.setEnabled(true);
            botonEnviarATwitter.setEnabled(true);
            botonEnviarImagenATwitter.setEnabled(true);
        } else {
            elTextoDeBienvenida.setText("Haz login");
            textoConElMensaje.setText("");
            textoConElMensaje.setEnabled(false);
            botonEnviarATwitter.setEnabled(false);
            botonEnviarImagenATwitter.setEnabled(false);
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode,
                                    final Intent data) {
        // se llama cuando otra actividad que hemos arrancado termina y
        //nos devuelve el control
        // tal vez, devolviéndonos algun resultado (resultCode, data)
        Log.d("cuandrav.onActivityResu", "llamado");
        super.onActivityResult(requestCode, resultCode, data);


        botonLoginTwitter.onActivityResult(requestCode, resultCode, data);


        if (requestCode == SELECCIONAR_FOTO) {
            if (resultCode == RESULT_OK) {
                TweetComposer.Builder builder = new TweetComposer.Builder(this)
                        .text(textoConElMensajeComposer.getText().toString())
                        .image(data.getData()); // se puede añadir una imagen dando su
                // URI: content://lo/que/sea
                builder.show();
            }

        } else if (requestCode == SELECCIONAR_FOTO_TWITTER_API) {
            if (resultCode == RESULT_OK) {
                envia_imagen_Tweetapi(data.getData());
            }
        }
    }

    private boolean sePuedePublicar() {
        // compruebo la red
        if (!this.hayRed()) {
            Toast.makeText(this, "¿no hay red? No puedo publicar",
                    Toast.LENGTH_LONG).show();
            return false;
        }
        // compruebo permisos
        if (!this.tengoPermisoParaPublicar()) {
            Toast.makeText(this, "¿no tengo permisos para publicar? Los pido.", Toast.LENGTH_LONG).show();
            // pedirPermisoParaPublicar();
            LoginManager.getInstance().logInWithPublishPermissions(this,
                    Arrays.asList("publish_actions"));
            return false;
        }
        return true;
    }

    private AccessToken obtenerAccessToken() {
        return AccessToken.getCurrentAccessToken();
    }

    private boolean tengoPermisoParaPublicar() {
        AccessToken accessToken = this.obtenerAccessToken();
        return accessToken != null && accessToken.getPermissions().contains("publish_actions");
    }

    private boolean hayRed() {
// comprobar que estamos conetactos a internet,
// antes de hacer el login
// con facebook. Si no: da problemas.
        ConnectivityManager connectivityManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void enviarTextoAFacebook_async(final String textoQueEnviar) {
        // si no se puede publicar no hago nada
        if (!sePuedePublicar()) {
            return;
        }
        // hago la petición a través del API Graph

        Bundle params = new Bundle();
        params.putString("message", textoQueEnviar);
        GraphRequest request = new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/feed",
                params,
                HttpMethod.POST,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        Toast.makeText(THIS, "Publicación realizada: " +
                                textoQueEnviar, Toast.LENGTH_LONG).show();
                    }
                }
        );
        request.executeAsync();
    }

    public void enviarFotoAFacebook_async(Bitmap image, String comentario) {
        Log.d("cuandrav.envFotoFBasync", "llamado");
        if (image == null) {
            Toast.makeText(this, "Enviar foto: la imagen está vacía.",
                    Toast.LENGTH_LONG).show();
            Log.d("cuandrav.envFotoFBasync", "acabo porque la imagen es null ");
            return;
        }
        // si no se puede publicar no hago nada
        if (!sePuedePublicar()) {
            return;
        }
        // convierto el bitmap a array de bytes
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, stream);
        //image.recycle ();
        final byte[] byteArray = stream.toByteArray();
        try {
            stream.close();
        } catch (IOException e) {
        }
        // hago la petición a traves del Graph API
        Bundle params = new Bundle();
        params.putByteArray("source", byteArray); // bytes de la imagen
        params.putString("caption", comentario); // comentario
        // si se quisiera publicar una imagen de internet:
        // params.putString("url", "{image-url}");
        GraphRequest request = new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/photos",
                params,
                HttpMethod.POST,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        Toast.makeText(THIS, "" + byteArray.length + "Foto enviada:" + response.toString(), Toast.LENGTH_LONG).show();
                        //textoConElMensaje.setText(response.toString());
                    }
                }
        );
        request.executeAsync();
    }

    public void publicarTextoConTwitter(View view) {
        if (textoConElMensaje.getText().toString().isEmpty()) {
            textoConElMensaje.setError("Introduce texto");
            return;
        }

        String mensaje = "Evento:" + elEvento + ", " + this.textoConElMensaje.getText() + " :"
                + System.currentTimeMillis();

        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(this.textoConElMensaje
                .getWindowToken(), 0);

        StatusesService statusesService = TwitterCore.getInstance().getApiClient().getStatusesService();

        Call<Tweet> call = statusesService.update(mensaje, null, null,
                null, null, null, null, null, null);
        call.enqueue(new Callback<Tweet>() {
            @Override
            public void success(Result<Tweet> result) {
                Toast.makeText(THIS, "Tweet publicado: " +
                        result.response.message(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void failure(TwitterException e) {
                Toast.makeText(THIS, "No se pudo publicar el tweet: " +
                        e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void publicarFotoConTwitter(View view) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/jpg");
        startActivityForResult(photoPickerIntent, SELECCIONAR_FOTO_TWITTER_API);


    }

    public void envia_texto_TweetComposer(View view) {
        if (textoConElMensajeComposer.getText().toString().isEmpty()) {
            textoConElMensajeComposer.setError("Introduce un texto");
            return;
        }

        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(this.textoConElMensajeComposer
                .getWindowToken(), 0);

        TweetComposer.Builder builder = new TweetComposer.Builder(this)
                .text(textoConElMensajeComposer.getText().toString());
        //.image(myImageUri); // se puede añadir una imagen dando su
        // URI: content://lo/que/sea
        builder.show();
    }

    public void envia_imagen_TweetComposer(View view) {

        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/jpg");
        startActivityForResult(photoPickerIntent, SELECCIONAR_FOTO);


    }

    public void envia_imagen_Tweetapi(Uri ficheroSeleccionado) {

        File photo = null;

        String[] proyeccionStream = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(ficheroSeleccionado, proyeccionStream, null, null, null);
        cursor.moveToFirst();
        String rutaImagen = cursor.getString(cursor.getColumnIndex(proyeccionStream[0]));
        cursor.close();

        try {
            // 1. Abrimos el fichero con la imagen
            // imagen que queremos enviar. Como necesitamos un path (para
            // TypedFile) debe estar fuera de /res o /assets porque estos
            // estan dentro del .apk y NO tiene path
            photo = new File(rutaImagen);
        } catch (Exception e) {
            Log.d("miApp", "enviarImagen : excepcion: " + e.getMessage());
            return;
        }
        // 2. ponemos el fichero en un TypedFile
        TypedFile typedFile = new TypedFile("image/jpg", photo);
        // 3. obtenemos referencia al media service
        MediaService ms = TwitterCore.getInstance().getApiClient().getMediaService();
        // 3.1 ponemos la foto en el request body de la petición
        okhttp3.RequestBody requestBody = okhttp3.RequestBody.create(
                MediaType.parse("image/png"), photo);
        // 4. con el media service: enviamos la foto a Twitter
        Call<Media> call1 = ms.upload(
                requestBody, // foto que enviamos
                null, null);
        call1.enqueue(new Callback<Media>() {
            @Override
            public void success(Result<Media> mediaResult) {
                // he tenido éxito:
                Toast.makeText(THIS, "imagen publicada: " +
                        mediaResult.response.toString(), Toast.LENGTH_LONG);
                // 5. como he tenido éxito, la foto está en twitter, pero no en el
                // timeline (no se ve) he de escribir un tweet referenciando la foto
                // 6. obtengo referencia al status service
                StatusesService statusesService = TwitterCore.getInstance()
                        .getApiClient().getStatusesService();
                // 7. publico un tweet
                Call<Tweet> call2 = statusesService.update(
                        "Evento:" + elEvento + " " + textoConElMensaje.getText().toString() + " "
                                + System.currentTimeMillis(),
                        // mensaje del tweet
                        null, false, null, null, null, true, false,
                        "" + mediaResult.data.mediaId
                        // string con los identicadores (hasta 4, separado
                        //por coma) de las imágenes
                        // que quiero que aparezcan en este tweet. El mediaId
                        // referencia a la foto que acabo de subir previamente
                );
                call2.enqueue(new Callback<Tweet>() {
                    @Override
                    public void success(Result<Tweet> result) {
                        Toast.makeText(THIS, "Tweet publicado: " +
                                result.response.message().toString(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void failure(TwitterException e) {
                        Toast.makeText(THIS, "No se pudo publicar el tweet:"
                                + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            } // sucess ()

            @Override
            public void failure(TwitterException e) {
// failure de call1
                Toast.makeText(THIS, "No se pudo publicar el tweet: " +
                        e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }


}

