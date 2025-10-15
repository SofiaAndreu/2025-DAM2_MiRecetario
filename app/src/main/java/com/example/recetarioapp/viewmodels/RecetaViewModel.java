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

    private final RecetaRepository repository;

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

        repository = new RecetaRepository(application);
        todasLasRecetas = repository.getAllRecetas();
        favs = repository.getFavs();
        categorias = repository.getCategorias();

        //LiveData derivado de otro LiveData -> BUSQUEDA REACTIVA
        //  switchMap -> reacciona cada vez que cambia el valor de busqueda
        recetasFiltradas = Transformations.switchMap(busquedaQuery, query ->{
            if(query == null || query.trim().isEmpty()) { //si esta vacio -> ver todas
                return todasLasRecetas;
            } else {
                return repository.buscarPorNombre(query);
            }
        });
    } //-------------------------------------------------------------------------------------

    //INSERTAR NUEVA RECETA
    public void insertarReceta(Receta receta){
        msgLoading.setValue(true);
        repository.insertarReceta(receta, new RecetaRepository.OnRecetaGuardadaListener() {
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

    //ACTUALIZAR RECETA
    public void actualizarReceta(Receta receta){
        msgLoading.setValue(true);
        repository.actualizarReceta(receta, new RecetaRepository.OnRecetaGuardadaListener() {
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

    //ELIMINAR RECETA
    public void eliminarReceta(Receta receta){
        msgLoading.setValue(true);
        repository.eliminarReceta(receta, new RecetaRepository.OnRecetaEliminadaListener() {
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
        repository.establecerFavorita(id, isFav);
    }

    //SUBIR IMAGEN
    public void subirImagen(Uri imagenUri, OnImagenSubidaListener listener){
        msgProgresoSubida.setValue(0);
        repository.subirImagen(imagenUri, new RecetaRepository.OnImagenSubidaListener(){
            @Override
            public void onSuccess(String url){
                msgProgresoSubida.postValue(100);
                listener.onImagenSubida(url);
            }
            @Override
            public void onProgress(int porcentaje){
                msgProgresoSubida.postValue(porcentaje);
            }
            @Override
            public void onError(String mensaje){
                msgError.postValue(mensaje);
                listener.onError(mensaje);
            }
        });
    } // ----------------------------------------------------------------------------------

    //BUSCAR/FILTRAR

    //BUSCAR
     public void buscar(String query){
        busquedaQuery.setValue(query);
     }
    //FILTRAR CATEGORIA
    public LiveData<List<Receta>> filtrarPorCategoria(String categoria){
        return repository.getRecetasPorCategoria(categoria);
    }
    //FILTRAR DIFICULTAD
    public LiveData<List<Receta>> filtrarPorDificultad(String dificultad){
        return repository.getRecetasPorDificultad(dificultad);
    }
    //FILTRAR POR TIEMPO PREPARACION
    public LiveData<List<Receta>> filtrarPorTiempo(int tiempo){
        return repository.getRecetasPorTiempo(tiempo);
    }
    //----------------------------------------------------------------------

    //SINCRONIZAR CON FIREBASE
    public void sincronizar(){
        msgLoading.setValue(true);
        repository.sincronizarFBaLocal();
        msgLoading.postValue(false);
        msgExito.postValue("Sincronizando...");
    }

    //LIMPIAR MENSAJES EXITO/ERROR
    public void limpiarMensajesE(){
        msgExito.setValue(null);
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


    //INTERFACE
    public interface OnImagenSubidaListener{
        void onImagenSubida(String url);
        void onError(String mensaje);
    }

}
