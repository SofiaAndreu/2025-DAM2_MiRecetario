package com.example.recetarioapp.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import com.example.recetarioapp.R;

//Helper para mostrar diálogos de importación de recetas desde URLs
// - Interfaz de usuario para ingresar URLs de recetas
// - Validación básica de URLs
// - Indicadores de progreso durante la importación
// - Banner informativo para el usuario
public class ImportDialogHelper {

    //Interfaz para manejar eventos de importación de URLs
    public interface OnUrlImportListener {
        //Se llama cuando el usuario confirma la importación de una URL
        void onUrlImport(String url, AlertDialog dialog);
    }

    //Muestra un diálogo para importar recetas desde una URL
    public static void show(Context context, OnUrlImportListener listener) {
        //Crear builder del diálogo
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        //Inflar la vista del diálogo desde el layout
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_importar_url, null);

        //Obtener referencias a las vistas del diálogo
        EditText etUrl = dialogView.findViewById(R.id.et_url);
        FrameLayout btnCancelar = dialogView.findViewById(R.id.btn_cancelar_dialog);
        FrameLayout btnImportar = dialogView.findViewById(R.id.btn_importar_dialog);
        ProgressBar progressBar = dialogView.findViewById(R.id.progress_import);
        LinearLayout bannerInfo = dialogView.findViewById(R.id.banner_info);

        //Crear y configurar el diálogo
        AlertDialog dialog = builder.setView(dialogView).create();

        //Mostrar el banner informativo al usuario
        ViewExtensions.setVisible(bannerInfo, true);

        //Configurar listener para botón cancelar
        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        //Configurar listener para botón importar
        btnImportar.setOnClickListener(v -> {
            String url = etUrl.getText().toString().trim();

            //Validar que la URL no esté vacía
            if (url.isEmpty()) {
                etUrl.setError("Ingresa una URL válida");
                return;
            }

            //Mostrar indicador de progreso y deshabilitar botón durante la importación
            ViewExtensions.setVisible(progressBar, true);
            ViewExtensions.setEnabled(btnImportar, false);

            //Notificar al listener sobre la URL a importar
            if (listener != null) {
                listener.onUrlImport(url, dialog);
            }
        });

        //Mostrar el diálogo
        dialog.show();
    }
}