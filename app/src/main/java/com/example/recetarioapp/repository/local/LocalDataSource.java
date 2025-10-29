package com.example.recetarioapp.repository.local;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.example.recetarioapp.database.RecetaDAO;
import com.example.recetarioapp.database.RecetasBD;
import com.example.recetarioapp.models.Receta;
import java.util.Date;
import java.util.List;

// ============================================================================
// LocalDataSource.java - Maneja Room Database
// ============================================================================
public class LocalDataSource {

    private final RecetaDAO dao;

    public LocalDataSource(Application app) {
        RecetasBD bd = RecetasBD.getInstance(app);
        this.dao = bd.recetaDAO();
    }

    // Operaciones de lectura (síncronas con LiveData)
    public LiveData<List<Receta>> getAllRecetas() {
        return dao.getAllRecetas();
    }

    public LiveData<Receta> getRecetaById(long id) {
        return dao.getRecetaById(id);
    }

    public LiveData<List<Receta>> getFavoritas() {
        return dao.getFavs();
    }

    public LiveData<List<String>> getCategorias() {
        return dao.getCategorias();
    }

    public LiveData<List<Receta>> buscarPorNombre(String query) {
        return dao.buscarPorNombre(query);
    }

    public LiveData<List<Receta>> getRecetasPorCategoria(String categoria) {
        return dao.getRecetasPorCategoria(categoria);
    }

    public LiveData<List<Receta>> getRecetasPorDificultad(String dificultad) {
        return dao.getRecetasPorDificultad(dificultad);
    }

    public LiveData<List<Receta>> getRecetasPorTiempo(int tiempo) {
        return dao.getRecetasPorTiempo(tiempo);
    }

    // Operaciones de escritura (asíncronas)
    public void insertar(Receta receta, OnInsertListener listener, OnErrorListener errorListener) {
        RecetasBD.bdWriteExecutor.execute(() -> {
            try {
                receta.setFechaCreacion(new Date());
                receta.setFechaModificacion(new Date());
                long id = dao.insert(receta);
                listener.onInserted(id);
            } catch (Exception e) {
                errorListener.onError(e.getMessage());
            }
        });
    }

    public void actualizar(Receta receta, Runnable onSuccess, OnErrorListener errorListener) {
        RecetasBD.bdWriteExecutor.execute(() -> {
            try {
                receta.setFechaModificacion(new Date());
                dao.update(receta);
                if (onSuccess != null) onSuccess.run();
            } catch (Exception e) {
                if (errorListener != null) errorListener.onError(e.getMessage());
            }
        });
    }

    public void eliminar(Receta receta, Runnable onSuccess, OnErrorListener errorListener) {
        RecetasBD.bdWriteExecutor.execute(() -> {
            try {
                dao.delete(receta);
                onSuccess.run();
            } catch (Exception e) {
                errorListener.onError(e.getMessage());
            }
        });
    }

    public void establecerFavorita(long id, boolean isFav) {
        RecetasBD.bdWriteExecutor.execute(() -> dao.actualizarFavorita(id, isFav));
    }

    public void insertarVarias(List<Receta> recetas) {
        RecetasBD.bdWriteExecutor.execute(() -> dao.insertAll(recetas));
    }

    // Interfaces
    public interface OnInsertListener {
        void onInserted(long id);
    }

    public interface OnErrorListener {
        void onError(String mensaje);
    }
}

