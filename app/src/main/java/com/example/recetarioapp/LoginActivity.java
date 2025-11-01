package com.example.recetarioapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.recetarioapp.databinding.ActivityLoginBinding;
import com.example.recetarioapp.utils.ViewExtensions;
import com.example.recetarioapp.viewmodels.AuthViewModel;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Activity de Login con validaciones y estado
 */
public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private AuthViewModel authViewModel;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializar
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        prefs = getSharedPreferences("RecetarioPrefs", MODE_PRIVATE);

        // Verificar si ya hay sesión (incluyendo modo anónimo)
        if (authViewModel.isUserLoggedInOrAnonymous()) {
            irAMain();
            return;
        }

        // Cargar email guardado si existe
        cargarEmailGuardado();

        // Configurar listeners
        setupListeners();

        // Observar estados
        observeAuthState();
    }

    private void setupListeners() {
        // Botón Login
        binding.btnLogin.setOnClickListener(v -> intentarLogin());

        // Botón Registro
        binding.btnIrRegistro.setOnClickListener(v -> irARegistro());

        // Continuar sin cuenta
        binding.tvContinuarSinCuenta.setOnClickListener(v -> continuarSinCuenta());

        // Olvidé contraseña
        // binding.tvOlvidePassword.setOnClickListener(v -> mostrarDialogRecuperarPassword());

        // Enter en password = login
        binding.etPassword.setOnEditorActionListener((v, actionId, event) -> {
            intentarLogin();
            return true;
        });
    }

    private void continuarSinCuenta() {
        // Usar el ViewModel en lugar de SharedPreferences directamente
        authViewModel.activarModoAnonimo();

        // Ir a MainActivity
        irAMain();
    }

    private boolean esUsuarioAnonimo() {
        return prefs.getBoolean("es_usuario_anonimo", false);
    }

    private void intentarLogin() {
        String email = getText(binding.etEmail);
        String password = getText(binding.etPassword);

        // Validaciones
        if (!validarCampos(email, password)) {
            return;
        }

        // Guardar email si está marcado "recordar"
        if (binding.cbRecordar.isChecked()) {
            guardarEmail(email);
        } else {
            limpiarEmail();
        }

        // Limpiar modo anónimo si existe (por si vuelve a loguearse)
        limpiarModoAnonimo();

        // Hacer login
        authViewModel.login(email, password);
    }

    private void limpiarModoAnonimo() {
        if (esUsuarioAnonimo()) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove("es_usuario_anonimo");
            editor.remove("user_mode");
            editor.remove("fecha_anonimo");
            editor.apply();
        }
    }

    private boolean validarCampos(String email, String password) {
        boolean esValido = true;

        // Validar email
        if (TextUtils.isEmpty(email)) {
            binding.tilEmail.setError("Ingresa tu email");
            esValido = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("Email inválido");
            esValido = false;
        } else {
            binding.tilEmail.setError(null);
        }

        // Validar password
        if (TextUtils.isEmpty(password)) {
            binding.tilPassword.setError("Ingresa tu contraseña");
            esValido = false;
        } else {
            binding.tilPassword.setError(null);
        }

        return esValido;
    }

    private void observeAuthState() {
        authViewModel.getAuthState().observe(this, state -> {
            if (state == null) return;

            // Mostrar/ocultar loading
            ViewExtensions.setVisible(findViewById(R.id.progress_bar), state.isLoading());
            ViewExtensions.setEnabled(binding.btnLogin, !state.isLoading());
            ViewExtensions.setEnabled(binding.btnIrRegistro, !state.isLoading());
            ViewExtensions.setEnabled(binding.tvContinuarSinCuenta, !state.isLoading());

            // Manejar estados
            if (state.isSuccess()) {
                Toast.makeText(this, state.mensaje, Toast.LENGTH_SHORT).show();
                authViewModel.limpiarEstado();
                irAMain();
            } else if (state.hasError()) {
                mostrarError(state.mensaje);
                authViewModel.limpiarEstado();
            }
        });
    }

//    private void mostrarDialogRecuperarPassword() {
//        View dialogView = getLayoutInflater().inflate(
//                R.layout.dialog_recuperar_password, null);
//
//        TextInputEditText etEmailRecuperar = dialogView.findViewById(R.id.et_email_recuperar);
//
//        new AlertDialog.Builder(this)
//                .setTitle("Recuperar Contraseña")
//                .setMessage("Ingresa tu email y te enviaremos un enlace para restablecer tu contraseña")
//                .setView(dialogView)
//                .setPositiveButton("Enviar", (dialog, which) -> {
//                    String email = etEmailRecuperar.getText().toString().trim();
//                    if (!TextUtils.isEmpty(email)) {
//                        authViewModel.recuperarPassword(email);
//                    } else {
//                        Toast.makeText(this, "Ingresa un email", Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .setNegativeButton("Cancelar", null)
//                .show();
//    }

    // ===== NAVEGACIÓN =====

    private void irAMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void irARegistro() {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    // ===== HELPERS =====

    private String getText(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }

    private void mostrarError(String mensaje) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(mensaje)
                .setPositiveButton("OK", null)
                .show();
    }

    private void cargarEmailGuardado() {
        String emailGuardado = prefs.getString("email_guardado", "");
        if (!emailGuardado.isEmpty()) {
            binding.etEmail.setText(emailGuardado);
            binding.cbRecordar.setChecked(true);
        }
    }

    private void guardarEmail(String email) {
        prefs.edit().putString("email_guardado", email).apply();
    }

    private void limpiarEmail() {
        prefs.edit().remove("email_guardado").apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}