package com.example.recetarioapp.repository;

import android.app.Application;
import android.net.Uri;
import androidx.lifecycle.LiveData;
import com.example.recetarioapp.models.Receta;
import com.example.recetarioapp.repository.local.LocalDataSource;
import com.example.recetarioapp.repository.remote.FirebaseDataSource;
import com.example.recetarioapp.repository.storage.ImageStorage;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

/**
 * Repository principal - Coordina entre fuentes de datos local y remota
 */
public class RecetaRepository {

    private static final String TAG = "RecetaRepository"; //Constante para logs

    private final LocalDataSource localDataSource;
    private final FirebaseDataSource firebaseDataSource;
    private final ImageStorage imageManager;

    public RecetaRepository(Application app) {
        this.localDataSource = new LocalDataSource(app);
        this.firebaseDataSource = new FirebaseDataSource();
        this.imageManager = new ImageStorage(app);

        // ✅ CORREGIDO: Sincronizar solo si hay usuario logueado
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            sincronizarFBaLocal();
        }
    }

    // ===== OPERACIONES DE LECTURA =====
    public LiveData<List<Receta>> getAllRecetas() {
        return localDataSource.getAllRecetas();
    }

    public LiveData<Receta> getRecetaById(long id) {
        return localDataSource.getRecetaById(id);
    }

    public LiveData<List<Receta>> getFavs() {
        return localDataSource.getFavoritas();
    }

    public LiveData<List<String>> getCategorias() {
        return localDataSource.getCategorias();
    }

    public LiveData<List<Receta>> buscarPorNombre(String query) {
        return localDataSource.buscarPorNombre(query);
    }

    public LiveData<List<Receta>> getRecetasPorCategoria(String categoria) {
        return localDataSource.getRecetasPorCategoria(categoria);
    }

    public LiveData<List<Receta>> getRecetasPorDificultad(String dificultad) {
        return localDataSource.getRecetasPorDificultad(dificultad);
    }

    public LiveData<List<Receta>> getRecetasPorTiempo(int tiempo) {
        return localDataSource.getRecetasPorTiempo(tiempo);
    }

    // ===== OPERACIONES DE ESCRITURA =====
    public void insertarReceta(Receta receta, OnRecetaGuardadaListener listener) {
        localDataSource.insertar(receta, localId -> {
            receta.setId(localId);
            listener.onSuccess(receta);

            // ✅ CORREGIDO: Solo sincronizar con Firebase si hay usuario logueado
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                firebaseDataSource.guardarReceta(receta, firebaseId -> {
                    receta.setFirebaseId(firebaseId);
                    localDataSource.actualizar(receta, () -> {
                        // Éxito al actualizar Firebase ID
                    }, error -> {
                        android.util.Log.w(TAG, "Error al actualizar Firebase ID: " + error);
                    });
                }, error -> {
                    android.util.Log.w(TAG, "Modo offline: " + error);
                });
            }
        }, listener::onError);
    }

    public void actualizarReceta(Receta receta, OnRecetaGuardadaListener listener) {
        localDataSource.actualizar(receta, () -> {
            listener.onSuccess(receta);

            // ✅ CORREGIDO: Solo sincronizar con Firebase si hay usuario logueado y tiene FirebaseId
            if (FirebaseAuth.getInstance().getCurrentUser() != null && receta.getFirebaseId() != null) {
                firebaseDataSource.actualizarReceta(receta,
                        NOOP_RUNNABLE,
                        error -> android.util.Log.w(TAG, "Error sync: " + error)
                );
            }
        }, listener::onError);
    }

    public void eliminarReceta(Receta receta, OnRecetaEliminadaListener listener) {
        localDataSource.eliminar(receta, () -> {
            listener.onSuccess();

            // ✅ CORREGIDO: Solo sincronizar con Firebase si hay usuario logueado y tiene FirebaseId
            if (FirebaseAuth.getInstance().getCurrentUser() != null && receta.getFirebaseId() != null) {
                firebaseDataSource.eliminarReceta(receta.getFirebaseId(),
                        NOOP_RUNNABLE,
                        error -> android.util.Log.w(TAG, "Error al eliminar en Firebase: " + error)
                );
            }
        }, listener::onError);
    }

    public void establecerFavorita(long id, boolean isFav) {
        localDataSource.establecerFavorita(id, isFav);
    }

    // ===== GESTIÓN DE IMÁGENES =====
    public void guardarImagenLocal(Uri uri, OnImagenSubidaListener listener) {
        imageManager.guardarImagen(uri, listener);
    }

    // ===== SINCRONIZACIÓN =====
    public void sincronizarFBaLocal() {
        // ✅ CORREGIDO: Verificar que hay usuario logueado antes de sincronizar
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            firebaseDataSource.obtenerRecetasUsuario(recetas -> {
                localDataSource.insertarVarias(recetas);
            }, error -> {
                android.util.Log.w(TAG, "Error sincronización: " + error);
            });
        }
    }

    // ✅ NUEVO MÉTODO: Forzar sincronización manual
    public void forzarSincronizacion() {
        sincronizarFBaLocal();
    }

    // ===== UTILIDADES =====
    private static final Runnable NOOP_RUNNABLE = () -> {}; //Helper para callbacks vacíos

    // ===== INTERFACES =====
    public interface OnRecetaGuardadaListener {
        void onSuccess(Receta receta);
        void onError(String mensaje);
    }

    public interface OnRecetaEliminadaListener {
        void onSuccess();
        void onError(String mensaje);
    }

    public interface OnImagenSubidaListener {
        void onSuccess(String path);
        void onProgress(int porcentaje);
        void onError(String mensaje);
    }
}