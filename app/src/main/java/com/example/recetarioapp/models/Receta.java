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
 * Modelo de RECETA con ROOM DATABASE y FIREBASE FIRESTORE
 */
@Entity(tableName ="recetas")
@TypeConverters(Converters.class) //pra conversores personalizados (listas,fechas,etc)
public class Receta {

    //ATRIBUTOS PRINCIPALES
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String firebaseId;
    private String nombre;
    private String descripcion;
    private String imagenPortadaURL;
    private int tiempoPreparacion; //minutos
    private int porciones;
    private String dificultad;
    private String categoria;
    private String origen;

    //LISTAS
    private List<Ingrediente> ingredientes;
    private List<Paso> pasos;
    private List<String> tags;

    //METADATOS
    private Date fechaCreacion;
    private Date fechaModificacion;
    private boolean isFav;
    private String usuarioId;

    //CONSTRUCTOR VACIO - para Room/Firebase
    public Receta() {
        //inicializar campos por defecto
        this.ingredientes = new ArrayList<>();
        this.pasos = new ArrayList<>();
        this.tags = new ArrayList<>();
        this.fechaCreacion = new Date();
        this.fechaModificacion = new Date();
        this.isFav = false;
    }

    //CONSTRUCTOR SIMPLE - Receta con nombre y descripción
    @Ignore
    public Receta(String nombre, String descripcion){
        this();
        this.nombre = nombre;
        this.descripcion = descripcion;
    }
    // ------------------------------------------------------------------------- //

    //MÉTODO para AÑADIR NUEVO INGREDIENTE a LISTA
    public void addIngrediente(Ingrediente ingrediente){ // + INGREDIENTE
        this.ingredientes.add(ingrediente);
    }

    //MÉTODO para AÑADIR NUEVO PASO a LISTA
    public void addPaso(Paso paso){ // + PASO
        this.pasos.add(paso);
    }

    //MÉTODO para AÑADIR NUEVO TAG a LISTA (si no existe)
    public void addTag(String tag){ // + ETIQUETA/TAG
        if (!this.tags.contains(tag)){
            this.tags.add(tag);
        }
    }

    //MÉTODO CONVERSOR de TIEMPO PREPARACIÓN
    public String getTiempoPrepFormateado(){
        if(tiempoPreparacion < 60){ //Si tiempo < 60 = "35 minutos"
            return tiempoPreparacion + " min";

        } else { //tiempo> 60 = "1h 30min"
            int horas = tiempoPreparacion /60;
            int minutos = tiempoPreparacion % 60;
            return horas + "h " + (minutos> 0 ? minutos + "min" : "");
        }
    } // ------------------------------------------------------------------- //

    //GETTERS/SETTERS
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