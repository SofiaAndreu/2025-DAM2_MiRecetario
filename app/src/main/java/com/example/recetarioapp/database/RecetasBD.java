package com.example.recetarioapp.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.recetarioapp.models.Receta;
import com.example.recetarioapp.models.Usuario;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Almacenamiento Local con ROOM DB
 * Actua como caché y permite modo offline ✓
 */
@Database(
        entities = {Receta.class, Usuario.class}, //Clase = tabla en BD
        version=2, //Version de BD
        exportSchema = false) //Exportar def. exquema = false)
@TypeConverters({Converters.class}) //Converters personalizados
public abstract class RecetasBD extends RoomDatabase {

    //Instancia unica de BD (singletone), volatile (visible para todos los hilos)
    private static volatile RecetasBD INSTANCE;

    //4 hilos para ejecutar operaciones (asincronas) sin bloqueo del principal
    private static final int NUM_THREADS = 4;
    //Executor para los HILOS
    public static final ExecutorService bdWriteExecutor = Executors.newFixedThreadPool(NUM_THREADS);

    //DAO
    public abstract RecetaDAO recetaDAO();
    public abstract UsuarioDAO usuarioDAO();

    public static RecetasBD getInstance(Context context){
        if (INSTANCE == null){ //Verificación 1: Si INSTANCE = NULL
            synchronized (RecetasBD.class){ //evita multiples hilos creando la bd
                //Verificacion 2
                if(INSTANCE == null){
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                            RecetasBD.class,
                            "recetario_bd"
                    )
                    .fallbackToDestructiveMigration() //borra datos de BD si hay cambios de version
                    .build(); //creación de instancia
                }
            }
        }
        return INSTANCE;
    }

/**
 *  Destruye la instancia de la base de datos.
 *  ¡¡ SOLO PARA TESTING - No usar en producción !!
 */
//    public static void destroyInstance(){
//        INSTANCE = null;
//    }
}
