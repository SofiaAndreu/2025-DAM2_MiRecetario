package com.example.recetarioapp.utils;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.recetarioapp.R;

import java.io.File;

//Utilidad para cargar y mostrar imágenes de forma eficiente usando Glide
// - Carga optimizada de imágenes desde rutas locales o URIs
// - Manejo automático de placeholders y errores
// - Recorte centrado para mantener proporciones
// - Soporte para diferentes fuentes de imagen (archivos, URIs)
public class ImageLoader {

    //Carga una imagen de receta desde una ruta o URI con configuración completa
    public static void loadRecipeImage(Context context, String imagePath, ImageView imageView) {
        //Verificar si la ruta de imagen es nula o vacía
        if (imagePath == null || imagePath.isEmpty()) {
            //Mostrar imagen placeholder por defecto
            imageView.setImageResource(R.drawable.placeholder_receta);
            return;
        }

        //Determinar si es una ruta local (archivo) o URI
        if (imagePath.startsWith("/")) {
            //Es una ruta local de archivo
            File imageFile = new File(imagePath);
            Glide.with(context)
                    .load(imageFile) //Cargar desde archivo
                    .placeholder(R.drawable.placeholder_receta) //Imagen mientras carga
                    .error(R.drawable.placeholder_receta) //Imagen si hay error
                    .centerCrop() //Recortar al centro manteniendo proporciones
                    .into(imageView); //ImageView destino
        } else {
            //Es una URI (posiblemente web o content URI)
            Glide.with(context)
                    .load(Uri.parse(imagePath)) //Cargar desde URI
                    .placeholder(R.drawable.placeholder_receta) //Imagen mientras carga
                    .error(R.drawable.placeholder_receta) //Imagen si hay error
                    .centerCrop() //Recortar al centro manteniendo proporciones
                    .into(imageView); //ImageView destino
        }
    }

    //Carga una imagen directamente desde un URI sin placeholders
    public static void loadImage(Context context, Uri uri, ImageView imageView) {
        Glide.with(context)
                .load(uri) //Cargar desde URI
                .centerCrop() //Recortar al centro manteniendo proporciones
                .into(imageView); //ImageView destino
    }
}