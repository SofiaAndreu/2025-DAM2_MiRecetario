package com.example.recetarioapp.utils;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.recetarioapp.R;

import java.io.File;

/**
 * Utilidad para cargar imágenes con Glide
 */
public class ImageLoader {

    /**
     * Carga una imagen desde un path/URI
     */
    public static void loadRecipeImage(Context context, String imagePath, ImageView imageView) {
        if (imagePath == null || imagePath.isEmpty()) {
            imageView.setImageResource(R.drawable.placeholder_receta);
            return;
        }

        // Si es un path local
        if (imagePath.startsWith("/")) {
            File imageFile = new File(imagePath);
            Glide.with(context)
                    .load(imageFile)
                    .placeholder(R.drawable.placeholder_receta)
                    .error(R.drawable.placeholder_receta)
                    .centerCrop()
                    .into(imageView);
        } else {
            // Si es una URI
            Glide.with(context)
                    .load(Uri.parse(imagePath))
                    .placeholder(R.drawable.placeholder_receta)
                    .error(R.drawable.placeholder_receta)
                    .centerCrop()
                    .into(imageView);
        }
    }
    /**
     * Carga una imagen desde un URI
     */
    public static void loadImage(Context context, Uri uri, ImageView imageView) {
        Glide.with(context)
                .load(uri)
                .centerCrop()
                .into(imageView);
    }
}