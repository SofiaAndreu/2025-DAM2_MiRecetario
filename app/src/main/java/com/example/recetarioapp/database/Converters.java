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

//Clase de conversores de tipos para Room Database
//Proporciona métodos para convertir tipos de datos complejos que Room
//no puede almacenar directamente en formatos compatibles con SQLite
//Utiliza la librería Gson para serialización/deserialización JSON
public class Converters {

    private static final Gson gson = new Gson(); //Instancia única de Gson para optimizar el rendimiento

    //Convierte un objeto Date a un valor Long (timestamp)
    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();   //Retorna Timestamp en milisegundos desde epoch/null si fecha es null
    }

    //Convierte un timestamp Long a objeto Date
    @TypeConverter
    public static Date timestampToDate(Long value) {
        return value == null ? null : new Date(value); //Objeto Date correspondiente, null si el valor es null
    }

    //Serializa una lista de Strings a formato JSON
    @TypeConverter
    public static String listStringToJson(List<String> list) {
        if (list == null) {
            return null;
        }
        return gson.toJson(list); //Retorna String en formato JSON, null si la lista es null
    }

    //Deserializa un string JSON a lista de Strings
    @TypeConverter
    public static List<String> jsonToListString(String value) {
        if (value == null) {
            return new ArrayList<>();
        }
        Type listType = new TypeToken<List<String>>() {}.getType();
        return gson.fromJson(value, listType); //Lista de strings, lista vacía si el valor es null
    }

    //Deserializa un string JSON a lista de objetos Ingrediente
    @TypeConverter
    public static List<Ingrediente> fromIngredientesList(String value) {
        if (value == null) {
            return new ArrayList<>();
        }
        Type listType = new TypeToken<List<Ingrediente>>() {}.getType();
        return gson.fromJson(value, listType); //Retorna lista de Ingredientes, lista vacía si el valor es null
    }

    //Serializa una lista de Ingredientes a formato JSON
    @TypeConverter
    public static String ingredientesListToString(List<Ingrediente> list) {
        if (list == null) {
            return null;
        }
        return gson.toJson(list); //Retorna String en formato JSON, null si la lista es null
    }

    //Deserializa un string JSON a lista de objetos Paso
    @TypeConverter
    public static List<Paso> fromPasosList(String value) {
        if (value == null) {
            return new ArrayList<>();
        }
        Type listType = new TypeToken<List<Paso>>() {}.getType();
        return gson.fromJson(value, listType); //Retorna lista de Pasos, lista vacía si el valor es null
    }

    //Serializa una lista de Pasos a formato JSON
    @TypeConverter
    public static String pasosListToString(List<Paso> list) {
        if (list == null) {
            return null;
        }
        return gson.toJson(list); //Retorna String en formato JSON, null si la lista es null
    }
}