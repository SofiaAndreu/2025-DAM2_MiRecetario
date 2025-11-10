package com.example.recetarioapp.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.recetarioapp.R;
import com.example.recetarioapp.databinding.ActivityLoginBinding;
import com.example.recetarioapp.utils.ViewExtensions;
import com.example.recetarioapp.viewmodels.AuthViewModel;
import com.example.recetarioapp.viewmodels.RecetaViewModel;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Activity para inicio de sesión con validaciones completas y soporte para modo anónimo.
 *
 * Proporciona un flujo de autenticación robusto con:
 * - Login tradicional con email/password
 * - Modo invitado (continuar sin cuenta)
 * - Recordar credenciales
 * - Validación en tiempo real
 * - Sincronización automática de datos después del login
 */
public class LoginActivity extends AppCompatActivity {

    // ViewBinding para acceso type-safe a las vistas del layout
    private ActivityLoginBinding binding;

    // ViewModels para gestión de autenticación y datos
    private AuthViewModel authViewModel;
    private RecetaViewModel recetaViewModel;

    // Preferencias para persistencia de datos de usuario
    private SharedPreferences preferencias;

    /**
     * Método principal de inicialización de la Activity.
     * Configura el layout, inicializa ViewModels y verifica sesiones existentes.
     *
     * @param savedInstanceState Estado previo de la Activity para restaurar estado
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflar el layout usando ViewBinding
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar ViewModels y preferencias
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        recetaViewModel = new ViewModelProvider(this).get(RecetaViewModel.class);
        preferencias = getSharedPreferences("RecetarioPrefs", MODE_PRIVATE);

        // Verificar si ya existe una sesión (autenticada o anónima)
        if (authViewModel.isUserLoggedInOrAnonymous()) {
            sincronizarRecetas();
            irAMainActivity();
            return;
        }

        // Configurar componentes de la UI
        cargarEmailGuardado();
        configurarListeners();
        observarEstadoAutenticacion();
    }

    /**
     * Configura todos los listeners de interacción del usuario.
     * Incluye botones de login, registro y modo invitado.
     */
    private void configurarListeners() {
        // Botón de login tradicional
        binding.btnLogin.setOnClickListener(v -> intentarLogin());

        // Navegación a pantalla de registro
        binding.btnIrRegistro.setOnClickListener(v -> irARegistroActivity());

        // Opción de modo invitado
        binding.tvContinuarSinCuenta.setOnClickListener(v -> activarModoAnonimo());

        // Login con tecla Enter en campo de password
        binding.etPassword.setOnEditorActionListener((v, actionId, event) -> {
            intentarLogin();
            return true;
        });
    }

    /**
     * Activa el modo anónimo/invitado y navega a la pantalla principal.
     * No requiere credenciales pero tiene funcionalidades limitadas.
     */
    private void activarModoAnonimo() {
        authViewModel.activarModoAnonimo();
        irAMainActivity();
    }

    /**
     * Intenta realizar el proceso de login después de validar los campos.
     * Ejecuta validaciones y, si son exitosas, inicia el proceso de autenticación.
     */
    private void intentarLogin() {
        String email = obtenerTexto(binding.etEmail);
        String password = obtenerTexto(binding.etPassword);

        // Validar campos antes de proceder con el login
        if (!validarCampos(email, password)) {
            return;
        }

        // Gestionar "recordar email" según preferencia del usuario
        if (binding.cbRecordar.isChecked()) {
            guardarEmail(email);
        } else {
            limpiarEmailGuardado();
        }

        // Limpiar modo anónimo previo si existe
        limpiarModoAnonimo();

        // Delegar autenticación al ViewModel
        authViewModel.login(email, password);
    }

    /**
     * Limpia las preferencias de modo anónimo al iniciar sesión tradicional.
     * Previene conflictos entre sesiones anónimas y autenticadas.
     */
    private void limpiarModoAnonimo() {
        if (esUsuarioAnonimo()) {
            SharedPreferences.Editor editor = preferencias.edit();
            editor.remove("es_usuario_anonimo");
            editor.remove("user_mode");
            editor.remove("fecha_anonimo");
            editor.apply();
        }
    }

    /**
     * Verifica si el usuario actual está en modo anónimo.
     *
     * @return true si es usuario anónimo, false en caso contrario
     */
    private boolean esUsuarioAnonimo() {
        return preferencias.getBoolean("es_usuario_anonimo", false);
    }

    /**
     * Valida los campos de email y password en tiempo real.
     * Muestra errores específicos para cada campo inválido.
     *
     * @param email Email a validar
     * @param password Password a validar
     * @return true si todos los campos son válidos, false en caso contrario
     */
    private boolean validarCampos(String email, String password) {
        boolean esValido = true;

        // Validación de email - formato de correo electrónico válido
        if (TextUtils.isEmpty(email)) {
            binding.tilEmail.setError("Ingresa tu email");
            esValido = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("Email inválido");
            esValido = false;
        } else {
            binding.tilEmail.setError(null);
        }

        // Validación de password - no vacío
        if (TextUtils.isEmpty(password)) {
            binding.tilPassword.setError("Ingresa tu contraseña");
            esValido = false;
        } else {
            binding.tilPassword.setError(null);
        }

        return esValido;
    }

    /**
     * Observa los cambios en el estado de autenticación del ViewModel.
     * Actualiza la UI según el estado actual (loading, success, error).
     */
    private void observarEstadoAutenticacion() {
        authViewModel.getAuthState().observe(this, estado -> {
            if (estado == null) return;

            // Actualizar UI según estado de carga
            ViewExtensions.setVisible(findViewById(R.id.progress_bar), estado.isLoading());
            ViewExtensions.setEnabled(binding.btnLogin, !estado.isLoading());
            ViewExtensions.setEnabled(binding.btnIrRegistro, !estado.isLoading());
            ViewExtensions.setEnabled(binding.tvContinuarSinCuenta, !estado.isLoading());

            // Manejar estado de éxito - navegar a pantalla principal
            if (estado.isSuccess()) {
                Toast.makeText(this, estado.mensaje, Toast.LENGTH_SHORT).show();
                authViewModel.limpiarEstado();
                sincronizarRecetas();
                irAMainActivity();
            }
            // Manejar estado de error - mostrar mensaje de error
            else if (estado.hasError()) {
                mostrarError(estado.mensaje);
                authViewModel.limpiarEstado();
            }
        });
    }

    /**
     * Sincroniza las recetas del usuario después de un login exitoso.
     * Descarga las recetas desde Firebase y las almacena localmente.
     */
    private void sincronizarRecetas() {
        recetaViewModel.sincronizar();
        Toast.makeText(this, "Sincronizando recetas...", Toast.LENGTH_SHORT).show();
    }

    // ==================== MÉTODOS DE NAVEGACIÓN ====================

    /**
     * Navega a la Activity principal limpiando el stack de actividades.
     * Previene que el usuario pueda volver al login con el botón back.
     */
    private void irAMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Navega a la Activity de registro.
     */
    private void irARegistroActivity() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Obtiene el texto de un EditText de forma segura.
     *
     * @param editText Campo de texto del que obtener el contenido
     * @return Texto contenido o string vacío si es null
     */
    private String obtenerTexto(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }

    /**
     * Muestra un diálogo de error al usuario.
     *
     * @param mensaje Mensaje de error a mostrar
     */
    private void mostrarError(String mensaje) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(mensaje)
                .setPositiveButton("OK", null)
                .show();
    }

    /**
     * Carga el email guardado en las preferencias (si existe).
     * Rellena automáticamente el campo de email si el usuario eligió "recordar".
     */
    private void cargarEmailGuardado() {
        String emailGuardado = preferencias.getString("email_guardado", "");
        if (!emailGuardado.isEmpty()) {
            binding.etEmail.setText(emailGuardado);
            binding.cbRecordar.setChecked(true);
        }
    }

    /**
     * Guarda el email en las preferencias para "recordar credenciales".
     *
     * @param email Email a guardar en preferencias
     */
    private void guardarEmail(String email) {
        preferencias.edit()
                .putString("email_guardado", email)
                .apply();
    }

    /**
     * Elimina el email guardado de las preferencias.
     */
    private void limpiarEmailGuardado() {
        preferencias.edit()
                .remove("email_guardado")
                .apply();
    }

    /**
     * Limpieza de recursos al destruir la Activity.
     * Libera la referencia al binding para evitar memory leaks.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}