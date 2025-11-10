package com.example.recetarioapp.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.recetarioapp.R;
import com.example.recetarioapp.viewmodels.AuthViewModel;

/**
 * SplashActivity - Pantalla de presentación que verifica el estado de autenticación.
 *
 * Funcionalidades principales:
 * - Muestra el logo de la aplicación durante un período breve
 * - Verifica el estado de autenticación del usuario
 * - Redirige automáticamente a la pantalla principal o de login
 * - Implementa el patrón de splash screen nativo de Android
 */
@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    /**
     * Duración en milisegundos que se mostrará la pantalla de splash.
     * Valor optimizado para permitir carga inicial sin hacer esperar al usuario.
     */
    private static final long SPLASH_DELAY = 2000;

    /**
     * Método principal del ciclo de vida que se ejecuta al crear la Activity.
     * Configura el layout y programa la redirección después del delay.
     *
     * @param savedInstanceState Estado previo de la Activity para restaurar estado
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Obtener instancia del ViewModel para gestión de autenticación
        AuthViewModel authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Programar la redirección después del delay especificado
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intentDestino;

            // Decidir la Activity destino según el estado de autenticación
            if (authViewModel.isUserLoggedIn()) {
                // Usuario autenticado - redirigir a la pantalla principal
                intentDestino = new Intent(this, MainActivity.class);
            } else {
                // Usuario no autenticado - redirigir a pantalla de login
                intentDestino = new Intent(this, LoginActivity.class);
            }

            // Iniciar la Activity destino y cerrar esta para evitar volver atrás
            startActivity(intentDestino);
            finish();
        }, SPLASH_DELAY);
    }
}