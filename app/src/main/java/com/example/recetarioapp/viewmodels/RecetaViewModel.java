package com.example.recetarioapp.viewmodels;

import android.app.Application;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.recetarioapp.models.Receta;
import com.example.recetarioapp.repository.RecetaRepository;

import java.util.List;

/**
 * ViewModel principal para gestionar todas las operaciones con recetas.
 *
 * Actúa como intermediario entre la interfaz de usuario y el Repository,
 * proporcionando datos observables y gestionando el ciclo de vida de las operaciones.
 * Implementa búsqueda reactiva, filtros y gestión de estado de la UI.
 */
public class RecetaViewModel extends AndroidViewModel {

    // ==================== DEPENDENCIAS Y CONFIGURACIÓN ====================

    private final RecetaRepository repositorioRecetas;

    // Identificador único para debugging de instancias ViewModel
    private final String idInstancia = java.util.UUID.randomUUID().toString().substring(0, 8);

    // ==================== LIVE DATA PARA DATOS OBSERVABLES ====================

    // Datos principales observables desde el Repository
    private final LiveData<List<Receta>> todasLasRecetas;
    private final LiveData<List<Receta>> recetasFavoritas;
    private final LiveData<List<String>> categoriasDisponibles;

    // Controles de búsqueda y filtrado
    private final MutableLiveData<String> queryBusqueda = new MutableLiveData<>();
    private final MutableLiveData<String> filtroCategoria = new MutableLiveData<>();
    private final MutableLiveData<String> filtroDificultad = new MutableLiveData<>();
    private final MutableLiveData<Integer> filtroTiempo = new MutableLiveData<>();

    // LiveData reactivo que combina búsqueda con datos base
    private final LiveData<List<Receta>> recetasFiltradas;

    // ==================== ESTADOS DE UI OBSERVABLES ====================

    private final MutableLiveData<String> mensajeError = new MutableLiveData<>();
    private final MutableLiveData<String> mensajeExito = new MutableLiveData<>();
    private final MutableLiveData<Boolean> estadoCargando = new MutableLiveData<>();
    private final MutableLiveData<Integer> progresoSubida = new MutableLiveData<>();

    // Notificador específico para cambios en favoritos
    private final MutableLiveData<Long> favoritoActualizado = new MutableLiveData<>();

    /**
     * Constructor que inicializa todas las dependencias y configuraciones reactivas.
     *
     * @param application Contexto de la aplicación para inicializar el Repository
     */
    public RecetaViewModel(@NonNull Application application) {
        super(application);
        repositorioRecetas = new RecetaRepository(application);

        // Log de creación para debugging de ciclo de vida
        Log.d("VIEWMODEL", "Nueva Instancia ViewModel: " + idInstancia);

        // Inicializar LiveData desde el Repository
        todasLasRecetas = repositorioRecetas.getAllRecetas();
        recetasFavoritas = repositorioRecetas.getFavs();
        categoriasDisponibles = repositorioRecetas.getCategorias();

        // Configurar transformación reactiva para búsqueda en tiempo real
        recetasFiltradas = Transformations.switchMap(queryBusqueda, query -> {
            if (query == null || query.trim().isEmpty()) {
                // Query vacía - mostrar todas las recetas
                return todasLasRecetas;
            } else {
                // Query con texto - buscar recetas que coincidan
                return repositorioRecetas.buscarPorNombre(query);
            }
        });
    }

    // ==================== OPERACIONES CRUD CON RECETAS ====================

    /**
     * Inserta una nueva receta en el sistema.
     * Gestiona el estado de carga y notifica el resultado.
     *
     * @param receta Receta a insertar
     */
    public void insertarReceta(Receta receta) {
        estadoCargando.postValue(true);
        repositorioRecetas.insertarReceta(receta, new RecetaRepository.OnRecetaGuardadaListener() {
            @Override
            public void onSuccess(Receta recetaGuardada) {
                estadoCargando.postValue(false);
                mensajeExito.postValue("Receta guardada correctamente");
                Log.d("VIEWMODEL", "Receta insertada - Instancia: " + idInstancia);
            }

            @Override
            public void onError(String mensaje) {
                estadoCargando.postValue(false);
                mensajeError.postValue(mensaje);
                Log.e("VIEWMODEL", "Error insertando receta: " + mensaje);
            }
        });
    }

    /**
     * Actualiza una receta existente.
     *
     * @param receta Receta con datos actualizados
     */
    public void actualizarReceta(Receta receta) {
        estadoCargando.postValue(true);
        repositorioRecetas.actualizarReceta(receta, new RecetaRepository.OnRecetaGuardadaListener() {
            @Override
            public void onSuccess(Receta recetaActualizada) {
                estadoCargando.postValue(false);
                mensajeExito.postValue("Receta actualizada correctamente");
            }

            @Override
            public void onError(String mensaje) {
                estadoCargando.postValue(false);
                mensajeError.postValue(mensaje);
            }
        });
    }

    /**
     * Elimina una receta del sistema.
     *
     * @param receta Receta a eliminar
     */
    public void eliminarReceta(Receta receta) {
        estadoCargando.postValue(true);
        repositorioRecetas.eliminarReceta(receta, new RecetaRepository.OnRecetaEliminadaListener() {
            @Override
            public void onSuccess() {
                estadoCargando.postValue(false);
                mensajeExito.postValue("Receta eliminada correctamente");
            }

            @Override
            public void onError(String mensaje) {
                estadoCargando.postValue(false);
                mensajeError.postValue(mensaje);
            }
        });
    }

    /**
     * Marca o desmarca una receta como favorita.
     * Operación inmediata con notificación específica.
     *
     * @param id ID de la receta a modificar
     * @param esFavorito Nuevo estado de favorito
     */
    public void marcarFavorita(long id, boolean esFavorito) {
        repositorioRecetas.establecerFavorita(id, esFavorito);
        // Notificar específicamente que este favorito fue actualizado
        favoritoActualizado.postValue(id);
    }

    // ==================== GESTIÓN DE IMÁGENES ====================

    /**
     * Guarda una imagen localmente y devuelve la ruta.
     * Proporciona feedback de progreso durante la operación.
     *
     * @param uriImagen URI de la imagen a guardar
     * @param listener Callback para resultado de la operación
     */
    public void guardarImagenLocal(Uri uriImagen, OnImagenSubidaListener listener) {
        progresoSubida.postValue(0); // Iniciar progreso
        repositorioRecetas.guardarImagenLocal(uriImagen, new RecetaRepository.OnImagenSubidaListener() {
            @Override
            public void onSuccess(String ruta) {
                progresoSubida.postValue(100); // Completado
                listener.onImagenSubida(ruta);
            }

            @Override
            public void onProgress(int porcentaje) {
                progresoSubida.postValue(porcentaje);
            }

            @Override
            public void onError(String mensaje) {
                mensajeError.postValue(mensaje);
                // Ejecutar en main thread para seguridad
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    listener.onError(mensaje);
                });
            }
        });
    }

    // ==================== SINCRONIZACIÓN Y RESINCROZACIÓN ====================

    /**
     * Ejecuta sincronización de recetas desde Firebase a local.
     */
    public void sincronizar() {
        Log.d("VIEWMODEL", "Sincronizando - Instancia: " + idInstancia);
        estadoCargando.postValue(true);
        repositorioRecetas.sincronizarFBaLocal();
        estadoCargando.postValue(false);
        mensajeExito.postValue("Sincronizando...");
    }

    /**
     * Fuerza una resincronización manual ignorando el control de ejecución única.
     */
    public void forzarResincronizacion() {
        Log.d("VIEWMODEL", "Forzando resincronización - Instancia: " + idInstancia);
        repositorioRecetas.forzarSincronizacion();
        mensajeExito.postValue("Resincronizando recetas...");
    }

    /**
     * Resetea el estado de sincronización (principalmente para testing).
     */
    public void resetearSincronizacion() {
        RecetaRepository.resetearSincronizacion();
        Log.d("VIEWMODEL", "Sincronización reseteada - Instancia: " + idInstancia);
    }

    // ==================== BÚSQUEDA Y FILTRADO ====================

    /**
     * Ejecuta búsqueda en tiempo real de recetas por nombre.
     * La transformación reactiva se encarga de actualizar recetasFiltradas.
     *
     * @param query Texto de búsqueda
     */
    public void buscar(String query) {
        queryBusqueda.setValue(query);
    }

    /**
     * Obtiene recetas filtradas por categoría específica.
     *
     * @param categoria Categoría a filtrar
     * @return LiveData con recetas de la categoría
     */
    public LiveData<List<Receta>> filtrarPorCategoria(String categoria) {
        return repositorioRecetas.getRecetasPorCategoria(categoria);
    }

    /**
     * Obtiene recetas filtradas por nivel de dificultad.
     *
     * @param dificultad Nivel de dificultad a filtrar
     * @return LiveData con recetas de la dificultad
     */
    public LiveData<List<Receta>> filtrarPorDificultad(String dificultad) {
        return repositorioRecetas.getRecetasPorDificultad(dificultad);
    }

    /**
     * Obtiene recetas filtradas por tiempo máximo de preparación.
     *
     * @param tiempoMax Tiempo máximo en minutos
     * @return LiveData con recetas dentro del tiempo
     */
    public LiveData<List<Receta>> filtrarPorTiempo(int tiempoMax) {
        return repositorioRecetas.getRecetasPorTiempo(tiempoMax);
    }

    // ==================== UTILIDADES Y MÉTODOS DE APOYO ====================

    /**
     * Limpia los mensajes de éxito y error de la UI.
     */
    public void limpiarMensajesE() {
        mensajeExito.setValue(null);
        mensajeError.setValue(null);
    }

    /**
     * Método de diagnóstico para debugging de búsquedas.
     *
     * @param query Texto de búsqueda a debuggear
     */
    public void debugBuscar(String query) {
        Log.d("BUSQUEDA", "Buscando: '" + query + "' - Instancia: " + idInstancia);
        buscar(query);
    }

    // ==================== GETTERS PARA LIVE DATA ====================

    /**
     * Obtiene todas las recetas disponibles.
     *
     * @return LiveData con lista de todas las recetas
     */
    public LiveData<List<Receta>> getTodasLasRecetas() {
        return todasLasRecetas;
    }

    /**
     * Obtiene las recetas marcadas como favoritas.
     *
     * @return LiveData con lista de recetas favoritas
     */
    public LiveData<List<Receta>> getFavs() {
        return recetasFavoritas;
    }

    /**
     * Obtiene las categorías únicas disponibles.
     *
     * @return LiveData con lista de categorías
     */
    public LiveData<List<String>> getCategorias() {
        return categoriasDisponibles;
    }

    /**
     * Obtiene recetas filtradas según la búsqueda actual.
     *
     * @return LiveData con lista de recetas filtradas
     */
    public LiveData<List<Receta>> getRecetasFiltradas() {
        return recetasFiltradas;
    }

    /**
     * Obtiene mensajes de error para mostrar en la UI.
     *
     * @return LiveData con mensajes de error
     */
    public LiveData<String> getMensajeError() {
        return mensajeError;
    }

    /**
     * Obtiene progreso de subida de imágenes.
     *
     * @return LiveData con porcentaje de progreso
     */
    public LiveData<Integer> getProgresoSubida() {
        return progresoSubida;
    }

    /**
     * Obtiene mensajes de éxito para mostrar en la UI.
     *
     * @return LiveData con mensajes de éxito
     */
    public LiveData<String> getMensajeExito() {
        return mensajeExito;
    }

    /**
     * Obtiene una receta específica por su ID.
     *
     * @param id ID de la receta a obtener
     * @return LiveData con la receta solicitada
     */
    public LiveData<Receta> getRecetaById(long id) {
        return repositorioRecetas.getRecetaById(id);
    }

    /**
     * Obtiene notificaciones de actualización de favoritos.
     *
     * @return LiveData con ID de receta actualizada
     */
    public LiveData<Long> getFavoritoActualizado() {
        return favoritoActualizado;
    }

    /**
     * Obtiene el estado de carga actual.
     *
     * @return LiveData con estado de carga
     */
    public LiveData<Boolean> getEstadoCargando() {
        return estadoCargando;
    }

    /**
     * Obtiene el ID de instancia para debugging.
     *
     * @return ID único de esta instancia ViewModel
     */
    public String getInstanciaId() {
        return idInstancia;
    }

    // ==================== INTERFACE PARA CALLBACKS DE IMAGEN ====================

    /**
     * Interfaz para recibir resultados de operaciones con imágenes.
     */
    public interface OnImagenSubidaListener {
        /**
         * Se ejecuta cuando la imagen se sube exitosamente.
         *
         * @param url Ruta local de la imagen guardada
         */
        void onImagenSubida(String url);

        /**
         * Se ejecuta cuando ocurre un error en la subida.
         *
         * @param mensaje Mensaje de error descriptivo
         */
        void onError(String mensaje);
    }
}