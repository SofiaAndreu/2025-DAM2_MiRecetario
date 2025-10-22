package com.example.recetarioapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Main Activity con Bottom Nav
 */
public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private BottomNavigationView bottomNavigationView;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Verificar si hay usuario autenticado, si no, autenticar anónimamente
        verificarAutenticacion();

        // Configurar Navigation
        setupNavigation();

        // Verificar si viene de edición
        verificarIntentoEdicion();
    }

    /**
     * Verifica si se está abriendo la app para editar una receta
     */
    private void verificarIntentoEdicion() {
        Intent intent = getIntent();
        if (intent.hasExtra("editar_receta_id")) {
            long recetaId = intent.getLongExtra("editar_receta_id", -1);
            if (recetaId != -1) {
                // Navegar al fragment de añadir en modo edición
                Bundle bundle = new Bundle();
                bundle.putLong("receta_id", recetaId);
                navController.navigate(R.id.addRecipeFragment, bundle);
            }
        }
    }

    /**
     * Configura el sistema de navegación
     */
    private void setupNavigation() {
        // Obtener NavHostFragment y NavController
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();

            // Configurar BottomNavigationView
            bottomNavigationView = findViewById(R.id.bottom_navigation);

            // Conectar BottomNavigationView con NavController
            NavigationUI.setupWithNavController(bottomNavigationView, navController);

            // Listener personalizado para manejar la navegación
            bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int itemId = item.getItemId();

                    if (itemId == R.id.navigation_home) {
                        navController.navigate(R.id.homeFragment);
                        return true;
                    } else if (itemId == R.id.navigation_recipes) {
                        navController.navigate(R.id.recipesFragment);
                        return true;
                    } else if (itemId == R.id.navigation_add) {
                        navController.navigate(R.id.addRecipeFragment);
                        return true;
                    } else if (itemId == R.id.navigation_favorites) {
                        navController.navigate(R.id.favoritesFragment);
                        return true;
                    }

                    return false;
                }
            });
        }
    }

    /**
     * Verifica si hay un usuario autenticado
     * Si no hay, inicia sesión anónima
     */
    private void verificarAutenticacion() {
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            // Autenticación anónima para permitir el uso sin cuenta
            auth.signInAnonymously()
                    .addOnSuccessListener(authResult -> {
                        // Usuario autenticado correctamente
                    })
                    .addOnFailureListener(e -> {
                        // Error al autenticar
                        // Aquí podrías mostrar un mensaje o intentar de nuevo
                    });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}