package com.example.recetarioapp.models;

/**
 * Modelo INGREDIENTE de RECETA
 */
public class Ingrediente {

    private String nombre;
    private String cantidad;
    private String notasOpcionales;

    //CONSTRUCTORES
    public Ingrediente() {
    }

    public Ingrediente(String nombre, String cantidad) {
        this.nombre = nombre;
        this.cantidad = cantidad;
    }
    public Ingrediente(String nombre, String cantidad, String notasOpcionales) {
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.notasOpcionales = notasOpcionales;
    }

    //MÉTODOS
    public String getIngredienteCompleto(){
        StringBuilder ingredienteCompleto = new StringBuilder();
        if(cantidad != null && !cantidad.isEmpty()){
            ingredienteCompleto.append(cantidad).append(" ");
        }
        ingredienteCompleto.append(nombre);
        if(notasOpcionales!= null && !notasOpcionales.isEmpty()) {
            ingredienteCompleto.append(" (").append(ingredienteCompleto).append(")");
        }
        return ingredienteCompleto.toString();
    }
    @Override
    public String toString(){
        return getIngredienteCompleto();
    }


    //GETTERS Y SETTERS
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCantidad() {
        return cantidad;
    }

    public void setCantidad(String cantidad) {
        this.cantidad = cantidad;
    }

    public String getNotasOpcionales() {
        return notasOpcionales;
    }

    public void setNotasOpcionales(String notasOpcionales) {
        this.notasOpcionales = notasOpcionales;
    }
}
