package com.example.recetarioapp.models;

import com.example.recetarioapp.database.Converters;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Modelo principal que representa una receta completa en el sistema.
 *
 * Esta entidad está mapeada para Room Database (@Entity) y es compatible con
 * Firebase Firestore. Utiliza conversores personalizados para manejar tipos
 * complejos como listas y fechas.
 *
 * Contiene toda la información de una receta: información básica, ingredientes,
 * pasos de preparación, metadatos y relaciones con el usuario.
 */
@Entity(tableName = "recetas")
@TypeConverters(Converters.class)
public class Receta {

    // ATRIBUTOS PRINCIPALES
    @PrimaryKey(autoGenerate = true)
    private long id;                    // ID local auto-generado por Room
    private String firebaseId;          // ID remoto en Firebase (sincronización)
    private String nombre;              // Nombre de la receta
    private String descripcion;         // Descripción breve
    private String imagenPortadaURL;    // URL de la imagen principal
    private int tiempoPreparacion;      // Tiempo en minutos
    private int porciones;              // Número de porciones
    private String dificultad;          // Nivel de dificultad (Fácil, Media, Difícil)
    private String categoria;           // Categoría culinaria
    private String origen;              // Origen geográfico o cultural

    // LISTAS DE COMPONENTES
    private List<Ingrediente> ingredientes; // Lista de ingredientes necesarios
    private List<Paso> pasos;               // Pasos de preparación ordenados
    private List<String> tags;              // Etiquetas para búsqueda y filtrado

    // METADATOS Y AUDITORÍA
    private Date fechaCreacion;         // Fecha de creación del registro
    private Date fechaModificacion;     // Fecha de última modificación
    private boolean isFav;              // Indicador de favorito del usuario
    private String usuarioId;           // ID del usuario propietario

    /**
     * Constructor vacío requerido para Room y Firebase.
     * Inicializa las listas y valores por defecto.
     */
    public Receta() {
        this.ingredientes = new ArrayList<>();
        this.pasos = new ArrayList<>();
        this.tags = new ArrayList<>();
        this.fechaCreacion = new Date();
        this.fechaModificacion = new Date();
        this.isFav = false;
    }

    /**
     * Constructor simple para creación rápida de recetas básicas.
     * Marcado con @Ignore para que Room no lo use en el mapeo.
     *
     * @param nombre Nombre de la receta
     * @param descripcion Descripción breve
     */
    @Ignore
    public Receta(String nombre, String descripcion) {
        this();
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    /**
     * Agrega un ingrediente a la lista de ingredientes de la receta.
     *
     * @param ingrediente Objeto Ingrediente a agregar
     */
    public void addIngrediente(Ingrediente ingrediente) {
        this.ingredientes.add(ingrediente);
    }

    /**
     * Agrega un paso a la lista de pasos de preparación.
     *
     * @param paso Objeto Paso a agregar
     */
    public void addPaso(Paso paso) {
        this.pasos.add(paso);
    }

    /**
     * Agrega una etiqueta a la lista de tags, evitando duplicados.
     *
     * @param tag Etiqueta a agregar
     */
    public void addTag(String tag) {
        if (!this.tags.contains(tag)) {
            this.tags.add(tag);
        }
    }

    /**
     * Convierte el tiempo de preparación en minutos a un formato legible.
     *
     * @return String formateado (ej: "45 min", "1h 30min", "-" si es 0)
     */
    public String getTiempoPrepFormateado() {
        if (tiempoPreparacion == 0) {
            return "-";
        }
        if (tiempoPreparacion < 60) {
            return tiempoPreparacion + " min";
        } else {
            int horas = tiempoPreparacion / 60;
            int minutos = tiempoPreparacion % 60;
            return horas + "h" + (minutos > 0 ? " " + minutos + "min" : "");
        }
    }

    // GETTERS Y SETTERS
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getFirebaseId() { return firebaseId; }
    public void setFirebaseId(String firebaseId) { this.firebaseId = firebaseId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getImagenPortadaURL() { return imagenPortadaURL; }
    public void setImagenPortadaURL(String imagenPortadaURL) { this.imagenPortadaURL = imagenPortadaURL; }

    public int getTiempoPreparacion() { return tiempoPreparacion; }
    public void setTiempoPreparacion(int tiempoPreparacion) { this.tiempoPreparacion = tiempoPreparacion; }

    public int getPorciones() { return porciones; }
    public void setPorciones(int porciones) { this.porciones = porciones; }

    public String getDificultad() { return dificultad; }
    public void setDificultad(String dificultad) { this.dificultad = dificultad; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getOrigen() { return origen; }
    public void setOrigen(String origen) { this.origen = origen; }

    public List<Ingrediente> getIngredientes() { return ingredientes; }
    public void setIngredientes(List<Ingrediente> ingredientes) { this.ingredientes = ingredientes; }

    public List<Paso> getPasos() { return pasos; }
    public void setPasos(List<Paso> pasos) { this.pasos = pasos; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public Date getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Date fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public Date getFechaModificacion() { return fechaModificacion; }
    public void setFechaModificacion(Date fechaModificacion) { this.fechaModificacion = fechaModificacion; }

    public boolean isFav() { return isFav; }
    public void setFav(boolean fav) { isFav = fav; }

    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }
}