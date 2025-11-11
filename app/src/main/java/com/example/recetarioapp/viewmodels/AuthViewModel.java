package com.example.recetarioapp.viewmodels;

import static android.content.Context.MODE_PRIVATE;

import android.app.Application;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.recetarioapp.models.Usuario;
import com.example.recetarioapp.repository.AuthRepository;

/**
 * ViewModel para gestionar la autenticación y estado del usuario.
 *
 * Coordina todas las operaciones relacionadas con autenticación: registro, login,
 * logout, recuperación de contraseña y gestión del modo anónimo.
 * Proporciona un estado observable unificado que permite a la UI reaccionar
 * consistentemente a los cambios en el estado de autenticación.
 */
public class AuthViewModel extends AndroidViewModel {

    // Dependencias
    private final AuthRepository repositorioAuth;
    private final SharedPreferences preferencias;

    // Estado observable de autenticación
    private final MutableLiveData<AuthState> estadoAuth = new MutableLiveData<>(AuthState.idle());

    /**
     * Constructor que inicializa el ViewModel con las dependencias necesarias.
     *
     * @param app Contexto de la aplicación para inicializar Repository y SharedPreferences
     */
    public AuthViewModel(@NonNull Application app) {
        super(app);
        this.repositorioAuth = new AuthRepository(app);
        this.preferencias = app.getSharedPreferences("RecetarioPrefs", MODE_PRIVATE);
    }

    // ==================== OPERACIONES DE AUTENTICACIÓN ====================

    /**
     * Registra un nuevo usuario en el sistema.
     * Maneja el flujo completo de registro incluyendo limpieza de modo anónimo.
     *
     * @param nombre Nombre display del usuario
     * @param email Email para autenticación
     * @param password Contraseña del usuario
     */
    public void registrar(String nombre, String email, String password) {
        estadoAuth.setValue(AuthState.loading());

        repositorioAuth.registrarUsuario(nombre, email, password,
                new AuthRepository.OnAuthListener() {
                    @Override
                    public void onSuccess(Usuario usuario) {
                        // Limpiar modo anónimo al registrarse exitosamente
                        limpiarModoAnonimo();
                        estadoAuth.postValue(AuthState.success("Registro exitoso"));
                    }

                    @Override
                    public void onError(String mensaje) {
                        estadoAuth.postValue(AuthState.error(mensaje));
                    }
                });
    }

    /**
     * Inicia sesión con credenciales de usuario.
     * Maneja autenticación y transición desde modo anónimo.
     *
     * @param email Email del usuario
     * @param password Contraseña del usuario
     */
    public void login(String email, String password) {
        estadoAuth.setValue(AuthState.loading());

        repositorioAuth.loginUsuario(email, password,
                new AuthRepository.OnAuthListener() {
                    @Override
                    public void onSuccess(Usuario usuario) {
                        // Limpiar modo anónimo al iniciar sesión exitosamente
                        limpiarModoAnonimo();
                        estadoAuth.postValue(AuthState.success("Inicio de sesión exitoso"));
                    }

                    @Override
                    public void onError(String mensaje) {
                        estadoAuth.postValue(AuthState.error(mensaje));
                    }
                });
    }

    // ==================== GESTIÓN DE MODO ANÓNIMO ====================

    /**
     * Activa el modo anónimo para usuarios invitados.
     * Almacena el estado en SharedPreferences para persistencia.
     */
    public void activarModoAnonimo() {
        SharedPreferences.Editor editor = preferencias.edit();
        editor.putBoolean("es_usuario_anonimo", true);
        editor.putString("user_mode", "anonimo");
        editor.putLong("fecha_anonimo", System.currentTimeMillis());
        editor.apply();

        estadoAuth.setValue(AuthState.success("Modo invitado activado"));
    }

    /**
     * Limpia el estado de modo anónimo.
     * Usado al registrar o loguear usuarios autenticados.
     */
    public void limpiarModoAnonimo() {
        SharedPreferences.Editor editor = preferencias.edit();
        editor.remove("es_usuario_anonimo");
        editor.remove("user_mode");
        editor.remove("fecha_anonimo");
        editor.apply();
    }

    /**
     * Verifica si el usuario actual está en modo anónimo.
     *
     * @return true si es usuario anónimo, false en caso contrario
     */
    public boolean esUsuarioAnonimo() {
        return preferencias.getBoolean("es_usuario_anonimo", false);
    }

    // ==================== CONSULTAS DE ESTADO ====================

    /**
     * Verifica si hay un usuario autenticado (no anónimo).
     *
     * @return true si hay usuario autenticado, false en caso contrario
     */
    public boolean isUserLoggedIn() {
        return repositorioAuth.isUserLoggedIn();
    }

    /**
     * Verifica si hay un usuario autenticado O en modo anónimo.
     *
     * @return true si hay algún tipo de sesión activa
     */
    public boolean isUserLoggedInOrAnonymous() {
        return repositorioAuth.isUserLoggedIn() || esUsuarioAnonimo();
    }

    /**
     * Obtiene el usuario actual como LiveData observable.
     *
     * @return LiveData con el usuario actual o null
     */
    public LiveData<Usuario> getUsuarioActual() {
        return repositorioAuth.getUsuarioActual();
    }

    // ==================== OBSERVABLES PARA LA UI ====================

    /**
     * Obtiene el estado de autenticación como LiveData observable.
     *
     * @return LiveData con el estado actual de autenticación
     */
    public LiveData<AuthState> getAuthState() {
        return estadoAuth;
    }

    /**
     * Limpia el estado de autenticación (útil después de mostrar mensajes).
     */
    public void limpiarEstado() {
        estadoAuth.setValue(AuthState.idle());
    }

    // ==================== CLASE DE ESTADO INTERNA ====================

    /**
     * Clase que representa el estado de autenticación con patrón de estado.
     * Permite a la UI reaccionar consistentemente a diferentes estados.
     */
    public static class AuthState {
        /**
         * Estados posibles del flujo de autenticación.
         */
        public enum Status {
            IDLE,       // Estado inicial/inactivo
            LOADING,    // Operación en progreso
            SUCCESS,    // Operación completada exitosamente
            ERROR       // Operación falló
        }

        // Atributos del estado
        public final Status status;
        public final String mensaje;

        /**
         * Constructor privado para forzar uso de métodos factory.
         *
         * @param status Estado de la operación
         * @param mensaje Mensaje descriptivo (opcional)
         */
        private AuthState(Status status, String mensaje) {
            this.status = status;
            this.mensaje = mensaje;
        }

        // ==================== MÉTODOS FACTORY ====================

        /**
         * Crea estado inicial/inactivo.
         *
         * @return Estado AuthState en modo IDLE
         */
        public static AuthState idle() {
            return new AuthState(Status.IDLE, null);
        }

        /**
         * Crea estado de carga/operación en progreso.
         *
         * @return Estado AuthState en modo LOADING
         */
        public static AuthState loading() {
            return new AuthState(Status.LOADING, null);
        }

        /**
         * Crea estado de éxito con mensaje opcional.
         *
         * @param mensaje Mensaje de éxito (opcional)
         * @return Estado AuthState en modo SUCCESS
         */
        public static AuthState success(String mensaje) {
            return new AuthState(Status.SUCCESS, mensaje);
        }

        /**
         * Crea estado de error con mensaje descriptivo.
         *
         * @param mensaje Mensaje de error descriptivo
         * @return Estado AuthState en modo ERROR
         */
        public static AuthState error(String mensaje) {
            return new AuthState(Status.ERROR, mensaje);
        }

        // ==================== MÉTODOS DE CONSULTA ====================

        /**
         * Verifica si el estado es de carga.
         *
         * @return true si está cargando, false en caso contrario
         */
        public boolean isLoading() {
            return status == Status.LOADING;
        }

        /**
         * Verifica si el estado es de éxito.
         *
         * @return true si la operación fue exitosa, false en caso contrario
         */
        public boolean isSuccess() {
            return status == Status.SUCCESS;
        }

        /**
         * Verifica si el estado es de error.
         *
         * @return true si hubo un error, false en caso contrario
         */
        public boolean hasError() {
            return status == Status.ERROR;
        }
    }
}