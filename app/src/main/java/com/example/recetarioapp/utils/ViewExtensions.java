package com.example.recetarioapp.utils;

import android.view.View;

/**
 * Utilidades para simplificar operaciones con Views
 */
public class ViewExtensions {

    /**
     * Muestra u oculta una vista
     */
    public static void setVisible(View view, boolean visible) {
        view.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    /**
     * Habilita o deshabilita una vista con transparencia
     */
    public static void setEnabled(View view, boolean enabled) {
        view.setEnabled(enabled);
        view.setAlpha(enabled ? 1.0f : 0.5f);
    }

    /**
     * Alterna la visibilidad de una vista
     */
    public static void toggleVisibility(View view) {
        setVisible(view, view.getVisibility() != View.VISIBLE);
    }
}