package org.example.eventos;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import static org.example.eventos.Comun.mostrarDialogo;
import static org.example.eventos.Comun.mostrarDialogo2;

/**
 * Created by jvg63 on 17/02/2018.
 */

public class EventosFCMService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getNotification().getClickAction() != null && remoteMessage.getNotification().getClickAction().equals("OPEN_ACTIVITY_1")) {
            mostrarDialogo2(getApplicationContext(),remoteMessage.getNotification().getBody(),  remoteMessage.getData().get("evento"));
        } else {
            if (remoteMessage.getData().size() > 0) {
                String evento = "";
                evento = "Evento: " + remoteMessage.getData().get("evento") + "\n";
                evento = evento + "Día: " + remoteMessage.getData().get("dia") + "\n";
                evento = evento + "Ciudad: " + remoteMessage.getData().get("ciudad") + "\n";
                evento = evento + "Comentario: "
                        + remoteMessage.getData().get("comentario");
                mostrarDialogo(getApplicationContext(), evento);
            } else {
                if (remoteMessage.getNotification() != null) {
                    mostrarDialogo(getApplicationContext(),
                            remoteMessage.getNotification().getBody());
                }
            }
        }
    }


}