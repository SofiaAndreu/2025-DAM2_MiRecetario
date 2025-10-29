package com.example.recetarioapp.utils;

import com.google.android.material.textfield.TextInputEditText;

/**
 * Validador de campos de recetas
 */
public class RecipeValidator {

    /**
     * Valida que un campo no esté vacío
     */
    public static boolean validateNotEmpty(TextInputEditText editText, String errorMsg) {
        String text = editText.getText().toString().trim();
        if (text.isEmpty()) {
            editText.setError(errorMsg);
            editText.requestFocus();
            return false;
        }
        return true;
    }

    /**
     * Valida el formulario completo de una receta
     */
    public static boolean validateRecipeForm(
            TextInputEditText etNombre,
            TextInputEditText etIngredientes,
            TextInputEditText etPasos) {

        if (!validateNotEmpty(etNombre, "El nombre es obligatorio")) {
            return false;
        }

        if (!validateNotEmpty(etIngredientes, "Los ingredientes son obligatorios")) {
            return false;
        }

        if (!validateNotEmpty(etPasos, "Los pasos son obligatorios")) {
            return false;
        }

        return true;
    }

    /**
     * Intenta parsear un número desde un EditText
     */
    public static int parseIntOrDefault(TextInputEditText editText, int defaultValue) {
        try {
            String text = editText.getText().toString().trim();
            return text.isEmpty() ? defaultValue : Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}

