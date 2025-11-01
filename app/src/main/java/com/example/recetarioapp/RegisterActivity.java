package com.example.recetarioapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.recetarioapp.databinding.ActivityRegisterBinding;
import com.example.recetarioapp.utils.ViewExtensions;
import com.example.recetarioapp.viewmodels.AuthViewModel;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Activity de Registro con validaciones completas
 */
public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupListeners();
        observeAuthState();
    }

    private void setupListeners() {
        // Botón volver
        binding.btnVolver.setOnClickListener(v -> finish());

        // Botón registrarse
        binding.btnRegistrarse.setOnClickListener(v -> intentarRegistro());

        // Ir a login
        binding.btnIrLogin.setOnClickListener(v -> {
            finish();
        });

        // Enter en último campo = registrar
        binding.etConfirmPassword.setOnEditorActionListener((v, actionId, event) -> {
            intentarRegistro();
            return true;
        });
    }

    private void intentarRegistro() {
        String nombre = getText(binding.etNombre);
        String email = getText(binding.etEmailRegistro);
        String password = getText(binding.etPasswordRegistro);
        String confirmPassword = getText(binding.etConfirmPassword);

        // Validaciones
        if (!validarCampos(nombre, email, password, confirmPassword)) {
            return;
        }

        // Verificar términos
        if (!binding.cbTerminos.isChecked()) {
            Toast.makeText(this, "Debes aceptar los términos y condiciones",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Hacer registro
        authViewModel.registrar(nombre, email, password);
    }

    private boolean validarCampos(String nombre, String email, String password, String confirmPassword) {
        boolean esValido = true;

        // Validar nombre
        if (TextUtils.isEmpty(nombre)) {
            binding.tilNombre.setError("Ingresa tu nombre");
            esValido = false;
        } else if (nombre.length() < 2) {
            binding.tilNombre.setError("Nombre muy corto");
            esValido = false;
        } else {
            binding.tilNombre.setError(null);
        }

        // Validar email
        if (TextUtils.isEmpty(email)) {
            binding.tilEmailRegistro.setError("Ingresa tu email");
            esValido = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmailRegistro.setError("Email inválido");
            esValido = false;
        } else {
            binding.tilEmailRegistro.setError(null);
        }

        // Validar password
        if (TextUtils.isEmpty(password)) {
            binding.tilPasswordRegistro.setError("Ingresa una contraseña");
            esValido = false;
        } else if (password.length() < 6) {
            binding.tilPasswordRegistro.setError("Mínimo 6 caracteres");
            esValido = false;
        } else if (!esPasswordSeguro(password)) {
            binding.tilPasswordRegistro.setError("Usa letras y números");
            esValido = false;
        } else {
            binding.tilPasswordRegistro.setError(null);
        }

        // Validar confirmación
        if (TextUtils.isEmpty(confirmPassword)) {
            binding.tilConfirmPassword.setError("Confirma tu contraseña");
            esValido = false;
        } else if (!password.equals(confirmPassword)) {
            binding.tilConfirmPassword.setError("Las contraseñas no coinciden");
            esValido = false;
        } else {
            binding.tilConfirmPassword.setError(null);
        }

        return esValido;
    }

    private boolean esPasswordSeguro(String password) {
        // Al menos una letra y un número
        return password.matches(".*[a-zA-Z].*") && password.matches(".*\\d.*");
    }

    private void observeAuthState() {
        authViewModel.getAuthState().observe(this, state -> {
            if (state == null) return;

            // Mostrar/ocultar loading
            ViewExtensions.setVisible(findViewById(R.id.progress_bar), state.isLoading());
            ViewExtensions.setEnabled(binding.btnRegistrarse, !state.isLoading());
            ViewExtensions.setEnabled(binding.btnIrLogin, !state.isLoading());

            // Manejar estados
            if (state.isSuccess()) {
                mostrarDialogExito();
                authViewModel.limpiarEstado();
            } else if (state.hasError()) {
                mostrarError(state.mensaje);
                authViewModel.limpiarEstado();
            }
        });
    }

    private void mostrarDialogExito() {
        new AlertDialog.Builder(this)
                .setTitle("¡Registro Exitoso!")
                .setMessage("Tu cuenta ha sido creada correctamente. " +
                        "Ahora puedes empezar a crear y compartir recetas.")
                .setPositiveButton("Continuar", (dialog, which) -> {
                    irAMain();
                })
                .setCancelable(false)
                .show();
    }

    private void irAMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }

    private void mostrarError(String mensaje) {
        new AlertDialog.Builder(this)
                .setTitle("Error en Registro")
                .setMessage(mensaje)
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
