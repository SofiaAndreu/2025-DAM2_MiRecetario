package com.example.recetarioapp.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.recetarioapp.R;
import com.example.recetarioapp.adapters.IngredienteAdapter;
import com.example.recetarioapp.adapters.PasoAdapter;
import com.example.recetarioapp.models.Receta;
import com.example.recetarioapp.viewmodels.RecetaViewModel;

import java.io.File;

/**
 * Activity para mostrar el detalle completo de una receta
 */
public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECETA_ID = "receta_id";

    private RecetaViewModel viewModel;
    private Receta recetaActual;

    // Views
    private CollapsingToolbarLayout collapsingToolbar;
    private MaterialToolbar toolbar;
    private ImageView ivRecetaImagen;
    private TextView tvDescripcion;
    private TextView tvTiempo;
    private TextView tvPorciones;
    private TextView tvDificultad;
    private Chip chipCategoria;
    private Chip chipOrigen;
    private RecyclerView rvIngredientes;
    private RecyclerView rvPasos;
    private FloatingActionButton fabFavorito;
    private MaterialButton btnEditar;
    private MaterialButton btnCompartir;

    // Adapters
    private IngredienteAdapter ingredienteAdapter;
    private PasoAdapter pasoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        // Obtener ID de la receta
        long recetaId = getIntent().getLongExtra(EXTRA_RECETA_ID, -1);
        if (recetaId == -1) {
            Toast.makeText(this, "Error al cargar receta", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inicializar ViewModel
        viewModel = new ViewModelProvider(this).get(RecetaViewModel.class);

        // Inicializar vistas
        initViews();

        // Configurar toolbar
        setupToolbar();

        // Configurar RecyclerViews
        setupRecyclerViews();

        // Configurar listeners
        setupListeners();

        // Cargar receta
        cargarReceta(recetaId);
    }

    private void initViews() {
        collapsingToolbar = findViewById(R.id.collapsing_toolbar);
        toolbar = findViewById(R.id.toolbar);
        ivRecetaImagen = findViewById(R.id.iv_receta_imagen);
        tvDescripcion = findViewById(R.id.tv_descripcion);
        tvTiempo = findViewById(R.id.tv_tiempo);
        tvPorciones = findViewById(R.id.tv_porciones);
        tvDificultad = findViewById(R.id.tv_dificultad);
        chipCategoria = findViewById(R.id.chip_categoria);
        chipOrigen = findViewById(R.id.chip_origen);
        rvIngredientes = findViewById(R.id.rv_ingredientes);
        rvPasos = findViewById(R.id.rv_pasos);
        fabFavorito = findViewById(R.id.fab_favorito);
        btnEditar = findViewById(R.id.btn_editar);
        btnCompartir = findViewById(R.id.btn_compartir);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerViews() {
        // Ingredientes
        ingredienteAdapter = new IngredienteAdapter();
        rvIngredientes.setLayoutManager(new LinearLayoutManager(this));
        rvIngredientes.setAdapter(ingredienteAdapter);

        // Pasos
        pasoAdapter = new PasoAdapter();
        rvPasos.setLayoutManager(new LinearLayoutManager(this));
        rvPasos.setAdapter(pasoAdapter);
    }

    private void setupListeners() {
        // Favorito
        fabFavorito.setOnClickListener(v -> {
            if (recetaActual != null) {
                boolean nuevoEstado = !recetaActual.isFav();
                viewModel.marcarFavorita(recetaActual.getId(), nuevoEstado);
                recetaActual.setFav(nuevoEstado);
                actualizarIconoFavorito();
                Toast.makeText(this,
                        nuevoEstado ? "Añadida a favoritos" : "Quitada de favoritos",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Editar
        btnEditar.setOnClickListener(v -> {
            if (recetaActual != null) {
                abrirEdicion();
            }
        });

        // Compartir
        btnCompartir.setOnClickListener(v -> compartirReceta());

        // Menú de opciones (eliminar)
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == android.R.id.home) {
                finish();
                return true;
            }
            return false;
        });

        // Agregar opción de eliminar al toolbar
        toolbar.inflateMenu(R.menu.menu_recipe_detail);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_delete) {
                mostrarDialogEliminar();
                return true;
            }
            return false;
        });
    }

    private void cargarReceta(long recetaId) {
        viewModel.getRecetaById(recetaId).observe(this, receta -> {
            if (receta != null) {
                recetaActual = receta;
                mostrarReceta(receta);
            }
        });
    }

    private void mostrarReceta(Receta receta) {
        // Título
        collapsingToolbar.setTitle(receta.getNombre());

        // Imagen
        if (receta.getImagenPortadaURL() != null && !receta.getImagenPortadaURL().isEmpty()) {
            File imageFile = new File(receta.getImagenPortadaURL());
            Glide.with(this)
                    .load(imageFile)
                    .placeholder(R.drawable.placeholder_receta)
                    .error(R.drawable.placeholder_receta)
                    .centerCrop()
                    .into(ivRecetaImagen);
        } else {
            ivRecetaImagen.setImageResource(R.drawable.placeholder_receta);
        }

        // Descripción
        if (receta.getDescripcion() != null && !receta.getDescripcion().isEmpty()) {
            tvDescripcion.setText(receta.getDescripcion());
            tvDescripcion.setVisibility(View.VISIBLE);
        } else {
            tvDescripcion.setVisibility(View.GONE);
        }

        // Metadata
        tvTiempo.setText(receta.getTiempoPrepFormateado());
        tvPorciones.setText(receta.getPorciones() + " porc.");
        tvDificultad.setText(receta.getDificultad() != null ? receta.getDificultad() : "Media");

        // Categoría
        if (receta.getCategoria() != null && !receta.getCategoria().isEmpty()) {
            chipCategoria.setText(receta.getCategoria());
            chipCategoria.setVisibility(View.VISIBLE);
        } else {
            chipCategoria.setVisibility(View.GONE);
        }

        // Origen
        if (receta.getOrigen() != null && !receta.getOrigen().isEmpty()) {
            chipOrigen.setText(receta.getOrigen());
            chipOrigen.setVisibility(View.VISIBLE);
        } else {
            chipOrigen.setVisibility(View.GONE);
        }

        // Ingredientes
        ingredienteAdapter.setIngredientes(receta.getIngredientes());

        // Pasos
        pasoAdapter.setPasos(receta.getPasos());

        // Favorito
        actualizarIconoFavorito();
    }

    private void actualizarIconoFavorito() {
        if (recetaActual != null && recetaActual.isFav()) {
            fabFavorito.setImageResource(android.R.drawable.star_big_on);
        } else {
            fabFavorito.setImageResource(android.R.drawable.star_big_off);
        }
    }

    private void compartirReceta() {
        if (recetaActual == null) return;

        StringBuilder texto = new StringBuilder();
        texto.append("📖 ").append(recetaActual.getNombre()).append("\n\n");

        if (recetaActual.getDescripcion() != null) {
            texto.append(recetaActual.getDescripcion()).append("\n\n");
        }

        texto.append("⏱️ Tiempo: ").append(recetaActual.getTiempoPrepFormateado()).append("\n");
        texto.append("👥 Porciones: ").append(recetaActual.getPorciones()).append("\n");
        texto.append("📊 Dificultad: ").append(recetaActual.getDificultad()).append("\n\n");

        // Ingredientes
        texto.append("🥘 INGREDIENTES:\n");
        if (recetaActual.getIngredientes() != null) {
            for (int i = 0; i < recetaActual.getIngredientes().size(); i++) {
                texto.append("• ").append(recetaActual.getIngredientes().get(i).getIngredienteCompleto()).append("\n");
            }
        }

        // Pasos
        texto.append("\n👨‍🍳 PREPARACIÓN:\n");
        if (recetaActual.getPasos() != null) {
            for (int i = 0; i < recetaActual.getPasos().size(); i++) {
                texto.append(i + 1).append(". ").append(recetaActual.getPasos().get(i).getDescripcion()).append("\n\n");
            }
        }

        texto.append("\n📱 Compartido desde Recetario Clásico");

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, recetaActual.getNombre());
        shareIntent.putExtra(Intent.EXTRA_TEXT, texto.toString());

        startActivity(Intent.createChooser(shareIntent, "Compartir receta"));
    }

    private void mostrarDialogEliminar() {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar receta")
                .setMessage("¿Estás seguro de que quieres eliminar esta receta? Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarReceta())
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarReceta() {
        if (recetaActual == null) return;

        viewModel.eliminarReceta(recetaActual);
        Toast.makeText(this, "Receta eliminada", Toast.LENGTH_SHORT).show();
        finish();
    }

    /**
     * Abre el formulario de edición
     */
    private void abrirEdicion() {
        // Como estamos en una Activity, necesitamos volver a MainActivity
        // y navegar al fragment de añadir con el ID de la receta
        Intent intent = new Intent(this, com.example.recetarioapp.MainActivity.class);
        intent.putExtra("editar_receta_id", recetaActual.getId());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }


    /**
     * Muestra opciones para compartir (texto o PDF)
     */
    private void mostrarOpcionesCompartir() {
        String[] opciones = {"Compartir como texto", "Exportar a PDF"};

        new AlertDialog.Builder(this)
                .setTitle("Compartir receta")
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) {
                        compartirReceta();
                    } else {
                        exportarPDF();
                    }
                })
                .show();
    }

    /**
     * Exporta la receta a PDF
     */
    private void exportarPDF() {
        if (recetaActual == null) return;

        Toast.makeText(this, "Generando PDF...", Toast.LENGTH_SHORT).show();

        // Ejecutar en background thread
        new Thread(() -> {
            String pdfPath = com.example.recetarioapp.utils.PDFHelper
                    .exportarRecetaToPDF(this, recetaActual);

            runOnUiThread(() -> {
                if (pdfPath != null) {
                    Toast.makeText(this, "PDF guardado en: Documentos/RecetasPDF", Toast.LENGTH_LONG).show();

                    // Opción de abrir el PDF
                    new AlertDialog.Builder(this)
                            .setTitle("PDF creado")
                            .setMessage("La receta se ha exportado correctamente.\n\nRuta: " + pdfPath)
                            .setPositiveButton("Abrir", (dialog, which) -> abrirPDF(pdfPath))
                            .setNegativeButton("Cerrar", null)
                            .show();
                } else {
                    Toast.makeText(this, "Error al crear PDF", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    /**
     * Abre el PDF generado
     */
    private void abrirPDF(String pdfPath) {
        try {
            File pdfFile = new File(pdfPath);
            android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(
                    this,
                    getApplicationContext().getPackageName() + ".provider",
                    pdfFile);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Abrir PDF"));
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo abrir el PDF. Búscalo en: Documentos/RecetasPDF",
                    Toast.LENGTH_LONG).show();
        }
    }
}