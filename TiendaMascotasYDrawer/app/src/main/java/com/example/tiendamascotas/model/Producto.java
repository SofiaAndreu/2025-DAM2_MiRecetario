package com.example.tiendamascotas.model;

import java.io.Serializable;

public class Producto implements Serializable {
    private int id;
    private String nombre;
    private String descripcion;
    private String precio;
    private String imagenUrl;

    public Producto(int id, String nombre, String descripcion, String precio, String imagenUrl) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.imagenUrl = imagenUrl;
    }

    // Getters
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public String getPrecio() { return precio; }
    public String getImagenUrl() { return imagenUrl; }
}