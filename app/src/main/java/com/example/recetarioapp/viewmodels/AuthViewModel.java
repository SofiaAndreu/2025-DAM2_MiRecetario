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
 * ViewModel para gestionar autenticación
 */
public class AuthViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;
    private final MutableLiveData<AuthState> authState = new MutableLiveData<>(AuthState.idle());
    private final SharedPreferences prefs;

    public AuthViewModel(@NonNull Application app) {
        super(app);
        this.authRepository = new AuthRepository(app);
        this.prefs = app.getSharedPreferences("RecetarioPrefs", MODE_PRIVATE);
    }

    // ===== REGISTRO =====
    public void registrar(String nombre, String email, String password) {
        authState.setValue(AuthState.loading());

        authRepository.registrarUsuario(nombre, email, password,
                new AuthRepository.OnAuthListener() {
                    @Override
                    public void onSuccess(Usuario usuario) {
                        // Limpiar modo anónimo al registrarse
                        limpiarModoAnonimo();
                        authState.postValue(AuthState.success("Registro exitoso"));
                    }

                    @Override
                    public void onError(String mensaje) {
                        authState.postValue(AuthState.error(mensaje));
                    }
                });
    }

    // ===== LOGIN =====
    public void login(String email, String password) {
        authState.setValue(AuthState.loading());

        authRepository.loginUsuario(email, password,
                new AuthRepository.OnAuthListener() {
                    @Override
                    public void onSuccess(Usuario usuario) {
                        // Limpiar modo anónimo al loguearse
                        limpiarModoAnonimo();
                        authState.postValue(AuthState.success("Inicio de sesión exitoso"));
                    }

                    @Override
                    public void onError(String mensaje) {
                        authState.postValue(AuthState.error(mensaje));
                    }
                });
    }

    // ===== MODO ANÓNIMO =====
    public void activarModoAnonimo() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("es_usuario_anonimo", true);
        editor.putString("user_mode", "anonimo");
        editor.putLong("fecha_anonimo", System.currentTimeMillis());
        editor.apply();

        authState.setValue(AuthState.success("Modo invitado activado"));
    }

    public void limpiarModoAnonimo() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("es_usuario_anonimo");
        editor.remove("user_mode");
        editor.remove("fecha_anonimo");
        editor.apply();
    }

    public boolean esUsuarioAnonimo() {
        return prefs.getBoolean("es_usuario_anonimo", false);
    }

    // ===== LOGOUT =====
    public void logout() {
        authRepository.logout();
        // También limpiar modo anónimo al cerrar sesión
        limpiarModoAnonimo();
        authState.setValue(AuthState.idle());
    }

    // ===== RECUPERAR CONTRASEÑA =====
    public void recuperarPassword(String email) {
        authState.setValue(AuthState.loading());

        authRepository.recuperarPassword(email,
                new AuthRepository.OnPasswordResetListener() {
                    @Override
                    public void onSuccess() {
                        authState.postValue(AuthState.success(
                                "Email de recuperación enviado. Revisa tu bandeja de entrada"));
                    }

                    @Override
                    public void onError(String mensaje) {
                        authState.postValue(AuthState.error(mensaje));
                    }
                });
    }

    // ===== VERIFICACIONES =====
    public boolean isUserLoggedIn() {
        return authRepository.isUserLoggedIn();
    }

    public boolean isUserLoggedInOrAnonymous() {
        return authRepository.isUserLoggedIn() || esUsuarioAnonimo();
    }

    public LiveData<Usuario> getUsuarioActual() {
        return authRepository.getUsuarioActual();
    }

    // ===== OBSERVABLES =====
    public LiveData<AuthState> getAuthState() {
        return authState;
    }

    public void limpiarEstado() {
        authState.setValue(AuthState.idle());
    }

    // ===== CLASE DE ESTADO =====
    public static class AuthState {
        public enum Status { IDLE, LOADING, SUCCESS, ERROR }

        public final Status status;
        public final String mensaje;

        private AuthState(Status status, String mensaje) {
            this.status = status;
            this.mensaje = mensaje;
        }

        public static AuthState idle() {
            return new AuthState(Status.IDLE, null);
        }

        public static AuthState loading() {
            return new AuthState(Status.LOADING, null);
        }

        public static AuthState success(String mensaje) {
            return new AuthState(Status.SUCCESS, mensaje);
        }

        public static AuthState error(String mensaje) {
            return new AuthState(Status.ERROR, mensaje);
        }

        public boolean isLoading() {
            return status == Status.LOADING;
        }

        public boolean isSuccess() {
            return status == Status.SUCCESS;
        }

        public boolean hasError() {
            return status == Status.ERROR;
        }
    }
}