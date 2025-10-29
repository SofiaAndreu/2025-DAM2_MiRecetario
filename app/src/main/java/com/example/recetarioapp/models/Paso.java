package com.example.recetarioapp.models;

/**
* Modelo de PASO en el proceso de preparacion
**/
public class Paso {

    //ATRIBUTOS
    private int numeroPaso;
    private String descripcion;

    //CONSTRUCTOR VACÍO para Firebase/Room
    public Paso(){
    }

    //CONSTRUCTOR COMPLETO - paso con numero y descripción
    public Paso(int numeroPaso, String descripcion) {
        this.numeroPaso = numeroPaso;
        this.descripcion = descripcion;
    }

    //Sobreescribe toString para imprimir
    @Override
    public String toString(){
        return "Paso " + numeroPaso + ": " + descripcion;
    }
    // ------------------------------------------------------------------------ //

    public int getNumeroPaso() {
        return numeroPaso;
    }
    public void setNumeroPaso(int numeroPaso) {
        this.numeroPaso = numeroPaso;
    }
    public String getDescripcion() {
        return descripcion;
    }
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
