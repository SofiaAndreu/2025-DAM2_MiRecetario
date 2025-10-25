package com.example.recetarioapp.utils;

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
 * Helper para extraer recetas desde URLs
 * Compatible con webs de recetas, YouTube, blogs, etc.
 */
public class WebScraperHelper {

    private static final String TAG = "WebScraperHelper";

    /**
     * Extrae una receta desde una URL
     */
    public static RecetaExtraida extraerRecetaDesdeURL(String url) {
        try {
            // Descargar HTML
            String html = descargarHTML(url);
            if (html == null) {
                return null;
            }

            // Parsear HTML
            Document doc = Jsoup.parse(html);

            RecetaExtraida receta = new RecetaExtraida();

            // Detectar tipo de sitio
            if (url.contains("youtube.com") || url.contains("youtu.be")) {
                receta = extraerDeYouTube(doc, url);
            } else {
                receta = extraerDeWebGenerica(doc);
            }

            return receta;

        } catch (Exception e) {
            Log.e(TAG, "Error al extraer receta", e);
            return null;
        }
    }

    /**
     * Descarga el HTML de una URL
     */
    private static String descargarHTML(String url) {
        try {
            OkHttpClient client = new OkHttpClient.Builder()
                    .followRedirects(true)
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
     * Extrae receta de YouTube (desde descripción)
     */
    private static RecetaExtraida extraerDeYouTube(Document doc, String url) {
        RecetaExtraida receta = new RecetaExtraida();

        // Título
        Element titulo = doc.selectFirst("meta[property=og:title]");
        if (titulo != null) {
            receta.nombre = titulo.attr("content");
        }

        // Descripción (contiene la receta)
        Element descripcion = doc.selectFirst("meta[property=og:description]");
        if (descripcion != null) {
            String texto = descripcion.attr("content");
            receta.descripcion = texto;

            // Intentar extraer ingredientes y pasos del texto
            extraerIngredientesYPasosDeTexto(texto, receta);
        }

        // Imagen
        Element imagen = doc.selectFirst("meta[property=og:image]");
        if (imagen != null) {
            receta.imagenUrl = imagen.attr("content");
        }

        receta.origen = "YouTube";

        return receta;
    }

    /**
     * Extrae receta de web genérica
     */
    private static RecetaExtraida extraerDeWebGenerica(Document doc) {
        RecetaExtraida receta = new RecetaExtraida();

        // TÍTULO
        receta.nombre = extraerTitulo(doc);

        // DESCRIPCIÓN
        receta.descripcion = extraerDescripcion(doc);

        // INGREDIENTES
        receta.ingredientes = extraerIngredientes(doc);

        // PASOS
        receta.pasos = extraerPasos(doc);

        // TIEMPO
        receta.tiempoPreparacion = extraerTiempo(doc);

        // PORCIONES
        receta.porciones = extraerPorciones(doc);

        // IMAGEN
        receta.imagenUrl = extraerImagen(doc);

        return receta;
    }

    /**
     * Extrae el título de la receta
     */
    private static String extraerTitulo(Document doc) {
        // Intentar Schema.org
        Element schemaName = doc.selectFirst("[itemtype*=Recipe] [itemprop=name]");
        if (schemaName != null) {
            return schemaName.text();
        }

        // Intentar meta tags
        Element ogTitle = doc.selectFirst("meta[property=og:title]");
        if (ogTitle != null) {
            return ogTitle.attr("content");
        }

        // Intentar h1
        Element h1 = doc.selectFirst("h1");
        if (h1 != null) {
            return h1.text();
        }

        // Título de la página
        return doc.title();
    }

    /**
     * Extrae la descripción
     */
    private static String extraerDescripcion(Document doc) {
        Element schemaDesc = doc.selectFirst("[itemprop=description]");
        if (schemaDesc != null) {
            return schemaDesc.text();
        }

        Element metaDesc = doc.selectFirst("meta[name=description]");
        if (metaDesc != null) {
            return metaDesc.attr("content");
        }

        // Buscar primer párrafo
        Element primerP = doc.selectFirst("p");
        if (primerP != null) {
            return primerP.text();
        }

        return "";
    }

    /**
     * Extrae ingredientes
     */
    private static List<Ingrediente> extraerIngredientes(Document doc) {
        List<Ingrediente> ingredientes = new ArrayList<>();

        // Selectores comunes para ingredientes
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
                if (!ingredientes.isEmpty()) {
                    break;
                }
            }
        }

        return ingredientes;
    }

    /**
     * Parsea un ingrediente separando cantidad y nombre
     */
    private static Ingrediente parsearIngrediente(String texto) {
        // Patrón: "200g de harina" o "2 tazas de leche"
        Pattern pattern = Pattern.compile("^([\\d.,]+\\s*[a-zA-Z]*)?\\s*(.+)$");
        Matcher matcher = pattern.matcher(texto);

        if (matcher.find()) {
            String cantidad = matcher.group(1);
            String nombre = matcher.group(2);

            if (cantidad != null && nombre != null) {
                return new Ingrediente(nombre.trim(), cantidad.trim());
            }
        }

        return new Ingrediente(texto, "");
    }

    /**
     * Extrae pasos de preparación
     */
    private static List<Paso> extraerPasos(Document doc) {
        List<Paso> pasos = new ArrayList<>();

        // Selectores comunes para pasos
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
                        // Limpiar numeración si existe
                        texto = texto.replaceFirst("^\\d+\\.?\\s*", "");
                        pasos.add(new Paso(numero++, texto));
                    }
                }
                if (!pasos.isEmpty()) {
                    break;
                }
            }
        }

        return pasos;
    }

    /**
     * Extrae tiempo de preparación
     */
    private static int extraerTiempo(Document doc) {
        Element tiempo = doc.selectFirst("[itemprop=totalTime], [itemprop=cookTime]");
        if (tiempo != null) {
            String valor = tiempo.attr("content");
            return parsearTiempo(valor);
        }

        // Buscar en texto
        String textoCompleto = doc.text();
        Pattern pattern = Pattern.compile("(\\d+)\\s*(minuto|min|hora)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(textoCompleto);

        if (matcher.find()) {
            int valor = Integer.parseInt(matcher.group(1));
            if (matcher.group(2).toLowerCase().contains("hora")) {
                valor *= 60;
            }
            return valor;
        }

        return 0;
    }

    /**
     * Parsea tiempo en formato ISO8601 (PT30M)
     */
    private static int parsearTiempo(String iso) {
        try {
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

    /**
     * Extrae número de porciones
     */
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

    /**
     * Extrae URL de imagen
     */
    private static String extraerImagen(Document doc) {
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

        Element ogImage = doc.selectFirst("meta[property=og:image]");
        if (ogImage != null) {
            return ogImage.attr("content");
        }

        return null;
    }

    /**
     * Extrae ingredientes y pasos de texto plano
     */
    private static void extraerIngredientesYPasosDeTexto(String texto, RecetaExtraida receta) {
        String[] lineas = texto.split("\n");

        boolean enIngredientes = false;
        boolean enPasos = false;
        int numeroPaso = 1;

        for (String linea : lineas) {
            linea = linea.trim();

            if (linea.toLowerCase().contains("ingrediente")) {
                enIngredientes = true;
                enPasos = false;
                continue;
            }

            if (linea.toLowerCase().contains("preparación") ||
                    linea.toLowerCase().contains("paso") ||
                    linea.toLowerCase().contains("instruccion")) {
                enIngredientes = false;
                enPasos = true;
                continue;
            }

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
     * Clase para almacenar datos extraídos
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
    }
}