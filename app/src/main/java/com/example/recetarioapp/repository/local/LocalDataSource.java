package com.example.recetarioapp.repository.local;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.example.recetarioapp.database.RecetaDAO;
import com.example.recetarioapp.database.RecetasBD;
import com.example.recetarioapp.models.Receta;
import java.util.Date;
import java.util.List;

//Fuente de datos local que encapsula todas las operaciones con Room Database
// - Proporciona API abstracta para las operaciones CRUD locales
// - Separa lógica de acceso a datos del resto de la aplicación
// - Todas las operaciones de escritura se ejecutan en background mediante ExecutorService (de RecetasBD)
public class LocalDataSource {

    private final RecetaDAO recetaDAO;

    //Constructor que inicializa la conexión con la base de datos local
    public LocalDataSource(Application app) {
        RecetasBD baseDatos = RecetasBD.getInstance(app);
        this.recetaDAO = baseDatos.recetaDAO();
    }

    //==================== OPERACIONES DE LECTURA OBSERVABLES ====================

    //Obtiene todas las recetas ordenadas por fecha de creación (más recientes primero)
    public LiveData<List<Receta>> getAllRecetas() {
        return recetaDAO.getAllRecetas();
    }

    //Obtiene una receta específica por su ID local
    public LiveData<Receta> getRecetaById(long id) {
        return recetaDAO.getRecetaById(id);
    }

    //Obtiene todas las recetas marcadas como favoritas
    public LiveData<List<Receta>> getFavoritas() {
        return recetaDAO.getFavs();
    }

    //Busca recetas cuyo nombre contenga el texto de búsqueda
    public LiveData<List<Receta>> buscarPorNombre(String query) {
        return recetaDAO.buscarPorNombre(query);
    }

    //Filtra recetas por categoría específica
    public LiveData<List<Receta>> getRecetasPorCategoria(String categoria) {
        return recetaDAO.getRecetasPorCategoria(categoria);
    }

    //Filtra recetas por nivel de dificultad
    public LiveData<List<Receta>> getRecetasPorDificultad(String dificultad) {
        return recetaDAO.getRecetasPorDificultad(dificultad);
    }

    //Filtra recetas por tiempo máximo de preparación
    public LiveData<List<Receta>> getRecetasPorTiempo(int tiempoMax) {
        return recetaDAO.getRecetasPorTiempo(tiempoMax);
    }

    //==================== OPERACIONES DE ESCRITURA ASÍNCRONAS ====================

    //Inserta una nueva receta en la base de datos local
    //Actualiza automáticamente las fechas de creación y modificación
    public void insertar(Receta receta, OnInsertListener listener, OnErrorListener errorListener) {
        RecetasBD.bdWriteExecutor.execute(() -> {
            try {
                //Establecer timestamps antes de insertar
                receta.setFechaCreacion(new Date());
                receta.setFechaModificacion(new Date());

                long idGenerado = recetaDAO.insert(receta);
                listener.onInserted(idGenerado);
            } catch (Exception excepcion) {
                errorListener.onError(excepcion.getMessage());
            }
        });
    }

    //Actualiza una receta existente en la base de datos local
    //Actualiza automáticamente la fecha de modificación
    public void actualizar(Receta receta, Runnable onSuccess, OnErrorListener errorListener) {
        RecetasBD.bdWriteExecutor.execute(() -> {
            try {
                receta.setFechaModificacion(new Date());
                recetaDAO.update(receta);
                if (onSuccess != null) onSuccess.run();
            } catch (Exception excepcion) {
                if (errorListener != null) errorListener.onError(excepcion.getMessage());
            }
        });
    }

    //Elimina una receta de la base de datos local
    public void eliminar(Receta receta, Runnable onSuccess, OnErrorListener errorListener) {
        RecetasBD.bdWriteExecutor.execute(() -> {
            try {
                recetaDAO.delete(receta);
                onSuccess.run();
            } catch (Exception excepcion) {
                errorListener.onError(excepcion.getMessage());
            }
        });
    }

    //==================== OPERACIONES ESPECIALIZADAS ====================

    //Busca una receta por su ID de Firebase (para sincronización)
    public Receta getRecetaByFirebaseId(String firebaseId) {
        try {
            return recetaDAO.getRecetaByFirebaseId(firebaseId);
        } catch (Exception excepcion) {
            android.util.Log.e("LocalDataSource", "Error buscando receta por firebaseId: " + excepcion.getMessage());
            return null;
        }
    }

    //Establece o remueve el estado de favorito de una receta
    public void establecerFavorita(long id, boolean esFavorito) {
        RecetasBD.bdWriteExecutor.execute(() ->
                recetaDAO.actualizarFavorita(id, esFavorito)
        );
    }

    //Inserta múltiples recetas en lote (para sincronización)
    public void insertarVarias(List<Receta> recetas) {
        RecetasBD.bdWriteExecutor.execute(() ->
                recetaDAO.insertAll(recetas)
        );
    }

    //==================== INTERFACES DE CALLBACK ====================

    //Interfaz para recibir el resultado de operaciones de inserción
    public interface OnInsertListener {
        void onInserted(long id);
    }

    //Interfaz para manejar errores en operaciones asíncronas
    public interface OnErrorListener {
        void onError(String mensaje);
    }
}