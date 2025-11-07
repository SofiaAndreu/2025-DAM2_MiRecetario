package com.example.recetarioapp;

// -- IMPORTACIONES --
import android.content.Intent; //Navegación entre actividades
import android.os.Bundle; //Manejo de estado
import android.text.TextUtils; //Utils para texto
import android.widget.Toast; //Mensajes breves
import androidx.appcompat.app.AlertDialog; //Dialogos de Alertas
import androidx.appcompat.app.AppCompatActivity;  // Activity base
import androidx.lifecycle.ViewModelProvider;      // Para ViewModel
import com.example.recetarioapp.databinding.ActivityRegisterBinding;  // ViewBinding
import com.example.recetarioapp.utils.ViewExtensions;  // Utilidades para vistas
import com.example.recetarioapp.viewmodels.AuthViewModel;  // ViewModel auth
import com.google.android.material.textfield.TextInputEditText;  // Campo texto material

/**
 * Activity para registro de nuevos usuarios con validaciones completas
 */
public class RegisterActivity extends AppCompatActivity {

    // Binding para acceso seguro a vistas del layout
    private ActivityRegisterBinding binding;
    // ViewModel para gestionar lógica de autenticación
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);  // Llama método padre
        // Infla el layout usando ViewBinding
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        // Establece la vista raíz del binding
        setContentView(binding.getRoot());

        // Obtiene instancia del ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        setupListeners();    // Configura listeners de botones
        observeAuthState();  // Observa cambios en estado de auth
    }

    /**
     * Configura todos los listeners de la interfaz de usuario
     */
    private void setupListeners() {
        // Botón volver - cierra esta actividad
        binding.btnVolver.setOnClickListener(v -> finish());

        // Botón registrarse - inicia proceso de registro
        binding.btnRegistrarse.setOnClickListener(v -> intentarRegistro());

        // Botón ir a login - cierra esta actividad (volverá a login)
        binding.btnIrLogin.setOnClickListener(v -> finish());

        // Listener para tecla Enter en confirmar password
        binding.etConfirmPassword.setOnEditorActionListener((v, actionId, event) -> {
            intentarRegistro();  // Intenta registrar al presionar Enter
            return true;         // Indica que el evento fue manejado
        });
    }

    /**
     * Intenta realizar el registro después de validar todos los campos
     */
    private void intentarRegistro() {
        // Obtiene y limpia texto de todos los campos
        String nombre = getText(binding.etNombre);
        String email = getText(binding.etEmailRegistro);
        String password = getText(binding.etPasswordRegistro);
        String confirmPassword = getText(binding.etConfirmPassword);

        // Valida campos, si hay error se detiene aquí
        if (!validarCampos(nombre, email, password, confirmPassword)) {
            return;  // Sale si validación falla
        }

        // Verifica si checkbox de términos está marcado
        if (!binding.cbTerminos.isChecked()) {
            // Muestra toast si no aceptó términos
            Toast.makeText(this, "Debes aceptar los términos y condiciones",
                    Toast.LENGTH_SHORT).show();
            return;  // Sale si no aceptó términos
        }

        // Llama al ViewModel para realizar registro
        authViewModel.registrar(nombre, email, password);
    }

    /**
     * Valida todos los campos del formulario de registro
     */
    private boolean validarCampos(String nombre, String email, String password, String confirmPassword) {
        boolean esValido = true;  // Bandera de validación general

        // VALIDACIÓN NOMBRE
        if (TextUtils.isEmpty(nombre)) {
            binding.tilNombre.setError("Ingresa tu nombre");  // Error campo vacío
            esValido = false;  // Marca como inválido
        } else if (nombre.length() < 2) {
            binding.tilNombre.setError("Nombre muy corto");   // Error longitud
            esValido = false;  // Marca como inválido
        } else {
            binding.tilNombre.setError(null);  // Limpia error si es válido
        }

        // VALIDACIÓN EMAIL
        if (TextUtils.isEmpty(email)) {
            binding.tilEmailRegistro.setError("Ingresa tu email");
            esValido = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmailRegistro.setError("Email inválido");  // Patrón email
            esValido = false;
        } else {
            binding.tilEmailRegistro.setError(null);
        }

        // VALIDACIÓN PASSWORD
        if (TextUtils.isEmpty(password)) {
            binding.tilPasswordRegistro.setError("Ingresa una contraseña");
            esValido = false;
        } else if (password.length() < 6) {
            binding.tilPasswordRegistro.setError("Mínimo 6 caracteres");  // Longitud
            esValido = false;
        } else if (!esPasswordSeguro(password)) {
            binding.tilPasswordRegistro.setError("Usa letras y números");  // Seguridad
            esValido = false;
        } else {
            binding.tilPasswordRegistro.setError(null);
        }

        // VALIDACIÓN CONFIRMACIÓN PASSWORD
        if (TextUtils.isEmpty(confirmPassword)) {
            binding.tilConfirmPassword.setError("Confirma tu contraseña");
            esValido = false;
        } else if (!password.equals(confirmPassword)) {
            binding.tilConfirmPassword.setError("Las contraseñas no coinciden");  // Match
            esValido = false;
        } else {
            binding.tilConfirmPassword.setError(null);
        }

        return esValido;  // Retorna resultado de validación
    }

    /**
     * Verifica seguridad básica de contraseña
     */
    private boolean esPasswordSeguro(String password) {
        // Regex: debe contener al menos una letra y un número
        return password.matches(".*[a-zA-Z].*") && password.matches(".*\\d.*");
    }

    /**
     * Observa cambios en el estado de autenticación del ViewModel
     */
    private void observeAuthState() {
        authViewModel.getAuthState().observe(this, state -> {
            if (state == null) return;  // Sale si estado es nulo

            // Controla visibilidad del progress bar
            ViewExtensions.setVisible(findViewById(R.id.progress_bar), state.isLoading());
            // Habilita/deshabilita botones según loading state
            ViewExtensions.setEnabled(binding.btnRegistrarse, !state.isLoading());
            ViewExtensions.setEnabled(binding.btnIrLogin, !state.isLoading());

            // Maneja estado de éxito
            if (state.isSuccess()) {
                mostrarDialogExito();    // Muestra diálogo éxito
                authViewModel.limpiarEstado();  // Limpia estado del ViewModel
            }
            // Maneja estado de error
            else if (state.hasError()) {
                mostrarError(state.mensaje);    // Muestra error
                authViewModel.limpiarEstado();  // Limpia estado
            }
        });
    }

    /**
     * Muestra diálogo de registro exitoso
     */
    private void mostrarDialogExito() {
        new AlertDialog.Builder(this)
                .setTitle("¡Registro Exitoso!")  // Título del diálogo
                .setMessage("Tu cuenta ha sido creada correctamente. " +
                        "Ahora puedes empezar a crear y compartir recetas.")  // Mensaje
                .setPositiveButton("Continuar", (dialog, which) -> {
                    irAMain();  // Navega a Main al hacer clic
                })
                .setCancelable(false)  // Evita que usuario cierre con back button
                .show();  // Muestra el diálogo
    }

    /**
     * Navega a MainActivity limpiando el stack de actividades
     */
    private void irAMain() {
        Intent intent = new Intent(this, MainActivity.class);  // Crea intent
        // Flags para limpiar stack y crear nueva tarea
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);  // Inicia actividad
        finish();               // Cierra esta actividad
    }

    /**
     * Obtiene texto de forma segura de un EditText
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
                .setTitle("Error en Registro")  // Título error
                .setMessage(mensaje)            // Mensaje del error
                .setPositiveButton("OK", null)  // Botón OK sin acción
                .show();  // Muestra diálogo
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();  // Llama método padre
        binding = null;     // Libera referencia del binding para evitar memory leaks
    }
}