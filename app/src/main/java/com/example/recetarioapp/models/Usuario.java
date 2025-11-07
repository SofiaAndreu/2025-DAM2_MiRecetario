package com.example.recetarioapp.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.Date;

/**
 * Modelo de Usuario para Room y Firebase
 */
@Entity(tableName = "usuarios")
public class Usuario {

    @PrimaryKey
    @NonNull
    private String uid; // Firebase UID
    private String nombre;
    private String email;

    // Metadatos
    private Date fechaRegistro;
    private Date ultimaConexion;

    // Constructor vacío (obligatorio)
    public Usuario() {
        this.fechaRegistro = new Date();
        this.ultimaConexion = new Date();
    }

    // Constructor básico
    public Usuario(String uid, String nombre, String email) {
        this();
        this.uid = uid;
        this.nombre = nombre;
        this.email = email;
    }

    // Getters y Setters
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

