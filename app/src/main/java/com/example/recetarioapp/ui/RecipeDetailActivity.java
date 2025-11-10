package com.example.recetarioapp.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.recetarioapp.R;
import com.example.recetarioapp.adapters.IngredienteAdapter;
import com.example.recetarioapp.adapters.PasoAdapter;
import com.example.recetarioapp.models.Receta;
import com.example.recetarioapp.utils.*;
import com.example.recetarioapp.viewmodels.RecetaViewModel;

/**
 * Activity para mostrar los detalles completos de una receta.
 *
 * Proporciona una interfaz rica e interactiva que incluye:
 * - Visualización completa de todos los datos de la receta
 * - Gestión de favoritos
 * - Edición y eliminación de recetas
 * - Compartir recetas en diferentes formatos
 * - Selector de imágenes para la receta
 * - Navegación intuitiva con Collapsing Toolbar
 */
public class RecipeDetailActivity extends AppCompatActivity {

    /**
     * Clave para pasar el ID de la receta entre actividades
     */
    public static final String EXTRA_RECETA_ID = "receta_id";

    // ViewModel para operaciones con recetas
    private RecetaViewModel viewModel;

    // Receta actual siendo mostrada
    private Receta recetaActual;

    // Componentes de UI
    private CollapsingToolbarLayout collapsingToolbar;
    private MaterialToolbar toolbar;
    private ImageView imagenReceta;
    private FloatingActionButton botonFavorito;

    // Adaptadores para las listas
    private IngredienteAdapter adaptadorIngredientes;
    private PasoAdapter adaptadorPasos;

    // Launcher para selección de imágenes
    private ActivityResultLauncher<PickVisualMediaRequest> selectorMedia;

    /**
     * Método principal de inicialización de la Activity.
     *
     * @param savedInstanceState Estado previo de la Activity para restaurar estado
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        // Obtener ID de la receta desde el Intent
        long idReceta = getIntent().getLongExtra(EXTRA_RECETA_ID, -1);
        if (idReceta == -1) {
            Toast.makeText(this, "Error al cargar receta", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inicializar ViewModel
        viewModel = new ViewModelProvider(this).get(RecetaViewModel.class);

        // Configurar todos los componentes
        inicializarVistas();
        configurarToolbar();
        configurarRecyclerViews();
        configurarSelectorFoto();
        configurarListeners();
        cargarReceta(idReceta);
        observarCambiosFavoritos();
    }

    /**
     * Observa cambios en el estado de favoritos para actualizar la UI.
     * Actualiza el icono de favorito cuando cambia el estado de una receta.
     */
    private void observarCambiosFavoritos() {
        viewModel.getFavoritoActualizado().observe(this, idReceta -> {
            if (recetaActual != null && idReceta == recetaActual.getId()) {
                // Actualizar estado local y icono
                recetaActual.setFav(!recetaActual.isFav());
                actualizarIconoFavorito();
            }
        });
    }

    /**
     * Inicializa las referencias a las vistas del layout.
     */
    private void inicializarVistas() {
        collapsingToolbar = findViewById(R.id.collapsing_toolbar);
        toolbar = findViewById(R.id.toolbar);
        imagenReceta = findViewById(R.id.iv_receta_imagen);
        botonFavorito = findViewById(R.id.fab_favorito);
    }

    /**
     * Configura la toolbar con navegación y título.
     * Establece el botón de retroceso y el comportamiento del collapsing toolbar.
     */
    private void configurarToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    /**
     * Configura los RecyclerViews para ingredientes y pasos.
     * Establece los adaptadores y layout managers para ambas listas.
     */
    private void configurarRecyclerViews() {
        RecyclerView recyclerIngredientes = findViewById(R.id.rv_ingredientes);
        RecyclerView recyclerPasos = findViewById(R.id.rv_pasos);

        // Configurar RecyclerView para ingredientes
        adaptadorIngredientes = new IngredienteAdapter();
        recyclerIngredientes.setLayoutManager(new LinearLayoutManager(this));
        recyclerIngredientes.setAdapter(adaptadorIngredientes);

        // Configurar RecyclerView para pasos
        adaptadorPasos = new PasoAdapter();
        recyclerPasos.setLayoutManager(new LinearLayoutManager(this));
        recyclerPasos.setAdapter(adaptadorPasos);
    }


    /**
     * Configura el selector de imágenes desde la galería.
     * Permite al usuario cambiar la imagen de la receta.
     */
    private void configurarSelectorFoto() {
        selectorMedia = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    // Manejar imagen seleccionada
                    if (uri != null && recetaActual != null) {
                        // GUARDAR EN ALMACENAMIENTO INTERNO
                        String internalImagePath = ImageHelper.saveImageToInternalStorage(this, uri);

                        if (internalImagePath != null) {
                            // Actualizar UI y base de datos
                            recetaActual.setImagenPortadaURL(internalImagePath);
                            ImageLoader.loadRecipeImage(this, internalImagePath, imagenReceta);

                            // GUARDAR CAMBIO EN LA BASE DE DATOS
                            viewModel.actualizarReceta(recetaActual);
                            Toast.makeText(this, "Imagen actualizada", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Error al guardar imagen", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    /**
     * Configura todos los listeners de interacción del usuario.
     * Incluye favoritos, edición, eliminación, compartir y cambio de imagen.
     */
    private void configurarListeners() {
        // Favoritos - alternar estado de favorito
        botonFavorito.setOnClickListener(v -> alternarFavorito());

        // Eliminar receta - mostrar diálogo de confirmación
        findViewById(R.id.btn_eliminar).setOnClickListener(v ->
                mostrarDialogoEliminar());

        // Editar receta - navegar a pantalla de edición
        findViewById(R.id.btn_editar).setOnClickListener(v ->
                abrirEdicion());

        // Compartir receta - mostrar opciones de compartir
        findViewById(R.id.btn_compartir).setOnClickListener(v ->
                mostrarOpcionesCompartir());

        // Cambiar imagen - abrir selector de imágenes
        imagenReceta.setOnClickListener(v ->
                selectorMedia.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build()));
    }

    /**
     * Carga los datos de la receta desde el ViewModel.
     * Observa los cambios en la receta y actualiza la UI cuando está disponible.
     *
     * @param idReceta ID de la receta a cargar
     */
    private void cargarReceta(long idReceta) {
        viewModel.getRecetaById(idReceta).observe(this, receta -> {
            if (receta != null) {
                recetaActual = receta;
                mostrarReceta(receta);
            }
        });
    }

    /**
     * Muestra todos los datos de la receta en la UI.
     * Actualiza todos los elementos visuales con la información de la receta.
     *
     * @param receta Receta a mostrar en la interfaz
     */
    private void mostrarReceta(Receta receta) {
        // Título en collapsing toolbar
        collapsingToolbar.setTitle(receta.getNombre());

        // Imagen de portada
        ImageLoader.loadRecipeImage(this, receta.getImagenPortadaURL(), imagenReceta);

        // Descripción - mostrar u ocultar según contenido
        TextView textoDescripcion = findViewById(R.id.tv_descripcion);
        if (receta.getDescripcion() != null && !receta.getDescripcion().isEmpty()) {
            textoDescripcion.setText(receta.getDescripcion());
            ViewExtensions.setVisible(textoDescripcion, true);
        } else {
            ViewExtensions.setVisible(textoDescripcion, false);
        }

        // Metadatos - tiempo, porciones y dificultad
        ((TextView) findViewById(R.id.tv_tiempo)).setText(receta.getTiempoPrepFormateado());
        ((TextView) findViewById(R.id.tv_porciones)).setText(receta.getPorciones() + " porc.");
        ((TextView) findViewById(R.id.tv_dificultad)).setText(
                receta.getDificultad() != null ? receta.getDificultad() : "Media");

        // Chips de categoría y origen
        configurarChip(findViewById(R.id.detail_chip_categoria), receta.getCategoria());
        configurarChip(findViewById(R.id.detail_chip_origen), receta.getOrigen());

        // Listas de ingredientes y pasos
        adaptadorIngredientes.setIngredientes(receta.getIngredientes());
        adaptadorPasos.setPasos(receta.getPasos());

        actualizarIconoFavorito();
    }

    /**
     * Configura un chip mostrándolo u ocultándolo según el contenido.
     * Solo muestra el chip si el texto no está vacío.
     *
     * @param chip Chip a configurar
     * @param texto Texto a mostrar en el chip
     */
    private void configurarChip(Chip chip, String texto) {
        if (texto != null && !texto.isEmpty()) {
            chip.setText(texto);
            ViewExtensions.setVisible(chip, true);
        } else {
            ViewExtensions.setVisible(chip, false);
        }
    }

    /**
     * Alterna el estado de favorito de la receta actual.
     * Actualiza la base de datos y muestra feedback al usuario.
     */
    private void alternarFavorito() {
        if (recetaActual == null) return;

        boolean nuevoEstado = !recetaActual.isFav();
        viewModel.marcarFavorita(recetaActual.getId(), nuevoEstado);

        Toast.makeText(this,
                nuevoEstado ? "Añadida a favoritos" : "Quitada de favoritos",
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Actualiza el icono del botón de favoritos según el estado actual.
     * Muestra icono de favorito lleno o vacío según el estado.
     */
    private void actualizarIconoFavorito() {
        if (recetaActual == null) return;
        botonFavorito.setImageResource(
                recetaActual.isFav() ? R.drawable.ic_fav : R.drawable.ic_fav_no
        );
    }

    /**
     * Muestra diálogo de confirmación para eliminar la receta.
     * Previene eliminaciones accidentales.
     */
    private void mostrarDialogoEliminar() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar receta")
                .setMessage("¿Estás seguro de que quieres eliminar esta receta?")
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarReceta())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    /**
     * Elimina la receta actual del sistema.
     * Navega atrás después de la eliminación exitosa.
     */
    private void eliminarReceta() {
        if (recetaActual == null) return;
        viewModel.eliminarReceta(recetaActual);
        Toast.makeText(this, "Receta eliminada", Toast.LENGTH_SHORT).show();
        finish();
    }

    /**
     * Abre la actividad de edición para la receta actual.
     * Pasa el ID de la receta a la MainActivity para que navegue al fragmento de edición.
     */
    private void abrirEdicion() {
        if (recetaActual == null) return;

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("editar_receta_id", recetaActual.getId());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    /**
     * Muestra las opciones disponibles para compartir la receta.
     * Ofrece compartir como texto o exportar a PDF.
     */
    private void mostrarOpcionesCompartir() {
        new AlertDialog.Builder(this)
                .setTitle("Compartir receta")
                .setItems(new String[]{"Compartir como texto", "Exportar a PDF"},
                        (dialog, which) -> {
                            if (which == 0) compartirComoTexto();
                            else exportarAPDF();
                        })
                .show();
    }

    /**
     * Comparte la receta como texto mediante el selector de apps.
     * Crea un intent de compartir con el texto formateado de la receta.
     */
    private void compartirComoTexto() {
        if (recetaActual == null) return;
        startActivity(RecipeShareHelper.createShareIntent(recetaActual));
    }

    /**
     * Exporta la receta a formato PDF en segundo plano.
     * Muestra progreso y notifica cuando está listo.
     */
    private void exportarAPDF() {
        if (recetaActual == null) return;

        Toast.makeText(this, "Generando PDF...", Toast.LENGTH_SHORT).show();

        // Generar PDF en hilo secundario
        new Thread(() -> {
            String rutaPDF = PDFHelper.exportarRecetaToPDF(this, recetaActual);

            runOnUiThread(() -> {
                if (rutaPDF != null) {
                    mostrarDialogoPDFCreado(rutaPDF);
                } else {
                    Toast.makeText(this, "Error al crear PDF",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    /**
     * Muestra diálogo informativo cuando el PDF ha sido creado.
     * Ofrece opción para abrir el PDF directamente.
     *
     * @param rutaPDF Ruta del archivo PDF creado
     */
    private void mostrarDialogoPDFCreado(String rutaPDF) {
        new AlertDialog.Builder(this)
                .setTitle("PDF creado")
                .setMessage("Ruta: " + rutaPDF)
                .setPositiveButton("Abrir", (dialog, which) -> PDFHelper.abrirPDF(this, rutaPDF))
                .setNegativeButton("Cerrar", null)
                .show();
    }
}