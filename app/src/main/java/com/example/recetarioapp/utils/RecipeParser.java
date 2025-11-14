package com.example.recetarioapp.utils;

import com.example.recetarioapp.models.Ingrediente;
import com.example.recetarioapp.models.Paso;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilidad para parsear y formatear texto entre objetos de dominio y texto plano.
 *
 * Funcionalidades principales:
 * - Conversión de texto multi-línea a listas de ingredientes y pasos
 * - Formateo de listas de ingredientes y pasos a texto multi-línea
 * - Manejo de numeración automática para pasos de preparación
 * - Parseo inteligente de cantidades y nombres de ingredientes
 */
public class RecipeParser {

    /**
     * Convierte texto multi-línea en una lista de objetos Ingrediente.
     * Soporta formato "cantidad nombre" o solo "nombre" para cada línea.
     *
     * @param texto Texto multi-línea con ingredientes (una por línea)
     * @return Lista de objetos Ingrediente parseados
     */
    public static List<Ingrediente> parseIngredientes(String texto) {
        // Manejar casos nulos o vacíos
        if (texto == null || texto.trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<Ingrediente> ingredientes = new ArrayList<>();
        // Dividir texto por saltos de línea
        String[] lineas = texto.split("\n");

        // Procesar cada línea del texto
        for (String linea : lineas) {
            linea = linea.trim();
            if (linea.isEmpty()) continue; // Saltar líneas vacías

            // Intentar separar cantidad y nombre (ej: "200g harina")
            String[] partes = linea.split(" ", 2); // Dividir en máximo 2 partes
            if (partes.length == 2) {
                // Línea con cantidad y nombre
                ingredientes.add(new Ingrediente(partes[1], partes[0]));
            } else {
                // Línea con solo nombre (sin cantidad especificada)
                ingredientes.add(new Ingrediente(linea, ""));
            }
        }

        return ingredientes;
    }

    /**
     * Convierte texto multi-línea en una lista de objetos Paso.
     * Elimina automáticamente numeración existente y genera nueva numeración.
     *
     * @param texto Texto multi-línea con pasos de preparación
     * @return Lista de objetos Paso con numeración secuencial
     */
    public static List<Paso> parsePasos(String texto) {
        // Manejar casos nulos o vacíos
        if (texto == null || texto.trim().isEmpty()) {
            return new ArrayList<>();
        }

        List<Paso> pasos = new ArrayList<>();
        // Dividir texto por saltos de línea
        String[] lineas = texto.split("\n");

        int numero = 1; // Contador para numeración secuencial
        for (String linea : lineas) {
            linea = linea.trim();
            if (linea.isEmpty()) continue; // Saltar líneas vacías

            // Eliminar numeración existente (ej: "1. Mezclar...")
            linea = linea.replaceFirst("^\\d+\\.\\s*", "");
            // Crear nuevo paso con numeración automática
            pasos.add(new Paso(numero++, linea));
        }

        return pasos;
    }

    /**
     * Convierte una lista de ingredientes a texto multi-línea formateado.
     * Cada ingrediente se representa en una línea separada.
     *
     * @param ingredientes Lista de ingredientes a convertir
     * @return Texto multi-línea con todos los ingredientes
     */
    public static String ingredientesToText(List<Ingrediente> ingredientes) {
        // Manejar casos nulos o vacíos
        if (ingredientes == null || ingredientes.isEmpty()) {
            return "";
        }

        StringBuilder texto = new StringBuilder();
        // Construir texto concatenando cada ingrediente en una línea
        for (Ingrediente ing : ingredientes) {
            texto.append(ing.getIngredienteCompleto()).append("\n");
        }
        return texto.toString().trim(); // Eliminar último salto de línea
    }

    /**
     * Convierte una lista de pasos a texto multi-línea formateado.
     * Cada paso se numera automáticamente y se separa con doble salto de línea.
     *
     * @param pasos Lista de pasos a convertir
     * @return Texto multi-línea con todos los pasos numerados
     */
    public static String pasosToText(List<Paso> pasos) {
        // Manejar casos nulos o vacíos
        if (pasos == null || pasos.isEmpty()) {
            return "";
        }

        StringBuilder texto = new StringBuilder();
        // Construir texto con numeración y descripción de cada paso
        for (Paso paso : pasos) {
            texto.append(paso.getNumeroPaso()).append(". ")
                    .append(paso.getDescripcion()).append("\n\n");
        }
        return texto.toString().trim(); //Eliminar últimos saltos de línea
    }
}