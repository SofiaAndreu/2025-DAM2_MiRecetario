package com.example.recetarioapp.models;

/**
 * Modelo que representa un paso individual en el proceso de preparación de una receta.
 *
 * Cada paso contiene un número de orden y una descripción detallada de la acción a realizar.
 * La numeración permite mantener el orden secuencial del proceso de cocina.
 */
public class Paso {

    // ATRIBUTOS
    private int numeroPaso;      // Número secuencial del paso (1, 2, 3...)
    private String descripcion;  // Descripción detallada de la acción a realizar

    /**
     * Constructor vacío requerido para la deserialización de Firebase y Room.
     */
    public Paso() {
    }

    /**
     * Constructor completo con todos los campos.
     *
     * @param numeroPaso Número secuencial que indica el orden del paso
     * @param descripcion Instrucción detallada del paso
     */
    public Paso(int numeroPaso, String descripcion) {
        this.numeroPaso = numeroPaso;
        this.descripcion = descripcion;
    }

    /**
     * Sobrescribe toString() para mostrar el paso formateado.
     *
     * @return String en formato "Paso X: Descripción del paso"
     */
    @Override
    public String toString() {
        return "Paso " + numeroPaso + ": " + descripcion;
    }

    // GETTERS Y SETTERS
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