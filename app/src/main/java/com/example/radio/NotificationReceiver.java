package com.example.radio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

// NotificationReceiver es un receptor de difusión que maneja las acciones de la notificación
public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Comprueba la acción de la intención recibida
        switch (intent.getAction()) {
            case "ACTION_TOGGLE_PLAY_PAUSE":
                MainActivity.togglePlayPauseStatic(); // Alterna entre Play y Pause
                break;

            case "ACTION_CLOSE":
                // Detiene la reproducción y cierra la aplicación
                if (MainActivity.instance != null) {
                    MainActivity.instance.stopRadio(); // Detener la reproducción
                    MainActivity.instance.cancelNotification(); // Cancela la notificación
                    MainActivity.instance.finish(); // Cierra la actividad
                }
                // Salir completamente de la aplicación
                System.exit(0);
                break;

            default:
                // Acciones no reconocidas
                break;
        }
    }
}
