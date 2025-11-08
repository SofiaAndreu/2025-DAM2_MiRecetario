package com.example.recetarioapp.ui;

// -- IMPORTACIONES --
import android.annotation.SuppressLint;  // Para suprimir advertencias específicas
import android.content.Intent;           // Para navegar entre actividades
import android.os.Bundle;                // Para pasar datos entre actividades
import android.os.Handler;               // Para ejecutar código después de un delay
import android.os.Looper;                // Para asegurar ejecución en hilo principal
import androidx.appcompat.app.AppCompatActivity;  // Activity base con compatibilidad
import androidx.lifecycle.ViewModelProvider;      // Para obtener instancias de ViewModel

import com.example.recetarioapp.R;
import com.example.recetarioapp.viewmodels.AuthViewModel;  // ViewModel para autenticación

/**
 * SplashActivity - Pantalla de presentación que verifica autenticación
 * Muestra logo/app durante 2 segundos y redirige según estado de login
 */
@SuppressLint("CustomSplashScreen")  // Suprime warning de splash screen personalizado
public class SplashActivity extends AppCompatActivity {

    // Constante para duración del splash screen (2 segundos)
    private static final long SPLASH_DELAY = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);  // Llama al método padre
        setContentView(R.layout.activity_splash);  // Establece el layout XML

        // Obtiene instancia del ViewModel para gestionar autenticación
        AuthViewModel authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Handler para ejecutar código después del delay especificado
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent;  // Variable para la actividad destino

            // Verifica si el usuario ya está logueado
            if (authViewModel.isUserLoggedIn()) {
                // Usuario autenticado -> va a MainActivity
                intent = new Intent(this, MainActivity.class);
            } else {
                // Usuario no autenticado -> va a LoginActivity
                intent = new Intent(this, LoginActivity.class);
            }

            startActivity(intent);  // Inicia la actividad destino
            finish();               // Cierra SplashActivity para evitar volver atrás
        }, SPLASH_DELAY);  // Delay de 2 segundos antes de ejecutar
    }
}