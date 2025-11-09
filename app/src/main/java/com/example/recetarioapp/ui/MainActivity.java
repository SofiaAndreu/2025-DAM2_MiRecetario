package com.example.recetarioapp.ui;

// Importaciones de Android y componentes
import android.content.Intent;              // Para navegación entre actividades
import android.content.SharedPreferences;   // Para almacenamiento persistente
import android.os.Bundle;                   // Para manejo de estado de la actividad
import android.view.MenuItem;               // Para manejar items de menú
import android.widget.Toast;                // Para mostrar mensajes breves al usuario

import androidx.annotation.NonNull;         // Para anotaciones de no nulidad
import androidx.appcompat.app.AppCompatActivity;  // Activity base con compatibilidad
import androidx.lifecycle.ViewModelProvider;      // Para obtener instancias de ViewModel
import androidx.navigation.NavController;          // Controlador de navegación
import androidx.navigation.fragment.NavHostFragment; // Fragmento host para navegación
import androidx.navigation.ui.NavigationUI;        // Utilidades UI para navegación

import com.example.recetarioapp.R;
import com.example.recetarioapp.viewmodels.AuthViewModel;  // ViewModel para autenticación
import com.google.android.material.bottomnavigation.BottomNavigationView;  // Barra navegación inferior
import com.google.android.material.navigation.NavigationBarView;           // Interface navegación
import com.google.firebase.auth.FirebaseAuth;      // Autenticación Firebase
import com.google.firebase.auth.FirebaseUser;      // Usuario Firebase

/**
 * MainActivity - Activity principal de la aplicación
 * Funcionalidades: Bottom Navigation, Autenticación, Detectar edición de recetas
 */
public class MainActivity extends AppCompatActivity {

    // VARIABLES DE INSTANCIA
    private NavController navController;    // Controla la navegación entre fragments
    private FirebaseAuth auth;              // Instancia de autenticación Firebase
    private AuthViewModel authViewModel;    // ViewModel para gestión de autenticación
    private SharedPreferences prefs;        // Preferencias para datos persistentes

    // MÉTODO PRINCIPAL - Ciclo de vida onCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);  // Llama al método padre de la Activity
        setContentView(R.layout.activity_main);  // Establece el layout principal

        // Inicialización de componentes
        auth = FirebaseAuth.getInstance();  // Obtiene instancia única de Firebase Auth
        prefs = getSharedPreferences("RecetarioPrefs", MODE_PRIVATE);  // Preferencias app
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);  // Obtiene ViewModel

        verificarAutenticacion();    // Verifica usuario autenticado/autentica anónimamente
        setupNavigation();           // Configura bottom navigation
        verificarSiEdicion();        // Verifica si viene de editar receta
        setupUIForUserMode();        // Configura UI según modo de usuario (normal/invitado)
    }
    // ---------------------------------------------------------------------------- //

    /**
     * Verifica si se está abriendo la app para editar una receta
     * Detecta el Intent que podría contener ID de receta a editar
     */
    private void verificarSiEdicion() {
        Intent intent = getIntent();  // Obtiene el Intent que inició esta Activity

        // Comprueba si el Intent contiene el parámetro "editar_receta_id"
        if (intent.hasExtra("editar_receta_id")) {
            // Obtiene el ID de la receta, valor por defecto -1 si no existe
            long recetaId = intent.getLongExtra("editar_receta_id", -1);
            if (recetaId != -1) {  // Si se encontró un ID válido
                // Crea Bundle para pasar datos al fragmento
                Bundle bundle = new Bundle();
                bundle.putLong("receta_id", recetaId);  // Agrega ID al bundle

                // Navega al fragmento addRecipeFragment para editar la receta
                navController.navigate(R.id.addRecipeFragment, bundle);
            }
        }
    }
    // ---------------------------------------------------------------------------- //

    /**
     * Configura el sistema de navegación con BottomNavigationView
     * Conecta los fragments con la barra de navegación inferior
     */
    private void setupNavigation() {
        // Obtiene el NavHostFragment del layout
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        // Verifica que el NavHostFragment no sea nulo
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();  // Obtiene NavController
            BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);  // Obtiene BottomNavigationView

            // Conecta BottomNavigationView con NavController (configuración automática)
            NavigationUI.setupWithNavController(bottomNavigationView, navController);

            // Listener personalizado para manejar la navegación inferior manualmente
            bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int itemId = item.getItemId();  // Obtiene ID del elemento seleccionado

                    // Navega al fragmento seleccionado correspondiente según el ID
                    if (itemId == R.id.navigation_home) {        // HOME/INICIO
                        navController.navigate(R.id.homeFragment);
                        return true;  // Indica que el evento fue manejado
                    } else if (itemId == R.id.navigation_recipes) {  // RECETAS
                        navController.navigate(R.id.recipesFragment);
                        return true;
                    } else if (itemId == R.id.navigation_add) {      // AÑADIR/NUEVA RECETA
                        navController.navigate(R.id.addRecipeFragment);
                        return true;
                    } else if (itemId == R.id.navigation_favorites) { // FAVORITOS
                        navController.navigate(R.id.favoritesFragment);
                        return true;
                    }
                    return false;  // Evento no manejado
                }
            });
        }
    }
    // ---------------------------------------------------------------------------- //

    /**
     * Configurar UI según el modo de usuario (normal o invitado)
     * Aplica diferentes comportamientos visuales según el tipo de usuario
     */
    private void setupUIForUserMode() {
        if (authViewModel.esUsuarioAnonimo()) {  // Verifica si es usuario anónimo/invitado
            // Mostrar indicador de modo invitado
            Toast.makeText(this, "Modo Invitado", Toast.LENGTH_SHORT).show();
            // Aquí puedes ocultar/mostrar elementos según el modo
            // Ejemplo: ocultar funcionalidades premium, mostrar banner invitado, etc.
        }
    }

    /**
     * Verifica autenticación del usuario actual.
     * - Si es modo anónimo: mantiene sesión local sin Firebase
     * - Si no hay usuario Firebase: crea sesión anónima de Firebase automáticamente
     */
    private void verificarAutenticacion() {
        FirebaseUser currentUser = auth.getCurrentUser();  // Obtiene usuario actual autenticado en Firebase

        // Si es usuario anónimo (modo "Continuar sin cuenta"), no hacer nada
        if (authViewModel.esUsuarioAnonimo()) {
            return;  // Mantener el modo anónimo sin autenticar con Firebase
        }

        // No hay usuario en Firebase -> crear sesión anónima de Firebase
        if (currentUser == null) {
            auth.signInAnonymously()  // Autentica anónimamente en Firebase
                    .addOnSuccessListener(authResult -> {
                        // Usuario autenticado correctamente con Firebase Anónimo
                    })
                    .addOnFailureListener(e -> { //Error al autenticar anónimamente
                        Toast.makeText(this, "Error de conexión", Toast.LENGTH_SHORT).show();
                    });
        }
    }
    // ---------------------------------------------------------------------------- //
    /**
     * Método para verificar si el usuario actual está en modo invitado
     * @return true si es usuario anónimo, false si es usuario registrado
     */
    public boolean esModoInvitado() {
        return authViewModel.esUsuarioAnonimo();  // Delegado al ViewModel
    }

    // COMENTADO: Botón para volver atrás (navegación Up)
    // @Override
    // public boolean onSupportNavigateUp() {
    //     return navController.navigateUp() || super.onSupportNavigateUp();
    // }

}