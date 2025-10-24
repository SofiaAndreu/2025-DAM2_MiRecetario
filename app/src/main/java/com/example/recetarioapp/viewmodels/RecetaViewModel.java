package com.example.recetarioapp.viewmodels;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.recetarioapp.models.Receta;
import com.example.recetarioapp.repository.RecetaRepository;

import java.util.List;

/**
 * ViewModel para gestión de recetas en Interfaz
 * Intermediario entre Activity y Repo
 */
public class RecetaViewModel extends AndroidViewModel {

    private final RecetaRepository recetaRepository;

    //LiveData
    //Datos que observa la interfaz -> si cambian se actualiza automaticamente
    private final LiveData<List<Receta>> todasLasRecetas;
    private final LiveData<List<Receta>> favs;
    private final LiveData<List<String>> categorias;
    //Filtros de busqueda
    private final MutableLiveData<String> busquedaQuery = new MutableLiveData<>();
    private final MutableLiveData<String> filtroCategoria = new MutableLiveData<>();
    private final MutableLiveData<String> filtroDificultad = new MutableLiveData<>();
    private final MutableLiveData<Integer> filtroTiempo = new MutableLiveData<>();

    private final LiveData<List<Receta>> recetasFiltradas;

    //Estados
    private final MutableLiveData<String> msgError = new MutableLiveData<>();
    private final MutableLiveData<String> msgExito = new MutableLiveData<>();
    private final MutableLiveData<Boolean> msgLoading = new MutableLiveData<>();
    private final MutableLiveData<Integer> msgProgresoSubida = new MutableLiveData<>();

    //CONSTRUCTOR
    public RecetaViewModel(@NonNull Application application) {
        super(application);

        recetaRepository = new RecetaRepository(application);

        todasLasRecetas = recetaRepository.getAllRecetas();
        favs = recetaRepository.getFavs();
        categorias = recetaRepository.getCategorias();

        //LiveData derivado de otro LiveData -> BUSQUEDA REACTIVA
        //  switchMap -> reacciona cada vez que cambia el valor de busqueda
        recetasFiltradas = Transformations.switchMap(busquedaQuery, query ->{
            if(query == null || query.trim().isEmpty()) { //si esta vacio -> ver todas
                return todasLasRecetas;
            } else {
                return recetaRepository.buscarPorNombre(query);
            }
        });
    } //-------------------------------------------------------------------------------------

    //INSERTAR NUEVA RECETA
    public void insertarReceta(Receta receta){
        msgLoading.postValue(true);
        recetaRepository.insertarReceta(receta, new RecetaRepository.OnRecetaGuardadaListener() {
            @Override
            public void onSuccess(Receta recetaG) {
                msgLoading.postValue(false);
                msgExito.postValue("Receta guardada correctamente");
            }
            @Override
            public void onError(String mensaje) {
                msgLoading.postValue(false);
                msgError.postValue(mensaje);
            }
        });
    }

    //ACTUALIZAR RECETA - CORREGIDO
    public void actualizarReceta(Receta receta){
        msgLoading.postValue(true); // Cambiado a postValue
        recetaRepository.actualizarReceta(receta, new RecetaRepository.OnRecetaGuardadaListener() {
            @Override
            public void onSuccess(Receta recetaA) {
                msgLoading.postValue(false);
                msgExito.postValue("Receta actualizada correctamente");
            }
            @Override
            public void onError(String mensaje) {
                msgLoading.postValue(false);
                msgError.postValue(mensaje);
            }
        });
    }

    //ELIMINAR RECETA - CORREGIDO
    public void eliminarReceta(Receta receta){
        msgLoading.postValue(true); // Cambiado a postValue
        recetaRepository.eliminarReceta(receta, new RecetaRepository.OnRecetaEliminadaListener() {
            @Override
            public void onSuccess() {
                msgLoading.postValue(false);
                msgExito.postValue("Receta eliminada correctamente");
            }
            @Override
            public void onError(String mensaje) {
                msgLoading.postValue(false);
                msgError.postValue(mensaje);
            }
        });
    }

    //MARCAR/DESMARCAR FAVORITA
    public void marcarFavorita(long id, boolean isFav){
        recetaRepository.establecerFavorita(id, isFav);
    }

    //SUBIR IMAGEN - YA ESTÁ CORRECTO CON postValue
    public void guardarImagenLocal(Uri imageUri, OnImagenSubidaListener listener) {
        msgProgresoSubida.postValue(0); // Cambiado a postValue
        recetaRepository.guardarImagenLocal(imageUri, new RecetaRepository.OnImagenSubidaListener() {
            @Override
            public void onSuccess(String path) {
                msgProgresoSubida.postValue(100);
                listener.onImagenSubida(path);
            }

            @Override
            public void onProgress(int porcentaje) {
                msgProgresoSubida.postValue(porcentaje);
            }

            @Override
            public void onError(String mensaje) {
                msgError.postValue(mensaje);
                listener.onError(mensaje);
            }
        });
    } // ----------------------------------------------------------------------------------

    //BUSCAR/FILTRAR

    //BUSCAR
    public void buscar(String query){
        busquedaQuery.setValue(query); // Este puede ser setValue porque se llama desde UI thread
    }
    //FILTRAR CATEGORIA
    public LiveData<List<Receta>> filtrarPorCategoria(String categoria){
        return recetaRepository.getRecetasPorCategoria(categoria);
    }
    //FILTRAR DIFICULTAD
    public LiveData<List<Receta>> filtrarPorDificultad(String dificultad){
        return recetaRepository.getRecetasPorDificultad(dificultad);
    }
    //FILTRAR POR TIEMPO PREPARACION
    public LiveData<List<Receta>> filtrarPorTiempo(int tiempo){
        return recetaRepository.getRecetasPorTiempo(tiempo);
    }
    //----------------------------------------------------------------------

    //SINCRONIZAR CON FIREBASE - CORREGIDO
    public void sincronizar(){
        msgLoading.postValue(true); // Cambiado a postValue
        recetaRepository.sincronizarFBaLocal();
        msgLoading.postValue(false);
        msgExito.postValue("Sincronizando...");
    }

    //LIMPIAR MENSAJES EXITO/ERROR
    public void limpiarMensajesE(){
        msgExito.setValue(null); // Estos pueden ser setValue si se llaman desde UI thread
        msgError.setValue(null);
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