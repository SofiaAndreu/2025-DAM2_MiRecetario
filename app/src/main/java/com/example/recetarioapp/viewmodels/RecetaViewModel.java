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

    //Identificador único para debugging de instancias ViewModel
    private final String idInstancia = java.util.UUID.randomUUID().toString().substring(0, 8);

    // ==================== LIVE DATA PARA DATOS OBSERVABLES ====================

    //Datos principales observables desde el Repository
    private final LiveData<List<Receta>> todasLasRecetas;
    private final LiveData<List<Receta>> recetasFavoritas;

    //Controles de búsqueda y filtrado
    private final MutableLiveData<String> queryBusqueda = new MutableLiveData<>();

    //LiveData reactivo que combina búsqueda con datos base
    private final LiveData<List<Receta>> recetasFiltradas;

    // ==================== ESTADOS DE INTERFAZ OBSERVABLES ====================
    private final MutableLiveData<String> mensajeError = new MutableLiveData<>(); //Error
    private final MutableLiveData<String> mensajeExito = new MutableLiveData<>();//Éxito
    private final MutableLiveData<Boolean> estadoCargando = new MutableLiveData<>(); //Cargando
    private final MutableLiveData<Integer> progresoSubida = new MutableLiveData<>();//Progreso

    //Notificador específico para cambios en favoritos
    private final MutableLiveData<Long> favoritoActualizado = new MutableLiveData<>();

    //Constructor que inicializa todas las dependencias y configuraciones reactivas.
    public RecetaViewModel(@NonNull Application application) {
        super(application);
        repositorioRecetas = new RecetaRepository(application);

        //Log de creación para debugging de ciclo de vida
        Log.d("VIEWMODEL", "Nueva Instancia ViewModel: " + idInstancia);

        //Inicializar LiveData desde el Repository
        todasLasRecetas = repositorioRecetas.getAllRecetas();
        recetasFavoritas = repositorioRecetas.getFavs();

        //Configurar transformación reactiva para búsqueda en tiempo real
        recetasFiltradas = Transformations.switchMap(queryBusqueda, query -> {
            if (query == null || query.trim().isEmpty()) {
                //Query vacía - mostrar todas las recetas
                return todasLasRecetas;
            } else {
                //Query con texto - buscar recetas que coincidan
                return repositorioRecetas.buscarPorNombre(query);
            }
        });
    }

    // ==================== OPERACIONES CRUD CON RECETAS ====================
    /**
     * Inserta una nueva receta en el sistema.
     * Gestiona el estado de carga y notifica el resultado.
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

    //Actualiza una receta existente.
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

    //Elimina una receta del sistema.
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
     * -> uriImagen URI de la imagen a guardar
     * -> listener Callback para resultado de la operación
     */
    public void guardarImagenLocal(Uri uriImagen, OnImagenSubidaListener listener) {
        progresoSubida.postValue(0); //Iniciar progreso
        repositorioRecetas.guardarImagenLocal(uriImagen, new RecetaRepository.OnImagenSubidaListener() {
            @Override
            public void onSuccess(String ruta) {
                progresoSubida.postValue(100); //Completado
                listener.onImagenSubida(ruta);
            }
            @Override
            public void onProgress(int porcentaje) {
                progresoSubida.postValue(porcentaje);
            }
            @Override
            public void onError(String mensaje) {
                mensajeError.postValue(mensaje);
                //Ejecutar en main thread para seguridad
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    listener.onError(mensaje);
                });
            }
        });
    }

    // ==================== SINCRONIZACIÓN Y RESINCROZACIÓN ====================

    // Ejecuta sincronización de recetas desde Firebase a local.
    public void sincronizar() {
        Log.d("VIEWMODEL", "Sincronizando - Instancia: " + idInstancia);
        estadoCargando.postValue(true);
        repositorioRecetas.sincronizarFBaLocal();
        estadoCargando.postValue(false);
        mensajeExito.postValue("Sincronizando...");
    }

    // ==================== BÚSQUEDA Y FILTRADO ====================

    /**
     * Ejecuta búsqueda en tiempo real de recetas por nombre.
     * La transformación reactiva se encarga de actualizar recetasFiltradas.
     * -> query Texto de búsqueda
     */
    public void buscar(String query) {
        queryBusqueda.setValue(query);
    }

    // Obtiene recetas filtradas por categoría específica.
    public LiveData<List<Receta>> filtrarPorCategoria(String categoria) {
        return repositorioRecetas.getRecetasPorCategoria(categoria);
    }

    //Obtiene recetas filtradas por nivel de dificultad.
    public LiveData<List<Receta>> filtrarPorDificultad(String dificultad) {
        return repositorioRecetas.getRecetasPorDificultad(dificultad);
    }

    //Obtiene recetas filtradas por tiempo máximo de preparación.
    public LiveData<List<Receta>> filtrarPorTiempo(int tiempoMax) {
        return repositorioRecetas.getRecetasPorTiempo(tiempoMax);
    }

    // ==================== UTILIDADES Y MÉTODOS DE APOYO ====================

    //Limpia los mensajes de éxito y error de la UI.
    public void limpiarMensajesE() {
        mensajeExito.setValue(null);
        mensajeError.setValue(null);
    }

    // ==================== GETTERS PARA LIVE DATA ====================

    //Obtiene todas las recetas disponibles.
    public LiveData<List<Receta>> getTodasLasRecetas() {
        return todasLasRecetas;
    }

    //Obtiene las recetas marcadas como favoritas.
    public LiveData<List<Receta>> getFavs() {
        return recetasFavoritas;
    }

    //Obtiene recetas filtradas según la búsqueda actual.
    public LiveData<List<Receta>> getRecetasFiltradas() {
        return recetasFiltradas;
    }

    //Obtiene mensajes de error para mostrar en la UI.
    public LiveData<String> getMensajeError() {
        return mensajeError;
    }

    //Obtiene progreso de subida de imágenes.
    public LiveData<Integer> getProgresoSubida() {
        return progresoSubida;
    }

    //Obtiene mensajes de éxito para mostrar en la UI.
    public LiveData<String> getMensajeExito() {
        return mensajeExito;
    }

    //Obtiene una receta específica por su ID.
    public LiveData<Receta> getRecetaById(long id) {
        return repositorioRecetas.getRecetaById(id);
    }

    //Obtiene notificaciones de actualización de favoritos.
    public LiveData<Long> getFavoritoActualizado() {
        return favoritoActualizado;
    }

    // ==================== INTERFACE PARA CALLBACKS DE IMAGEN ====================

    //Interfaz para recibir resultados de operaciones con imágenes.
    public interface OnImagenSubidaListener {
        //Cuando la imagen se sube exitosamente.
        void onImagenSubida(String url);

        //Se ejecuta cuando ocurre un error en la subida.
        void onError(String mensaje);
    }
}