package com.example.recetarioapp.utils;

import com.google.android.material.textfield.TextInputEditText;

/**
 * Validador de campos de formularios de recetas.
 *
 * Funcionalidades principales:
 * - Validación de campos obligatorios
 * - Parseo seguro de valores numéricos
 * - Manejo de errores de validación con feedback visual
 * - Soporte para validación de formularios completos
 */
public class RecipeValidator {

    /**
     * Valida que un campo EditText no esté vacío.
     * Muestra error visual y solicita foco si la validación falla.
     *
     * @param editText Campo de texto a validar
     * @param errorMsg Mensaje de error a mostrar si la validación falla
     * @return true si el campo no está vacío, false en caso contrario
     */
    public static boolean validateNotEmpty(TextInputEditText editText, String errorMsg) {
        String text = editText.getText().toString().trim();
        if (text.isEmpty()) {
            // Mostrar error visual y solicitar foco
            editText.setError(errorMsg);
            editText.requestFocus();
            return false;
        }
        return true;
    }

    /**
     * Valida el formulario completo de una receta.
     * Verifica que los campos obligatorios (nombre, ingredientes, pasos) no estén vacíos.
     *
     * @param etNombre Campo del nombre de la receta
     * @param etIngredientes Campo de ingredientes de la receta
     * @param etPasos Campo de pasos de preparación
     * @return true si todos los campos obligatorios son válidos, false en caso contrario
     */
    public static boolean validateRecipeForm(
            TextInputEditText etNombre,
            TextInputEditText etIngredientes,
            TextInputEditText etPasos) {

        // Validar campo nombre
        if (!validateNotEmpty(etNombre, "El nombre es obligatorio")) {
            return false;
        }

        // Validar campo ingredientes
        if (!validateNotEmpty(etIngredientes, "Los ingredientes son obligatorios")) {
            return false;
        }

        // Validar campo pasos
        if (!validateNotEmpty(etPasos, "Los pasos son obligatorios")) {
            return false;
        }

        return true;
    }

    /**
     * Intenta parsear un valor numérico desde un EditText de forma segura.
     * Devuelve un valor por defecto si el parseo falla o el campo está vacío.
     *
     * @param editText Campo de texto que contiene el número
     * @param defaultValue Valor a devolver si el parseo falla
     * @return Valor numérico parseado o valor por defecto
     */
    public static int parseIntOrDefault(TextInputEditText editText, int defaultValue) {
        try {
            String text = editText.getText().toString().trim();
            return text.isEmpty() ? defaultValue : Integer.parseInt(text);
        } catch (NumberFormatException e) {
            // Devolver valor por defecto en caso de error de formato
            return defaultValue;
        }
    }
}