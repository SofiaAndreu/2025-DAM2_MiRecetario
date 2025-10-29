package com.example.recetarioapp.repository.storage;

import android.app.Application;
import android.net.Uri;
import com.example.recetarioapp.database.RecetasBD;
import com.example.recetarioapp.repository.RecetaRepository;
import com.example.recetarioapp.utils.ImageHelper;

// ============================================================================
// 5. ImageStorageManager.java - Gestión de imágenes
// ============================================================================
public class ImageStorage {

    private final Application app;

    public ImageStorage(Application app) {
        this.app = app;
    }

    public void guardarImagen(Uri uri, RecetaRepository.OnImagenSubidaListener listener) {
        if (uri == null) {
            listener.onError("URI inválida");
            return;
        }

        RecetasBD.bdWriteExecutor.execute(() -> {
            try {
                listener.onProgress(50);

                String path = ImageHelper.saveImageToInternalStorage(
                        app.getApplicationContext(), uri);

                if (path != null) {
                    listener.onProgress(100);
                    listener.onSuccess(path);
                } else {
                    listener.onError("Error al guardar imagen");
                }
            } catch (Exception e) {
                listener.onError("Error: " + e.getMessage());
            }
        });
    }
}
