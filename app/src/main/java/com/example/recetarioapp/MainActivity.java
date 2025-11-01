package com.example.recetarioapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.recetarioapp.viewmodels.AuthViewModel;
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
    private AuthViewModel authViewModel;
    private SharedPreferences prefs;

    //MÉTODO PRINCIPAL
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance(); //Inicializar Firebase Auth
        prefs = getSharedPreferences("RecetarioPrefs", MODE_PRIVATE);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        verificarAutenticacion(); //Método verificar usuario autenticado/autenticar anónimamente
        setupNavigation(); //Método config. bottom nav
        verificarSiEdicion(); //Método verificar si edición
        setupUIForUserMode(); // Configurar UI según el modo de usuario
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

    // Configurar UI según el modo de usuario
    private void setupUIForUserMode() {
        if (authViewModel.esUsuarioAnonimo()) {
            // Mostrar indicador de modo invitado
            Toast.makeText(this, "Modo Invitado", Toast.LENGTH_SHORT).show();
            // Aquí puedes ocultar/mostrar elementos según el modo
        }
    }

    // Verifica si hay un usuario autenticado - VERSIÓN MEJORADA
    private void verificarAutenticacion() {
        FirebaseUser currentUser = auth.getCurrentUser(); //Obtener usuario actual autenticado

        // Si es usuario anónimo (modo "Continuar sin cuenta"), no hacer nada
        if (authViewModel.esUsuarioAnonimo()) {
            return; // Mantener el modo anónimo sin autenticar con Firebase
        }

        //No hay usuario -> sesión anónima de Firebase (tu lógica original)
        if (currentUser == null) {
            auth.signInAnonymously()
                    .addOnSuccessListener(authResult -> {
                        // Usuario autenticado correctamente con Firebase Anónimo
                    })
                    .addOnFailureListener(e -> {
                        // Error al autenticar
                        Toast.makeText(this, "Error de conexión", Toast.LENGTH_SHORT).show();
                    });
        }
    } // ---------------------------------------------------------------------------- //

    // Método para cerrar sesión (incluyendo modo anónimo)
    public void cerrarSesionCompleta() {
        // Cerrar sesión de Firebase
        auth.signOut();
        // Limpiar modo anónimo
        authViewModel.limpiarModoAnonimo();

        // Redirigir al login
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // Método para verificar si el usuario actual es anónimo
    public boolean esModoInvitado() {
        return authViewModel.esUsuarioAnonimo();
    }

//    Botón para volver atrás
//    @Override
//    public boolean onSupportNavigateUp() {
//        return navController.navigateUp() || super.onSupportNavigateUp();
//    }

}