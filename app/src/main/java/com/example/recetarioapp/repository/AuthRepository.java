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

//Repository especializado en operaciones de autenticación y gestión de usuarios
//-Coordina la autenticación con Firebase Auth
//- Almacenamiento de perfiles en Firestore
// - Caché local de datos de usuario en Room Database
public class AuthRepository {

    //Constantes para logging y configuración
    private static final String TAG = "AuthRepository";
    private static final String COLECCION_USUARIOS = "usuarios";

    //Dependencias de Firebase y Room
    private final FirebaseAuth autenticacion;
    private final FirebaseFirestore firestore;
    private final UsuarioDAO usuarioDAO;

    //Estado observable del usuario actual
    private final MutableLiveData<Usuario> usuarioActual = new MutableLiveData<>();

    //Constructor que inicializa todas las dependencias y carga el usuario actual
    public AuthRepository(Application app) {
        this.autenticacion = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();

        //Inicializar acceso a base de datos local
        RecetasBD baseDatos = RecetasBD.getInstance(app);
        this.usuarioDAO = baseDatos.usuarioDAO();

        //Cargar usuario actual si existe una sesión activa
        cargarUsuarioActual();
    }

    //==================== OPERACIONES DE AUTENTICACIÓN ====================

    //Registra un nuevo usuario en el sistema
    //Crea cuenta en Firebase Auth, actualiza perfil y guarda datos en Firestore/Room
    public void registrarUsuario(String nombre, String email, String password,
                                 OnAuthListener listener) {

        autenticacion.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(resultadoAuth -> {
                    FirebaseUser usuarioFirebase = resultadoAuth.getUser();
                    if (usuarioFirebase == null) {
                        listener.onError("Error al crear usuario en Firebase");
                        return;
                    }

                    //Actualizar perfil de Firebase con el nombre
                    UserProfileChangeRequest actualizacionPerfil =
                            new UserProfileChangeRequest.Builder()
                                    .setDisplayName(nombre)
                                    .build();

                    usuarioFirebase.updateProfile(actualizacionPerfil)
                            .addOnSuccessListener(aVoid -> {
                                //Crear objeto usuario y guardar en bases de datos
                                Usuario usuario = new Usuario(
                                        usuarioFirebase.getUid(),
                                        nombre,
                                        email
                                );

                                guardarUsuarioEnFirestore(usuario, listener);
                            })
                            .addOnFailureListener(error -> {
                                listener.onError("Error al actualizar perfil: " + error.getMessage());
                            });
                })
                .addOnFailureListener(error -> {
                    listener.onError(parsearErrorAutenticacion(error.getMessage()));
                });
    }

    //Inicia sesión con email y password
    //Carga los datos del usuario desde Firestore y actualiza la caché local
    public void loginUsuario(String email, String password, OnAuthListener listener) {

        autenticacion.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(resultadoAuth -> {
                    FirebaseUser usuarioFirebase = resultadoAuth.getUser();
                    if (usuarioFirebase == null) {
                        listener.onError("Error al iniciar sesión");
                        return;
                    }

                    //Cargar datos completos del usuario desde Firestore
                    cargarUsuarioDesdeFirestore(usuarioFirebase.getUid(), listener);
                })
                .addOnFailureListener(error -> {
                    listener.onError(parsearErrorAutenticacion(error.getMessage()));
                });
    }

    //Cierra la sesión actual del usuario
    //Limpia Firebase Auth y los datos locales del usuario
    public void logout() {
        autenticacion.signOut();
        usuarioActual.setValue(null);

        //Limpiar datos de usuario de la base de datos local
        RecetasBD.bdWriteExecutor.execute(() -> {
            usuarioDAO.eliminarTodos();
        });
    }


    //==================== CONSULTAS DE ESTADO ====================

    //Verifica si hay un usuario con sesión activa
    public boolean isUserLoggedIn() {
        return autenticacion.getCurrentUser() != null;
    }

    //Obtiene el usuario actual de Firebase Auth
    public FirebaseUser getCurrentFirebaseUser() {
        return autenticacion.getCurrentUser();
    }

    //Obtiene el usuario actual como LiveData observable
    public LiveData<Usuario> getUsuarioActual() {
        return usuarioActual;
    }

    //==================== MÉTODOS PRIVADOS DE APOYO ====================

    //Carga el usuario actual si existe una sesión activa
    //Se ejecuta automáticamente al inicializar el repository
    private void cargarUsuarioActual() {
        FirebaseUser usuarioFirebase = autenticacion.getCurrentUser();
        if (usuarioFirebase != null) {
            cargarUsuarioDesdeFirestore(usuarioFirebase.getUid(), new OnAuthListener() {
                @Override
                public void onSuccess(Usuario usuario) {
                    usuarioActual.postValue(usuario);
                }

                @Override
                public void onError(String mensaje) {
                    Log.e(TAG, "Error al cargar usuario actual: " + mensaje);
                }
            });
        }
    }

    //Guarda un usuario en Firestore y Room Database
    private void guardarUsuarioEnFirestore(Usuario usuario, OnAuthListener listener) {
        Map<String, Object> usuarioMap = usuarioToMap(usuario);

        firestore.collection(COLECCION_USUARIOS)
                .document(usuario.getUid())
                .set(usuarioMap)
                .addOnSuccessListener(aVoid -> {
                    //Guardar también en base de datos local
                    RecetasBD.bdWriteExecutor.execute(() -> {
                        usuarioDAO.insertar(usuario);
                    });

                    usuarioActual.postValue(usuario);
                    listener.onSuccess(usuario);
                    Log.d(TAG, "Usuario guardado exitosamente: " + usuario.getUid());
                })
                .addOnFailureListener(error -> {
                    listener.onError("Error al guardar datos de usuario: " + error.getMessage());
                });
    }

    //Carga un usuario desde Firestore por su UID
    private void cargarUsuarioDesdeFirestore(String uid, OnAuthListener listener) {
        firestore.collection(COLECCION_USUARIOS)
                .document(uid)
                .get()
                .addOnSuccessListener(documento -> {
                    if (documento.exists()) {
                        Usuario usuario = mapToUsuario(documento.getData());
                        usuario.setUid(uid);

                        //Actualizar timestamp de última conexión
                        usuario.setUltimaConexion(new Date());
                        actualizarUltimaConexion(uid);

                        //Guardar en base de datos local
                        RecetasBD.bdWriteExecutor.execute(() -> {
                            usuarioDAO.insertar(usuario);
                        });

                        usuarioActual.postValue(usuario);
                        listener.onSuccess(usuario);
                    } else {
                        listener.onError("Usuario no encontrado en base de datos");
                    }
                })
                .addOnFailureListener(error -> {
                    listener.onError("Error al cargar datos de usuario: " + error.getMessage());
                });
    }

    //Actualiza la última conexión del usuario en Firestore
    private void actualizarUltimaConexion(String uid) {
        firestore.collection(COLECCION_USUARIOS)
                .document(uid)
                .update("ultimaConexion", new Date())
                .addOnFailureListener(error ->
                        Log.w(TAG, "Error al actualizar última conexión: " + error.getMessage())
                );
    }

    //==================== CONVERSORES ====================

    //Convierte objeto Usuario a Map para Firestore
    private Map<String, Object> usuarioToMap(Usuario usuario) {
        Map<String, Object> mapa = new HashMap<>();
        mapa.put("nombre", usuario.getNombre());
        mapa.put("email", usuario.getEmail());
        mapa.put("fechaRegistro", usuario.getFechaRegistro());
        mapa.put("ultimaConexion", usuario.getUltimaConexion());
        return mapa;
    }

    //Convierte Map de Firestore a objeto Usuario
    private Usuario mapToUsuario(Map<String, Object> mapa) {
        Usuario usuario = new Usuario();
        usuario.setNombre((String) mapa.get("nombre"));
        usuario.setEmail((String) mapa.get("email"));
        return usuario;
    }

    //==================== MANEJO DE ERRORES ====================

    //Parsea y traduce mensajes de error de Firebase a español
    private String parsearErrorAutenticacion(String mensajeError) {
        if (mensajeError == null) return "Error desconocido";

        if (mensajeError.contains("email address is already in use")) {
            return "Este email ya está registrado";
        } else if (mensajeError.contains("password is invalid")) {
            return "Contraseña incorrecta";
        } else if (mensajeError.contains("no user record")) {
            return "Usuario no encontrado";
        } else if (mensajeError.contains("network error")) {
            return "Error de conexión. Verifica tu internet";
        } else if (mensajeError.contains("too many requests")) {
            return "Demasiados intentos. Intenta más tarde";
        } else if (mensajeError.contains("weak password")) {
            return "La contraseña debe tener al menos 6 caracteres";
        } else if (mensajeError.contains("badly formatted")) {
            return "Email inválido";
        }

        return "Error: " + mensajeError;
    }

    //==================== INTERFACES DE CALLBACK ====================

    //Interfaz para recibir resultados de operaciones de autenticación
    public interface OnAuthListener {
        void onSuccess(Usuario usuario);
        void onError(String mensaje);
    }
}