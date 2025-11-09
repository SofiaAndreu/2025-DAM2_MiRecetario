package com.example.recetarioapp.ui;

// -- IMPORTACIONES --
import android.content.Intent;              // Para navegación
import android.content.SharedPreferences;   // Para almacenamiento persistente
import android.os.Bundle;                   // Para estado
import android.text.TextUtils;              // Utilidades texto
import android.widget.Toast;                // Mensajes breves
import androidx.appcompat.app.AlertDialog;  // Diálogos
import androidx.appcompat.app.AppCompatActivity;  // Activity base
import androidx.lifecycle.ViewModelProvider;      // ViewModel

import com.example.recetarioapp.R;
import com.example.recetarioapp.databinding.ActivityLoginBinding;  // ViewBinding
import com.example.recetarioapp.utils.ViewExtensions;  // Extensiones vistas
import com.example.recetarioapp.viewmodels.AuthViewModel;  // ViewModel auth
import com.example.recetarioapp.viewmodels.RecetaViewModel;  // ✅ NUEVO: Para sincronizar recetas
import com.google.android.material.textfield.TextInputEditText;  // Campo texto

/**
 * Activity para inicio de sesión con validaciones y modo anónimo
 */
public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;    // Binding para vistas
    private AuthViewModel authViewModel;     // ViewModel autenticación
    private RecetaViewModel recetaViewModel; // ✅ NUEVO: ViewModel para recetas
    private SharedPreferences prefs;         // Preferencias persistentes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);  // Llama método padre
        // Infla layout con ViewBinding
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());  // Establece vista

        // Inicializa ViewModel para autenticación
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        // ✅ NUEVO: Inicializa ViewModel para recetas
        recetaViewModel = new ViewModelProvider(this).get(RecetaViewModel.class);
        // Obtiene SharedPreferences para datos persistentes
        prefs = getSharedPreferences("RecetarioPrefs", MODE_PRIVATE);

        // Verifica si usuario ya tiene sesión (normal o anónima)
        if (authViewModel.isUserLoggedInOrAnonymous()) {
            // ✅ CORREGIDO: Sincronizar recetas antes de ir a Main
            sincronizarRecetas();
            irAMain();  // Si ya está logueado, va directo a main
            return;     // Termina onCreate aquí
        }

        cargarEmailGuardado();  // Carga email guardado si existe
        setupListeners();       // Configura listeners
        observeAuthState();     // Observa cambios auth
    }

    /**
     * Configura todos los listeners de la UI
     */
    private void setupListeners() {
        // Botón Login - inicia proceso de login
        binding.btnLogin.setOnClickListener(v -> intentarLogin());

        // Botón Registro - navega a pantalla de registro
        binding.btnIrRegistro.setOnClickListener(v -> irARegistro());

        // Texto "Continuar sin cuenta" - activa modo anónimo
        binding.tvContinuarSinCuenta.setOnClickListener(v -> continuarSinCuenta());

        // Listener para tecla Enter en password
        binding.etPassword.setOnEditorActionListener((v, actionId, event) -> {
            intentarLogin();  // Intenta login al presionar Enter
            return true;      // Evento manejado
        });
    }

    /**
     * Activa modo anónimo y navega a Main
     */
    private void continuarSinCuenta() {
        // Usa ViewModel para activar modo anónimo
        authViewModel.activarModoAnonimo();
        // ✅ CORREGIDO: No sincronizar en modo anónimo (no hay recetas en Firebase)
        irAMain();  // Navega a pantalla principal
    }

    /**
     * Verifica si el usuario actual es anónimo
     */
    private boolean esUsuarioAnonimo() {
        // Lee preferencia que indica modo anónimo
        return prefs.getBoolean("es_usuario_anonimo", false);
    }

    /**
     * Intenta realizar login después de validar campos
     */
    private void intentarLogin() {
        // Obtiene y limpia texto de campos
        String email = getText(binding.etEmail);
        String password = getText(binding.etPassword);

        // Valida campos, si hay error se detiene
        if (!validarCampos(email, password)) {
            return;
        }

        // Gestiona "recordar email" según estado del checkbox
        if (binding.cbRecordar.isChecked()) {
            guardarEmail(email);   // Guarda email si está marcado
        } else {
            limpiarEmail();        // Limpia email si no está marcado
        }

        // Limpia modo anónimo previo si existe
        limpiarModoAnonimo();

        // Llama al ViewModel para realizar login
        authViewModel.login(email, password);
    }

    /**
     * Limpia preferencias de modo anónimo
     */
    private void limpiarModoAnonimo() {
        // Solo limpia si actualmente es usuario anónimo
        if (esUsuarioAnonimo()) {
            SharedPreferences.Editor editor = prefs.edit();  // Editor para modificar
            editor.remove("es_usuario_anonimo");  // Elimina flag anónimo
            editor.remove("user_mode");           // Elimina modo usuario
            editor.remove("fecha_anonimo");       // Elimina fecha anónimo
            editor.apply();  // Aplica cambios
        }
    }

    /**
     * Valida campos de email y password
     */
    private boolean validarCampos(String email, String password) {
        boolean esValido = true;

        // VALIDACIÓN EMAIL
        if (TextUtils.isEmpty(email)) {
            binding.tilEmail.setError("Ingresa tu email");  // Error vacío
            esValido = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("Email inválido");  // Error formato
            esValido = false;
        } else {
            binding.tilEmail.setError(null);  // Limpia error
        }

        // VALIDACIÓN PASSWORD
        if (TextUtils.isEmpty(password)) {
            binding.tilPassword.setError("Ingresa tu contraseña");  // Error vacío
            esValido = false;
        } else {
            binding.tilPassword.setError(null);  // Limpia error
        }
        return esValido;  // Retorna resultado
    }

    /**
     * Observa cambios en estado de autenticación
     */
    private void observeAuthState() {
        authViewModel.getAuthState().observe(this, state -> {
            if (state == null) return;  // Sale si estado nulo

            // Controla UI según estado loading
            ViewExtensions.setVisible(findViewById(R.id.progress_bar), state.isLoading());
            ViewExtensions.setEnabled(binding.btnLogin, !state.isLoading());
            ViewExtensions.setEnabled(binding.btnIrRegistro, !state.isLoading());
            ViewExtensions.setEnabled(binding.tvContinuarSinCuenta, !state.isLoading());

            // Maneja estado éxito
            if (state.isSuccess()) {
                Toast.makeText(this, state.mensaje, Toast.LENGTH_SHORT).show();  // Mensaje
                authViewModel.limpiarEstado();  // Limpia estado
                //Sincronizar recetas después del login exitoso
                sincronizarRecetas();
                irAMain();  // Navega a main
            }
            // Maneja estado error
            else if (state.hasError()) {
                mostrarError(state.mensaje);    // Muestra error
                authViewModel.limpiarEstado();  // Limpia estado
            }
        });
    }

    //NUEVO MÉTODO: Sincronizar recetas después del login
    private void sincronizarRecetas() {
        recetaViewModel.sincronizar();
        Toast.makeText(this, "Sincronizando recetas...", Toast.LENGTH_SHORT).show();
    }

    // ===== MÉTODOS DE NAVEGACIÓN =====

    /**
     * Navega a MainActivity limpiando stack
     */
    private void irAMain() {
        Intent intent = new Intent(this, MainActivity.class);  // Crea intent
        // Flags para nueva tarea y limpiar stack
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);  // Inicia actividad
        finish();               // Cierra esta actividad
    }

    /**
     * Navega a RegisterActivity
     */
    private void irARegistro() {
        Intent intent = new Intent(this, RegisterActivity.class);  // Crea intent
        startActivity(intent);  // Inicia actividad (sin flags especiales)
    }

    // ===== MÉTODOS AUXILIARES =====

    /**
     * Obtiene texto de forma segura de EditText
     */
    private String getText(TextInputEditText editText) {
        // Retorna texto trimmeado o string vacío si es null
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }

    /**
     * Muestra diálogo de error
     */
    private void mostrarError(String mensaje) {
        new AlertDialog.Builder(this)
                .setTitle("Error")        // Título
                .setMessage(mensaje)      // Mensaje del error
                .setPositiveButton("OK", null)  // Botón OK
                .show();  // Muestra diálogo
    }

    /**
     * Carga email guardado en preferencias
     */
    private void cargarEmailGuardado() {
        // Obtiene email guardado, vacío si no existe
        String emailGuardado = prefs.getString("email_guardado", "");
        if (!emailGuardado.isEmpty()) {
            binding.etEmail.setText(emailGuardado);    // Establece email en campo
            binding.cbRecordar.setChecked(true);       // Marca checkbox recordar
        }
    }

    /**
     * Guarda email en preferencias
     */
    private void guardarEmail(String email) {
        prefs.edit()                    // Obtiene editor
                .putString("email_guardado", email)  // Guarda email
                .apply();               // Aplica cambios
    }

    /**
     * Elimina email guardado de preferencias
     */
    private void limpiarEmail() {
        prefs.edit()                    // Obtiene editor
                .remove("email_guardado")  // Elimina email
                .apply();               // Aplica cambios
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();  // Llama método padre
        binding = null;     // Libera binding para evitar memory leaks
    }
}