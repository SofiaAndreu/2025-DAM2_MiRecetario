package com.example.recetarioapp.ui;

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
import com.example.recetarioapp.R;
import com.example.recetarioapp.viewmodels.AuthViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Activity principal que actúa como contenedor de fragments y gestiona la navegación.
 *
 * Funcionalidades principales:
 * - Configuración del sistema de navegación con BottomNavigationView
 * - Gestión de autenticación de usuarios y modo invitado
 * - Detección y manejo de intentos de edición de recetas
 * - Configuración de la interfaz según el tipo de usuario
 */
public class MainActivity extends AppCompatActivity {

    // Controlador para gestionar la navegación entre fragments
    private NavController controladorNavegacion;

    // Instancia de Firebase Authentication para gestión de usuarios
    private FirebaseAuth autenticacion;

    // ViewModel para operaciones de autenticación
    private AuthViewModel authViewModel;

    // Preferencias compartidas para persistencia de datos
    private SharedPreferences preferencias;

    /**
     * Método principal de inicialización de la Activity.
     * Configura la interfaz, ViewModels y verifica el estado de autenticación.
     *
     * @param savedInstanceState Estado previo de la Activity para restaurar estado
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar componentes principales
        autenticacion = FirebaseAuth.getInstance();
        preferencias = getSharedPreferences("RecetarioPrefs", MODE_PRIVATE);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Configurar funcionalidades principales
        verificarAutenticacion();
        configurarNavegacion();
        verificarSiEdicion();
        configurarUIparaModoUsuario();
    }

    /**
     * Verifica si se está abriendo la aplicación para editar una receta específica.
     * Extrae el ID de receta del Intent y navega al fragmento de edición.
     */
    private void verificarSiEdicion() {
        Intent intent = getIntent();

        // Verificar si el Intent contiene un ID de receta para editar
        if (intent.hasExtra("editar_receta_id")) {
            long recetaId = intent.getLongExtra("editar_receta_id", -1);
            if (recetaId != -1) {
                // Crear bundle con el ID de receta y navegar al fragmento de edición
                Bundle bundle = new Bundle();
                bundle.putLong("receta_id", recetaId);
                controladorNavegacion.navigate(R.id.addRecipeFragment, bundle);
            }
        }
    }

    /**
     * Configura el sistema de navegación con BottomNavigationView.
     * Establece la relación entre items del menú y fragments destino.
     */
    private void configurarNavegacion() {
        // Obtener el fragmento host de navegación
        NavHostFragment fragmentoHost = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (fragmentoHost != null) {
            controladorNavegacion = fragmentoHost.getNavController();
            BottomNavigationView navegacionInferior = findViewById(R.id.bottom_navigation);

            // Configurar navegación automática con el controlador
            NavigationUI.setupWithNavController(navegacionInferior, controladorNavegacion);

            // Configurar listener personalizado para items de navegación
            navegacionInferior.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int idItem = item.getItemId();

                    // Manejar navegación según el item seleccionado
                    if (idItem == R.id.navigation_home) {
                        controladorNavegacion.navigate(R.id.homeFragment);
                        return true;
                    } else if (idItem == R.id.navigation_recipes) {
                        controladorNavegacion.navigate(R.id.recipesFragment);
                        return true;
                    } else if (idItem == R.id.navigation_add) {
                        controladorNavegacion.navigate(R.id.addRecipeFragment);
                        return true;
                    } else if (idItem == R.id.navigation_favorites) {
                        controladorNavegacion.navigate(R.id.favoritesFragment);
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    /**
     * Configura la interfaz de usuario según el modo de usuario (normal o invitado).
     * Muestra indicaciones visuales cuando el usuario está en modo invitado.
     */
    private void configurarUIparaModoUsuario() {
        if (authViewModel.esUsuarioAnonimo()) {
            Toast.makeText(this, "Modo Invitado", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Verifica y gestiona la autenticación del usuario.
     * Si no hay usuario autenticado, inicia sesión anónima con Firebase.
     */
    private void verificarAutenticacion() {
        FirebaseUser usuarioActual = autenticacion.getCurrentUser();

        // Si ya es usuario anónimo, no es necesario autenticar
        if (authViewModel.esUsuarioAnonimo()) {
            return;
        }

        // Si no hay usuario autenticado, iniciar sesión anónima
        if (usuarioActual == null) {
            autenticacion.signInAnonymously()
                    .addOnSuccessListener(authResult -> {
                        // Usuario autenticado correctamente con Firebase Anónimo
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error de conexión", Toast.LENGTH_SHORT).show();
                    });
        }
    }
}