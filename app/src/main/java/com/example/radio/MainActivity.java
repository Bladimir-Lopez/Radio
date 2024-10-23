package com.example.radio;

// Importaciones necesarias para el funcionamiento de la aplicación
import android.media.AudioManager; // Control del audio del dispositivo
import android.net.Uri; // Manejo de URIs para abrir enlaces
import android.os.Bundle; // Permite transferir datos entre actividades
import android.view.View; // Control de la interfaz de usuario
import android.content.Intent; // Para realizar acciones como abrir enlaces externos
import android.widget.ImageButton; // Botón que muestra una imagen
import android.media.MediaPlayer; // Control de la reproducción de audio
import android.widget.SeekBar; // Barra deslizante para controlar el volumen
import android.widget.Toast; // Mostrar mensajes breves en pantalla
import android.telephony.PhoneStateListener; // Para escuchar cambios en el estado del teléfono
import android.telephony.TelephonyManager; // Acceso al servicio de telefonía
import androidx.appcompat.app.AppCompatActivity; // Clase base para actividades
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import java.io.IOException;


// MainActivity es la actividad principal de la aplicación
public class MainActivity extends AppCompatActivity {
    private static final String CHANNEL_ID = "radio_channel"; // ID del canal de notificación
    private static final int NOTIFICATION_ID = 1; // ID de la notificación

    private MediaPlayer mediaPlayer; // Objeto para manejar la reproducción de audio
    private ImageButton playPauseButton; // Botón para reproducir o pausar la música
    private boolean isPlaying = false; // Indica si la música está en reproducción
    private AudioManager audioManager; // Administrador de audio del dispositivo
    private SeekBar seekBarVolume; // Barra deslizante para controlar el volumen

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Establece el diseño de la actividad principal

        initializeUI();
        initializeMediaPlayer();
        initializeVolumeControl();
        initializePhoneStateListener();
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Radio Channel"; // Nombre del canal
            String description = "Canal para notificaciones de la radio"; // Descripción del canal
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel); // Crea el canal de notificación
        }
    }

    private void showNotification(String title, String message) {
        int icon = isPlaying ? R.drawable.pausa : R.drawable.play; // Selecciona el ícono según el estado de reproducción

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(icon) // Ícono de la notificación
                .setContentTitle(title) // Título de la notificación
                .setContentText(message) // Mensaje de la notificación
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true); // Elimina la notificación al hacer clic

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void cancelNotification() {
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.cancel(NOTIFICATION_ID); // Cancela la notificación por ID
    }

    private void initializeUI() {
        playPauseButton = findViewById(R.id.buttonPlayPause);
        playPauseButton.setOnClickListener(v -> togglePlayPause());

        // Configura los enlaces de las vistas a diferentes URLs
        setOnClickListener(R.id.facebook, "https://www.facebook.com/profile.php?id=100077840239066");
        setOnClickListener(R.id.whatsapp, "https://twitter.com/tu_perfil");
        setOnClickListener(R.id.youtube, "https://www.instagram.com/tu_perfil");
        setOnClickListener(R.id.pagina, "https://tapacari.gob.bo/");
    }

    private void setOnClickListener(int viewId, String url) {
        findViewById(viewId).setOnClickListener(v -> openLink(url)); // Asigna el enlace a la vista especificada
    }

    private void initializeMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(mp -> mediaPlayer.start());
    }

    private void initializeVolumeControl() {
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        ImageButton buttonVolume = findViewById(R.id.buttonVolume);
        seekBarVolume = findViewById(R.id.seekBarVolume);

        seekBarVolume.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        seekBarVolume.setProgress(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC));

        buttonVolume.setOnClickListener(v -> toggleSeekBarVisibility());
        seekBarVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0); // Cambia el volumen
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Método vacío
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Método vacío
            }
        });
    }

    private void toggleSeekBarVisibility() {
        int visibility = (seekBarVolume.getVisibility() == View.GONE) ? View.VISIBLE : View.GONE;
        seekBarVolume.setVisibility(visibility); // Cambia la visibilidad del SeekBar
        Toast.makeText(this, visibility == View.VISIBLE ? "Control de volumen visible" : "Control de volumen oculto", Toast.LENGTH_SHORT).show();
    }

    private void initializePhoneStateListener() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        telephonyManager.listen(new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String phoneNumber) {
                if ((state == TelephonyManager.CALL_STATE_OFFHOOK || state == TelephonyManager.CALL_STATE_RINGING) && isPlaying) {
                    togglePlayPause(); // Pausa la reproducción
                }
            }
        }, PhoneStateListener.LISTEN_CALL_STATE); // Registra el listener para cambios de estado
    }

    private void togglePlayPause() {
        if (isPlaying) {
            stopRadio();
            updatePlayPauseButton(R.drawable.play, "Pausado", "Radio Tapacari");
        } else {
            playRadio();
            updatePlayPauseButton(R.drawable.pausa, "Reproduciendo", "Radio Tapacari");
            Toast.makeText(this, "Reproduciendo", Toast.LENGTH_SHORT).show(); // Mensaje de reproducción
        }
        isPlaying = !isPlaying; // Alterna el estado de reproducción
    }

    private void updatePlayPauseButton(int icon, String title, String message) {
        playPauseButton.setImageResource(icon); // Cambia el ícono
        showNotification(title, message); // Muestra la notificación
    }

    private void playRadio() {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource("https://stream.zeno.fm/xtvmziqutm5vv"); // URL de la transmisión de radio
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            Toast.makeText(this, "Error al iniciar la transmisión", Toast.LENGTH_SHORT).show(); // Mensaje de error
        }
    }

    private void stopRadio() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            playPauseButton.setImageResource(R.drawable.stop); // Cambia el ícono a "Stop"
        }
    }

    private void openLink(String url) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); // Crea una intención para abrir el enlace
    }
}

