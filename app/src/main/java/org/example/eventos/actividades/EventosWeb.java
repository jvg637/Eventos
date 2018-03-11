package org.example.eventos.actividades;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.example.eventos.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class EventosWeb extends AppCompatActivity {

    WebView navegador;
    //    private ProgressBar barraProgreso;
    ProgressDialog dialogo;
//    Button btnDetener, btnAnterior, btnSiguiente;
//    EditText txtDireccion;


    final InterfazComunicacion miInterfazJava = new InterfazComunicacion(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.eventos_web);


//        btnDetener = (Button) findViewById(R.id.btnDetener);
//        btnAnterior = (Button) findViewById(R.id.btnAnterior);
//        btnSiguiente = (Button) findViewById(R.id.btnSiguiente);
//        txtDireccion = (EditText) findViewById(R.id.txtDireccion);

        final String evento = getIntent().getStringExtra("evento");

        navegador = (WebView) findViewById(R.id.webkit);
        navegador.getSettings().setJavaScriptEnabled(true);
        navegador.getSettings().setBuiltInZoomControls(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
//        navegador.loadUrl("file:///android_asset/index.html");
        navegador.loadUrl("https://eventos-eae83.firebaseapp.com/index.html");
        navegador.addJavascriptInterface(miInterfazJava, "jsInterfazNativa");


//
//        barraProgreso = (ProgressBar) findViewById(R.id.barraProgreso);
        navegador.setWebChromeClient(new WebChromeClient() {
//            @Override
//            public void onProgressChanged(WebView view, int progreso) {
//                barraProgreso.setProgress(0);
//                barraProgreso.setVisibility(View.VISIBLE);
//                ActividadPrincipal.this.setProgress(progreso * 1000);
//                Log.d("LOG", "" + progreso);
//                barraProgreso.incrementProgressBy(progreso);
//                if (progreso == 100) {
//                    barraProgreso.setVisibility(View.GONE);
//                }
//            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message,
                                     final JsResult result) {
                new AlertDialog.Builder(EventosWeb.this).setTitle("Mensaje")
                        .setMessage(message).setPositiveButton
                        (android.R.string.ok, new AlertDialog.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                result.confirm();
                            }
                        }).setCancelable(false).create().show();
                return true;
            }

        });
        navegador.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (dialogo!=null)
                    dialogo.dismiss();
                dialogo = new ProgressDialog(EventosWeb.this);
                dialogo.setMessage("Cargando...");
                dialogo.setCancelable(true);
                dialogo.show();
//                btnDetener.setEnabled(true);

                comprobarConectividad();
//                if (comprobarConectividad()) {
//                    btnDetener.setEnabled(true);
//                } else {
//                    btnDetener.setEnabled(false);
//                }

            }

            @Override
            public void onPageFinished(WebView view, String url) {
                dialogo.dismiss();
//                btnDetener.setEnabled(false);
//                if (view.canGoBack()) {
//                    btnAnterior.setEnabled(true);
//                } else {
//                    btnAnterior.setEnabled(false);
//                }
//                if (view.canGoForward()) {
//                    btnSiguiente.setEnabled(true);
//                } else {
//                    btnSiguiente.setEnabled(false);
//                }
                navegador.loadUrl("javascript:muestraEvento(\"" + evento + "\");");
            }

            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(EventosWeb.this);
                builder.setMessage(description).setPositiveButton("Aceptar",
                        null).setTitle("onReceivedError");
                builder.show();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                String url_filtro = "http://www.androidcurso.com/";
//                if (!url.toString().equals(url_filtro)) {
//                    view.loadUrl(url_filtro);
//                }
                return false;
            }

        });
//
//        ActivityCompat.requestPermissions(EventosWeb.this,
//                new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE,},
//                1);
//
//
//        ActivityCompat.requestPermissions(EventosWeb.this,
//                new String[]{android.Manifest.permission.ACCESS_NETWORK_STATE},
//                4);
        navegador.setDownloadListener(new DownloadListener() {
            public void onDownloadStart(final String url, String userAgent, String
                    contentDisposition, String mimetype, long contentLength) {
                AlertDialog.Builder builder =
                        new AlertDialog.Builder(EventosWeb.this);
                builder.setTitle("Descarga");
                builder.setMessage("¿Deseas guardar el archivo?");
                builder.setCancelable(false).setPositiveButton("Aceptar",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                URL urlDescarga;
                                try {
                                    urlDescarga = new URL(url);
                                    new DescargarFichero().execute(urlDescarga);
                                } catch (MalformedURLException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).setNegativeButton("Cancelar",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                builder.create().show();
            }
        });
    }

    private boolean comprobarConectividad() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) this.getSystemService(
                        Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if ((info == null || !info.isConnected() || !info.isAvailable())) {
            Toast.makeText(EventosWeb.this,
                    "Oops! No tienes conexión a internet",
                    Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    public void detenerCarga(View v) {
        navegador.stopLoading();
    }

    public void irPaginaAnterior(View v) {
//        navegador.goBack();
        if (comprobarConectividad()) {
            navegador.goBack();
        }
    }

    public void irPaginaSiguiente(View v) {
//        navegador.goForward();
        if (comprobarConectividad()) {
            navegador.goForward();
        }
    }

//    public void ir(View view) {
//        navegador.loadUrl(txtDireccion.getText().toString());
//    }

    @Override
    public void onBackPressed() {
        if (navegador.canGoBack()) {
            navegador.goBack();

        } else {
            super.onBackPressed();
        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           String permissions[], int[]
//                                                   grantResults) {
//        switch (requestCode) {
//            case 1: {
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                } else {
//                    Toast.makeText(EventosWeb.this,
//                            "Permiso denegado para escribir en el almacenamiento.",
//                            Toast.LENGTH_SHORT).show();
//                }
//                return;
//            }
//            case 4: {
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                } else {
//                    Toast.makeText(EventosWeb.this,
//                            "Permiso denegado para conocer el estado de la red.",
//                            Toast.LENGTH_SHORT).show();
//                }
//                return;
//            }
//        }
//    }

    private class DescargarFichero extends AsyncTask<URL, Integer, Long> {
        private String mensaje;

        @Override
        protected Long doInBackground(URL... url) {
            String urlDescarga = url[0].toString();
            mensaje = "";
            InputStream inputStream = null;
            try {
                URL direccion = new URL(urlDescarga);
                HttpURLConnection conexion =
                        (HttpURLConnection) direccion.openConnection();
                if (conexion.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    inputStream = conexion.getInputStream();
                    String fileName = android.os.Environment
                            .getExternalStorageDirectory().getAbsolutePath() +
                            "/descargas";
                    File directorio = new File(fileName);
                    directorio.mkdirs();
                    File file = new File(directorio, urlDescarga.substring(
                            urlDescarga.lastIndexOf("/"),
                            (urlDescarga.indexOf("?") == -1 ?
                                    urlDescarga.length() : urlDescarga.indexOf("?"))));
                    FileOutputStream fileOutputStream = new
                            FileOutputStream(file);
                    byte[] buffer = new byte[1024];
                    int len = 0;
                    int bytesRead = -1;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, bytesRead);
                    }
                    fileOutputStream.close();
                    inputStream.close();
                    mensaje = "Guardado en: " + file.getAbsolutePath();
                } else {
                    throw new Exception(conexion.getResponseMessage());
                }
            } catch (Exception ex) {
                mensaje = ex.getClass().getSimpleName() + " " + ex.getMessage();
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                    }
                }
            }
            return (long) 0;
        }

        protected void onPostExecute(Long result) {
            AlertDialog.Builder builder = new
                    AlertDialog.Builder(EventosWeb.this);
            builder.setTitle("Descarga");
            builder.setMessage(mensaje);
            builder.setCancelable(true);
            builder.create().show();
        }
    }

    public class InterfazComunicacion {
        Context mContext;

        InterfazComunicacion(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void volver() {
            finish();
        }
    }
}
