package com.example.recetarioapp.database;

import com.example.recetarioapp.models.Usuario;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

/**
 * DAO para operaciones con Usuario en Room
 */
@Dao
public interface UsuarioDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertar(Usuario usuario);

    @Update
    void actualizar(Usuario usuario);

    @Query("SELECT * FROM usuarios WHERE uid = :uid")
    LiveData<Usuario> getUsuarioById(String uid);

    @Query("SELECT * FROM usuarios WHERE uid = :uid")
    Usuario getUsuarioByIdSync(String uid);

    @Query("DELETE FROM usuarios WHERE uid = :uid")
    void eliminar(String uid);

    @Query("DELETE FROM usuarios")
    void eliminarTodos();
}