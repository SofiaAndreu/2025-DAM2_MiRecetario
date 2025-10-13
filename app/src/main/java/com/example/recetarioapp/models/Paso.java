package com.example.recetarioapp.models;

/**
* Modelo de PASO en el proceso de preparacion
**/
public class Paso {

    private int numeroPaso;
    private String descripcion;
    private String imagenOpcionalURL;
    private int duracionOpcional;

    //CONSTRUCTORES
    public Paso(){
    }
    public Paso(int numeroPaso, String descripcion) {
        this.numeroPaso = numeroPaso;
        this.descripcion = descripcion;
    }
    public Paso(int numeroPaso, String descripcion, String imagenOpcionalURL, int duracionOpcional) {
        this.numeroPaso = numeroPaso;
        this.descripcion = descripcion;
        this.imagenOpcionalURL = imagenOpcionalURL;
        this.duracionOpcional = duracionOpcional;
    }
    // -- -- -- --

    //METODOS
    public boolean tieneImg(){
        return imagenOpcionalURL != null && !imagenOpcionalURL.isEmpty();
    }
    public String getDuracionFormateada(){
        if (duracionOpcional <= 0){
            return "";
        }
        return duracionOpcional + "min";
    }
    @Override
    public String toString(){
        return "Paso " + numeroPaso + ": " + descripcion;
    }
    // ------------------------------------------------------------------------

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

    public String getImagenOpcionalURL() {
        return imagenOpcionalURL;
    }

    public void setImagenOpcionalURL(String imagenOpcionalURL) {
        this.imagenOpcionalURL = imagenOpcionalURL;
    }

    public int getDuracionOpcional() {
        return duracionOpcional;
    }

    public void setDuracionOpcional(int duracionOpcional) {
        this.duracionOpcional = duracionOpcional;
    }
}
