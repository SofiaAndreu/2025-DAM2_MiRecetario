package com.example.recetarioapp.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;
import androidx.core.content.FileProvider;
import com.example.recetarioapp.models.Receta;
import java.io.File;

/**
 * Helper para exportar recetas a PDF
 */
public class PDFHelper {

    /**
     * Exporta una receta a PDF y devuelve la ruta
     * @param context Contexto de la aplicación
     * @param receta Receta a exportar
     * @return Ruta del PDF generado o null si hubo error
     */
    public static String exportarRecetaToPDF(Context context, Receta receta) {
        try {
            File pdfDir = new File(context.getExternalFilesDir(null), "RecetasPDF");
            if (!pdfDir.exists()) {
                pdfDir.mkdirs();
            }
            String fileName = sanitizeFileName(receta.getNombre()) + ".pdf";
            File pdfFile = new File(pdfDir, fileName);

            return pdfFile.getAbsolutePath();

        } catch (Exception e) {
            android.util.Log.e("PDFHelper", "Error al exportar PDF", e);
            return null;
        }
    }

    /**
     * Abre un PDF con el visor predeterminado
     * @param context Contexto de la actividad
     * @param pdfPath Ruta del PDF
     */
    public static void abrirPDF(Context context, String pdfPath) {
        try {
            File pdfFile = new File(pdfPath);
            Uri uri = FileProvider.getUriForFile(
                    context,
                    context.getApplicationContext().getPackageName() + ".provider",
                    pdfFile
            );

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(Intent.createChooser(intent, "Abrir PDF"));

        } catch (Exception e) {
            Toast.makeText(context,
                    "No se pudo abrir el PDF. Búscalo en: Documentos/RecetasPDF",
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Limpia el nombre de archivo para que sea válido
     */
    private static String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9-_\\.]", "_");
    }
}