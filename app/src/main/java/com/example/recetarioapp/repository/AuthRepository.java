package com.example.recetarioapp.repository;

import com.example.recetarioapp.database.RecetasBD;
import com.example.recetarioapp.database.UsuarioDAO;

import android.app.Application;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.recetarioapp.models.Usuario;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * Repository para operaciones de autenticación
 */
public class AuthRepository {

    private static final String TAG = "AuthRepository";
    private static final String USUARIOS_COLLECTION = "usuarios";

    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;
    private final UsuarioDAO usuarioDAO;
    private final MutableLiveData<Usuario> usuarioActual = new MutableLiveData<>();

    public AuthRepository(Application app) {
        this.auth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();

        RecetasBD bd = RecetasBD.getInstance(app);
        this.usuarioDAO = bd.usuarioDAO();

        // Cargar usuario actual si existe
        cargarUsuarioActual();
    }

    // ===== REGISTRO =====
    public void registrarUsuario(String nombre, String email, String password,
                                 OnAuthListener listener) {

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser == null) {
                        listener.onError("Error al crear usuario");
                        return;
                    }

                    // Actualizar perfil con nombre
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(nombre)
                            .build();

                    firebaseUser.updateProfile(profileUpdates)
                            .addOnSuccessListener(aVoid -> {
                                // Crear usuario en Firestore y Room
                                Usuario usuario = new Usuario(
                                        firebaseUser.getUid(),
                                        nombre,
                                        email
                                );

                                guardarUsuarioEnFirestore(usuario, listener);
                            })
                            .addOnFailureListener(e -> {
                                listener.onError("Error al actualizar perfil: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    listener.onError(parseAuthError(e.getMessage()));
                });
    }

    // ===== LOGIN =====
    public void loginUsuario(String email, String password, OnAuthListener listener) {

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser == null) {
                        listener.onError("Error al iniciar sesión");
                        return;
                    }

                    // Cargar datos del usuario desde Firestore
                    cargarUsuarioDesdeFirestore(firebaseUser.getUid(), listener);
                })
                .addOnFailureListener(e -> {
                    listener.onError(parseAuthError(e.getMessage()));
                });
    }

    // ===== LOGOUT =====
    public void logout() {
        auth.signOut();
        usuarioActual.setValue(null);

        // Opcional: Limpiar datos locales
        RecetasBD.bdWriteExecutor.execute(() -> {
            usuarioDAO.eliminarTodos();
        });
    }

    // ===== RECUPERAR CONTRASEÑA =====
    public void recuperarPassword(String email, OnPasswordResetListener listener) {
        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onError(parseAuthError(e.getMessage())));
    }

    // ===== VERIFICAR SESIÓN =====
    public boolean isUserLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    public FirebaseUser getCurrentFirebaseUser() {
        return auth.getCurrentUser();
    }

    public LiveData<Usuario> getUsuarioActual() {
        return usuarioActual;
    }

    // ===== HELPERS PRIVADOS =====

    private void cargarUsuarioActual() {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser != null) {
            cargarUsuarioDesdeFirestore(firebaseUser.getUid(), new OnAuthListener() {
                @Override
                public void onSuccess(Usuario usuario) {
                    usuarioActual.postValue(usuario);
                }

                @Override
                public void onError(String mensaje) {
                    Log.e(TAG, "Error al cargar usuario: " + mensaje);
                }
            });
        }
    }

    private void guardarUsuarioEnFirestore(Usuario usuario, OnAuthListener listener) {
        Map<String, Object> usuarioMap = usuarioToMap(usuario);

        firestore.collection(USUARIOS_COLLECTION)
                .document(usuario.getUid())
                .set(usuarioMap)
                .addOnSuccessListener(aVoid -> {
                    // Guardar en Room
                    RecetasBD.bdWriteExecutor.execute(() -> {
                        usuarioDAO.insertar(usuario);
                    });

                    usuarioActual.postValue(usuario);
                    listener.onSuccess(usuario);
                    Log.d(TAG, "Usuario guardado: " + usuario.getUid());
                })
                .addOnFailureListener(e -> {
                    listener.onError("Error al guardar datos: " + e.getMessage());
                });
    }

    private void cargarUsuarioDesdeFirestore(String uid, OnAuthListener listener) {
        firestore.collection(USUARIOS_COLLECTION)
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Usuario usuario = mapToUsuario(documentSnapshot.getData());
                        usuario.setUid(uid);

                        // Actualizar última conexión
                        usuario.setUltimaConexion(new Date());
                        actualizarUltimaConexion(uid);

                        // Guardar en Room
                        RecetasBD.bdWriteExecutor.execute(() -> {
                            usuarioDAO.insertar(usuario);
                        });

                        usuarioActual.postValue(usuario);
                        listener.onSuccess(usuario);
                    } else {
                        listener.onError("Usuario no encontrado en base de datos");
                    }
                })
                .addOnFailureListener(e -> {
                    listener.onError("Error al cargar datos: " + e.getMessage());
                });
    }

    private void actualizarUltimaConexion(String uid) {
        firestore.collection(USUARIOS_COLLECTION)
                .document(uid)
                .update("ultimaConexion", new Date())
                .addOnFailureListener(e ->
                        Log.w(TAG, "Error al actualizar última conexión: " + e.getMessage())
                );
    }

    // ===== MAPPERS =====

    private Map<String, Object> usuarioToMap(Usuario u) {
        Map<String, Object> map = new HashMap<>();
        map.put("nombre", u.getNombre());
        map.put("email", u.getEmail());
        map.put("fechaRegistro", u.getFechaRegistro());
        map.put("ultimaConexion", u.getUltimaConexion());
        return map;
    }

    private Usuario mapToUsuario(Map<String, Object> map) {
        Usuario u = new Usuario();
        u.setNombre((String) map.get("nombre"));
        u.setEmail((String) map.get("email"));
        return u;
    }

    // ===== PARSEO DE ERRORES =====

    private String parseAuthError(String errorMessage) {
        if (errorMessage == null) return "Error desconocido";

        if (errorMessage.contains("email address is already in use")) {
            return "Este email ya está registrado";
        } else if (errorMessage.contains("password is invalid")) {
            return "Contraseña incorrecta";
        } else if (errorMessage.contains("no user record")) {
            return "Usuario no encontrado";
        } else if (errorMessage.contains("network error")) {
            return "Error de conexión. Verifica tu internet";
        } else if (errorMessage.contains("too many requests")) {
            return "Demasiados intentos. Intenta más tarde";
        } else if (errorMessage.contains("weak password")) {
            return "La contraseña debe tener al menos 6 caracteres";
        } else if (errorMessage.contains("badly formatted")) {
            return "Email inválido";
        }

        return "Error: " + errorMessage;
    }

    // ===== INTERFACES =====

    public interface OnAuthListener {
        void onSuccess(Usuario usuario);
        void onError(String mensaje);
    }

    public interface OnPasswordResetListener {
        void onSuccess();
        void onError(String mensaje);
    }
}

