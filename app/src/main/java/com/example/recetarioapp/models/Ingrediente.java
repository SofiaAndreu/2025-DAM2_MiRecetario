package com.example.recetarioapp.models;

/**
 * Modelo INGREDIENTE individual dentro de RECETA ✓
 */
public class Ingrediente {

    //ATRIBUTOS
    private String nombre;
    private String cantidad;
    private String notasOpcionales;

    //CONSTRUCTOR VACIO - para Firebase/Room
    public Ingrediente() {
    }

    //CONSTRUCTOR CON PARÁMETROS - Solo los minimos necesarios
    public Ingrediente(String nombre, String cantidad) {
        this.nombre = nombre;
        this.cantidad = cantidad;
    }

    //CONSTRUCTOR COMPLETO - Todos los campos
    public Ingrediente(String nombre, String cantidad, String notasOpcionales) {
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.notasOpcionales = notasOpcionales;
    }
    // ------------------------------------------------------------------------------ //

    //MÉTODO para CONSTRUIR un TEXTO completo con la INFO del INGREDIENTE (nombre + cantidad + notaOpcional)
    public String getIngredienteCompleto(){
        //StringBuilder para oncatenar cadenas de texto
        StringBuilder ingredienteCompleto = new StringBuilder();

        //Si cantidad NO vacia -> añadir a texto con espacio
        if(cantidad != null && !cantidad.isEmpty()){
            ingredienteCompleto.append(cantidad).append(" ");
        }
        ingredienteCompleto.append(nombre); //añadir nombre de ingrediente

        //Si hay notasOpcionales -> agregar entre paréntesis
        if(notasOpcionales!= null && !notasOpcionales.isEmpty()) {
            ingredienteCompleto.append(" (").append(notasOpcionales).append(")");
        }
        return ingredienteCompleto.toString(); //devuelve texto completo como String
    } // ------------------------------------------------------------------------------ //

    //Sobreescribe toString para mostrar texto de ingrediente completo
    @Override
    public String toString(){
        return getIngredienteCompleto();
    }
    // ------------------------------------------------------- //


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
