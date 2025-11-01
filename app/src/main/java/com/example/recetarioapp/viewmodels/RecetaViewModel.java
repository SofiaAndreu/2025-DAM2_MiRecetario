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
 * ViewModel intermediario entre interfaz <-> datos RecetaRepository ✓
 */
public class RecetaViewModel extends AndroidViewModel {

    //ATRIBUTOS PRINCIPALES
    private final RecetaRepository recetaRepository;

    //LIVEDATA - Datos que observa la interfaz (se actualiza automaticamente)
    private final LiveData<List<Receta>> todasLasRecetas;
    private final LiveData<List<Receta>> favs;
    private final LiveData<List<String>> categorias;

    //Filtros + Busqueda
    private final MutableLiveData<String> busquedaQuery = new MutableLiveData<>();
    private final MutableLiveData<String> filtroCategoria = new MutableLiveData<>();
    private final MutableLiveData<String> filtroDificultad = new MutableLiveData<>();
    private final MutableLiveData<Integer> filtroTiempo = new MutableLiveData<>();

    //Lista Recetas - resultante de la búsqueda/filtros
    private final LiveData<List<Receta>> recetasFiltradas;

    //Estados/Mensajes UI
    private final MutableLiveData<String> msgError = new MutableLiveData<>();
    private final MutableLiveData<String> msgExito = new MutableLiveData<>();
    private final MutableLiveData<Boolean> msgLoading = new MutableLiveData<>();
    private final MutableLiveData<Integer> msgProgresoSubida = new MutableLiveData<>();

    // Añade este LiveData para notificar cambios en favoritos
    private final MutableLiveData<Long> favoritoActualizado = new MutableLiveData<>();
    public LiveData<Long> getFavoritoActualizado() {
        return favoritoActualizado;
    }

    //CONSTRUCTOR
    public RecetaViewModel(@NonNull Application application) {
        super(application);
        recetaRepository = new RecetaRepository(application);

        //Obtiene LiveData iniciales
        todasLasRecetas = recetaRepository.getAllRecetas();
        favs = recetaRepository.getFavs();
        categorias = recetaRepository.getCategorias();

        //LiveData derivado de otro LiveData -> BUSQUEDA REACTIVA
        //switchMap -> reacciona cada vez que cambia el valor de busqueda
        recetasFiltradas = Transformations.switchMap(busquedaQuery, query ->{
            if(query == null || query.trim().isEmpty()) { //si esta vacio -> ver todas
                return todasLasRecetas;
            } else { // si tiene texto -> buscar por nombre
                return recetaRepository.buscarPorNombre(query);
            }
        });
    } // --------------------------------------------------------------------------- //

    //INSERTAR NUEVA RECETA
    public void insertarReceta(Receta receta){
        //Llama a Repo. para insertar
        msgLoading.postValue(true);
        recetaRepository.insertarReceta(receta, new RecetaRepository.OnRecetaGuardadaListener() {
            @Override //Éxito
            public void onSuccess(Receta recetaG) {
                msgLoading.postValue(false);
                msgExito.postValue("Receta guardada correctamente");
            }
            @Override //Error
            public void onError(String mensaje) {
                msgLoading.postValue(false);
                msgError.postValue(mensaje);
            }
        });
    } // --------------------------------------------------------------------------- //


    //ACTUALIZAR RECETA
    public void actualizarReceta(Receta receta){
        //Llama a Repo. para actualizar
        msgLoading.postValue(true);
        recetaRepository.actualizarReceta(receta, new RecetaRepository.OnRecetaGuardadaListener() {
            @Override //Éxito
            public void onSuccess(Receta recetaA) {
                msgLoading.postValue(false);
                msgExito.postValue("Receta actualizada correctamente");
            }
            @Override //Error
            public void onError(String mensaje) {
                msgLoading.postValue(false);
                msgError.postValue(mensaje);
            }
        });
    } // --------------------------------------------------------------------------- //

    //ELIMINAR RECETA
    public void eliminarReceta(Receta receta){
        //Llama a Repo. para eliminar
        msgLoading.postValue(true);
        recetaRepository.eliminarReceta(receta, new RecetaRepository.OnRecetaEliminadaListener() {
            @Override //Éxito
            public void onSuccess() {
                msgLoading.postValue(false);
                msgExito.postValue("Receta eliminada correctamente");
            }
            @Override //Error
            public void onError(String mensaje) {
                msgLoading.postValue(false);
                msgError.postValue(mensaje);
            }
        });
    } // --------------------------------------------------------------------------- //

    // MARCAR/DESMARCAR FAVORITA - MODIFICADO
    public void marcarFavorita(long id, boolean isFav){
        //Llama a Repo. para marcar directamente.
        recetaRepository.establecerFavorita(id, isFav);
        // Notificar que este favorito fue actualizado
        favoritoActualizado.postValue(id);
    }// --------------------------------------------------------------------------- //

    //SUBIR IMAGEN
    public void guardarImagenLocal(Uri imageUri, OnImagenSubidaListener listener) {
        //Sube img. actualizando progreso
        msgProgresoSubida.postValue(0); //Inicia msg en 0
        recetaRepository.guardarImagenLocal(imageUri, new RecetaRepository.OnImagenSubidaListener() {
            @Override //Éxito
            public void onSuccess(String path) {
                msgProgresoSubida.postValue(100);
                listener.onImagenSubida(path);
            }
            @Override //Progreso
            public void onProgress(int porcentaje) {
                msgProgresoSubida.postValue(porcentaje);
            }
            @Override //Error
            public void onError(String mensaje) {
                msgError.postValue(mensaje);
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    listener.onError(mensaje);
                });
            }
        });
    } // ---------------------------------------------------------------------------------- //

    //BUSCAR/FILTRAR
    public void buscar(String query){
        //Actualiza valor de busqueda - transformación reactiva
        busquedaQuery.setValue(query);
    }
    // --------------------------------------------------------------------- //

    //FILTRAR CATEGORIA
    public LiveData<List<Receta>> filtrarPorCategoria(String categoria){
        //Livedata con filtro x categoria seleccionada
        return recetaRepository.getRecetasPorCategoria(categoria);
    }
    // --------------------------------------------------------------------- //

    //FILTRAR DIFICULTAD
    public LiveData<List<Receta>> filtrarPorDificultad(String dificultad){
        //Livedata con filtro x dificultad seleccionada
        return recetaRepository.getRecetasPorDificultad(dificultad);
    }
    // --------------------------------------------------------------------- //

    //FILTRAR POR TIEMPO PREPARACION
    public LiveData<List<Receta>> filtrarPorTiempo(int tiempo){
        //Livedata con filtro x tiempo seleccionado
        return recetaRepository.getRecetasPorTiempo(tiempo);
    }
    // --------------------------------------------------------------------- //

    //SINCRONIZAR CON FIREBASE a LOCAL
    public void sincronizar(){
        msgLoading.postValue(true);
        recetaRepository.sincronizarFBaLocal();
        msgLoading.postValue(false);
        msgExito.postValue("Sincronizando...");
    }

    //LIMPIAR MENSAJES EXITO/ERROR
    public void limpiarMensajesE(){
        //Reset mensajes Exito/Error
        msgExito.setValue(null);
        msgError.setValue(null);
    }

    // MÉTODO DE DIAGNÓSTICO
    public void debugBuscar(String query) {
        Log.d("BUSQUEDA", "Buscando: '" + query + "'");
        buscar(query);
    }

    //GETTERS LIVEDATA
    public LiveData<List<Receta>> getTodasLasRecetas() {
        return todasLasRecetas;
    }
    public LiveData<List<Receta>> getFavs() {
        return favs;
    }
    public LiveData<List<String>> getCategorias() {
        return categorias;
    }
    public LiveData<List<Receta>> getRecetasFiltradas() {
        return recetasFiltradas;
    }
    public LiveData<String> getMensajeError() {
        return msgError;
    }
    public LiveData<Integer> getProgresoSubida() {
        return msgProgresoSubida;
    }
    public LiveData<String> getMensajeExito() {
        return msgExito;
    }

    public LiveData<Receta> getRecetaById(long id) {
        return recetaRepository.getRecetaById(id);
    }


    //INTERFACE
    public interface OnImagenSubidaListener{
        void onImagenSubida(String url);
        void onError(String mensaje);
    }

}