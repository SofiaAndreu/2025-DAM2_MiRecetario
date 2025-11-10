package com.example.recetarioapp.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.Date;

/**
 * Modelo que representa un usuario del sistema.
 *
 * Esta entidad se sincroniza entre Room Database (local) y Firebase Auth (remoto).
 * Almacena información básica del usuario y metadatos de actividad.
 */
@Entity(tableName = "usuarios")
public class Usuario {

    // ATRIBUTOS PRINCIPALES
    @PrimaryKey
    @NonNull
    private String uid;             // ID único de Firebase Authentication
    private String nombre;          // Nombre display del usuario
    private String email;           // Email de autenticación

    // METADATOS DE ACTIVIDAD
    private Date fechaRegistro;     // Fecha de registro en el sistema
    private Date ultimaConexion;    // Fecha de última sesión activa

    /**
     * Constructor vacío requerido para Room.
     * Inicializa las fechas con valores por defecto.
     */
    public Usuario() {
        this.fechaRegistro = new Date();
        this.ultimaConexion = new Date();
    }

    /**
     * Constructor básico para crear usuarios con información esencial.
     *
     * @param uid ID único de Firebase Auth
     * @param nombre Nombre display del usuario
     * @param email Email de autenticación
     */
    public Usuario(String uid, String nombre, String email) {
        this();
        this.uid = uid;
        this.nombre = nombre;
        this.email = email;
    }

    // GETTERS Y SETTERS
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Date getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(Date fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public Date getUltimaConexion() { return ultimaConexion; }
    public void setUltimaConexion(Date ultimaConexion) { this.ultimaConexion = ultimaConexion; }
}