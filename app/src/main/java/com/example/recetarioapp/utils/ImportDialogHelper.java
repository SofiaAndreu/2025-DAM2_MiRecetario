package com.example.recetarioapp.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import com.example.recetarioapp.R;

public class ImportDialogHelper {

    public interface OnUrlImportListener {
        void onUrlImport(String url, AlertDialog dialog); // Agregar dialog como parámetro
    }

    public static void show(Context context, OnUrlImportListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_importar_url, null);
        EditText etUrl = dialogView.findViewById(R.id.et_url);
        FrameLayout btnCancelar = dialogView.findViewById(R.id.btn_cancelar_dialog);
        FrameLayout btnImportar = dialogView.findViewById(R.id.btn_importar_dialog);
        ProgressBar progressBar = dialogView.findViewById(R.id.progress_import);

        AlertDialog dialog = builder.setView(dialogView).create();

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnImportar.setOnClickListener(v -> {
            String url = etUrl.getText().toString().trim();
            if (url.isEmpty()) {
                etUrl.setError("Ingresa una URL válida");
                return;
            }

            // Mostrar progreso
            ViewExtensions.setVisible(progressBar, true);
            ViewExtensions.setEnabled(btnImportar, false);

            if (listener != null) {
                listener.onUrlImport(url, dialog); // Pasar el diálogo al listener
            }
        });

        dialog.show();
    }
}
