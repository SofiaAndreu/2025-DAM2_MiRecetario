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
 * Clase principal de la base de datos Room para la aplicación Recetario.
 *
 * Actúa como la capa de abstracción sobre SQLite y proporciona:
 * - Almacenamiento local persistente
 * - Caché para modo offline
 * - Sincronización con fuentes de datos remotas
 *
 * Implementa el patrón Singleton para asegurar una única instancia de BD.
 */
@Database(
        entities = {Receta.class, Usuario.class}, // Entidades mapeadas a tablas
        version = 2, // Versión del esquema (incrementar en cambios estructurales)
        exportSchema = false) // No exportar esquema (simplificación para el proyecto)
@TypeConverters({Converters.class}) // Convertidores para tipos complejos
public abstract class RecetasBD extends RoomDatabase {

    // Instancia única Singleton (volatile para visibilidad entre hilos)
    private static volatile RecetasBD INSTANCE;

    // Configuración del Executor para operaciones de BD en background
    private static final int NUM_THREADS = 4; // Número de hilos en el pool
    public static final ExecutorService bdWriteExecutor =
            Executors.newFixedThreadPool(NUM_THREADS);

    // ==================== DEFINICIÓN DE DAOs ====================

    /**
     * Proporciona acceso al DAO de Recetas.
     *
     * @return Instancia de RecetaDAO
     */
    public abstract RecetaDAO recetaDAO();

    /**
     * Proporciona acceso al DAO de Usuarios.
     *
     * @return Instancia de UsuarioDAO
     */
    public abstract UsuarioDAO usuarioDAO();

    // ==================== PATRÓN SINGLETON ====================

    /**
     * Obtiene la instancia única de la base de datos.
     * Implementa Double-Checked Locking para thread-safety.
     *
     * @param context Contexto de la aplicación
     * @return Instancia única de RecetasBD
     */
    public static RecetasBD getInstance(Context context) {
        // Primera verificación (sin sincronización para mejor rendimiento)
        if (INSTANCE == null) {
            // Sincronización para evitar creación múltiple en hilos concurrentes
            synchronized (RecetasBD.class) {
                // Segunda verificación dentro del bloque sincronizado
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    RecetasBD.class,
                                    "recetario_bd" // Nombre del archivo de BD
                            )
                            .fallbackToDestructiveMigration() // Elimina BD en migraciones fallidas
                            .build(); // Construye la instancia de la BD
                }
            }
        }
        return INSTANCE;
    }
}