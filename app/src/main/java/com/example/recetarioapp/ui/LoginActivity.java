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

//Activity para inicio de sesión con validaciones completas y soporte para modo anónimo
// - Login tradicional con email/password
// - Modo invitado (continuar sin cuenta)
// - Recordar credenciales
// - Validación en tiempo real
// - Sincronización automática de datos después del login
public class LoginActivity extends AppCompatActivity {

    //ViewBinding para acceso type-safe a las vistas del layout
    private ActivityLoginBinding binding;

    //ViewModels para gestión de autenticación y datos
    private AuthViewModel authViewModel;
    private RecetaViewModel recetaViewModel;

    //Preferencias para persistencia de datos de usuario
    private SharedPreferences preferencias;

    //Método principal de inicialización de la Activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Inflar el layout usando ViewBinding
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Inicializar ViewModels y preferencias
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        recetaViewModel = new ViewModelProvider(this).get(RecetaViewModel.class);
        preferencias = getSharedPreferences("RecetarioPrefs", MODE_PRIVATE);

        //Verificar si ya existe una sesión (autenticada o anónima)
        if (authViewModel.isUserLoggedInOrAnonymous()) {
            sincronizarRecetas();
            irAMainActivity();
            return;
        }

        //Configurar componentes de la UI
        cargarEmailGuardado();
        configurarListeners();
        observarEstadoAutenticacion();
    }

    //Configura todos los listeners de interacción del usuario
    private void configurarListeners() {
        //Botón de login tradicional
        binding.btnLogin.setOnClickListener(v -> intentarLogin());

        //Navegación a pantalla de registro
        binding.btnIrRegistro.setOnClickListener(v -> irARegistroActivity());

        //Opción de modo invitado
        binding.tvContinuarSinCuenta.setOnClickListener(v -> activarModoAnonimo());

        //Login con tecla Enter en campo de password
        binding.etPassword.setOnEditorActionListener((v, actionId, event) -> {
            intentarLogin();
            return true;
        });
    }

    //Activa el modo anónimo/invitado y navega a la pantalla principal
    private void activarModoAnonimo() {
        authViewModel.activarModoAnonimo();
        irAMainActivity();
    }

    //Intenta realizar el proceso de login después de validar los campos
    private void intentarLogin() {
        String email = obtenerTexto(binding.etEmail);
        String password = obtenerTexto(binding.etPassword);

        //Validar campos antes de proceder con el login
        if (!validarCampos(email, password)) {
            return;
        }

        //Limpiar modo anónimo previo si existe
        limpiarModoAnonimo();

        //Delegar autenticación al ViewModel
        authViewModel.login(email, password);
    }

    //Limpia las preferencias de modo anónimo al iniciar sesión tradicional
    private void limpiarModoAnonimo() {
        if (esUsuarioAnonimo()) {
            SharedPreferences.Editor editor = preferencias.edit();
            editor.remove("es_usuario_anonimo");
            editor.remove("user_mode");
            editor.remove("fecha_anonimo");
            editor.apply();
        }
    }

    //Verifica si el usuario actual está en modo anónimo
    private boolean esUsuarioAnonimo() {
        return preferencias.getBoolean("es_usuario_anonimo", false);
    }

    //Valida los campos de email y password en tiempo real
    private boolean validarCampos(String email, String password) {
        boolean esValido = true;

        //Validación de email - formato de correo electrónico válido
        if (TextUtils.isEmpty(email)) {
            binding.tilEmail.setError("Ingresa tu email");
            esValido = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("Email inválido");
            esValido = false;
        } else {
            binding.tilEmail.setError(null);
        }

        //Validación de password - no vacío
        if (TextUtils.isEmpty(password)) {
            binding.tilPassword.setError("Ingresa tu contraseña");
            esValido = false;
        } else {
            binding.tilPassword.setError(null);
        }

        return esValido;
    }

    //Observa los cambios en el estado de autenticación del ViewModel
    private void observarEstadoAutenticacion() {
        authViewModel.getAuthState().observe(this, estado -> {
            if (estado == null) return;

            //Actualizar UI según estado de carga
            ViewExtensions.setVisible(findViewById(R.id.progress_bar), estado.isLoading());
            ViewExtensions.setEnabled(binding.btnLogin, !estado.isLoading());
            ViewExtensions.setEnabled(binding.btnIrRegistro, !estado.isLoading());
            ViewExtensions.setEnabled(binding.tvContinuarSinCuenta, !estado.isLoading());

            //Manejar estado de éxito - navegar a pantalla principal
            if (estado.isSuccess()) {
                Toast.makeText(this, estado.mensaje, Toast.LENGTH_SHORT).show();
                authViewModel.limpiarEstado();
                sincronizarRecetas();
                irAMainActivity();
            }
            //Manejar estado de error - mostrar mensaje de error
            else if (estado.hasError()) {
                mostrarError(estado.mensaje);
                authViewModel.limpiarEstado();
            }
        });
    }

    //Sincroniza las recetas del usuario después de un login exitoso
    private void sincronizarRecetas() {
        recetaViewModel.sincronizar();
        Toast.makeText(this, "Sincronizando recetas...", Toast.LENGTH_SHORT).show();
    }

    //==================== MÉTODOS DE NAVEGACIÓN ====================

    //Navega a la Activity principal limpiando el stack de actividades
    private void irAMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    //Navega a la Activity de registro
    private void irARegistroActivity() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    //==================== MÉTODOS AUXILIARES ====================

    //Obtiene el texto de un EditText de forma segura
    private String obtenerTexto(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }

    //Muestra un diálogo de error al usuario
    private void mostrarError(String mensaje) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(mensaje)
                .setPositiveButton("OK", null)
                .show();
    }

    //Carga el email guardado en las preferencias (si existe)
    private void cargarEmailGuardado() {
        String emailGuardado = preferencias.getString("email_guardado", "");
        if (!emailGuardado.isEmpty()) {
            binding.etEmail.setText(emailGuardado);
        }
    }

    //Limpieza de recursos al destruir la Activity
    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}