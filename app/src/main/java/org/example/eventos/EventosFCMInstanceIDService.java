package org.example.eventos;

import android.app.Service;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import static org.example.eventos.Comun.guardarIdRegistro;

/**
 * Created by jvg63 on 17/02/2018.
 */

public class EventosFCMInstanceIDService
        extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        String idPush;
        idPush = FirebaseInstanceId.getInstance().getToken();
        guardarIdRegistro(getApplicationContext(), idPush);
    }
}