package com.example.recetarioapp.utils;

import android.content.Intent;

import com.example.recetarioapp.models.Receta;

/**
 * Utilidad para compartir recetas a través de diferentes aplicaciones.
 *
 * Funcionalidades principales:
 * - Creación de intents para compartir recetas como texto
 * - Formateo atractivo de recetas para compartir
 * - Soporte para cualquier aplicación que acepte texto plano
 */
public class RecipeShareHelper {

    /**
     * Crea un Intent listo para compartir una receta como texto formateado.
     * El Intent puede ser usado con cualquier aplicación que soporte compartir texto.
     *
     * @param receta Receta a compartir
     * @return Intent configurado para compartir la receta
     */
    public static Intent createShareIntent(Receta receta) {
        // Construir texto formateado de la receta
        String texto = buildShareText(receta);

        // Crear intent de compartir
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain"); // Tipo MIME para texto plano
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, receta.getNombre()); // Asunto
        shareIntent.putExtra(Intent.EXTRA_TEXT, texto); // Contenido

        // Crear selector de aplicaciones con título personalizado
        return Intent.createChooser(shareIntent, "Compartir receta");
    }

    /**
     * Construye el texto formateado de la receta para compartir.
     * Incluye todos los detalles de la receta en un formato legible y atractivo.
     *
     * @param receta Receta de la que generar el texto
     * @return Texto formateado listo para compartir
     */
    private static String buildShareText(Receta receta) {
        StringBuilder texto = new StringBuilder();

        // Encabezado con nombre de la receta
        texto.append("📖 ").append(receta.getNombre()).append("\n\n");

        // Descripción (si existe)
        if (receta.getDescripcion() != null && !receta.getDescripcion().isEmpty()) {
            texto.append(receta.getDescripcion()).append("\n\n");
        }

        // Metadatos de la receta
        texto.append("⏱️ Tiempo: ").append(receta.getTiempoPrepFormateado()).append("\n");
        texto.append("👥 Porciones: ").append(receta.getPorciones()).append("\n");
        texto.append("📊 Dificultad: ").append(receta.getDificultad()).append("\n\n");

        // Sección de ingredientes
        texto.append("🥘 INGREDIENTES:\n");
        if (receta.getIngredientes() != null) {
            for (int i = 0; i < receta.getIngredientes().size(); i++) {
                texto.append("• ").append(receta.getIngredientes().get(i)
                        .getIngredienteCompleto()).append("\n");
            }
        }

        // Sección de pasos de preparación
        texto.append("\n👨‍🍳 PREPARACIÓN:\n");
        if (receta.getPasos() != null) {
            for (int i = 0; i < receta.getPasos().size(); i++) {
                texto.append(i + 1).append(". ")
                        .append(receta.getPasos().get(i).getDescripcion())
                        .append("\n\n");
            }
        }

        // Pie de página con atribución
        texto.append("\n📱 Compartido desde Recetario Clásico");

        return texto.toString();
    }
}