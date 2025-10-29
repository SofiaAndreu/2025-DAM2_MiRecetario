package com.example.recetarioapp.utils;

import android.content.Intent;

import com.example.recetarioapp.models.Receta;

/**
 * Utilidad para compartir recetas
 */
public class RecipeShareHelper {

    /**
     * Crea un Intent para compartir una receta como texto
     */
    public static Intent createShareIntent(Receta receta) {
        String texto = buildShareText(receta);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, receta.getNombre());
        shareIntent.putExtra(Intent.EXTRA_TEXT, texto);

        return Intent.createChooser(shareIntent, "Compartir receta");
    }

    /**
     * Construye el texto formateado de la receta
     */
    private static String buildShareText(Receta receta) {
        StringBuilder texto = new StringBuilder();

        texto.append("📖 ").append(receta.getNombre()).append("\n\n");

        if (receta.getDescripcion() != null && !receta.getDescripcion().isEmpty()) {
            texto.append(receta.getDescripcion()).append("\n\n");
        }

        texto.append("⏱️ Tiempo: ").append(receta.getTiempoPrepFormateado()).append("\n");
        texto.append("👥 Porciones: ").append(receta.getPorciones()).append("\n");
        texto.append("📊 Dificultad: ").append(receta.getDificultad()).append("\n\n");

        // Ingredientes
        texto.append("🥘 INGREDIENTES:\n");
        if (receta.getIngredientes() != null) {
            for (int i = 0; i < receta.getIngredientes().size(); i++) {
                texto.append("• ").append(receta.getIngredientes().get(i)
                        .getIngredienteCompleto()).append("\n");
            }
        }

        // Pasos
        texto.append("\n👨‍🍳 PREPARACIÓN:\n");
        if (receta.getPasos() != null) {
            for (int i = 0; i < receta.getPasos().size(); i++) {
                texto.append(i + 1).append(". ")
                        .append(receta.getPasos().get(i).getDescripcion())
                        .append("\n\n");
            }
        }

        texto.append("\n📱 Compartido desde Recetario Clásico");

        return texto.toString();
    }
}
