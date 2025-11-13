package com.example.recetarioapp.repository.remote;

import com.example.recetarioapp.models.Receta;
import com.example.recetarioapp.repository.mappers.RecetaMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Fuente de datos remota que maneja todas las operaciones con Firebase Firestore.
 *
 * Proporciona una API para las operaciones CRUD en la nube, incluyendo
 * autenticación de usuarios y sincronización de datos.
 * Utiliza RecetaMapper para convertir entre objetos Receta y la estructura
 * de datos plana requerida por Firebase.
 */
public class FirebaseDataSource {

    // Constantes de configuración
    private static final String COLECCION_RECETAS = "recetas";

    // Dependencias de Firebase
    private final FirebaseFirestore firestore;
    private final FirebaseAuth autenticacion;
    private final RecetaMapper mapeador;

    /**
     * Constructor que inicializa las instancias de Firebase.
     */
    public FirebaseDataSource() {
        this.firestore = FirebaseFirestore.getInstance();
        this.autenticacion = FirebaseAuth.getInstance();
        this.mapeador = new RecetaMapper();
    }

    // ==================== OPERACIONES CRUD EN FIREBASE ====================

    /**
     * Guarda una nueva receta en Firebase Firestore.
     * Asocia automáticamente la receta al usuario autenticado actual.
     *
     * @param receta Receta a guardar en la nube
     * @param listener Callback para éxito con el ID generado
     * @param errorListener Callback para manejo de errores
     */
    public void guardarReceta(Receta receta, OnSuccessListener listener, OnErrorListener errorListener) {
        FirebaseUser usuario = autenticacion.getCurrentUser();
        if (usuario == null) {
            errorListener.onError("Usuario no autenticado");
            return;
        }
        // Asociar receta al usuario actual
        receta.setUsuarioId(usuario.getUid());
        Map<String, Object> datosReceta = mapeador.toMap(receta);

        firestore.collection(COLECCION_RECETAS)
                .add(datosReceta)
                .addOnSuccessListener(documento ->
                        listener.onSuccess(documento.getId())
                )
                .addOnFailureListener(error ->
                        errorListener.onError(error.getMessage())
                );
    }

    /**
     * Actualiza una receta existente en Firebase.
     *
     * @param receta Receta con datos actualizados
     * @param onSuccess Callback ejecutado al completar la actualización
     * @param errorListener Callback para manejo de errores
     */
    public void actualizarReceta(Receta receta, Runnable onSuccess, OnErrorListener errorListener) {
        if (receta.getFirebaseId() == null) {
            errorListener.onError("La receta no tiene ID de Firebase");
            return;
        }

        Map<String, Object> datosReceta = mapeador.toMap(receta);

        firestore.collection(COLECCION_RECETAS)
                .document(receta.getFirebaseId())
                .set(datosReceta)
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(error ->
                        errorListener.onError(error.getMessage())
                );
    }

    /**
     * Elimina una receta de Firebase.
     *
     * @param firebaseId ID único de la receta en Firebase
     * @param onSuccess Callback ejecutado al completar la eliminación
     * @param errorListener Callback para manejo de errores
     */
    public void eliminarReceta(String firebaseId, Runnable onSuccess, OnErrorListener errorListener) {
        firestore.collection(COLECCION_RECETAS)
                .document(firebaseId)
                .delete()
                .addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(error ->
                        errorListener.onError(error.getMessage())
                );
    }

    /**
     * Obtiene todas las recetas del usuario autenticado actual desde Firebase.
     *
     * @param listener Callback con la lista de recetas obtenidas
     * @param errorListener Callback para manejo de errores
     */
    public void obtenerRecetasUsuario(OnRecetasListener listener, OnErrorListener errorListener) {
        FirebaseUser usuario = autenticacion.getCurrentUser();
        if (usuario == null) {
            errorListener.onError("Usuario no autenticado");
            return;
        }

        firestore.collection(COLECCION_RECETAS)
                .whereEqualTo("usuarioId", usuario.getUid())
                .get()
                .addOnSuccessListener(consulta -> {
                    List<Receta> recetas = new ArrayList<>();

                    for (QueryDocumentSnapshot documento : consulta) {
                        // Reconstruir receta desde los datos de Firebase
                        Receta receta = mapeador.fromMap(documento.getData());
                        receta.setFirebaseId(documento.getId());
                        recetas.add(receta);
                    }

                    listener.onRecetasLoaded(recetas);
                })
                .addOnFailureListener(error ->
                        errorListener.onError(error.getMessage())
                );
    }

    // ==================== INTERFACES DE CALLBACK ====================

    /**
     * Interfaz para recibir el ID generado al guardar en Firebase.
     */
    public interface OnSuccessListener {
        void onSuccess(String firebaseId);
    }

    /**
     * Interfaz para recibir listas de recetas desde Firebase.
     */
    public interface OnRecetasListener {
        void onRecetasLoaded(List<Receta> recetas);
    }

    /**
     * Interfaz para manejar errores en operaciones con Firebase.
     */
    public interface OnErrorListener {
        void onError(String mensaje);
    }
}