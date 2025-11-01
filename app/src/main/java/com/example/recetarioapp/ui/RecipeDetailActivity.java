package com.example.recetarioapp.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.recetarioapp.R;
import com.example.recetarioapp.adapters.IngredienteAdapter;
import com.example.recetarioapp.adapters.PasoAdapter;
import com.example.recetarioapp.models.Receta;
import com.example.recetarioapp.utils.*;
import com.example.recetarioapp.viewmodels.RecetaViewModel;

public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECETA_ID = "receta_id";

    private RecetaViewModel viewModel;
    private Receta recetaActual;

    // Views principales
    private CollapsingToolbarLayout collapsingToolbar;
    private MaterialToolbar toolbar;
    private ImageView ivRecetaImagen;
    private FloatingActionButton fabFavorito;

    // Adaptadores
    private IngredienteAdapter ingredienteAdapter;
    private PasoAdapter pasoAdapter;

    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        long recetaId = getIntent().getLongExtra(EXTRA_RECETA_ID, -1);
        if (recetaId == -1) {
            Toast.makeText(this, "Error al cargar receta", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(RecetaViewModel.class);

        initViews();
        setupToolbar();
        setupRecyclerViews();
        setupPhotoPicke();
        setupListeners();
        cargarReceta(recetaId);
        setupFavoritoObserver();
    }

    private void setupFavoritoObserver() {
        viewModel.getFavoritoActualizado().observe(this, recetaId -> {
            if (recetaActual != null && recetaId == recetaActual.getId()) {
                // Actualizar el estado local y el icono
                recetaActual.setFav(!recetaActual.isFav());
                actualizarIconoFavorito();
            }
        });
    }

    private void initViews() {
        collapsingToolbar = findViewById(R.id.collapsing_toolbar);
        toolbar = findViewById(R.id.toolbar);
        ivRecetaImagen = findViewById(R.id.iv_receta_imagen);
        fabFavorito = findViewById(R.id.fab_favorito);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerViews() {
        RecyclerView rvIngredientes = findViewById(R.id.rv_ingredientes);
        RecyclerView rvPasos = findViewById(R.id.rv_pasos);

        ingredienteAdapter = new IngredienteAdapter();
        rvIngredientes.setLayoutManager(new LinearLayoutManager(this));
        rvIngredientes.setAdapter(ingredienteAdapter);

        pasoAdapter = new PasoAdapter();
        rvPasos.setLayoutManager(new LinearLayoutManager(this));
        rvPasos.setAdapter(pasoAdapter);
    }

    private void setupPhotoPicke() {
        pickMedia = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null && recetaActual != null) {
                        ivRecetaImagen.setImageURI(uri);
                        recetaActual.setImagenPortadaURL(uri.toString());
                    }
                }
        );
    }

    private void setupListeners() {
        fabFavorito.setOnClickListener(v -> toggleFavorito());

        findViewById(R.id.btn_eliminar).setOnClickListener(v ->
                mostrarDialogEliminar());

        findViewById(R.id.btn_editar).setOnClickListener(v ->
                abrirEdicion());

        findViewById(R.id.btn_compartir).setOnClickListener(v ->
                mostrarOpcionesCompartir());

        ivRecetaImagen.setOnClickListener(v ->
                pickMedia.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build()));
    }

    private void cargarReceta(long recetaId) {
        viewModel.getRecetaById(recetaId).observe(this, receta -> {
            if (receta != null) {
                recetaActual = receta;
                mostrarReceta(receta);
            }
        });
    }

    private void mostrarReceta(Receta r) {
        collapsingToolbar.setTitle(r.getNombre());

        // Imagen
        ImageLoader.loadRecipeImage(this, r.getImagenPortadaURL(), ivRecetaImagen);

        // Descripción
        TextView tvDescripcion = findViewById(R.id.tv_descripcion);
        if (r.getDescripcion() != null && !r.getDescripcion().isEmpty()) {
            tvDescripcion.setText(r.getDescripcion());
            ViewExtensions.setVisible(tvDescripcion, true);
        } else {
            ViewExtensions.setVisible(tvDescripcion, false);
        }

        // Metadata
        ((TextView) findViewById(R.id.tv_tiempo)).setText(r.getTiempoPrepFormateado());
        ((TextView) findViewById(R.id.tv_porciones)).setText(r.getPorciones() + " porc.");
        ((TextView) findViewById(R.id.tv_dificultad)).setText(
                r.getDificultad() != null ? r.getDificultad() : "Media");

        // Chips
        setupChip(findViewById(R.id.detail_chip_categoria), r.getCategoria());
        setupChip(findViewById(R.id.detail_chip_origen), r.getOrigen());

        // Listas
        ingredienteAdapter.setIngredientes(r.getIngredientes());
        pasoAdapter.setPasos(r.getPasos());

        actualizarIconoFavorito();
    }

    private void setupChip(Chip chip, String texto) {
        if (texto != null && !texto.isEmpty()) {
            chip.setText(texto);
            ViewExtensions.setVisible(chip, true);
        } else {
            ViewExtensions.setVisible(chip, false);
        }
    }

    private void toggleFavorito() {
        if (recetaActual == null) return;

        boolean nuevoEstado = !recetaActual.isFav();
        viewModel.marcarFavorita(recetaActual.getId(), nuevoEstado);

        Toast.makeText(this,
                nuevoEstado ? "Añadida a favoritos" : "Quitada de favoritos",
                Toast.LENGTH_SHORT).show();
    }

    // Metodo para iconos personalizados:
    private void actualizarIconoFavorito() {
        fabFavorito.setImageResource(
                recetaActual != null && recetaActual.isFav()
                        ? R.drawable.ic_fav  // Estrella rellena
                        : R.drawable.ic_fav_no  // Estrella vacía
        );
    }

    private void mostrarDialogEliminar() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar receta")
                .setMessage("¿Estás seguro de que quieres eliminar esta receta?")
                .setPositiveButton("Eliminar", (d, w) -> eliminarReceta())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarReceta() {
        if (recetaActual == null) return;
        viewModel.eliminarReceta(recetaActual);
        Toast.makeText(this, "Receta eliminada", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void abrirEdicion() {
        if (recetaActual == null) return;

        Intent intent = new Intent(this, com.example.recetarioapp.MainActivity.class);
        intent.putExtra("editar_receta_id", recetaActual.getId());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void mostrarOpcionesCompartir() {
        new AlertDialog.Builder(this)
                .setTitle("Compartir receta")
                .setItems(new String[]{"Compartir como texto", "Exportar a PDF"},
                        (d, which) -> {
                            if (which == 0) compartirTexto();
                            else exportarPDF();
                        })
                .show();
    }

    private void compartirTexto() {
        if (recetaActual == null) return;
        startActivity(RecipeShareHelper.createShareIntent(recetaActual));
    }

    private void exportarPDF() {
        if (recetaActual == null) return;

        Toast.makeText(this, "Generando PDF...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            String pdfPath = PDFHelper.exportarRecetaToPDF(this, recetaActual);

            runOnUiThread(() -> {
                if (pdfPath != null) {
                    mostrarDialogPDFCreado(pdfPath);
                } else {
                    Toast.makeText(this, "Error al crear PDF",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void mostrarDialogPDFCreado(String pdfPath) {
        new AlertDialog.Builder(this)
                .setTitle("PDF creado")
                .setMessage("Ruta: " + pdfPath)
                .setPositiveButton("Abrir", (d, w) -> PDFHelper.abrirPDF(this, pdfPath))
                .setNegativeButton("Cerrar", null)
                .show();
    }
}