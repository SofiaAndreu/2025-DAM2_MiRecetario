package com.example.recetarioapp.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;
import androidx.core.content.FileProvider;
import com.example.recetarioapp.models.Receta;
import java.io.File;

//Helper para exportar recetas a formato PDF y gestionar su visualización
// - Exportación de recetas a archivos PDF
// - Apertura de PDFs con visores predeterminados
// - Gestión segura de archivos usando FileProvider
// - Sanitización de nombres de archivo
public class PDFHelper {

    //Exporta una receta a formato PDF y devuelve la ruta del archivo generado
    public static String exportarRecetaToPDF(Context context, Receta receta) {
        try {
            //Crear directorio para PDFs en almacenamiento externo/interno
            File pdfDir = new File(context.getExternalFilesDir(null), "RecetasPDF");
            if (!pdfDir.exists()) {
                pdfDir.mkdirs(); //Crear directorio si no existe
            }

            //Generar nombre de archivo seguro a partir del nombre de la receta
            String fileName = sanitizeFileName(receta.getNombre()) + ".pdf";
            File pdfFile = new File(pdfDir, fileName);

            //TODO: Implementar generación real del contenido PDF aquí
            //Por ahora solo devuelve la ruta donde se guardaría

            return pdfFile.getAbsolutePath();

        } catch (Exception e) {
            //Log del error para debugging
            android.util.Log.e("PDFHelper", "Error al exportar PDF", e);
            return null;
        }
    }

    //Abre un archivo PDF con el visor predeterminado del dispositivo
    public static void abrirPDF(Context context, String pdfPath) {
        try {
            File pdfFile = new File(pdfPath);

            //Generar URI segura usando FileProvider
            Uri uri = FileProvider.getUriForFile(
                    context,
                    context.getApplicationContext().getPackageName() + ".provider",
                    pdfFile
            );

            //Crear intent para visualizar PDF
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //Permiso de lectura

            //Iniciar selector de aplicaciones para abrir PDF
            context.startActivity(Intent.createChooser(intent, "Abrir PDF"));

        } catch (Exception e) {
            //Mostrar mensaje al usuario si no se puede abrir el PDF
            Toast.makeText(context,
                    "No se pudo abrir el PDF. Búscalo en: Documentos/RecetasPDF",
                    Toast.LENGTH_LONG).show();
        }
    }

    //Limpia y sanitiza un nombre de archivo para que sea válido en sistemas de archivos
    private static String sanitizeFileName(String fileName) {
        //Reemplazar cualquier carácter que no sea alfanumérico, guión, guión bajo o punto
        return fileName.replaceAll("[^a-zA-Z0-9-_\\.]", "_");
    }
}