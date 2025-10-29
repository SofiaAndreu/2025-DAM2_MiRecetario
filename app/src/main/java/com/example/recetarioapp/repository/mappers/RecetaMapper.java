package com.example.recetarioapp.repository.mappers;

import com.example.recetarioapp.models.Receta;
import java.util.HashMap;
import java.util.Map;

// ============================================================================
// 4. RecetaMapper.java - Conversiones Map ↔ Receta
// ============================================================================
public class RecetaMapper {

    public Map<String, Object> toMap(Receta r) {
        Map<String, Object> map = new HashMap<>();
        map.put("nombre", r.getNombre());
        map.put("descripcion", r.getDescripcion());
        map.put("imagenPortadaUrl", r.getImagenPortadaURL());
        map.put("tiempoPreparacion", r.getTiempoPreparacion());
        map.put("porciones", r.getPorciones());
        map.put("dificultad", r.getDificultad());
        map.put("categoria", r.getCategoria());
        map.put("origen", r.getOrigen());
        map.put("ingredientes", r.getIngredientes());
        map.put("pasos", r.getPasos());
        map.put("etiquetas", r.getTags());
        map.put("fechaCreacion", r.getFechaCreacion());
        map.put("fechaModificacion", r.getFechaModificacion());
        map.put("esFavorita", r.isFav());
        map.put("usuarioId", r.getUsuarioId());
        return map;
    }

    public Receta fromMap(Map<String, Object> map) {
        Receta r = new Receta();
        r.setNombre(getString(map, "nombre"));
        r.setDescripcion(getString(map, "descripcion"));
        r.setImagenPortadaURL(getString(map, "imagenPortadaUrl"));
        r.setTiempoPreparacion(getInt(map, "tiempoPreparacion"));
        r.setPorciones(getInt(map, "porciones"));
        r.setDificultad(getString(map, "dificultad"));
        r.setCategoria(getString(map, "categoria"));
        r.setOrigen(getString(map, "origen"));
        r.setUsuarioId(getString(map, "usuarioId"));
        r.setFav(getBoolean(map, "esFavorita"));
        return r;
    }

    private String getString(Map<String, Object> map, String key) {
        return map.get(key) != null ? (String) map.get(key) : null;
    }

    private int getInt(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? ((Long) value).intValue() : 0;
    }

    private boolean getBoolean(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null && (Boolean) value;
    }
}