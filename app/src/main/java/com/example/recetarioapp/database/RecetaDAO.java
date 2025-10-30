package com.example.recetarioapp.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.recetarioapp.models.Receta;

import java.util.List;

/**
 * RECETAS Data Acces Object - Definicion operaciones BD ✓
 */
@Dao
public interface RecetaDAO {

    //INSERTAR Receta(s)
    @Insert(onConflict = OnConflictStrategy.REPLACE) // 1
    long insert(Receta receta);
    @Insert(onConflict = OnConflictStrategy.REPLACE) // >1
    void insertAll(List<Receta> recetas);
    // ---------------------------------------------------- //

    //ACTUALIZAR
    @Update
    void update(Receta receta);
    // ---------------------------------------------------- //

    //ELIMINAR Receta(s)
    @Delete
    void delete(Receta receta); // 1
    @Query("DELETE FROM recetas") // TODAS
    void deleteAll();
    // ---------------------------------------------------- //

    //OBTENER TODAS (ordenadas x fecha)
    //con LiveData -> refleja los cambios automaticamente en interfaz
    @Query("SELECT * FROM recetas ORDER BY fechaCreacion DESC")
    LiveData<List<Receta>> getAllRecetas();
    //Sincronizacion manual
    @Query("SELECT * FROM recetas ORDER BY fechaCreacion DESC")
    List<Receta> getAllRecetasSync();
    // ---------------------------------------------------- //

    //OBTENER X ID
    @Query("SELECT * FROM recetas WHERE id = :id")
    LiveData<Receta> getRecetaById(long id);
    // ---------------------------------------------------- //

    //OBTENER X FIREBASE ID
    @Query("SELECT * FROM recetas WHERE firebaseId = :firebaseId")
    Receta getRecetaByFirebaseId(String firebaseId);
    // ---------------------------------------------------- //

    //FAVORITOS
    @Query("SELECT * FROM recetas WHERE isFav = 1 ORDER BY fechaCreacion DESC")
    LiveData<List<Receta>> getFavs();
    @Query("UPDATE recetas SET isFav = :isFav WHERE id = :id")
    void actualizarFavorita(long id, boolean isFav);
    // ---------------------------------------------------- //

    //BUSCAR X NOMBRE
    @Query("SELECT * FROM recetas WHERE nombre LIKE '%' || :query || '%' ORDER BY fechaCreacion DESC")
    LiveData<List<Receta>> buscarPorNombre(String query);
    // ---------------------------------------------------- //

    //FILTRAR ---------------------------------------------------- //
    //categoria
    @Query("SELECT * FROM recetas WHERE categoria = :categoria ORDER BY fechaCreacion DESC")
    LiveData<List<Receta>> getRecetasPorCategoria(String categoria);
    //dificultad
    @Query("SELECT * FROM recetas WHERE dificultad = :dificultad ORDER BY fechaCreacion DESC")
    LiveData<List<Receta>> getRecetasPorDificultad(String dificultad);
    //tiempo
    @Query("SELECT * FROM recetas WHERE tiempoPreparacion <= :tiempoMax ORDER BY tiempoPreparacion ASC")
    LiveData<List<Receta>> getRecetasPorTiempo(int tiempoMax);
    // ---------------------------------------------------- //

    //OBTENER CATEGORIAS UNICAS
    @Query("SELECT DISTINCT categoria FROM recetas WHERE categoria IS NOT NULL ORDER BY categoria ASC")
    LiveData<List<String>> getCategorias();
    // ---------------------------------------------------- //

    //CONTADOR RECETAS TOTALES
    @Query("SELECT COUNT(*) FROM recetas")
    LiveData<Integer> getConteoRecetas();
    // ---------------------------------------------------- //
}
