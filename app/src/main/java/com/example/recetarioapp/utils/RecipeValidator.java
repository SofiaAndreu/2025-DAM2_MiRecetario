package com.example.recetarioapp.utils;

import com.google.android.material.textfield.TextInputEditText;

//Validador de campos de formularios de recetas
// - Validación de campos obligatorios
// - Parseo seguro de valores numéricos
// - Manejo de errores de validación con feedback visual
// - Soporte para validación de formularios completos
public class RecipeValidator {

    //Valida que un campo EditText no esté vacío
    public static boolean validateNotEmpty(TextInputEditText editText, String errorMsg) {
        String text = editText.getText().toString().trim();
        if (text.isEmpty()) {
            //Mostrar error visual y solicitar foco
            editText.setError(errorMsg);
            editText.requestFocus();
            return false;
        }
        return true;
    }

    //Valida el formulario completo de una receta
    public static boolean validateRecipeForm(
            TextInputEditText etNombre,
            TextInputEditText etIngredientes,
            TextInputEditText etPasos) {

        //Validar campo nombre
        if (!validateNotEmpty(etNombre, "El nombre es obligatorio")) {
            return false;
        }

        //Validar campo ingredientes
        if (!validateNotEmpty(etIngredientes, "Los ingredientes son obligatorios")) {
            return false;
        }

        //Validar campo pasos
        if (!validateNotEmpty(etPasos, "Los pasos son obligatorios")) {
            return false;
        }

        return true;
    }

    //Intenta parsear un valor numérico desde un EditText de forma segura
    public static int parseIntOrDefault(TextInputEditText editText, int defaultValue) {
        try {
            String text = editText.getText().toString().trim();
            return text.isEmpty() ? defaultValue : Integer.parseInt(text);
        } catch (NumberFormatException e) {
            //Devolver valor por defecto en caso de error de formato
            return defaultValue;
        }
    }
}