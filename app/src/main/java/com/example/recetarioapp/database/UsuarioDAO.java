package com.example.recetarioapp.database;

import com.example.recetarioapp.models.Usuario;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

/**
 * Data Access Object (DAO) para operaciones de base de datos con la entidad Usuario.
 *
 * Gestiona las operaciones CRUD para usuarios y proporciona métodos específicos
 * para la autenticación y gestión de perfiles de usuario.
 */
@Dao
public interface UsuarioDAO {

    /**
     * Inserta o reemplaza un usuario en la base de datos.
     *
     * @param usuario Usuario a insertar/actualizar
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertar(Usuario usuario);

    /**
     * Actualiza un usuario existente en la base de datos.
     *
     * @param usuario Usuario con datos actualizados
     */
    @Update
    void actualizar(Usuario usuario);

    /**
     * Obtiene un usuario por su UID de forma observable.
     *
     * @param uid Identificador único del usuario (Firebase Auth)
     * @return LiveData con el usuario observado
     */
    @Query("SELECT * FROM usuarios WHERE uid = :uid")
    LiveData<Usuario> getUsuarioById(String uid);

    /**
     * Obtiene un usuario por su UID de forma síncrona.
     *
     * @param uid Identificador único del usuario
     * @return Usuario correspondiente al UID
     */
    @Query("SELECT * FROM usuarios WHERE uid = :uid")
    Usuario getUsuarioByIdSync(String uid);

    /**
     * Elimina un usuario específico de la base de datos.
     *
     * @param uid UID del usuario a eliminar
     */
    @Query("DELETE FROM usuarios WHERE uid = :uid")
    void eliminar(String uid);

    /**
     * Elimina todos los usuarios de la base de datos.
     * ¡PRECAUCIÓN: Operación destructiva!
     */
    @Query("DELETE FROM usuarios")
    void eliminarTodos();
}