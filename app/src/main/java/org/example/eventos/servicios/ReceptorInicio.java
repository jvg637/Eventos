package org.example.eventos.servicios;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.example.eventos.servicios.EventosFCMService;

/**
 * Created by jvg63 on 17/02/2018.
 */

public class ReceptorInicio extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("ReceptorInicio", "Iniciando servicio");
        context.startService(new Intent(context,
                EventosFCMService.class));
//        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
//            context.startForegroundService(new Intent(context, EventosFCMService.class));
//        } else {
//            context.startService(new Intent(context, EventosFCMService.class));
//        }
    }
}
