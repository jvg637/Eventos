package org.example.eventos.actividades.redessociales;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
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

import org.example.eventos.R;
import org.json.JSONObject;

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

public class FacebookTwitter extends AppCompatActivity {
    private TextView elTextoDeBienvenida;
    private Button botonHacerLogin;
    private Button botonLogOut;
    private Button botonEnviarFoto;
    private TextView textoConElMensaje;
    private Button botonCompartir;
    // boton oficial de Facebook para login/logout
    LoginButton loginButtonOficial;
    // gestiona los callbacks al FacebookSdk desde el método
    // onActivityResult() de una actividad
    private CallbackManager elCallbackManagerDeFacebook;
    // puntero a this para los callback
    private final Activity THIS = this;
    private Button botonEnviarATwitter;
    private com.twitter.sdk.android.core.identity.TwitterLoginButton botonLoginTwitter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        // botón oficial de "login en Facebook"
        // obtengo referencia
        loginButtonOficial = (LoginButton) findViewById(R.id.login_button);
        // declaro los permisos que debe pedir al ser pulsado
        // ver lista en: https://developers.facebook.com/docs/facebook-login/permissions
        loginButtonOficial.setPublishPermissions("publish_actions");
        //loginButtonOficial.setReadPermissions("public_profile");
        // si pones uno, no puedes poner el otro
        // crear callback manager de Facebook
        conseguirReferenciasAElementosGraficos();
        this.elCallbackManagerDeFacebook = CallbackManager.Factory.create();
        // registro un callback para saber cómo ha ido el login
        LoginManager.getInstance().registerCallback(this.elCallbackManagerDeFacebook,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // App code
                        Toast.makeText(THIS, "Login onSuccess()",
                                Toast.LENGTH_LONG).show();
                        actualizarVentanita();
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(THIS, "Login onCancel()",
                                Toast.LENGTH_LONG).show();
                        actualizarVentanita();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                        Toast.makeText(THIS, "Login onError(): " + exception.getMessage(),
                                Toast.LENGTH_LONG).show();
                        actualizarVentanita();
                    }
                });
        // otras cosas
        // obtengo referencias a mis otros widgets en el layout
        elTextoDeBienvenida = (TextView) findViewById(
                R.id.elTextoDeBienvenida);
        botonHacerLogin = (Button) findViewById(R.id.boton_hacerLogin);
        botonLogOut = (Button) findViewById(R.id.boton_LogOut);
        botonEnviarFoto = (Button) findViewById(R.id.boton_EnviarFoto);
        textoConElMensaje = (TextView) findViewById(R.id.txt_mensajeFB);
        botonCompartir = (Button) findViewById(R.id.boton_EnviarAFB);
        this.actualizarVentanita();
        Log.d("cuandrav.onCreate", "final .onCreate() ");

        // crear objeto share dialog
        this.elShareDialog = new ShareDialog(this);

        this.elShareDialog.registerCallback(this.elCallbackManagerDeFacebook, new
                FacebookCallback<Sharer.Result>() {
                    @Override
                    public void onSuccess(Sharer.Result result) {
                        Toast.makeText(THIS, "Sharer onSuccess()",
                                Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCancel() {
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Toast.makeText(THIS, "Sharer onError(): " +
                                error.toString(), Toast.LENGTH_LONG).show();
                    }
                });


        TwitterAuthConfig authConfig = new TwitterAuthConfig(getString(R.string.com_twitter_sdk_android_CONSUMER_KEY), getString(R.string.com_twitter_sdk_android_CONSUMER_SECRET));
        TwitterConfig config = new TwitterConfig.Builder(this)
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(authConfig)
                .debug(true)
                .build();
        Twitter.initialize(config);

        botonEnviarATwitter = (Button) findViewById(R.id.boton_EnviarATwitter);
        botonLoginTwitter = findViewById(R.id.twitter_login_button);
        botonLoginTwitter.setEnabled(true);
        botonLoginTwitter.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                Toast.makeText(THIS, "Autenticado en twitter: " + result.data.getUserName(),
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void failure(TwitterException e) {
                Toast.makeText(THIS, "Fallo en autentificación: " +
                        e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode,
                                    final Intent data) {
        // se llama cuando otra actividad que hemos arrancado termina y
        //nos devuelve el control
        // tal vez, devolviéndonos algun resultado (resultCode, data)
        Log.d("cuandrav.onActivityResu", "llamado");
        super.onActivityResult(requestCode, resultCode, data);
        // avisar a Facebook (a su callback manager) por si le afecta
        this.elCallbackManagerDeFacebook.onActivityResult(requestCode,
                resultCode, data);


        botonLoginTwitter.onActivityResult(requestCode, resultCode, data);
    }

    private void actualizarVentanita() {
        Log.d("cuandrav.actualizarVent", "empiezo");
        // obtengo el access token para ver si hay sesión
        AccessToken accessToken = this.obtenerAccessToken();

        if (accessToken == null) {
            Log.d("cuandrav.actualizarVent", "no hay sesion, deshabilito");
            // sesion con facebook cerrada
            this.botonHacerLogin.setEnabled(true);
            this.botonLogOut.setEnabled(false);
            this.textoConElMensaje.setEnabled(false);
            this.botonCompartir.setEnabled(false);
            this.botonEnviarFoto.setEnabled(false);
            this.elTextoDeBienvenida.setText("haz login");
            return;
        }
        // sí hay sesión
        Log.d("cuandrav.actualizarVent", "hay sesion habilito");
        this.botonHacerLogin.setEnabled(false);
        this.botonLogOut.setEnabled(true);
        this.textoConElMensaje.setEnabled(true);
        this.botonCompartir.setEnabled(true);
        this.botonEnviarFoto.setEnabled(true);
        // averiguo los datos básicos del usuario acreditado
        Profile profile = Profile.getCurrentProfile();
        if (profile != null) {
            this.textoConElMensaje.setText(profile.getName());
        }
        // otra forma de averiguar los datos básicos:
        // hago una petición con "graph api" para obtener datos del
        // usuario acreditado
        this.obtenerPublicProfileConRequest_async(
                // como es asíncrono he de dar un callback
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject datosJSON, GraphResponse
                            response) {
                        // muestro los datos
                        String nombre = "nombre desconocido";
                        try {
                            nombre = datosJSON.getString("name");
                        } catch (org.json.JSONException ex) {
                            Log.d("cuandrav.actualizarVent", "callback de obtenerPublicProfileConRequest_async:excepcion:"
                                    + ex.getMessage());
                        } catch (NullPointerException ex) {
                            Log.d("cuandrav.actualizarVent", "callback de obtenerPublicProfileConRequest_async:excepcion:"
                                    + ex.getMessage());
                        }
                        elTextoDeBienvenida.setText("bienvenido 2018: " + nombre);
                    }
                });
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

    public void boton_Login_pulsado(View quien) {
        // compruebo la red
        if (!this.hayRed()) {
            Toast.makeText(this, "¿No hay red? No puedo abrir sesión",
                    Toast.LENGTH_LONG).show();
        }
        // login
        LoginManager.getInstance().logInWithPublishPermissions(this,
                Arrays.asList("publish_actions"));
        // actualizar
        this.actualizarVentanita();
    }

    public void boton_Logout_pulsado(View quien) {
        // compruebo la red
        if (!this.hayRed()) {
            Toast.makeText(this, "¿No hay red? No puedo cerrar sesión",
                    Toast.LENGTH_LONG).show();
        }
        // logout
        LoginManager.getInstance().logOut();
        // actualizar
        this.actualizarVentanita();
    }

    private void obtenerPublicProfileConRequest_async(
            GraphRequest.GraphJSONObjectCallback callback) {
        if (!this.hayRed()) {
            Toast.makeText(this, "¿No hay red ? ",
                    Toast.LENGTH_LONG).show();
        }
        // obtengo access token y compruebo que hay sesión
        AccessToken accessToken = obtenerAccessToken();
        if (accessToken == null) {
            Toast.makeText(THIS, "no hay sesión con Facebook",
                    Toast.LENGTH_LONG).show();
            return;
        }
        // monto la petición: /me
        GraphRequest request = GraphRequest.newMeRequest(accessToken,
                callback);
        Bundle params = new Bundle();
        params.putString("fields", "id, name");
        request.setParameters(params);
        // la ejecuto (asíncronamente)
        request.executeAsync();
    }

    public void boton_enviarTextoAFB_pulsado(View quien) {
        // cojo el mensaje que ha escrito el usuario
        String mensaje = "msg:" + this.textoConElMensaje.getText() + " :"
                + System.currentTimeMillis();
        // borro lo escrito
        this.textoConElMensaje.setText("");
        // cierro el soft-teclado
        InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(this.textoConElMensaje
                .getWindowToken(), 0);
        // llamo al método que publica
        enviarTextoAFacebook_async(mensaje);
    }

    // ------------------------------------------------------------
    // ------------------------------------------------------------
    public void boton_enviarImagen_pulsado(View quien) {
        //
        // cojo el mensaje que ha escrito el usuario
        //

        String mensaje = "img:" + this.textoConElMensaje.getText() + " :"
                + System.currentTimeMillis();

        //
        // llamo al método que publica
        //
        Drawable imagen = ContextCompat.getDrawable(this, R.drawable.sampleimage);
        Bitmap bitmap;
        bitmap = ((BitmapDrawable) imagen).getBitmap();
        enviarFotoAFacebook_async(bitmap, this.textoConElMensaje.getText().toString());

    }


    private Button boton2;
    private Button boton3;
    private Button boton1;
    private TextView textoEntrada1;
    private TextView textoSalida1;
    // gestiona los callbacks al FacebookSdk desde el método
    // onActivityResult() de una actividad
    private ShareDialog elShareDialog;

    private void conseguirReferenciasAElementosGraficos() {
//        boton1 = (Button) findViewById(R.id.button1);
        boton2 = (Button) findViewById(R.id.button2);
        boton3 = (Button) findViewById(R.id.button3);
//        textoEntrada1 = (TextView) findViewById(R.id.textoEntrada1);
        textoSalida1 = (TextView) findViewById(R.id.textoSalida1);
    }


    private void publicarMensajeConIntent() {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        startActivityForResult(Intent.createChooser(shareIntent, "Share"),
                1234); //requestId);
    }

    public void boton1_pulsado(View quien) {
        Log.d("cuandrav.boton1_pulsado", " llamado ");
        textoSalida1.setText("boton1_pulsado");
        this.publicarMensajeConIntent();
    }

    private boolean puedoUtilizarShareDialogParaPublicarMensaje() {
        return puedoUtilizarShareDialogParaPublicarLink();
    }

    private boolean puedoUtilizarShareDialogParaPublicarLink() {
        return ShareDialog.canShow(ShareLinkContent.class);
    }

    private boolean puedoUtilizarShareDialogParaPublicarFoto() {
        return ShareDialog.canShow(SharePhotoContent.class);
    }

    public void boton2_pulsado(View quien) {
        Log.d("cuandrav.boton2_pulsado", " llamado ");
        textoSalida1.setText("boton2_pulsado");
        // llamar al metodo para publicar
        this.publicarMensajeConShareDialog();
    }

    private void publicarMensajeConShareDialog() {
        // https://developers.facebook.com/docs/android/share -> Using the Share Dialog
        if (!puedoUtilizarShareDialogParaPublicarMensaje()) {
            Log.d("cuandrav.boton2_pul()",
                    " ¡¡¡ No puedo utilizar share dialog !!!");
            return;
        }
        // llamar a share dialog aunque utilizamos ShareLinkContent,
        // al no poner link publica un mensaje
        ShareLinkContent content = new ShareLinkContent.Builder().build();
        this.elShareDialog.show(content);
    }

    public void boton3_pulsado(View quien) {
        Log.d("cuandrav.boton3_pulsado", " llamado ");
        textoSalida1.setText("boton3_pulsado");
        // llamar al metodo para publicar foto
        this.publicarFotoConShareDialog();
    }

    private void publicarFotoConShareDialog() {
        // https://developers.facebook.com/docs/android/share -> Using the Share Dialog
        if (!puedoUtilizarShareDialogParaPublicarFoto()) {
            return;
        }
        // cojo una imagen directamente de los recursos para publicarla
        Bitmap image = BitmapFactory.decodeResource(
                getResources(), R.drawable.sampleimage);
        // monto la petición
        SharePhoto photo = new SharePhoto.Builder()
                .setBitmap(image).build();
        SharePhotoContent content = new SharePhotoContent.Builder()
                .addPhoto(photo).build();
        this.elShareDialog.show(content);
    }

    public void publicarTextoConTwitter(View view) {
        StatusesService statusesService = TwitterCore.getInstance().getApiClient().getStatusesService();

        Call<Tweet> call = statusesService.update(textoConElMensaje.getText().toString(), null, null,
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
        File photo = null;
        try {
            // 1. Abrimos el fichero con la imagen
            // imagen que queremos enviar. Como necesitamos un path (para
            // TypedFile) debe estar fuera de /res o /assets porque estos
            // estan dentro del .apk y NO tiene path
            photo = new File("/sdcard/DCIM/Camera/DSC_0001.jpg");
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
                            "prueba de enviar imagen"
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

