package com.example.recetarioapp.utils;

import android.view.View;

/**
 * Utilidades de extensión para simplificar operaciones comunes con Views.
 *
 * Funcionalidades principales:
 * - Control simplificado de visibilidad de vistas
 * - Habilitación/deshabilitación con efectos visuales
 * - Alternancia de visibilidad con un solo método
 */
public class ViewExtensions {

    /**
     * Muestra u oculta una vista de forma controlada.
     * Usa View.GONE en lugar de View.INVISIBLE para optimizar layout.
     *
     * @param view Vista a mostrar u ocultar
     * @param visible true para mostrar (VISIBLE), false para ocultar (GONE)
     */
    public static void setVisible(View view, boolean visible) {
        view.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    /**
     * Habilita o deshabilita una vista con efecto visual de transparencia.
     * Las vistas deshabilitadas se muestran semitransparentes para feedback visual.
     *
     * @param view Vista a habilitar o deshabilitar
     * @param enabled true para habilitar (opaca), false para deshabilitar (transparente)
     */
    public static void setEnabled(View view, boolean enabled) {
        view.setEnabled(enabled);
        view.setAlpha(enabled ? 1.0f : 0.5f); // Efecto visual de deshabilitado
    }

    /**
     * Alterna la visibilidad de una vista entre VISIBLE y GONE.
     * Útil para implementar comportamientos de toggle en la interfaz.
     *
     * @param view Vista cuya visibilidad se va a alternar
     */
    public static void toggleVisibility(View view) {
        setVisible(view, view.getVisibility() != View.VISIBLE);
    }
}