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

//Data Access Object (DAO) para operaciones de base de datos con la entidad Receta
//Define todas las operaciones CRUD (Create, Read, Update, Delete) y consultas
//específicas para la gestión de recetas en la base de datos local
//Utiliza LiveData para proporcionar datos observables que actualizan
//automáticamente la UI cuando cambian los datos subyacentes
@Dao
public interface RecetaDAO {

    //==================== OPERACIONES CRUD BÁSICAS ====================

    //Inserta una receta en la base de datos
    //Si ya existe una receta con el mismo ID, la reemplaza
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Receta receta);

    //Inserta múltiples recetas en la base de datos
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Receta> recetas);

    //Actualiza una receta existente en la base de datos
    @Update
    void update(Receta receta);

    //Elimina una receta específica de la base de datos
    @Delete
    void delete(Receta receta);

    //==================== CONSULTAS DE LECTURA ====================

    //Obtiene todas las recetas ordenadas por fecha de creación (más recientes primero)
    @Query("SELECT * FROM recetas ORDER BY fechaCreacion DESC")
    LiveData<List<Receta>> getAllRecetas();

    //Obtiene una receta específica por su ID local
    @Query("SELECT * FROM recetas WHERE id = :id")
    LiveData<Receta> getRecetaById(long id);

    //Obtiene una receta por su ID de Firebase (para sincronización)
    @Query("SELECT * FROM recetas WHERE firebaseId = :firebaseId")
    Receta getRecetaByFirebaseId(String firebaseId);

    //==================== CONSULTAS DE FAVORITOS ====================

    //Obtiene todas las recetas marcadas como favoritas
    @Query("SELECT * FROM recetas WHERE isFav = 1 ORDER BY fechaCreacion DESC")
    LiveData<List<Receta>> getFavs();

    //Actualiza el estado de favorito de una receta
    @Query("UPDATE recetas SET isFav = :isFav WHERE id = :id")
    void actualizarFavorita(long id, boolean isFav);

    //==================== CONSULTAS DE BÚSQUEDA ====================

    //Busca recetas cuyo nombre contenga el texto de búsqueda
    @Query("SELECT * FROM recetas WHERE nombre LIKE '%' || :query || '%' ORDER BY fechaCreacion DESC")
    LiveData<List<Receta>> buscarPorNombre(String query);

    //==================== CONSULTAS DE FILTRADO ====================

    //Filtra recetas por categoría específica
    @Query("SELECT * FROM recetas WHERE categoria = :categoria ORDER BY fechaCreacion DESC")
    LiveData<List<Receta>> getRecetasPorCategoria(String categoria);

    //Filtra recetas por nivel de dificultad
    @Query("SELECT * FROM recetas WHERE dificultad = :dificultad ORDER BY fechaCreacion DESC")
    LiveData<List<Receta>> getRecetasPorDificultad(String dificultad);

    //Filtra recetas por tiempo máximo de preparación
    @Query("SELECT * FROM recetas WHERE tiempoPreparacion <= :tiempoMax ORDER BY tiempoPreparacion ASC")
    LiveData<List<Receta>> getRecetasPorTiempo(int tiempoMax);
}