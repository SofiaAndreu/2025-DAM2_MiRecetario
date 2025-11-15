package com.example.recetarioapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.recetarioapp.R;
import com.example.recetarioapp.databinding.ActivityRegisterBinding;
import com.example.recetarioapp.utils.ViewExtensions;
import com.example.recetarioapp.viewmodels.AuthViewModel;
import com.google.android.material.textfield.TextInputEditText;

//Activity para registro de nuevos usuarios con validaciones completas
// - Validación en tiempo real de todos los campos
// - Verificación de seguridad de contraseña
// - Aceptación de términos y condiciones
// - Integración con Firebase Authentication
// - Redirección automática al completar registro
public class RegisterActivity extends AppCompatActivity {

    //Binding para acceso type-safe a las vistas del layout
    private ActivityRegisterBinding binding;

    //ViewModel para gestión de autenticación y registro
    private AuthViewModel authViewModel;

    //Método de inicialización de la Activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Inflar layout usando ViewBinding
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Inicializar ViewModel para autenticación
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        configurarListeners();
        observarEstadoAutenticacion();
    }

    //Configura los listeners de interacción del usuario
    private void configurarListeners() {
        //Botón para volver atrás
        binding.btnVolver.setOnClickListener(v -> finish());

        //Botón principal de registro
        binding.btnRegistrarse.setOnClickListener(v -> intentarRegistro());

        //Enlace para ir a login
        binding.btnIrLogin.setOnClickListener(v -> finish());

        //Acción de teclado para confirmar contraseña
        binding.etConfirmPassword.setOnEditorActionListener((v, actionId, event) -> {
            intentarRegistro();
            return true;
        });
    }

    //Intenta realizar el registro después de validar todos los campos
    private void intentarRegistro() {
        String nombre = obtenerTexto(binding.etNombre);
        String email = obtenerTexto(binding.etEmailRegistro);
        String password = obtenerTexto(binding.etPasswordRegistro);
        String confirmPassword = obtenerTexto(binding.etConfirmPassword);

        //Validar campos antes de proceder con el registro
        if (!validarCampos(nombre, email, password, confirmPassword)) {
            return;
        }

        //Verificar aceptación de términos y condiciones
        if (!binding.cbTerminos.isChecked()) {
            Toast.makeText(this, "Debes aceptar los términos y condiciones", Toast.LENGTH_SHORT).show();
            return;
        }

        //Iniciar proceso de registro con el ViewModel
        authViewModel.registrar(nombre, email, password);
    }

    //Valida todos los campos del formulario de registro
    private boolean validarCampos(String nombre, String email, String password, String confirmPassword) {
        boolean esValido = true;

        //Validación de nombre - debe tener al menos 2 caracteres
        if (TextUtils.isEmpty(nombre)) {
            binding.tilNombre.setError("Ingresa tu nombre");
            esValido = false;
        } else if (nombre.length() < 2) {
            binding.tilNombre.setError("Nombre muy corto");
            esValido = false;
        } else {
            binding.tilNombre.setError(null);
        }

        //Validación de email - formato de correo electrónico válido
        if (TextUtils.isEmpty(email)) {
            binding.tilEmailRegistro.setError("Ingresa tu email");
            esValido = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmailRegistro.setError("Email inválido");
            esValido = false;
        } else {
            binding.tilEmailRegistro.setError(null);
        }

        //Validación de password - mínimo 6 caracteres y segura
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

        //Validación de confirmación de password - debe coincidir
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

    //Verifica que la contraseña cumpla con requisitos básicos de seguridad
    private boolean esPasswordSeguro(String password) {
        return password.matches(".*[a-zA-Z].*") && password.matches(".*\\d.*");
    }

    //Observa los cambios en el estado de autenticación del ViewModel
    private void observarEstadoAutenticacion() {
        authViewModel.getAuthState().observe(this, estado -> {
            if (estado == null) return;

            //Actualizar UI según estado de carga
            ViewExtensions.setVisible(findViewById(R.id.progress_bar), estado.isLoading());
            ViewExtensions.setEnabled(binding.btnRegistrarse, !estado.isLoading());
            ViewExtensions.setEnabled(binding.btnIrLogin, !estado.isLoading());

            //Manejar estado de éxito - mostrar diálogo de confirmación
            if (estado.isSuccess()) {
                mostrarDialogExito();
                authViewModel.limpiarEstado();
            }
            //Manejar estado de error - mostrar mensaje de error
            else if (estado.hasError()) {
                mostrarError(estado.mensaje);
                authViewModel.limpiarEstado();
            }
        });
    }

    //Muestra diálogo de registro exitoso
    private void mostrarDialogExito() {
        new AlertDialog.Builder(this)
                .setTitle("¡Registro Exitoso!")
                .setMessage("Tu cuenta ha sido creada correctamente. " +
                        "Ahora puedes empezar a crear y compartir recetas.")
                .setPositiveButton("Continuar", (dialog, which) -> irAMainActivity())
                .setCancelable(false)
                .show();
    }

    //Navega a la Activity principal de la aplicación
    private void irAMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    //Obtiene texto de forma segura de un EditText
    private String obtenerTexto(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }

    //Muestra diálogo de error con el mensaje proporcionado
    private void mostrarError(String mensaje) {
        new AlertDialog.Builder(this)
                .setTitle("Error en Registro")
                .setMessage(mensaje)
                .setPositiveButton("OK", null)
                .show();
    }

    //Limpieza de recursos al destruir la Activity
    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}