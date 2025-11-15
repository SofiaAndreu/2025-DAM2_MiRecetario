package com.example.recetarioapp.models;

//Modelo que representa un ingrediente individual dentro de una receta
//- Almacena la información básica de un ingrediente: nombre, cantidad y notas opcionales.
//- Incluye métodos utilitarios para formatear la información de manera legible
//- Constructores vacíos para compatibilidad con Room/Firebase
public class Ingrediente {

    //ATRIBUTOS PRINCIPALES
    private String nombre; //Nombre del ingrediente (ej: "Harina")
    private String cantidad; //Cantidad requerida (ej: "200 gramos")
    private String notasOpcionales; //Notas adicionales (ej: "Tamizada")

    //Constructor vacío requerido para la deserialización de Firebase y Room
    public Ingrediente() {}

    //Constructor básico con parámetros mínimos requeridos
    public Ingrediente(String nombre, String cantidad) {
        this.nombre = nombre;
        this.cantidad = cantidad;
    }

    //Constructor completo con todos los campos disponibles
    public Ingrediente(String nombre, String cantidad, String notasOpcionales) {
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.notasOpcionales = notasOpcionales;
    }

    //Genera una representación completa del ingrediente formateada para mostrar al usuario
    //Combina cantidad, nombre y notas opcionales en un string legible
    public String getIngredienteCompleto() {
        StringBuilder ingredienteCompleto = new StringBuilder();

        //Agregar cantidad si está disponible
        if (cantidad != null && !cantidad.isEmpty()) {
            ingredienteCompleto.append(cantidad).append(" ");
        }

        //Agregar nombre del ingrediente
        ingredienteCompleto.append(nombre);

        //Agregar notas opcionales entre paréntesis si existen
        if (notasOpcionales != null && !notasOpcionales.isEmpty()) {
            ingredienteCompleto.append(" (").append(notasOpcionales).append(")");
        }
        return ingredienteCompleto.toString();
    }

    //Sobrescribe toString() para devolver la representación completa del ingrediente
    @Override
    public String toString() {
        return getIngredienteCompleto();
    }

    //GETTERS Y SETTERS
    public String getNombre() {return nombre;}
    public void setNombre(String nombre) {this.nombre = nombre;}

    public String getCantidad() { return cantidad;}
    public void setCantidad(String cantidad) {this.cantidad = cantidad;}

    public String getNotasOpcionales() {return notasOpcionales;}
    public void setNotasOpcionales(String notasOpcionales) {this.notasOpcionales = notasOpcionales;}
}