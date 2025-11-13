package com.example.recetarioapp.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.UUID;

/**
 * Helper para gestionar operaciones con imágenes en almacenamiento interno.
 *
 * Funcionalidades principales:
 * - Guardar imágenes desde URI con optimización de memoria y tamaño
 * - Manejo de errores con logging
 */
public class ImageHelper {

    // Tag para logging de operaciones con imágenes
    private static final String TAG = "ImageHelper";

    //Directorio donde se almacenan las imágenes de recetas
    private static final String IMAGES_DIR = "recetas_images";

    /**
     * Guarda una imagen desde URI al almacenamiento interno con optimización.
     * Realiza compresión y escalado para evitar problemas de memoria.
     *
     * @param context Contexto de la aplicación para acceso a archivos
     * @param imageUri URI de la imagen a guardar (desde galería o cámara)
     * @return Ruta absoluta del archivo guardado, o null si falla la operación
     */
    public static String saveImageToInternalStorage(Context context, Uri imageUri) {
        try {
            // Crear directorio de imágenes si no existe
            File directory = new File(context.getFilesDir(), IMAGES_DIR);
            if (!directory.exists()) directory.mkdirs();

            // Generar nombre único para evitar colisiones
            String fileName = UUID.randomUUID().toString() + ".jpg";
            File imageFile = new File(directory, fileName);

            // Paso 1: Obtener dimensiones sin cargar la imagen completa en memoria
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true; // Solo leer metadatos
            InputStream input1 = context.getContentResolver().openInputStream(imageUri);
            BitmapFactory.decodeStream(input1, null, options);
            if (input1 != null) input1.close();

            // Paso 2: Calcular factor de escala para reducir tamaño de imagen
            int maxSize = 1200; // Tamaño máximo en píxeles
            int scale = 1;
            while (options.outWidth / scale > maxSize || options.outHeight / scale > maxSize) {
                scale *= 2;
            }

            // Paso 3: Decodificar imagen con el factor de escala calculado
            BitmapFactory.Options scaledOptions = new BitmapFactory.Options();
            scaledOptions.inSampleSize = scale;
            InputStream input2 = context.getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(input2, null, scaledOptions);
            if (input2 != null) input2.close();

            // Verificar que la imagen se decodificó correctamente
            if (bitmap == null) {
                Log.e(TAG, "No se pudo decodificar la imagen");
                return null;
            }

            // Paso 4: Guardar imagen comprimida en formato JPEG
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream); // 85% calidad
            outputStream.close();
            bitmap.recycle(); // Liberar memoria inmediatamente

            Log.d(TAG, "Imagen guardada optimizada: " + imageFile.getAbsolutePath());
            return imageFile.getAbsolutePath();

        } catch (OutOfMemoryError oom) {
            // Manejar específicamente errores de memoria
            Log.e(TAG, "Error de memoria al procesar imagen", oom);
            return null;
        } catch (Exception e) {
            // Manejar otros errores genéricos
            Log.e(TAG, "Error al guardar imagen", e);
            return null;
        }
    }
}