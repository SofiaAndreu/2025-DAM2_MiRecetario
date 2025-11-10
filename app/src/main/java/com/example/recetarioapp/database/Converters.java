package com.example.recetarioapp.database;

import androidx.room.TypeConverter;
import com.example.recetarioapp.models.Ingrediente;
import com.example.recetarioapp.models.Paso;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Clase de conversores de tipos para Room Database.
 *
 * Proporciona métodos para convertir tipos de datos complejos que Room
 * no puede almacenar directamente en formatos compatibles con SQLite.
 * Utiliza la librería Gson para serialización/deserialización JSON.
 */
public class Converters {

    // Instancia única de Gson para optimizar el rendimiento
    private static final Gson gson = new Gson();

    /**
     * Convierte un objeto Date a un valor Long (timestamp).
     *
     * @param date Fecha a convertir
     * @return Timestamp en milisegundos desde epoch, null si la fecha es null
     */
    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    /**
     * Convierte un timestamp Long a objeto Date.
     *
     * @param value Timestamp en milisegundos desde epoch
     * @return Objeto Date correspondiente, null si el valor es null
     */
    @TypeConverter
    public static Date timestampToDate(Long value) {
        return value == null ? null : new Date(value);
    }

    /**
     * Serializa una lista de Strings a formato JSON.
     *
     * @param list Lista de strings a serializar
     * @return String en formato JSON, null si la lista es null
     */
    @TypeConverter
    public static String listStringToJson(List<String> list) {
        if (list == null) {
            return null;
        }
        return gson.toJson(list);
    }

    /**
     * Deserializa un string JSON a lista de Strings.
     *
     * @param value String JSON a deserializar
     * @return Lista de strings, lista vacía si el valor es null
     */
    @TypeConverter
    public static List<String> jsonToListString(String value) {
        if (value == null) {
            return new ArrayList<>();
        }
        Type listType = new TypeToken<List<String>>() {}.getType();
        return gson.fromJson(value, listType);
    }

    /**
     * Deserializa un string JSON a lista de objetos Ingrediente.
     *
     * @param value String JSON que representa la lista de ingredientes
     * @return Lista de Ingredientes, lista vacía si el valor es null
     */
    @TypeConverter
    public static List<Ingrediente> fromIngredientesList(String value) {
        if (value == null) {
            return new ArrayList<>();
        }
        Type listType = new TypeToken<List<Ingrediente>>() {}.getType();
        return gson.fromJson(value, listType);
    }

    /**
     * Serializa una lista de Ingredientes a formato JSON.
     *
     * @param list Lista de ingredientes a serializar
     * @return String en formato JSON, null si la lista es null
     */
    @TypeConverter
    public static String ingredientesListToString(List<Ingrediente> list) {
        if (list == null) {
            return null;
        }
        return gson.toJson(list);
    }

    /**
     * Deserializa un string JSON a lista de objetos Paso.
     *
     * @param value String JSON que representa la lista de pasos
     * @return Lista de Pasos, lista vacía si el valor es null
     */
    @TypeConverter
    public static List<Paso> fromPasosList(String value) {
        if (value == null) {
            return new ArrayList<>();
        }
        Type listType = new TypeToken<List<Paso>>() {}.getType();
        return gson.fromJson(value, listType);
    }

    /**
     * Serializa una lista de Pasos a formato JSON.
     *
     * @param list Lista de pasos a serializar
     * @return String en formato JSON, null si la lista es null
     */
    @TypeConverter
    public static String pasosListToString(List<Paso> list) {
        if (list == null) {
            return null;
        }
        return gson.toJson(list);
    }
}