package com.example.recetarioapp.utils;

import com.example.recetarioapp.models.Ingrediente;
import com.example.recetarioapp.models.Paso;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilidad para parsear texto a ingredientes y pasos
 */
public class RecipeParser {

    /**
     * Convierte texto multi-línea en lista de ingredientes
     * Formato esperado: "cantidad nombre" o solo "nombre"
     */
    public static List<Ingrediente> parseIngredientes(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<Ingrediente> ingredientes = new ArrayList<>();
        String[] lineas = texto.split("\n");

        for (String linea : lineas) {
            linea = linea.trim();
            if (linea.isEmpty()) continue;

            // Intentar separar cantidad y nombre (ej: "200g harina")
            String[] partes = linea.split(" ", 2);
            if (partes.length == 2) {
                ingredientes.add(new Ingrediente(partes[1], partes[0]));
            } else {
                ingredientes.add(new Ingrediente(linea, ""));
            }
        }

        return ingredientes;
    }

    /**
     * Convierte texto multi-línea en lista de pasos
     * Elimina numeración automática si existe
     */
    public static List<Paso> parsePasos(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<Paso> pasos = new ArrayList<>();
        String[] lineas = texto.split("\n");

        int numero = 1;
        for (String linea : lineas) {
            linea = linea.trim();
            if (linea.isEmpty()) continue;

            // Quitar numeración existente (ej: "1. Mezclar...")
            linea = linea.replaceFirst("^\\d+\\.\\s*", "");
            pasos.add(new Paso(numero++, linea));
        }

        return pasos;
    }

    /**
     * Convierte lista de ingredientes a texto multi-línea
     */
    public static String ingredientesToText(List<Ingrediente> ingredientes) {
        if (ingredientes == null || ingredientes.isEmpty()) {
            return "";
        }

        StringBuilder texto = new StringBuilder();
        for (Ingrediente ing : ingredientes) {
            texto.append(ing.getIngredienteCompleto()).append("\n");
        }
        return texto.toString().trim();
    }

    /**
     * Convierte lista de pasos a texto multi-línea
     */
    public static String pasosToText(List<Paso> pasos) {
        if (pasos == null || pasos.isEmpty()) {
            return "";
        }

        StringBuilder texto = new StringBuilder();
        for (Paso paso : pasos) {
            texto.append(paso.getNumeroPaso()).append(". ")
                    .append(paso.getDescripcion()).append("\n\n");
        }
        return texto.toString().trim();
    }
}