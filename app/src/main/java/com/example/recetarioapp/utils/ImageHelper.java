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
 * Helper para guardar y cargar imágenes en almacenamiento interno
 */
public class ImageHelper {

    private static final String TAG = "ImageHelper";
    private static final String IMAGES_DIR = "recetas_images";

    /**
     * Guarda una imagen desde URI al almacenamiento interno
     * @param context Contexto de la aplicación
     * @param imageUri URI de la imagen a guardar
     * @return Ruta del archivo guardado, o null si falla
     */
    public static String saveImageToInternalStorage(Context context, Uri imageUri) {
        try {
            File directory = new File(context.getFilesDir(), IMAGES_DIR);
            if (!directory.exists()) directory.mkdirs();

            String fileName = UUID.randomUUID().toString() + ".jpg";
            File imageFile = new File(directory, fileName);

            // Paso 1: obtener tamaño sin cargar la imagen completa
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            InputStream input1 = context.getContentResolver().openInputStream(imageUri);
            BitmapFactory.decodeStream(input1, null, options);
            if (input1 != null) input1.close();

            // Paso 2: calcular factor de escala
            int maxSize = 1200;
            int scale = 1;
            while (options.outWidth / scale > maxSize || options.outHeight / scale > maxSize) {
                scale *= 2;
            }

            // Paso 3: decodificar con el factor de escala
            BitmapFactory.Options scaledOptions = new BitmapFactory.Options();
            scaledOptions.inSampleSize = scale;
            InputStream input2 = context.getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(input2, null, scaledOptions);
            if (input2 != null) input2.close();

            if (bitmap == null) {
                Log.e(TAG, "No se pudo decodificar la imagen");
                return null;
            }

            // Paso 4: guardar comprimido
            FileOutputStream outputStream = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream);
            outputStream.close();
            bitmap.recycle();

            Log.d(TAG, "Imagen guardada optimizada: " + imageFile.getAbsolutePath());
            return imageFile.getAbsolutePath();

        } catch (OutOfMemoryError oom) {
            Log.e(TAG, "Error de memoria al procesar imagen", oom);
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Error al guardar imagen", e);
            return null;
        }
    }

    /**
     * Elimina una imagen del almacenamiento interno
     * @param imagePath Ruta de la imagen a eliminar
     * @return true si se eliminó correctamente
     */
    public static boolean deleteImage(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return false;
        }

        try {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                boolean deleted = imageFile.delete();
                if (deleted) {
                    Log.d(TAG, "Imagen eliminada: " + imagePath);
                }
                return deleted;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al eliminar imagen", e);
        }

        return false;
    }

    /**
     * Verifica si una imagen existe
     * @param imagePath Ruta de la imagen
     * @return true si existe
     */
    public static boolean imageExists(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return false;
        }

        File imageFile = new File(imagePath);
        return imageFile.exists();
    }

    /**
     * Obtiene el File de una imagen
     * @param imagePath Ruta de la imagen
     * @return File de la imagen
     */
    public static File getImageFile(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return null;
        }
        return new File(imagePath);
    }
}