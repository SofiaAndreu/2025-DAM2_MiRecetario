package com.example.recetarioapp.repository;

import android.app.Application;
import android.net.Uri;
import androidx.lifecycle.LiveData;

import com.example.recetarioapp.database.RecetasBD;
import com.example.recetarioapp.models.Receta;
import com.example.recetarioapp.repository.local.LocalDataSource;
import com.example.recetarioapp.repository.remote.FirebaseDataSource;
import com.example.recetarioapp.repository.storage.ImageStorage;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository principal - Coordina entre fuentes de datos local y remota
 */
public class RecetaRepository {

    private static final String TAG = "RecetaRepository";

    private final LocalDataSource localDataSource;
    private final FirebaseDataSource firebaseDataSource;
    private final ImageStorage imageManager;

    // ✅ NUEVO: Control GLOBAL de sincronización (static = compartido entre todas las instancias)
    private static boolean sincronizacionGlobalRealizada = false;

    public RecetaRepository(Application app) {
        this.localDataSource = new LocalDataSource(app);
        this.firebaseDataSource = new FirebaseDataSource();
        this.imageManager = new ImageStorage(app);

        // ✅ SOLO inicialización, SIN sincronización automática
        android.util.Log.d(TAG, "🆕 Nuevo Repository - Sincronización Global: " + sincronizacionGlobalRealizada);
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
        // ✅ CORREGIDO: Primero intentar Firebase, luego local
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            firebaseDataSource.guardarReceta(receta, firebaseId -> {
                // ✅ Éxito en Firebase - guardar local con firebaseId
                receta.setFirebaseId(firebaseId);
                localDataSource.insertar(receta, localId -> {
                    receta.setId(localId);
                    listener.onSuccess(receta);
                }, listener::onError);
            }, error -> {
                // ❌ Falló Firebase - guardar solo local (modo offline)
                android.util.Log.w(TAG, "Modo offline, guardando solo local: " + error);
                localDataSource.insertar(receta, localId -> {
                    receta.setId(localId);
                    listener.onSuccess(receta);
                }, listener::onError);
            });
        } else {
            // ❌ No hay usuario - guardar solo local
            localDataSource.insertar(receta, localId -> {
                receta.setId(localId);
                listener.onSuccess(receta);
            }, listener::onError);
        }
    }

    public void actualizarReceta(Receta receta, OnRecetaGuardadaListener listener) {
        localDataSource.actualizar(receta, () -> {
            listener.onSuccess(receta);

            // Sincronizar con Firebase si hay usuario y tiene FirebaseId
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

            // Sincronizar con Firebase si hay usuario y tiene FirebaseId
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
    // En app/src/main/java/com/example/recetarioapp/repository/RecetaRepository.java
// REEMPLAZA completamente el método sincronizarFBaLocal:

    public void sincronizarFBaLocal() {
        // ✅ CORREGIDO: Control GLOBAL para evitar duplicación
        if (!sincronizacionGlobalRealizada && FirebaseAuth.getInstance().getCurrentUser() != null) {
            android.util.Log.d(TAG, "🔄 SINCRONIZACIÓN GLOBAL iniciada");

            firebaseDataSource.obtenerRecetasUsuario(recetas -> {
                android.util.Log.d(TAG, "📥 Descargadas " + recetas.size() + " recetas de Firebase");

                if (recetas.isEmpty()) {
                    sincronizacionGlobalRealizada = true;
                    android.util.Log.d(TAG, "✅ No hay recetas para sincronizar");
                    return;
                }

                // ✅ NUEVO: Verificar y evitar duplicados
                RecetasBD.bdWriteExecutor.execute(() -> {
                    List<Receta> recetasSinDuplicados = new ArrayList<>();
                    int duplicadosEncontrados = 0;

                    for (Receta recetaFirebase : recetas) {
                        if (recetaFirebase.getFirebaseId() == null) {
                            android.util.Log.w(TAG, "⏭️  Receta sin firebaseId, omitiendo");
                            continue;
                        }

                        // Verificar si ya existe en local por firebaseId
                        Receta existente = localDataSource.getRecetaByFirebaseId(recetaFirebase.getFirebaseId());
                        if (existente == null) {
                            recetasSinDuplicados.add(recetaFirebase);
                            android.util.Log.d(TAG, "➕ Nueva receta: " + recetaFirebase.getNombre());
                        } else {
                            duplicadosEncontrados++;
                            android.util.Log.d(TAG, "⏭️  Receta ya existe: " + recetaFirebase.getFirebaseId());
                        }
                    }

                    // Insertar solo las nuevas
                    if (!recetasSinDuplicados.isEmpty()) {
                        localDataSource.insertarVarias(recetasSinDuplicados);
                        android.util.Log.d(TAG, "✅ Insertadas " + recetasSinDuplicados.size() + " recetas nuevas");
                    }

                    android.util.Log.d(TAG, "📊 Resumen: " + duplicadosEncontrados + " duplicados evitados, " +
                            recetasSinDuplicados.size() + " nuevas insertadas");

                    sincronizacionGlobalRealizada = true;
                    android.util.Log.d(TAG, "✅ SINCRONIZACIÓN GLOBAL completada");
                });
            }, error -> {
                android.util.Log.w(TAG, "❌ Error sincronización: " + error);
                sincronizacionGlobalRealizada = true; // Marcar como completada incluso en error
            });
        } else {
            android.util.Log.d(TAG, "⏭️  SINCRONIZACIÓN omitida - Global: " + sincronizacionGlobalRealizada +
                    ", Usuario: " + (FirebaseAuth.getInstance().getCurrentUser() != null));
        }
    }

    // ✅ NUEVO: Forzar sincronización manual (resetear flag)
    public void forzarSincronizacion() {
        sincronizacionGlobalRealizada = false;
        sincronizarFBaLocal();
    }

    // ✅ NUEVO: Resetear para testing
    public static void resetearSincronizacion() {
        sincronizacionGlobalRealizada = false;
        android.util.Log.d(TAG, "🔄 Sincronización reseteada");
    }

    // ✅ NUEVO: Verificar estado
    public static boolean isSincronizacionRealizada() {
        return sincronizacionGlobalRealizada;
    }

    // ===== UTILIDADES =====
    private static final Runnable NOOP_RUNNABLE = () -> {};

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