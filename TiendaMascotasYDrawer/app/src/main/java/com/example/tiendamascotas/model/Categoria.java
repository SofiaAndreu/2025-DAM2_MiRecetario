package com.example.tiendamascotas.model;

import java.io.Serializable;
import java.util.List;

public class Categoria implements Serializable {
    private String nombre;
    private List<Producto> productos;

    public Categoria(String nombre, List<Producto> productos) {
        this.nombre = nombre;
        this.productos = productos;
    }

    public String getNombre() { return nombre; }
    public List<Producto> getProductos() { return productos; }
}