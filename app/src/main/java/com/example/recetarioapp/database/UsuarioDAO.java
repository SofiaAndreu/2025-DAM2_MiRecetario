package com.example.recetarioapp.database;

import com.example.recetarioapp.models.Usuario;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

//Data Access Object (DAO) para operaciones de base de datos con la entidad Usuario
//- Gestiona operaciones CRUD para usuarios
//- proporciona métodos específicos para autenticación y gestión de usuarios
@Dao
public interface UsuarioDAO {

    //Inserta o reemplaza un usuario en la base de datos
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertar(Usuario usuario);

    //Actualiza un usuario existente en la base de datos
    @Update
    void actualizar(Usuario usuario);

    //Obtiene un usuario por su UID de forma observable
    @Query("SELECT * FROM usuarios WHERE uid = :uid")
    LiveData<Usuario> getUsuarioById(String uid);

    //Obtiene un usuario por su UID de forma síncrona
    @Query("SELECT * FROM usuarios WHERE uid = :uid")
    Usuario getUsuarioByIdSync(String uid);

    //Elimina un usuario específico de la base de datos
    @Query("DELETE FROM usuarios WHERE uid = :uid")
    void eliminar(String uid);

    //Elimina todos los usuarios de la base de datos
    //¡¡ !!
    @Query("DELETE FROM usuarios")
    void eliminarTodos();
}