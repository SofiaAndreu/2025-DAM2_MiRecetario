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
 * Converters para Room Database (libreria de BD basada en SQLite).
 * No puede guardar tipos complejos directamente, por eso se usa TypeConverters (""traductor"")
 * Converters.java ->pasa tipos complejos a tipos compatibles para guardar con ROOM
 */
public class Converters {

    private static final Gson gson = new Gson();


    //De DATE -> Long (milisegundos)
    @TypeConverter
    public static Long dateAtimestamp(Date date){
        return date == null ? null :date.getTime();
    }
    //De Long -> a DATE (contrario al anterior)
    @TypeConverter
    public static Date deTimestamp(Long value){
        return value == null ? null : new Date(value);
    }

    //LIST -------------------------------

    //De List -> cadena JSON
    @TypeConverter
    public static String ListStringToString(List<String> list){
        if(list==null){
            return null;
        }
        return gson.toJson(list);
    }
    //De cadena JSON -> Lista de String
    @TypeConverter
    public static List<String> deListString(String value){
        if(value == null){
            return new ArrayList<>();
        }
        Type listType = new TypeToken<List<String>>() {}.getType();
        return gson.fromJson(value, listType);
    }
    //Lista de INGREDIENTES -> cadena JSON
    @TypeConverter
    public static List<Ingrediente> fromIngredientesList(String value){
        if (value == null) {
            return new ArrayList<>();
        }
        Type listType = new TypeToken<List<Ingrediente>>() {}.getType();
        return gson.fromJson(value, listType);
    }
    //JSON Ingredientes -> List<Ingredientes>
    @TypeConverter
    public static String ingredientesListToString(List<Ingrediente> list){
        if (list == null) {
            return null;
        }
        return gson.toJson(list);
    }
    //Lista de PASOS -> cadena JSON
    @TypeConverter
    public static List<Paso> fromPasosList(String value) {
        if (value == null) {
            return new ArrayList<>();
        }
        Type listType = new TypeToken<List<Paso>>() {}.getType();
        return gson.fromJson(value, listType);
    }
    //JSON Pasos -> List<Paso>
    @TypeConverter
    public static String pasosListToString(List<Paso> list) {
        if (list == null) {
            return null;
        }
        return gson.toJson(list);
    }

}
