package com.example.recetarioapp.repository.mappers;

import com.example.recetarioapp.models.Ingrediente;
import com.example.recetarioapp.models.Paso;
import com.example.recetarioapp.models.Receta;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mapper especializado para conversiones bidireccionales entre objetos Receta y Map.
 *
 * Esta clase resuelve el problema de incompatibilidad entre los objetos Java complejos
 * y la estructura plana de datos requerida por Firebase Firestore.
 * Proporciona serialización/deserialización completa de todos los componentes
 * de una receta, incluyendo listas anidadas de ingredientes y pasos.
 */
public class RecetaMapper {

    // ==================== CONVERSIÓN RECETA → MAP ====================

    /**
     * Convierte un objeto Receta completo a Map para almacenamiento en Firebase.
     * Serializa todos los atributos incluyendo listas complejas de ingredientes y pasos.
     *
     * @param receta Objeto Receta a serializar
     * @return Map con todos los datos de la receta en formato Firebase-compatible
     */
    public Map<String, Object> toMap(Receta receta) {
        Map<String, Object> map = new HashMap<>();

        // Atributos básicos de la receta
        map.put("nombre", receta.getNombre());
        map.put("descripcion", receta.getDescripcion());
        map.put("imagenPortadaUrl", receta.getImagenPortadaURL());
        map.put("tiempoPreparacion", receta.getTiempoPreparacion());
        map.put("porciones", receta.getPorciones());
        map.put("dificultad", receta.getDificultad());
        map.put("categoria", receta.getCategoria());
        map.put("origen", receta.getOrigen());

        // Conversión de listas complejas a estructuras Firebase-compatibles
        map.put("ingredientes", ingredientesToMapList(receta.getIngredientes()));
        map.put("pasos", pasosToMapList(receta.getPasos()));
        map.put("etiquetas", receta.getTags());

        // Metadatos
        map.put("fechaCreacion", receta.getFechaCreacion());
        map.put("fechaModificacion", receta.getFechaModificacion());
        map.put("esFavorita", receta.isFav());
        map.put("usuarioId", receta.getUsuarioId());

        return map;
    }

    // ==================== CONVERSIÓN MAP → RECETA ====================

    /**
     * Reconstruye un objeto Receta completo desde un Map de Firebase.
     * Deserializa todos los componentes incluyendo listas anidadas.
     *
     * @param map Map con datos de Firebase
     * @return Objeto Receta completamente reconstruido
     */
    public Receta fromMap(Map<String, Object> map) {
        Receta receta = new Receta();

        // Atributos básicos
        receta.setNombre(getString(map, "nombre"));
        receta.setDescripcion(getString(map, "descripcion"));
        receta.setImagenPortadaURL(getString(map, "imagenPortadaUrl"));
        receta.setTiempoPreparacion(getInt(map, "tiempoPreparacion"));
        receta.setPorciones(getInt(map, "porciones"));
        receta.setDificultad(getString(map, "dificultad"));
        receta.setCategoria(getString(map, "categoria"));
        receta.setOrigen(getString(map, "origen"));
        receta.setUsuarioId(getString(map, "usuarioId"));
        receta.setFav(getBoolean(map, "esFavorita"));

        // Reconstrucción de listas complejas
        receta.setIngredientes(mapListToIngredientes(map.get("ingredientes")));
        receta.setPasos(mapListToPasos(map.get("pasos")));
        receta.setTags(mapListToStrings(map.get("etiquetas")));

        return receta;
    }

    // ==================== CONVERSORES DE INGREDIENTES ====================

    /**
     * Serializa una lista de Ingredientes a lista de Maps para Firebase.
     *
     * @param ingredientes Lista de objetos Ingrediente
     * @return Lista de Maps con datos de ingredientes
     */
    private List<Map<String, Object>> ingredientesToMapList(List<Ingrediente> ingredientes) {
        List<Map<String, Object>> listaMaps = new ArrayList<>();
        if (ingredientes == null) return listaMaps;

        for (Ingrediente ingrediente : ingredientes) {
            Map<String, Object> ingredienteMap = new HashMap<>();
            ingredienteMap.put("nombre", ingrediente.getNombre());
            ingredienteMap.put("cantidad", ingrediente.getCantidad());
            ingredienteMap.put("notasOpcionales", ingrediente.getNotasOpcionales());
            listaMaps.add(ingredienteMap);
        }
        return listaMaps;
    }

    /**
     * Deserializa una lista de Maps a objetos Ingrediente.
     *
     * @param obj Objeto que puede contener la lista de ingredientes
     * @return Lista de objetos Ingrediente reconstruidos
     */
    private List<Ingrediente> mapListToIngredientes(Object obj) {
        List<Ingrediente> ingredientes = new ArrayList<>();
        if (!(obj instanceof List)) return ingredientes;

        List<?> lista = (List<?>) obj;
        for (Object item : lista) {
            if (item instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> ingredienteMap = (Map<String, Object>) item;

                Ingrediente ingrediente = new Ingrediente();
                ingrediente.setNombre(getString(ingredienteMap, "nombre"));
                ingrediente.setCantidad(getString(ingredienteMap, "cantidad"));
                ingrediente.setNotasOpcionales(getString(ingredienteMap, "notasOpcionales"));
                ingredientes.add(ingrediente);
            }
        }
        return ingredientes;
    }

    // ==================== CONVERSORES DE PASOS ====================

    /**
     * Serializa una lista de Pasos a lista de Maps para Firebase.
     *
     * @param pasos Lista de objetos Paso
     * @return Lista de Maps con datos de pasos
     */
    private List<Map<String, Object>> pasosToMapList(List<Paso> pasos) {
        List<Map<String, Object>> listaMaps = new ArrayList<>();
        if (pasos == null) return listaMaps;

        for (Paso paso : pasos) {
            Map<String, Object> pasoMap = new HashMap<>();
            pasoMap.put("numeroPaso", paso.getNumeroPaso());
            pasoMap.put("descripcion", paso.getDescripcion());
            listaMaps.add(pasoMap);
        }
        return listaMaps;
    }

    /**
     * Deserializa una lista de Maps a objetos Paso.
     *
     * @param obj Objeto que puede contener la lista de pasos
     * @return Lista de objetos Paso reconstruidos
     */
    private List<Paso> mapListToPasos(Object obj) {
        List<Paso> pasos = new ArrayList<>();
        if (!(obj instanceof List)) return pasos;

        List<?> lista = (List<?>) obj;
        for (Object item : lista) {
            if (item instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> pasoMap = (Map<String, Object>) item;

                Paso paso = new Paso();
                paso.setNumeroPaso(getInt(pasoMap, "numeroPaso"));
                paso.setDescripcion(getString(pasoMap, "descripcion"));
                pasos.add(paso);
            }
        }
        return pasos;
    }

    // ==================== CONVERSOR DE ETIQUETAS ====================

    /**
     * Deserializa una lista genérica a lista de Strings para etiquetas.
     *
     * @param obj Objeto que puede contener la lista de etiquetas
     * @return Lista de Strings con las etiquetas
     */
    private List<String> mapListToStrings(Object obj) {
        List<String> strings = new ArrayList<>();
        if (!(obj instanceof List)) return strings;

        List<?> lista = (List<?>) obj;
        for (Object item : lista) {
            if (item instanceof String) {
                strings.add((String) item);
            }
        }
        return strings;
    }

    // ==================== HELPERS DE TIPADO SEGURO ====================

    /**
     * Obtiene un String de forma segura desde un Map.
     *
     * @param map Map que contiene el valor
     * @param key Clave del valor
     * @return String o null si no existe
     */
    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? String.valueOf(value) : null;
    }

    /**
     * Obtiene un int de forma segura desde un Map.
     * Maneja diferentes tipos numéricos (Long, Integer, etc.)
     *
     * @param map Map que contiene el valor
     * @param key Clave del valor
     * @return Valor numérico o 0 por defecto
     */
    private int getInt(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return 0;

        // Conversión segura de diferentes tipos numéricos
        if (value instanceof Long) {
            return ((Long) value).intValue();
        } else if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return 0;
    }

    /**
     * Obtiene un boolean de forma segura desde un Map.
     *
     * @param map Map que contiene el valor
     * @param key Clave del valor
     * @return Valor boolean o false por defecto
     */
    private boolean getBoolean(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return false;
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return false;
    }
}