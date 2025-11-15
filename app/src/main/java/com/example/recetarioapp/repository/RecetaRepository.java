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

//Repository principal que coordina todas las operaciones con recetas
// - Implementa patrón Repository para abstraer origen de datos
// - Proporciona API unificada para operaciones con recetas
// - Sincroniza automáticamente entre base de datos local (Room) y remota (Firebase)
// - Gestiona lógica de sincronización, resolución de conflictos y modo offline
public class RecetaRepository {

    //Configuración y logging
    private static final String TAG = "RecetaRepository";

    //Fuentes de datos
    private final LocalDataSource fuenteLocal;
    private final FirebaseDataSource fuenteFirebase;
    private final ImageStorage gestorImagenes;

    //Control de sincronización global (static para toda la aplicación)
    private static boolean sincronizacionGlobalRealizada = false;

    //Constructor que inicializa todas las fuentes de datos
    public RecetaRepository(Application app) {
        this.fuenteLocal = new LocalDataSource(app);
        this.fuenteFirebase = new FirebaseDataSource();
        this.gestorImagenes = new ImageStorage(app);

        //Log de inicialización (sin sincronización automática)
        android.util.Log.d(TAG, "Repository inicializado - Sincronización Global: " + sincronizacionGlobalRealizada);
    }

    //==================== OPERACIONES DE LECTURA (SOLO LOCAL) ====================

    //Obtiene todas las recetas ordenadas por fecha de creación
    public LiveData<List<Receta>> getAllRecetas() {
        return fuenteLocal.getAllRecetas();
    }

    //Obtiene una receta específica por su ID local
    public LiveData<Receta> getRecetaById(long id) {
        return fuenteLocal.getRecetaById(id);
    }

    //Obtiene todas las recetas marcadas como favoritas
    public LiveData<List<Receta>> getFavs() {
        return fuenteLocal.getFavoritas();
    }

    //Busca recetas por nombre (búsqueda parcial)
    public LiveData<List<Receta>> buscarPorNombre(String query) {
        return fuenteLocal.buscarPorNombre(query);
    }

    //Filtra recetas por categoría específica
    public LiveData<List<Receta>> getRecetasPorCategoria(String categoria) {
        return fuenteLocal.getRecetasPorCategoria(categoria);
    }

    //Filtra recetas por nivel de dificultad
    public LiveData<List<Receta>> getRecetasPorDificultad(String dificultad) {
        return fuenteLocal.getRecetasPorDificultad(dificultad);
    }

    //Filtra recetas por tiempo máximo de preparación
    public LiveData<List<Receta>> getRecetasPorTiempo(int tiempoMax) {
        return fuenteLocal.getRecetasPorTiempo(tiempoMax);
    }

    //==================== OPERACIONES DE ESCRITURA (SINCRONIZADAS) ====================

    //Inserta una nueva receta con sincronización automática
    public void insertarReceta(Receta receta, OnRecetaGuardadaListener listener) {
        //Verificar autenticación para sincronización con Firebase
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            //Estrategia: Firebase primero
            fuenteFirebase.guardarReceta(receta, firebaseId -> {
                //Éxito en Firebase - guardar local con firebaseId
                receta.setFirebaseId(firebaseId);
                fuenteLocal.insertar(receta, localId -> {
                    receta.setId(localId);
                    listener.onSuccess(receta);
                }, listener::onError);
            }, error -> {
                //Falló Firebase - guardar solo local (modo offline)
                android.util.Log.w(TAG, "Modo offline, guardando solo local: " + error);
                fuenteLocal.insertar(receta, localId -> {
                    receta.setId(localId);
                    listener.onSuccess(receta);
                }, listener::onError);
            });
        } else {
            //No hay usuario - guardar solo local
            fuenteLocal.insertar(receta, localId -> {
                receta.setId(localId);
                listener.onSuccess(receta);
            }, listener::onError);
        }
    }

    //Actualiza una receta existente con sincronización
    public void actualizarReceta(Receta receta, OnRecetaGuardadaListener listener) {
        fuenteLocal.actualizar(receta, () -> {
            //Éxito local - notificar inmediatamente
            listener.onSuccess(receta);

            //Sincronizar con Firebase si es posible (async, no bloqueante)
            if (FirebaseAuth.getInstance().getCurrentUser() != null && receta.getFirebaseId() != null) {
                fuenteFirebase.actualizarReceta(receta,
                        NOOP_RUNNABLE,
                        error -> android.util.Log.w(TAG, "Error sincronizando actualización: " + error)
                );
            }
        }, listener::onError);
    }

    //Elimina una receta con sincronización
    public void eliminarReceta(Receta receta, OnRecetaEliminadaListener listener) {
        fuenteLocal.eliminar(receta, () -> {
            //Éxito local - notificar inmediatamente
            listener.onSuccess();

            //Sincronizar con Firebase si es posible (async, no bloqueante)
            if (FirebaseAuth.getInstance().getCurrentUser() != null && receta.getFirebaseId() != null) {
                fuenteFirebase.eliminarReceta(receta.getFirebaseId(),
                        NOOP_RUNNABLE,
                        error -> android.util.Log.w(TAG, "Error al eliminar en Firebase: " + error)
                );
            }
        }, listener::onError);
    }

    //Establece o remueve el estado de favorito de una receta
    public void establecerFavorita(long id, boolean esFavorito) {
        fuenteLocal.establecerFavorita(id, esFavorito);
    }

    //==================== GESTIÓN DE IMÁGENES ====================

    //Guarda una imagen en almacenamiento interno y devuelve la ruta
    public void guardarImagenLocal(Uri uri, OnImagenSubidaListener listener) {
        gestorImagenes.guardarImagen(uri, listener);
    }

    //==================== SINCRONIZACIÓN FIREBASE → LOCAL ====================

    //Sincroniza recetas desde Firebase a la base de datos local
    public void sincronizarFBaLocal() {
        //Control GLOBAL para evitar sincronizaciones duplicadas
        if (!sincronizacionGlobalRealizada && FirebaseAuth.getInstance().getCurrentUser() != null) {
            android.util.Log.d(TAG, "SINCRONIZACIÓN GLOBAL iniciada");

            fuenteFirebase.obtenerRecetasUsuario(recetas -> {
                android.util.Log.d(TAG, "Descargadas " + recetas.size() + " recetas de Firebase");

                if (recetas.isEmpty()) {
                    sincronizacionGlobalRealizada = true;
                    android.util.Log.d(TAG, "No hay recetas para sincronizar");
                    return;
                }

                //Procesamiento en background con control de duplicados
                RecetasBD.bdWriteExecutor.execute(() -> {
                    List<Receta> recetasSinDuplicados = new ArrayList<>();
                    int duplicadosEncontrados = 0;

                    for (Receta recetaFirebase : recetas) {
                        if (recetaFirebase.getFirebaseId() == null) {
                            android.util.Log.w(TAG, "Receta sin firebaseId, omitiendo");
                            continue;
                        }

                        //Verificar si ya existe en local por firebaseId
                        Receta existente = fuenteLocal.getRecetaByFirebaseId(recetaFirebase.getFirebaseId());
                        if (existente == null) {
                            recetasSinDuplicados.add(recetaFirebase);
                            android.util.Log.d(TAG, "Nueva receta: " + recetaFirebase.getNombre());
                        } else {
                            duplicadosEncontrados++;
                            android.util.Log.d(TAG, "Receta ya existe: " + recetaFirebase.getFirebaseId());
                        }
                    }

                    //Insertar solo las recetas nuevas
                    if (!recetasSinDuplicados.isEmpty()) {
                        fuenteLocal.insertarVarias(recetasSinDuplicados);
                        android.util.Log.d(TAG, "Insertadas " + recetasSinDuplicados.size() + " recetas nuevas");
                    }

                    android.util.Log.d(TAG, "Resumen: " + duplicadosEncontrados + " duplicados evitados, " +
                            recetasSinDuplicados.size() + " nuevas insertadas");

                    sincronizacionGlobalRealizada = true;
                    android.util.Log.d(TAG, "SINCRONIZACIÓN GLOBAL completada");
                });
            }, error -> {
                android.util.Log.w(TAG, "Error en sincronización: " + error);
                sincronizacionGlobalRealizada = true; //Marcar como completada incluso en error
            });
        } else {
            android.util.Log.d(TAG, "SINCRONIZACIÓN omitida - Global: " + sincronizacionGlobalRealizada +
                    ", Usuario: " + (FirebaseAuth.getInstance().getCurrentUser() != null));
        }
    }

    //==================== UTILIDADES ====================

    //Runnable vacío para operaciones que no requieren acción en callback
    private static final Runnable NOOP_RUNNABLE = () -> {};

    //==================== INTERFACES DE CALLBACK ====================

    //Interfaz para operaciones de guardado de recetas
    public interface OnRecetaGuardadaListener {
        void onSuccess(Receta receta);
        void onError(String mensaje);
    }

    //Interfaz para operaciones de eliminación de recetas
    public interface OnRecetaEliminadaListener {
        void onSuccess();
        void onError(String mensaje);
    }

    //Interfaz para operaciones de subida de imágenes
    public interface OnImagenSubidaListener {
        void onSuccess(String path);
        void onProgress(int porcentaje);
        void onError(String mensaje);
    }
}