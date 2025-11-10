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
 * Data Access Object (DAO) para operaciones de base de datos con la entidad Receta.
 *
 * Define todas las operaciones CRUD (Create, Read, Update, Delete) y consultas
 * específicas para la gestión de recetas en la base de datos local.
 * Utiliza LiveData para proporcionar datos observables que actualizan
 * automáticamente la UI cuando cambian los datos subyacentes.
 */
@Dao
public interface RecetaDAO {

    // ==================== OPERACIONES CRUD BÁSICAS ====================

    /**
     * Inserta una receta en la base de datos.
     * Si ya existe una receta con el mismo ID, la reemplaza.
     *
     * @param receta Receta a insertar
     * @return ID de la receta insertada
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Receta receta);

    /**
     * Inserta múltiples recetas en la base de datos.
     *
     * @param recetas Lista de recetas a insertar
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Receta> recetas);

    /**
     * Actualiza una receta existente en la base de datos.
     *
     * @param receta Receta con los datos actualizados
     */
    @Update
    void update(Receta receta);

    /**
     * Elimina una receta específica de la base de datos.
     *
     * @param receta Receta a eliminar
     */
    @Delete
    void delete(Receta receta);

    /**
     * Elimina todas las recetas de la base de datos.
     * ¡PRECAUCIÓN: Operación destructiva!
     */
    @Query("DELETE FROM recetas")
    void deleteAll();

    // ==================== CONSULTAS DE LECTURA ====================

    /**
     * Obtiene todas las recetas ordenadas por fecha de creación (más recientes primero).
     *
     * @return LiveData con lista observable de recetas
     */
    @Query("SELECT * FROM recetas ORDER BY fechaCreacion DESC")
    LiveData<List<Receta>> getAllRecetas();

    /**
     * Obtiene todas las recetas de forma síncrona (para operaciones en background).
     *
     * @return Lista de recetas ordenadas por fecha
     */
    @Query("SELECT * FROM recetas ORDER BY fechaCreacion DESC")
    List<Receta> getAllRecetasSync();

    /**
     * Obtiene una receta específica por su ID local.
     *
     * @param id ID local de la receta
     * @return LiveData con la receta observada
     */
    @Query("SELECT * FROM recetas WHERE id = :id")
    LiveData<Receta> getRecetaById(long id);

    /**
     * Obtiene una receta por su ID de Firebase (para sincronización).
     *
     * @param firebaseId ID único de Firebase Firestore
     * @return Receta correspondiente al ID de Firebase
     */
    @Query("SELECT * FROM recetas WHERE firebaseId = :firebaseId")
    Receta getRecetaByFirebaseId(String firebaseId);

    // ==================== OPERACIONES DE MANTENIMIENTO ====================

    /**
     * Elimina recetas que no tienen ingredientes o pasos válidos.
     * Limpia la base de datos de registros corruptos o incompletos.
     */
    @Query("DELETE FROM recetas WHERE ingredientes IS NULL OR ingredientes = '' OR pasos IS NULL OR pasos = ''")
    void eliminarRecetasVacias();

    // ==================== CONSULTAS DE FAVORITOS ====================

    /**
     * Obtiene todas las recetas marcadas como favoritas.
     *
     * @return LiveData con lista observable de recetas favoritas
     */
    @Query("SELECT * FROM recetas WHERE isFav = 1 ORDER BY fechaCreacion DESC")
    LiveData<List<Receta>> getFavs();

    /**
     * Actualiza el estado de favorito de una receta.
     *
     * @param id ID de la receta a actualizar
     * @param isFav Nuevo estado de favorito (true/false)
     */
    @Query("UPDATE recetas SET isFav = :isFav WHERE id = :id")
    void actualizarFavorita(long id, boolean isFav);

    // ==================== CONSULTAS DE BÚSQUEDA ====================

    /**
     * Busca recetas cuyo nombre contenga el texto de búsqueda.
     *
     * @param query Texto a buscar en los nombres de recetas
     * @return LiveData con lista observable de recetas que coinciden
     */
    @Query("SELECT * FROM recetas WHERE nombre LIKE '%' || :query || '%' ORDER BY fechaCreacion DESC")
    LiveData<List<Receta>> buscarPorNombre(String query);

    // ==================== CONSULTAS DE FILTRADO ====================

    /**
     * Filtra recetas por categoría específica.
     *
     * @param categoria Categoría a filtrar
     * @return LiveData con recetas de la categoría especificada
     */
    @Query("SELECT * FROM recetas WHERE categoria = :categoria ORDER BY fechaCreacion DESC")
    LiveData<List<Receta>> getRecetasPorCategoria(String categoria);

    /**
     * Filtra recetas por nivel de dificultad.
     *
     * @param dificultad Nivel de dificultad (Fácil, Media, Difícil)
     * @return LiveData con recetas del nivel especificado
     */
    @Query("SELECT * FROM recetas WHERE dificultad = :dificultad ORDER BY fechaCreacion DESC")
    LiveData<List<Receta>> getRecetasPorDificultad(String dificultad);

    /**
     * Filtra recetas por tiempo máximo de preparación.
     *
     * @param tiempoMax Tiempo máximo en minutos
     * @return LiveData con recetas que no exceden el tiempo especificado
     */
    @Query("SELECT * FROM recetas WHERE tiempoPreparacion <= :tiempoMax ORDER BY tiempoPreparacion ASC")
    LiveData<List<Receta>> getRecetasPorTiempo(int tiempoMax);

    // ==================== CONSULTAS DE METADATOS ====================

    /**
     * Obtiene todas las categorías únicas existentes en las recetas.
     *
     * @return LiveData con lista de categorías disponibles
     */
    @Query("SELECT DISTINCT categoria FROM recetas WHERE categoria IS NOT NULL ORDER BY categoria ASC")
    LiveData<List<String>> getCategorias();

    /**
     * Obtiene el número total de recetas en la base de datos.
     *
     * @return LiveData con el conteo de recetas
     */
    @Query("SELECT COUNT(*) FROM recetas")
    LiveData<Integer> getConteoRecetas();
}