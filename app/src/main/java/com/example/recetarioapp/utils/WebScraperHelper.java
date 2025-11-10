package com.example.recetarioapp.utils;

import android.content.Context;
import android.util.Log;

import com.example.recetarioapp.models.Ingrediente;
import com.example.recetarioapp.models.Paso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Helper para extraer recetas desde URLs web mediante web scraping.
 *
 * Funcionalidades principales:
 * - Extracción de recetas desde sitios web genéricos y YouTube
 * - Normalización automática de categorías usando CategoryHelper
 * - Soporte para Schema.org y estructuras HTML comunes
 * - Descarga y parseo robusto de contenido web
 * - Manejo de errores con logging detallado
 */
public class WebScraperHelper {

    // Tag para logging de operaciones de web scraping
    private static final String TAG = "WebScraperHelper";

    /**
     * Extrae una receta desde una URL web usando técnicas de web scraping.
     * Soporta sitios web genéricos y YouTube, con normalización automática de categorías.
     *
     * @param url URL de la receta a extraer
     * @param context Contexto para normalización de categorías (requerido)
     * @return Objeto RecetaExtraida con los datos parseados, o null si falla
     */
    public static RecetaExtraida extraerRecetaDesdeURL(String url, Context context) {
        try {
            // Descargar contenido HTML de la URL
            String html = descargarHTML(url);
            if (html == null) {
                return null;
            }

            // Parsear HTML con JSoup
            Document doc = Jsoup.parse(html);
            RecetaExtraida receta;

            // Determinar estrategia de extracción según el tipo de URL
            if (url.contains("youtube.com") || url.contains("youtu.be")) {
                receta = extraerDeYouTube(doc, url);
            } else {
                receta = extraerDeWebGenerica(doc);
            }

            // Normalizar categoría usando CategoryHelper
            if (receta != null && context != null) {
                receta.categoria = CategoryHelper.normalizarCategoria(
                        receta.categoria != null ? receta.categoria : receta.origen,
                        context
                );
            }

            return receta;

        } catch (Exception e) {
            Log.e(TAG, "Error al extraer receta", e);
            return null;
        }
    }

    /**
     * Descarga el contenido HTML de una URL usando OkHttp.
     * Incluye configuración de user agent y seguimiento de redirecciones.
     *
     * @param url URL a descargar
     * @return Contenido HTML como String, o null si falla
     */
    private static String descargarHTML(String url) {
        try {
            OkHttpClient client = new OkHttpClient.Builder()
                    .followRedirects(true) // Seguir redirecciones automáticamente
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    return response.body().string();
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error al descargar HTML", e);
        }
        return null;
    }

    /**
     * Extrae información de receta desde una página de YouTube.
     * Utiliza meta tags Open Graph para obtener título, descripción e imagen.
     *
     * @param doc Documento JSoup parseado
     * @param url URL original de YouTube
     * @return RecetaExtraida con datos de YouTube
     */
    private static RecetaExtraida extraerDeYouTube(Document doc, String url) {
        RecetaExtraida receta = new RecetaExtraida();

        // Extraer título desde meta tags Open Graph
        Element titulo = doc.selectFirst("meta[property=og:title]");
        if (titulo != null) {
            receta.nombre = titulo.attr("content");
        }

        // Extraer descripción desde meta tags Open Graph
        Element descripcion = doc.selectFirst("meta[property=og:description]");
        if (descripcion != null) {
            String texto = descripcion.attr("content");
            receta.descripcion = texto;
            // Intentar extraer ingredientes y pasos del texto de descripción
            extraerIngredientesYPasosDeTexto(texto, receta);
        }

        // Extraer imagen miniatura desde meta tags Open Graph
        Element imagen = doc.selectFirst("meta[property=og:image]");
        if (imagen != null) {
            receta.imagenUrl = imagen.attr("content");
        }

        receta.origen = "YouTube";
        receta.categoria = "Otros"; // Se normalizará después

        return receta;
    }

    /**
     * Extrae información de receta desde sitios web genéricos.
     * Utiliza múltiples estrategias (Schema.org, selectores CSS, meta tags).
     *
     * @param doc Documento JSoup parseado
     * @return RecetaExtraida con datos del sitio web
     */
    private static RecetaExtraida extraerDeWebGenerica(Document doc) {
        RecetaExtraida receta = new RecetaExtraida();

        // Extraer todos los componentes de la receta
        receta.nombre = extraerTitulo(doc);
        receta.descripcion = extraerDescripcion(doc);
        receta.ingredientes = extraerIngredientes(doc);
        receta.pasos = extraerPasos(doc);
        receta.tiempoPreparacion = extraerTiempo(doc);
        receta.porciones = extraerPorciones(doc);
        receta.imagenUrl = extraerImagen(doc);

        // Extraer categoría raw (se normalizará después)
        receta.categoria = extraerCategoria(doc);
        receta.origen = "Web";

        return receta;
    }

    /**
     * Extrae categoría de receta desde el documento HTML.
     * Intenta múltiples fuentes: Schema.org, meta tags, clases CSS.
     *
     * @param doc Documento JSoup parseado
     * @return Categoría extraída, o null si no se encuentra
     */
    private static String extraerCategoria(Document doc) {
        // Intentar Schema.org primero
        Element schemaCategoria = doc.selectFirst("[itemprop=recipeCategory]");
        if (schemaCategoria != null) {
            return schemaCategoria.text();
        }

        // Intentar meta tags
        Element metaCategoria = doc.selectFirst("meta[property=article:section]");
        if (metaCategoria != null) {
            return metaCategoria.attr("content");
        }

        // Buscar en clases CSS comunes
        Element categoriaElement = doc.selectFirst(".category, .recipe-category, [class*=category]");
        if (categoriaElement != null) {
            return categoriaElement.text();
        }

        return null;
    }

    // Métodos auxiliares para extracción específica de componentes...

    private static String extraerTitulo(Document doc) {
        // Múltiples estrategias para extraer título
        Element schemaName = doc.selectFirst("[itemtype*=Recipe] [itemprop=name]");
        if (schemaName != null) return schemaName.text();

        Element ogTitle = doc.selectFirst("meta[property=og:title]");
        if (ogTitle != null) return ogTitle.attr("content");

        Element h1 = doc.selectFirst("h1");
        if (h1 != null) return h1.text();

        return doc.title(); // Fallback al título de la página
    }

    private static String extraerDescripcion(Document doc) {
        // Múltiples estrategias para extraer descripción
        Element schemaDesc = doc.selectFirst("[itemprop=description]");
        if (schemaDesc != null) return schemaDesc.text();

        Element metaDesc = doc.selectFirst("meta[name=description]");
        if (metaDesc != null) return metaDesc.attr("content");

        Element primerP = doc.selectFirst("p");
        if (primerP != null) return primerP.text();

        return ""; // Fallback a string vacío
    }

    private static List<Ingrediente> extraerIngredientes(Document doc) {
        List<Ingrediente> ingredientes = new ArrayList<>();

        // Múltiples selectores CSS para ingredientes
        String[] selectores = {
                "[itemprop=recipeIngredient]",
                ".ingredient",
                ".ingredients li",
                "[class*=ingredient] li",
                "ul:has(li:contains(gr)) li",
                "ul:has(li:contains(cucharada)) li"
        };

        for (String selector : selectores) {
            Elements elementos = doc.select(selector);
            if (!elementos.isEmpty()) {
                for (Element elem : elementos) {
                    String texto = elem.text().trim();
                    if (!texto.isEmpty() && texto.length() < 200) {
                        ingredientes.add(parsearIngrediente(texto));
                    }
                }
                if (!ingredientes.isEmpty()) break;
            }
        }

        return ingredientes;
    }

    private static Ingrediente parsearIngrediente(String texto) {
        // Regex para separar cantidad y nombre
        Pattern pattern = Pattern.compile("^([\\d.,]+\\s*[a-zA-Z]*)?\\s*(.+)$");
        Matcher matcher = pattern.matcher(texto);

        if (matcher.find()) {
            String cantidad = matcher.group(1);
            String nombre = matcher.group(2);

            if (cantidad != null && nombre != null) {
                return new Ingrediente(nombre.trim(), cantidad.trim());
            }
        }

        return new Ingrediente(texto, ""); // Fallback a ingrediente sin cantidad
    }

    private static List<Paso> extraerPasos(Document doc) {
        List<Paso> pasos = new ArrayList<>();

        // Múltiples selectores CSS para pasos
        String[] selectores = {
                "[itemprop=recipeInstructions] li",
                "[itemprop=recipeInstructions] p",
                ".instructions li",
                ".steps li",
                "[class*=step] li",
                "ol li"
        };

        int numero = 1;
        for (String selector : selectores) {
            Elements elementos = doc.select(selector);
            if (!elementos.isEmpty()) {
                for (Element elem : elementos) {
                    String texto = elem.text().trim();
                    if (!texto.isEmpty() && texto.length() > 10) {
                        texto = texto.replaceFirst("^\\d+\\.?\\s*", ""); // Quitar numeración existente
                        pasos.add(new Paso(numero++, texto));
                    }
                }
                if (!pasos.isEmpty()) break;
            }
        }

        return pasos;
    }

    private static int extraerTiempo(Document doc) {
        // Extraer tiempo desde Schema.org
        Element tiempo = doc.selectFirst("[itemprop=totalTime], [itemprop=cookTime]");
        if (tiempo != null) {
            String valor = tiempo.attr("content");
            return parsearTiempo(valor);
        }

        // Búsqueda por regex en texto completo
        String textoCompleto = doc.text();
        Pattern pattern = Pattern.compile("(\\d+)\\s*(minuto|min|hora)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(textoCompleto);

        if (matcher.find()) {
            int valor = Integer.parseInt(matcher.group(1));
            if (matcher.group(2).toLowerCase().contains("hora")) {
                valor *= 60; // Convertir horas a minutos
            }
            return valor;
        }

        return 0; // Fallback a 0 minutos
    }

    private static int parsearTiempo(String iso) {
        try {
            // Parsear formato ISO 8601 para duraciones (PT1H30M)
            Pattern pattern = Pattern.compile("PT(?:(\\d+)H)?(?:(\\d+)M)?");
            Matcher matcher = pattern.matcher(iso);

            if (matcher.find()) {
                int horas = matcher.group(1) != null ? Integer.parseInt(matcher.group(1)) : 0;
                int minutos = matcher.group(2) != null ? Integer.parseInt(matcher.group(2)) : 0;
                return horas * 60 + minutos;
            }
        } catch (Exception e) {
            Log.w(TAG, "Error al parsear tiempo", e);
        }
        return 0;
    }

    private static int extraerPorciones(Document doc) {
        Element porciones = doc.selectFirst("[itemprop=recipeYield]");
        if (porciones != null) {
            String texto = porciones.text();
            Pattern pattern = Pattern.compile("(\\d+)");
            Matcher matcher = pattern.matcher(texto);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
        }
        return 0;
    }

    private static String extraerImagen(Document doc) {
        // Extraer imagen desde Schema.org
        Element imagen = doc.selectFirst("[itemprop=image]");
        if (imagen != null) {
            if (imagen.tagName().equals("img")) {
                return imagen.attr("src");
            }
            String content = imagen.attr("content");
            if (!content.isEmpty()) {
                return content;
            }
        }

        // Extraer imagen desde Open Graph
        Element ogImage = doc.selectFirst("meta[property=og:image]");
        if (ogImage != null) {
            return ogImage.attr("content");
        }

        return null;
    }

    private static void extraerIngredientesYPasosDeTexto(String texto, RecetaExtraida receta) {
        String[] lineas = texto.split("\n");

        boolean enIngredientes = false;
        boolean enPasos = false;
        int numeroPaso = 1;

        for (String linea : lineas) {
            linea = linea.trim();

            // Detectar secciones por palabras clave
            if (linea.toLowerCase().contains("ingrediente")) {
                enIngredientes = true;
                enPasos = false;
                continue;
            }

            if (linea.toLowerCase().contains("preparación") || linea.toLowerCase().contains("paso") ||
                    linea.toLowerCase().contains("instruccion") || linea.toLowerCase().contains("instrucción")) {
                enIngredientes = false;
                enPasos = true;
                continue;
            }

            // Extraer ingredientes y pasos según la sección actual
            if (enIngredientes && !linea.isEmpty()) {
                receta.ingredientes.add(parsearIngrediente(linea));
            }

            if (enPasos && !linea.isEmpty() && linea.length() > 10) {
                linea = linea.replaceFirst("^\\d+\\.?\\s*", "");
                receta.pasos.add(new Paso(numeroPaso++, linea));
            }
        }
    }

    /**
     * Clase contenedora para almacenar datos de receta extraídos.
     * Utilizada para transferir datos entre métodos de extracción.
     */
    public static class RecetaExtraida {
        public String nombre = "";
        public String descripcion = "";
        public List<Ingrediente> ingredientes = new ArrayList<>();
        public List<Paso> pasos = new ArrayList<>();
        public int tiempoPreparacion = 0;
        public int porciones = 0;
        public String imagenUrl = null;
        public String origen = "Web";
        public String categoria = null; // Categoría extraída (se normalizará)
    }
}