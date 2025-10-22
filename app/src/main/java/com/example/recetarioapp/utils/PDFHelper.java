package com.example.recetarioapp.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.List;
import com.itextpdf.layout.element.ListItem;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.example.recetarioapp.models.Ingrediente;
import com.example.recetarioapp.models.Paso;
import com.example.recetarioapp.models.Receta;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Helper para exportar recetas a PDF
 */
public class PDFHelper {

    private static final String TAG = "PDFHelper";

    // Color principal de la app
    private static final DeviceRgb COLOR_PRIMARY = new DeviceRgb(93, 64, 55); // #5D4037

    /**
     * Exporta una receta a PDF
     * @param context Contexto
     * @param receta Receta a exportar
     * @return Ruta del PDF generado, o null si falla
     */
    public static String exportarRecetaToPDF(Context context, Receta receta) {
        try {
            // Crear directorio para PDFs
            File pdfDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "RecetasPDF");
            if (!pdfDir.exists()) {
                pdfDir.mkdirs();
            }

            // Nombre del archivo
            String nombreArchivo = receta.getNombre().replaceAll("[^a-zA-Z0-9.-]", "_") + ".pdf";
            File pdfFile = new File(pdfDir, nombreArchivo);

            // Crear PDF
            PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile));
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // TÍTULO
            Paragraph titulo = new Paragraph(receta.getNombre())
                    .setFontSize(24)
                    .setBold()
                    .setFontColor(COLOR_PRIMARY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(10);
            document.add(titulo);

            // Línea separadora
            document.add(new Paragraph("_".repeat(80))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.LIGHT_GRAY)
                    .setMarginBottom(15));

            // IMAGEN (si existe)
            if (receta.getImagenPortadaURL() != null && !receta.getImagenPortadaURL().isEmpty()) {
                try {
                    File imagenFile = new File(receta.getImagenPortadaURL());
                    if (imagenFile.exists()) {
                        Image img = new Image(ImageDataFactory.create(imagenFile.getAbsolutePath()));
                        img.setWidth(400);
                        img.setAutoScale(true);
                        img.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);
                        document.add(img);
                        document.add(new Paragraph("\n"));
                    }
                } catch (Exception e) {
                    Log.w(TAG, "No se pudo añadir imagen al PDF", e);
                }
            }

            // DESCRIPCIÓN
            if (receta.getDescripcion() != null && !receta.getDescripcion().isEmpty()) {
                Paragraph descripcion = new Paragraph(receta.getDescripcion())
                        .setItalic()
                        .setFontSize(12)
                        .setMarginBottom(15);
                document.add(descripcion);
            }

            // INFORMACIÓN BÁSICA
            String info = String.format("⏱ Tiempo: %s  |  👥 Porciones: %d  |  📊 Dificultad: %s",
                    receta.getTiempoPrepFormateado(),
                    receta.getPorciones(),
                    receta.getDificultad() != null ? receta.getDificultad() : "Media");

            Paragraph infoParrafo = new Paragraph(info)
                    .setFontSize(11)
                    .setMarginBottom(20)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBackgroundColor(new DeviceRgb(239, 235, 233))
                    .setPadding(10);
            document.add(infoParrafo);

            // INGREDIENTES
            Paragraph tituloIngredientes = new Paragraph("🥘 INGREDIENTES")
                    .setFontSize(16)
                    .setBold()
                    .setFontColor(COLOR_PRIMARY)
                    .setMarginTop(10)
                    .setMarginBottom(10);
            document.add(tituloIngredientes);

            List listaIngredientes = new List()
                    .setSymbolIndent(12)
                    .setListSymbol("•")
                    .setMarginLeft(20);

            if (receta.getIngredientes() != null) {
                for (Ingrediente ing : receta.getIngredientes()) {
                    listaIngredientes.add((ListItem) new ListItem(ing.getIngredienteCompleto())
                            .setMarginBottom(5));
                }
            }
            document.add(listaIngredientes);
            document.add(new Paragraph("\n"));

            // PREPARACIÓN
            Paragraph tituloPreparacion = new Paragraph("👨‍🍳 PREPARACIÓN")
                    .setFontSize(16)
                    .setBold()
                    .setFontColor(COLOR_PRIMARY)
                    .setMarginTop(10)
                    .setMarginBottom(10);
            document.add(tituloPreparacion);

            if (receta.getPasos() != null) {
                for (Paso paso : receta.getPasos()) {
                    Paragraph pasoParrafo = new Paragraph()
                            .add(new com.itextpdf.layout.element.Text(paso.getNumeroPaso() + ". ")
                                    .setBold()
                                    .setFontColor(COLOR_PRIMARY)
                                    .setFontSize(12))
                            .add(new com.itextpdf.layout.element.Text(paso.getDescripcion())
                                    .setFontSize(11))
                            .setMarginBottom(10)
                            .setMarginLeft(10);
                    document.add(pasoParrafo);
                }
            }

            // PIE DE PÁGINA
            document.add(new Paragraph("\n\n"));
            Paragraph pie = new Paragraph("📱 Creado con Recetario Clásico")
                    .setFontSize(9)
                    .setItalic()
                    .setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(pie);

            // Cerrar documento
            document.close();

            Log.d(TAG, "PDF creado: " + pdfFile.getAbsolutePath());
            return pdfFile.getAbsolutePath();

        } catch (Exception e) {
            Log.e(TAG, "Error al crear PDF", e);
            return null;
        }
    }
}