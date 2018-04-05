package org.example.eventos.actividades;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.example.eventos.R;

/**
 * Created by jordi 
 */

public class DialogoAutenticacionInstagram extends Dialog {

    public interface EscuchadorAutenticacion {
        void alRecibidrElToken(String auth_token);
    }

    private final EscuchadorAutenticacion elEscuchador;
    private Context elContexto;

    private WebView elWebView;

    private String url;

    public DialogoAutenticacionInstagram(@NonNull Context contexto, EscuchadorAutenticacion escuchador) {
        super(contexto);
        this.elContexto = contexto;
        this.elEscuchador = escuchador;

        this.url = elContexto.getString(R.string.INSTAGRAM_API_URL)
                + "oauth/authorize/?client_id="
                + elContexto.getString(R.string.CLIENT_ID)
                + "&redirect_uri="
                +  elContexto.getString(R.string.REDIRECT_URI)
                + "&response_type=token"
                + "&display=touch&scope=public_content";

        Log.d( "cuandrav", "URL para autenticar = " + this.url);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        this.setContentView(R.layout.dialogo_autenticacion_instagram);

        inicializarWebView();
    }

    ProgressDialog dialogo;

    private void inicializarWebView() {
        elWebView = (WebView) findViewById(R.id.web_view);

        WebSettings settings = elWebView.getSettings();
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        elWebView.loadUrl(url);

//        elWebView.setWebChromeClient(new WebChromeClient() {
//
//            @Override
//            public boolean onJsAlert(WebView view, String url, String message,
//                                     final JsResult result) {
//                new AlertDialog.Builder(elContexto).setTitle("Mensaje")
//                        .setMessage(message).setPositiveButton
//                        (android.R.string.ok, new AlertDialog.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int which) {
//                                result.confirm();
//                            }
//                        }).setCancelable(false).create().show();
//                return true;
//            }
//
//        });

        elWebView.setWebViewClient(new WebViewClient() {
            boolean authComplete = false;

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (dialogo != null)
                    dialogo.dismiss();
                dialogo = new ProgressDialog(elContexto);
                dialogo.setMessage("Login Instagram...");
                dialogo.setCancelable(true);
                dialogo.show();
                super.onPageStarted(view, url, favicon);
            }

            String access_token;

            @Override
            public void onPageFinished(WebView view, String url) {

                dialogo.dismiss();
                if (url.contains("#access_token=") && !authComplete) {
                    Uri uri = Uri.parse(url);
                    access_token = uri.getEncodedFragment();
                    // get the whole token after the '=' sign
                    access_token = access_token.substring(access_token.lastIndexOf("=")+1);
                    Log.i("", "CODE : " + access_token);
                    authComplete = true;
                    elEscuchador.alRecibidrElToken(access_token);
                    dismiss();

                } else if (url.contains("?error")) {
                    Toast.makeText(elContexto, "Hubo error:", Toast.LENGTH_SHORT).show();
                    dismiss();
                }

                super.onPageFinished(view, url);
            }
        });
    }
}
