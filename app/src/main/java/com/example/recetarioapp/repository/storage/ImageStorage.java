package com.example.recetarioapp.repository.storage;

import android.app.Application;
import android.net.Uri;
import com.example.recetarioapp.database.RecetasBD;
import com.example.recetarioapp.repository.RecetaRepository;
import com.example.recetarioapp.utils.ImageHelper;

//Gestor especializado para el almacenamiento y manejo de imágenes
// - Proporciona operaciones para guardar imágenes en almacenamiento interno
// - Gestiona ciclo de vida de imágenes
// - Todas las operaciones se ejecutan en background para no bloquear hilo principal
public class ImageStorage {

    private final Application aplicacion;

    //Constructor que recibe el contexto de la aplicación
    public ImageStorage(Application app) {
        this.aplicacion = app;
    }

    //Guarda una imagen desde una URI al almacenamiento interno de la aplicación
    public void guardarImagen(Uri uri, RecetaRepository.OnImagenSubidaListener listener) {
        if (uri == null) {
            listener.onError("URI inválida");
            return;
        }

        //Ejecutar en background para no bloquear la UI
        RecetasBD.bdWriteExecutor.execute(() -> {
            try {
                //Notificar progreso inicial
                listener.onProgress(50);

                //Guardar imagen usando el helper especializado
                String ruta = ImageHelper.saveImageToInternalStorage(
                        aplicacion.getApplicationContext(), uri);

                if (ruta != null) {
                    //Éxito - notificar completado
                    listener.onProgress(100);
                    listener.onSuccess(ruta);
                } else {
                    //Error en el guardado
                    listener.onError("Error al guardar imagen en almacenamiento interno");
                }
            } catch (Exception excepcion) {
                //Error inesperado
                listener.onError("Error inesperado: " + excepcion.getMessage());
            }
        });
    }
}