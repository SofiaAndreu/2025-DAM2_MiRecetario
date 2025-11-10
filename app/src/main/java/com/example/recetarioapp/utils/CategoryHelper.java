package com.example.recetarioapp.utils;

import android.content.Context;
import com.example.recetarioapp.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper para gestionar y normalizar categorías de recetas de forma consistente.
 *
 * Funcionalidades principales:
 * - Proporciona acceso a categorías predefinidas de la aplicación
 * - Normaliza categorías externas (web, importadas) a categorías estándar
 * - Valida si una categoría es válida según las definiciones de la app
 * - Soporte multilingüe para categorías en español e inglés
 */
public class CategoryHelper {

    // ==================== MÉTODOS PARA OBTENER CATEGORÍAS PREDEFINIDAS ====================

    /**
     * Obtiene la cadena de categoría "Postres" desde recursos.
     *
     * @param context Contexto para acceder a los recursos de string
     * @return Cadena de categoría "Postres"
     */
    public static String getCategoriaPostres(Context context) {
        return context.getString(R.string.categoria_postres);
    }

    /**
     * Obtiene la cadena de categoría "Platos Principales" desde recursos.
     *
     * @param context Contexto para acceder a los recursos de string
     * @return Cadena de categoría "Platos Principales"
     */
    public static String getCategoriaPrincipales(Context context) {
        return context.getString(R.string.categoria_principales);
    }

    /**
     * Obtiene la cadena de categoría "Aperitivos" desde recursos.
     *
     * @param context Contexto para acceder a los recursos de string
     * @return Cadena de categoría "Aperitivos"
     */
    public static String getCategoriaAperitivos(Context context) {
        return context.getString(R.string.categoria_aperitivos);
    }

    /**
     * Obtiene la cadena de categoría "Panadería" desde recursos.
     *
     * @param context Contexto para acceder a los recursos de string
     * @return Cadena de categoría "Panadería"
     */
    public static String getCategoriaPanaderia(Context context) {
        return context.getString(R.string.categoria_panaderia);
    }

    /**
     * Obtiene la cadena de categoría "Bebidas" desde recursos.
     *
     * @param context Contexto para acceder a los recursos de string
     * @return Cadena de categoría "Bebidas"
     */
    public static String getCategoriaBebidas(Context context) {
        return context.getString(R.string.categoria_bebidas);
    }

    /**
     * Obtiene la cadena de categoría "Ensaladas" desde recursos.
     *
     * @param context Contexto para acceder a los recursos de string
     * @return Cadena de categoría "Ensaladas"
     */
    public static String getCategoriaEnsaladas(Context context) {
        return context.getString(R.string.categoria_ensaladas);
    }

    /**
     * Obtiene la cadena de categoría "Sopas" desde recursos.
     *
     * @param context Contexto para acceder a los recursos de string
     * @return Cadena de categoría "Sopas"
     */
    public static String getCategoriaSopas(Context context) {
        return context.getString(R.string.categoria_sopas);
    }

    /**
     * Obtiene la cadena de categoría "Otros" desde recursos.
     *
     * @param context Contexto para acceder a los recursos de string
     * @return Cadena de categoría "Otros"
     */
    public static String getCategoriaOtros(Context context) {
        return context.getString(R.string.categoria_otros);
    }

    // ==================== MÉTODO DE NORMALIZACIÓN DE CATEGORÍAS ====================

    /**
     * Normaliza una categoría externa (web, importada) a una categoría predefinida de la aplicación.
     * Utiliza un mapeo de palabras clave para identificar y clasificar categorías.
     *
     * @param categoriaExterna Categoría raw obtenida desde web o importación
     * @param context Contexto para obtener strings de categorías predefinidas
     * @return Categoría normalizada (predefinida de la aplicación)
     */
    public static String normalizarCategoria(String categoriaExterna, Context context) {
        // Manejar casos nulos o vacíos devolviendo categoría "Otros"
        if (categoriaExterna == null || categoriaExterna.trim().isEmpty()) {
            return getCategoriaOtros(context);
        }

        // Convertir a minúsculas y trim para comparación insensible a mayúsculas
        String categoriaLower = categoriaExterna.toLowerCase().trim();

        // Mapa de palabras clave → categoría predefinida
        Map<String, String> mapeo = new HashMap<>();

        // POSTRES - palabras clave en español e inglés
        mapeo.put("postre", getCategoriaPostres(context));
        mapeo.put("postres", getCategoriaPostres(context));
        mapeo.put("dulce", getCategoriaPostres(context));
        mapeo.put("dessert", getCategoriaPostres(context));
        mapeo.put("tarta", getCategoriaPostres(context));
        mapeo.put("pastel", getCategoriaPostres(context));
        mapeo.put("galleta", getCategoriaPostres(context));
        mapeo.put("helado", getCategoriaPostres(context));

        // PLATOS PRINCIPALES - palabras clave en español e inglés
        mapeo.put("principal", getCategoriaPrincipales(context));
        mapeo.put("principales", getCategoriaPrincipales(context));
        mapeo.put("plato", getCategoriaPrincipales(context));
        mapeo.put("carne", getCategoriaPrincipales(context));
        mapeo.put("pescado", getCategoriaPrincipales(context));
        mapeo.put("pollo", getCategoriaPrincipales(context));
        mapeo.put("arroz", getCategoriaPrincipales(context));
        mapeo.put("pasta", getCategoriaPrincipales(context));
        mapeo.put("main", getCategoriaPrincipales(context));

        // APERITIVOS - palabras clave en español e inglés
        mapeo.put("aperitivo", getCategoriaAperitivos(context));
        mapeo.put("aperitivos", getCategoriaAperitivos(context));
        mapeo.put("entrante", getCategoriaAperitivos(context));
        mapeo.put("tapa", getCategoriaAperitivos(context));
        mapeo.put("snack", getCategoriaAperitivos(context));
        mapeo.put("appetizer", getCategoriaAperitivos(context));

        // PANADERÍA - palabras clave en español e inglés
        mapeo.put("pan", getCategoriaPanaderia(context));
        mapeo.put("panaderia", getCategoriaPanaderia(context));
        mapeo.put("bolleria", getCategoriaPanaderia(context));
        mapeo.put("masa", getCategoriaPanaderia(context));
        mapeo.put("bread", getCategoriaPanaderia(context));

        // BEBIDAS - palabras clave en español e inglés
        mapeo.put("bebida", getCategoriaBebidas(context));
        mapeo.put("bebidas", getCategoriaBebidas(context));
        mapeo.put("drink", getCategoriaBebidas(context));
        mapeo.put("zumo", getCategoriaBebidas(context));
        mapeo.put("batido", getCategoriaBebidas(context));
        mapeo.put("cocktail", getCategoriaBebidas(context));
        mapeo.put("coctel", getCategoriaBebidas(context));

        // ENSALADAS - palabras clave en español e inglés
        mapeo.put("ensalada", getCategoriaEnsaladas(context));
        mapeo.put("ensaladas", getCategoriaEnsaladas(context));
        mapeo.put("salad", getCategoriaEnsaladas(context));
        mapeo.put("vegetal", getCategoriaEnsaladas(context));

        // SOPAS - palabras clave en español e inglés
        mapeo.put("sopa", getCategoriaSopas(context));
        mapeo.put("sopas", getCategoriaSopas(context));
        mapeo.put("caldo", getCategoriaSopas(context));
        mapeo.put("crema", getCategoriaSopas(context));
        mapeo.put("soup", getCategoriaSopas(context));

        // Buscar coincidencia en el mapa de palabras clave
        for (Map.Entry<String, String> entry : mapeo.entrySet()) {
            if (categoriaLower.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        // Si no hay coincidencia, devolver categoría "Otros" por defecto
        return getCategoriaOtros(context);
    }

    // ==================== MÉTODO DE VALIDACIÓN DE CATEGORÍAS ====================

    /**
     * Verifica si una categoría es una de las categorías predefinidas válidas de la aplicación.
     * Útil para validar categorías antes de guardarlas en la base de datos.
     *
     * @param categoria Categoría a validar
     * @param context Contexto para obtener strings de categorías predefinidas
     * @return true si la categoría es válida, false en caso contrario
     */
    public static boolean esCategoriaValida(String categoria, Context context) {
        // Manejar casos nulos o vacíos
        if (categoria == null || categoria.trim().isEmpty()) {
            return false;
        }

        String categoriaTrim = categoria.trim();

        // Verificar coincidencia con cada categoría predefinida
        return categoriaTrim.equals(getCategoriaPostres(context)) ||
                categoriaTrim.equals(getCategoriaPrincipales(context)) ||
                categoriaTrim.equals(getCategoriaAperitivos(context)) ||
                categoriaTrim.equals(getCategoriaPanaderia(context)) ||
                categoriaTrim.equals(getCategoriaBebidas(context)) ||
                categoriaTrim.equals(getCategoriaEnsaladas(context)) ||
                categoriaTrim.equals(getCategoriaSopas(context)) ||
                categoriaTrim.equals(getCategoriaOtros(context));
    }
}