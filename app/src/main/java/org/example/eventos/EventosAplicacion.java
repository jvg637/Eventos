package org.example.eventos;

import android.app.Application;

import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Created by jvg63 on 25/02/2018.
 */

public class EventosAplicacion extends Application {

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    public void onCreate() {
        super.onCreate();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    public FirebaseAnalytics getFirebaseAnalytics() {
        return mFirebaseAnalytics;
    }
}
