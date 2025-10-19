package com.example.recetarioapp;

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

        //Inicializar Firebase Auth.
        auth = FirebaseAuth.getInstance();
        //Verificar usuario autenticado
        verificarAutenticacion();
        //Config. Nav
        setNavigation();
    }

    //VERIFICACIÓN USUARIO AUTENTICADO / SESIÓN ANÓNIMA
    private void verificarAutenticacion(){
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            // Autenticación anónima para permitir el uso sin cuenta
            auth.signInAnonymously()
                    .addOnSuccessListener(authResult -> {
                        // Usuario autenticado correctamente
                    })
                    .addOnFailureListener(e -> {
                        // Error al autenticar
                        // Mostrar un mensaje o intentar de nuevo
                    });
        }
    }

    //SISTEMA DE NAVEGACION
    private void setNavigation(){
        //Obtener NavHostFragment
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment!= null){
            navController = navHostFragment.getNavController();

            // Configurar BottomNavigationView
            bottomNavigationView = findViewById(R.id.bottom_navigation);
            // Conectar BottomNavigationView con NavController
            NavigationUI.setupWithNavController(bottomNavigationView, navController);

            //Listener para manejar la navegación
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

    //
    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }


}