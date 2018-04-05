package org.example.eventos.actividades;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.facebook.AccessTokenTracker;
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

import org.example.eventos.R;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Created by jvg63 on 24/03/2018.
 */

public class EventosFacebook extends AppCompatActivity {
    private static final int SELECCIONAR_FOTO = 10000;
    private static final int SELECCIONAR_FOTO_SHARED_DIALOG = 10001;
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

    private String elEvento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("cuandrav.onCreate()", " .onCreate() llamado");

        elEvento = getIntent().getStringExtra("evento");
        if (elEvento == null) {
            elEvento = "";
        }
        // cosas de Facebook
        // inicializar FacebookSDK
        // 2018: no hace falta, se puede borrar:
        // http:stackoverflow.com/questions/41904350/facebooksdk-sdkinitializegetapplicationcontextdeprecated
        // FacebookSdk.sdkInitialize(this.getApplicationContext());
        // pongo el contenido visual de la actividad (hacer antes que findViewById()
        // y después de inicializar FacebookSDK)
        //
        this.setContentView(R.layout.evento_detalles_facebook);
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

        Profile fbProfile = Profile.getCurrentProfile();
        AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken,
                                                       AccessToken currentAccessToken) {
                if (currentAccessToken == null) {
                    Toast.makeText(THIS, "onLogout catched",
                            Toast.LENGTH_LONG).show();
                    actualizarVentanita();
                }
            }
        };
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
        accessTokenTracker.startTracking();
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

        if (requestCode == SELECCIONAR_FOTO) {
            if (resultCode == RESULT_OK) {
                send_text_and_image(data.getData());
            }
        } else if (requestCode == SELECCIONAR_FOTO_SHARED_DIALOG) {
            {
                this.publicarFotoConShareDialog(data.getData());
            }

        }
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
            this.textoConElMensaje.setText("");
            this.botonCompartir.setEnabled(false);
            this.botonEnviarFoto.setEnabled(false);
            this.elTextoDeBienvenida.setText("Haz login");
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
//        Profile profile = Profile.getCurrentProfile();
//        if (profile != null) {
//            this.textoConElMensaje.setText(profile.getName());
//        }
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
                        elTextoDeBienvenida.setText("Bienvenido 2018: " + nombre);
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
                        Toast.makeText(THIS, "Tamaño: " + byteArray.length + " Foto enviada:" + response.toString(), Toast.LENGTH_LONG).show();
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
        if (this.textoConElMensaje.getText().toString().isEmpty()) {
            this.textoConElMensaje.setError("Introduzca el texto del mensaje");
            return;
        }

        // cojo el mensaje que ha escrito el usuario
        String mensaje = "Evento:" + elEvento + ", " + this.textoConElMensaje.getText() + " :"
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
//        send_text_and_image();
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/jpg");
        startActivityForResult(photoPickerIntent, SELECCIONAR_FOTO);
    }

    private void send_text_and_image(Uri ficheroSeleccionado) {
        //
        // cojo el mensaje que ha escrito el usuario
        //

        String mensaje = "img:" + this.textoConElMensaje.getText() + " :"
                + System.currentTimeMillis();
        //
        // llamo al método que publica
        //
        String[] proyeccionStream = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(ficheroSeleccionado, proyeccionStream, null, null, null);
        cursor.moveToFirst();
        String rutaImagen = cursor.getString(cursor.getColumnIndex(proyeccionStream[0]));
        cursor.close();

        try {
            InputStream stream = new FileInputStream(new File(rutaImagen));

            Bitmap bitmap = BitmapFactory.decodeStream(stream);

            enviarFotoAFacebook_async(bitmap, this.textoConElMensaje.getText().toString());
        } catch (FileNotFoundException e) {
            Toast.makeText(getApplicationContext(), "Error enviando imagen por Facebook", Toast.LENGTH_SHORT).show();
        }
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
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/jpg");
        startActivityForResult(photoPickerIntent, SELECCIONAR_FOTO_SHARED_DIALOG);

    }

    private void publicarFotoConShareDialog(Uri ficheroSeleccionado) {
        // https://developers.facebook.com/docs/android/share -> Using the Share Dialog
        if (!puedoUtilizarShareDialogParaPublicarFoto()) {
            return;
        }
        // cojo una imagen directamente de los recursos para publicarla
        String[] proyeccionStream = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(ficheroSeleccionado, proyeccionStream, null, null, null);
        cursor.moveToFirst();
        String rutaImagen = cursor.getString(cursor.getColumnIndex(proyeccionStream[0]));
        cursor.close();

        try {
            InputStream stream = new FileInputStream(new File(rutaImagen));

            Bitmap image = BitmapFactory.decodeStream(stream);

            // monto la petición
            SharePhoto photo = new SharePhoto.Builder()
                    .setBitmap(image).build();
            SharePhotoContent content = new SharePhotoContent.Builder()
                    .addPhoto(photo).build();
            this.elShareDialog.show(content);
        } catch (FileNotFoundException e) {
            Toast.makeText(getApplicationContext(), "Error enviando imagen por Facebook Share Dialog", Toast.LENGTH_SHORT).show();
        }
    }
}

