package com.example.radio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("ACTION_TOGGLE_PLAY_PAUSE".equals(intent.getAction())) {
            MainActivity.togglePlayPauseStatic(); // Alterna entre Play y Pause
        } else if ("ACTION_CLOSE".equals(intent.getAction())) {
            // Detiene la reproducción y cierra la aplicación
            if (MainActivity.instance != null) {
                MainActivity.instance.stopRadio(); // Detener la reproducción
                MainActivity.instance.cancelNotification(); // Cancela la notificación
                MainActivity.instance.finish(); // Cierra la actividad
            }
            // Salir completamente de la aplicación
            System.exit(0);
        }
    }
}




