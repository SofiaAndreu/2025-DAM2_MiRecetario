package com.example.recetarioapp.utils;

import android.view.View;

//Utilidades de extensión para simplificar operaciones comunes con Views
// - Control simplificado de visibilidad de vistas
// - Habilitación/deshabilitación con efectos visuales
// - Alternancia de visibilidad con un solo método
public class ViewExtensions {

    //Muestra u oculta una vista de forma controlada
    public static void setVisible(View view, boolean visible) {
        view.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    //Habilita o deshabilita una vista con efecto visual de transparencia
    public static void setEnabled(View view, boolean enabled) {
        view.setEnabled(enabled);
        view.setAlpha(enabled ? 1.0f : 0.5f); //Efecto visual de deshabilitado
    }

    //Alterna la visibilidad de una vista entre VISIBLE y GONE
    public static void toggleVisibility(View view) {
        setVisible(view, view.getVisibility() != View.VISIBLE);
    }
}