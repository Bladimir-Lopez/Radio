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


/** @noinspection ALL*/ // Definición de la actividad principal de la aplicación
public class MainActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer; // Reproductor de audio
    private ImageButton playPauseButton; // Botón de reproducir/pausar
    private boolean isPlaying = false; // Estado de la reproducción (true si está reproduciendo)

    private AudioManager audioManager; // Control de audio del dispositivo
    private SeekBar seekBarVolume; // Control deslizante para el volumen



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Establece el diseño de la interfaz principal

        // Inicialización de botones y vistas
        playPauseButton = findViewById(R.id.buttonPlayPause); // Botón de reproducir/pausar
        View facebook = findViewById(R.id.facebook); // Vista para el enlace a Facebook
        View whatsapp = findViewById(R.id.whatsapp); // Vista para el enlace a WhatsApp
        View youtube = findViewById(R.id.youtube); // Vista para el enlace a YouTube
        View pagina = findViewById(R.id.pagina); // Vista para el enlace a la página web

        mediaPlayer = new MediaPlayer(); // Inicializa el reproductor de audio

        // Configuración de clics en los botones
        playPauseButton.setOnClickListener(v -> togglePlayPause()); // Alterna entre reproducir y pausar
        facebook.setOnClickListener(v -> openLink("https://www.facebook.com/profile.php?id=100077840239066")); // Abre enlace a Facebook
        whatsapp.setOnClickListener(v -> openLink("https://twitter.com/tu_perfil")); // Abre enlace a WhatsApp
        youtube.setOnClickListener(v -> openLink("https://www.instagram.com/tu_perfil")); // Abre enlace a YouTube
        pagina.setOnClickListener(v -> openLink("https://tapacari.gob.bo/")); // Abre enlace a la página web

        // Inicializar el AudioManager para controlar el volumen del dispositivo
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE); // Obtiene el servicio de audio

        // Configurar el SeekBar de volumen
        ImageButton buttonVolume = findViewById(R.id.buttonVolume); // Botón para mostrar/ocultar el control de volumen
        seekBarVolume = findViewById(R.id.seekBarVolume); // Barra deslizante para el volumen
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC); // Obtiene el volumen máximo
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC); // Obtiene el volumen actual
        seekBarVolume.setMax(maxVolume); // Establece el volumen máximo en el SeekBar
        seekBarVolume.setProgress(currentVolume); // Establece el volumen actual en el SeekBar

        // Configura el clic en el botón de volumen para mostrar u ocultar el SeekBar
        buttonVolume.setOnClickListener(v -> {
            if (seekBarVolume.getVisibility() == View.GONE) {
                seekBarVolume.setVisibility(View.VISIBLE); // Muestra el SeekBar
                Toast.makeText(MainActivity.this, "Control de volumen visible", Toast.LENGTH_SHORT).show(); // Mensaje de notificación
            } else {
                seekBarVolume.setVisibility(View.GONE); // Oculta el SeekBar
                Toast.makeText(MainActivity.this, "Control de volumen oculto", Toast.LENGTH_SHORT).show(); // Mensaje de notificación
            }
        });

        // Configura el comportamiento del SeekBar cuando se cambia su valor
        seekBarVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0); // Establece el volumen según el progreso del SeekBar
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { } // No se necesita implementar lógica al iniciar el seguimiento

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { } // No se necesita implementar lógica al detener el seguimiento
        });

        // Configuración de TelephonyManager y PhoneStateListener para detectar cambios en el estado de las llamadas
        // Servicio para monitorear el estado del teléfono
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE); // Obtiene el servicio de telefonía
        // Llama a la implementación de la superclase
        // Cuando hay una llamada activa
        // Cuando el teléfono está sonando
        // Pausa la reproducción si el teléfono está en llamada o suena
        // Cuando no hay llamadas activas
        // No hacer nada cuando la llamada termina
        // Listener para detectar cambios en el estado de las llamadas
        PhoneStateListener phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String phoneNumber) {
                super.onCallStateChanged(state, phoneNumber); // Llama a la implementación de la superclase
                switch (state) {
                    case TelephonyManager.CALL_STATE_OFFHOOK: // Cuando hay una llamada activa
                    case TelephonyManager.CALL_STATE_RINGING: // Cuando el teléfono está sonando
                        if (isPlaying) {
                            togglePlayPause(); // Pausa la reproducción si el teléfono está en llamada o suena
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE: // Cuando no hay llamadas activas
                        // No hacer nada cuando la llamada termina
                        break;
                }
            }
        };

        // Registrar el listener para cambios en el estado del teléfono
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE); // Escucha cambios en el estado de la llamada
    }

    private void openLink(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url)); // Establece el URI de la URL
        startActivity(intent); // Inicia la actividad para abrir el enlace
    }


    // Método para reproducir o pausar el audio
    private void togglePlayPause() {
        if (isPlaying) {
            stopRadio(); // Detiene la reproducción si está activa
            playPauseButton.setImageResource(R.drawable.play); // Cambia el ícono a "play"
        } else {
            playRadio(); // Inicia la reproducción si está detenida
            playPauseButton.setImageResource(R.drawable.pausa); // Cambia el ícono a "pause"
        }
        isPlaying = !isPlaying; // Alterna el estado de reproducción
    }

    // Método para iniciar la reproducción de la radio
    private void playRadio() {
        try {
            mediaPlayer.reset(); // Resetea el reproductor de audio
            String RADIO_URL = "https://stream.zeno.fm/xtvmziqutm5vv"; // URL de la radio
            mediaPlayer.setDataSource(RADIO_URL); // Establece la fuente de datos para el reproductor
            mediaPlayer.prepareAsync(); // Prepara la reproducción de forma asíncrona
            mediaPlayer.setOnPreparedListener(mp -> mediaPlayer.start()); // Inicia la reproducción cuando esté preparado
        } catch (Exception e) {
            e.printStackTrace(); // Imprime la traza de errores en caso de excepción
        }
    }

    // Método para detener la reproducción de la radio
    private void stopRadio() {
        if (mediaPlayer.isPlaying()) { // Comprueba si el reproductor está en reproducción
            mediaPlayer.stop(); // Detiene la reproducción
        }
    }
}
