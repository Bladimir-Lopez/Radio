package com.example.radio;

// Importaciones necesarias para el funcionamiento de la aplicación
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.media.AudioManager; // Control del audio del dispositivo
import android.net.Uri; // Manejo de URIs para abrir enlaces
import android.os.Bundle; // Permite transferir datos entre actividades
import android.view.View; // Control de la interfaz de usuario
import android.content.Intent; // Para realizar acciones como abrir enlaces externos
import android.widget.ImageButton; // Botón que muestra una imagen
import android.media.MediaPlayer; // Control de la reproducción de audio
import android.widget.RemoteViews;
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
    protected boolean isPlaying = false; // Indica si la música está en reproducción
    private AudioManager audioManager; // Administrador de audio del dispositivo
    private SeekBar seekBarVolume; // Barra deslizante para controlar el volumen
    public static MainActivity instance; // Instancia estática de MainActivity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Establece el diseño de la actividad principal
        instance = this; // Asigna la instancia actual a la variable estática

        // Inicializa componentes de la interfaz y la lógica de reproducción
        initializeUI();
        initializeMediaPlayer();
        initializeVolumeControl();
        initializePhoneStateListener();
        createNotificationChannel();
    }

    // Método estático para alternar la reproducción/pausa
    public static void togglePlayPauseStatic() {
        if (instance != null) {
            instance.togglePlayPause(); // Llama al método de instancia
        }
    }

    // Crea un canal de notificación para dispositivos Android O y superiores
    protected void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Radio Channel"; // Nombre del canal
            String description = "Canal para notificaciones de la radio"; // Descripción del canal
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel); // Crea el canal de notificación
        }
    }

    // Muestra una notificación personalizada con los controles de reproducción
    private void showNotification(String title, String message) {
        // Crear RemoteViews personalizado
        @SuppressLint("RemoteViewLayout") RemoteViews notificationLayout = new RemoteViews(getPackageName(), R.layout.notification_custom);
        notificationLayout.setTextViewText(R.id.notification_title, title);

        int playPauseIcon = isPlaying ? R.drawable.pausa : R.drawable.play;
        notificationLayout.setImageViewResource(R.id.button_play_pause, playPauseIcon);

        // Configurar acciones para los botones
        configureNotificationAction(notificationLayout, R.id.button_play_pause, "ACTION_TOGGLE_PLAY_PAUSE");
        configureNotificationAction(notificationLayout, R.id.button_close, "ACTION_CLOSE");

        // Crear la notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setCustomContentView(notificationLayout)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        // Mostrar la notificación
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    // Configura las acciones de los botones en la notificación
    private void configureNotificationAction(RemoteViews notificationLayout, int viewId, String action) {
        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.setAction(action);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationLayout.setOnClickPendingIntent(viewId, pendingIntent);
    }

    // Cancela la notificación activa
    protected void cancelNotification() {
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.cancel(NOTIFICATION_ID); // Cancela la notificación por ID
    }

    // Inicializa la interfaz de usuario y asigna eventos de clic
    protected void initializeUI() {
        playPauseButton = findViewById(R.id.buttonPlayPause);
        playPauseButton.setOnClickListener(v -> togglePlayPause());

        // Configura los enlaces de las vistas a diferentes URLs
        setOnClickListener(R.id.facebook, "https://www.facebook.com/profile.php?id=100077840239066");
        setOnClickListener(R.id.whatsapp, "https://twitter.com/tu_perfil");
        setOnClickListener(R.id.youtube, "https://www.instagram.com/tu_perfil");
        setOnClickListener(R.id.pagina, "https://tapacari.gob.bo/");
    }

    // Asigna un enlace a una vista específica
    protected void setOnClickListener(int viewId, String url) {
        findViewById(viewId).setOnClickListener(v -> openLink(url)); // Asigna el enlace a la vista especificada
    }

    // Inicializa el MediaPlayer y configura el listener para iniciar la reproducción
    protected void initializeMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(mp -> mediaPlayer.start());
    }

    // Inicializa el control de volumen y su comportamiento
    protected void initializeVolumeControl() {
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        ImageButton buttonVolume = findViewById(R.id.buttonVolume);
        seekBarVolume = findViewById(R.id.seekBarVolume);

        // Configura la barra de volumen
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

    // Alterna la visibilidad de la barra de volumen y muestra un mensaje
    protected void toggleSeekBarVisibility() {
        int visibility = (seekBarVolume.getVisibility() == View.GONE) ? View.VISIBLE : View.GONE;
        seekBarVolume.setVisibility(visibility); // Cambia la visibilidad del SeekBar
        Toast.makeText(this, visibility == View.VISIBLE ? "Control de volumen visible" : "Control de volumen oculto", Toast.LENGTH_SHORT).show();
    }

    // Inicializa el listener del estado del teléfono
    protected void initializePhoneStateListener() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        telephonyManager.listen(new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String phoneNumber) {
                // Si hay una llamada y la radio está sonando, pausa la reproducción
                if ((state == TelephonyManager.CALL_STATE_OFFHOOK || state == TelephonyManager.CALL_STATE_RINGING) && isPlaying) {
                    togglePlayPause(); // Pausa la reproducción
                }
            }
        }, PhoneStateListener.LISTEN_CALL_STATE); // Registra el listener para cambios de estado
    }

    // Alterna entre reproducir y pausar la radio
    protected void togglePlayPause() {
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

    // Actualiza el botón de reproducción/pausa y muestra la notificación
    protected void updatePlayPauseButton(int icon, String title, String message) {
        playPauseButton.setImageResource(icon); // Cambia el ícono
        showNotification(title, message); // Muestra la notificación
    }

    // Inicia la transmisión de la radio
    protected void playRadio() {
        try {
            mediaPlayer.reset(); // Restablece el MediaPlayer
            mediaPlayer.setDataSource("https://stream.zeno.fm/xtvmziqutm5vv"); // URL de la transmisión de radio
            mediaPlayer.prepareAsync(); // Prepara la transmisión de forma asíncrona
        } catch (IOException e) {
            Toast.makeText(this, "Error al iniciar la transmisión", Toast.LENGTH_SHORT).show(); // Mensaje de error
        }
    }

    // Detiene la reproducción de la radio
    protected void stopRadio() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop(); // Detiene la reproducción
            playPauseButton.setImageResource(R.drawable.stop); // Cambia el ícono a "Stop"
        }
    }

    // Abre un enlace en el navegador
    private void openLink(String url) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); // Crea una intención para abrir el enlace
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        instance = null; // Limpia la referencia estática
    }
}
