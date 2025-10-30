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
 * Activity principal: Bottom Navigation, Autenticación, Detectar edición. ✓
 */
public class MainActivity extends AppCompatActivity {

    //VARIABLES
    private NavController navController;
    private FirebaseAuth auth;

    //MÉTODO PRINCIPAL
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance(); //Inicializar Firebase Auth
        verificarAutenticacion(); //Método verificar usuario autenticado/autenticar anónimamente
        setupNavigation(); //Método config. bottom nav
        verificarSiEdicion(); //Método verificar si edición
    } // ---------------------------------------------------------------------------- //

    //Verifica si se está abriendo la app para editar una receta
    private void verificarSiEdicion() {
        Intent intent = getIntent();

        //Comprobar si intent contiene el parámetro "editar_receta_id"
        if (intent.hasExtra("editar_receta_id")) {
            long recetaId = intent.getLongExtra("editar_receta_id", -1); //Receta ID / default: -1
            if (recetaId != -1) {
                //Bundle con ID
                Bundle bundle = new Bundle();
                bundle.putLong("receta_id", recetaId);
                //Navegación al fragmento addRecipeFragment para editar correspondiente
                navController.navigate(R.id.addRecipeFragment, bundle);
            }
        }
    } // ---------------------------------------------------------------------------- //

    //Configura el sistema de navegación
    private void setupNavigation() {
        // Obtener NavHostFragment
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        //Obtener NavController
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation); //Configurar BottomNavigationView
            NavigationUI.setupWithNavController(bottomNavigationView, navController); //Conectar BottomNavigationView <-> NavController

            // Listener personalizado para manejar la navegación inferior
            bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int itemId = item.getItemId(); //Obtener ID del elemento seleccionado

                    //Navega al fragmento seleccionado correspondiente
                    if (itemId == R.id.navigation_home) { //HOME/INICIO
                        navController.navigate(R.id.homeFragment);
                        return true;
                    } else if (itemId == R.id.navigation_recipes) { //RECETAS
                        navController.navigate(R.id.recipesFragment);
                        return true;
                    } else if (itemId == R.id.navigation_add) { //AÑADIR/NUEVA
                        navController.navigate(R.id.addRecipeFragment);
                        return true;
                    } else if (itemId == R.id.navigation_favorites) { //FAVORITOS
                        navController.navigate(R.id.favoritesFragment);
                        return true;
                    }
                    return false;
                }
            });
        }
    } // ---------------------------------------------------------------------------- //

    // Verifica si hay un usuario autenticado
    private void verificarAutenticacion() {
        FirebaseUser currentUser = auth.getCurrentUser(); //Obtener usuario actual autenticado

        //No hay usuario -> sesión anónima
        if (currentUser == null) {
            auth.signInAnonymously()
                    .addOnSuccessListener(authResult -> {
                        // Usuario autenticado correctamente
                    })
                    .addOnFailureListener(e -> {
                        // Error al autenticar
                    });
        }
    } // ---------------------------------------------------------------------------- //

//    Botón para volver atrás
//    @Override
//    public boolean onSupportNavigateUp() {
//        return navController.navigateUp() || super.onSupportNavigateUp();
//    }

}