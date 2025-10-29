package com.example.recetarioapp.ui.add;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;
import com.google.android.material.textfield.TextInputEditText;
import com.example.recetarioapp.R;
import com.example.recetarioapp.models.Receta;
import com.example.recetarioapp.ui.base.BaseFragment;
import com.example.recetarioapp.utils.*;

public class AddRecetasFragment extends BaseFragment {

    private boolean modoEdicion = false;
    private long recetaIdEditar = -1;
    private Receta recetaEditar;

    private Uri imagenUri;
    private String imagenUrl;

    // Views
    private FrameLayout cardImagen;
    private ImageView ivPreview;
    private LinearLayout layoutAddImage;
    private TextInputEditText etNombre, etDescripcion, etTiempo, etPorciones;
    private TextInputEditText etOrigen, etIngredientes, etPasos;
    private AutoCompleteTextView etDificultad, etCategoria;
    private FrameLayout btnCancelar, btnGuardar, btnImportarUrl;
    private ProgressBar progressBar;

    private ActivityResultLauncher<Intent> pickImageLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        imagenUri = result.getData().getData();
                        mostrarImagenPreview();
                    }
                }
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_recetas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViewModel();
        checkEditMode();
        initViews(view);
        setupDropdowns();
        setupListeners();
        observeViewModel();

        if (modoEdicion) cargarRecetaParaEditar();
    }

    private void checkEditMode() {
        if (getArguments() != null) {
            recetaIdEditar = getArguments().getLong("receta_id", -1);
            modoEdicion = (recetaIdEditar != -1);
        }
    }

    private void initViews(View view) {
        cardImagen = view.findViewById(R.id.card_imagen);
        ivPreview = view.findViewById(R.id.iv_preview);
        layoutAddImage = view.findViewById(R.id.layout_add_image);
        etNombre = view.findViewById(R.id.et_nombre);
        etDescripcion = view.findViewById(R.id.et_descripcion);
        etTiempo = view.findViewById(R.id.et_tiempo);
        etPorciones = view.findViewById(R.id.et_porciones);
        etDificultad = view.findViewById(R.id.et_dificultad);
        etCategoria = view.findViewById(R.id.et_categoria);
        etOrigen = view.findViewById(R.id.et_origen);
        etIngredientes = view.findViewById(R.id.et_ingredientes);
        etPasos = view.findViewById(R.id.et_pasos);
        btnCancelar = view.findViewById(R.id.btn_cancelar);
        btnGuardar = view.findViewById(R.id.btn_guardar);
        btnImportarUrl = view.findViewById(R.id.btn_importar_url);
        progressBar = view.findViewById(R.id.progress_bar);
    }

    private void setupDropdowns() {
        setupDropdown(etDificultad, R.string.dificultad_facil,
                R.string.dificultad_medio, R.string.dificultad_dificil);

        setupDropdown(etCategoria, R.string.categoria_postres,
                R.string.categoria_principales, R.string.categoria_aperitivos,
                R.string.categoria_panaderia, R.string.categoria_bebidas,
                R.string.categoria_ensaladas, R.string.categoria_sopas,
                R.string.categoria_otros);
    }

    private void setupDropdown(AutoCompleteTextView view, int... stringResIds) {
        String[] items = new String[stringResIds.length];
        for (int i = 0; i < stringResIds.length; i++) {
            items[i] = getString(stringResIds[i]);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                items
        );
        view.setAdapter(adapter);
    }

    private void setupListeners() {
        cardImagen.setOnClickListener(v -> seleccionarImagen());
        btnCancelar.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        btnGuardar.setOnClickListener(v -> guardarReceta());
        btnImportarUrl.setOnClickListener(v -> showImportDialog());
    }

    private void seleccionarImagen() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    private void mostrarImagenPreview() {
        ViewExtensions.setVisible(layoutAddImage, false);
        ViewExtensions.setVisible(ivPreview, true);
        ImageLoader.loadImage(requireContext(), imagenUri, ivPreview);
    }

    private void guardarReceta() {
        if (!RecipeValidator.validateRecipeForm(etNombre, etIngredientes, etPasos)) {
            return;
        }

        ViewExtensions.setEnabled(btnGuardar, false);

        if (imagenUri != null) {
            ViewExtensions.setVisible(progressBar, true);
            viewModel.guardarImagenLocal(imagenUri, new com.example.recetarioapp.viewmodels.RecetaViewModel.OnImagenSubidaListener() {
                @Override
                public void onImagenSubida(String path) {
                    imagenUrl = path;
                    crearYGuardarReceta();
                }

                @Override
                public void onError(String mensaje) {
                    showToast(mensaje);
                    ViewExtensions.setEnabled(btnGuardar, true);
                    ViewExtensions.setVisible(progressBar, false);
                }
            });
        } else {
            crearYGuardarReceta();
        }
    }

    private void crearYGuardarReceta() {
        Receta receta = modoEdicion && recetaEditar != null ? recetaEditar : new Receta();

        // Datos básicos
        receta.setNombre(etNombre.getText().toString().trim());
        receta.setDescripcion(etDescripcion.getText().toString().trim());
        receta.setTiempoPreparacion(RecipeValidator.parseIntOrDefault(etTiempo, 0));
        receta.setPorciones(RecipeValidator.parseIntOrDefault(etPorciones, 0));
        receta.setDificultad(etDificultad.getText().toString());
        receta.setCategoria(etCategoria.getText().toString());
        receta.setOrigen(etOrigen.getText().toString().trim());

        // Imagen
        if (imagenUrl != null) {
            receta.setImagenPortadaURL(imagenUrl);
        }

        // Ingredientes y pasos
        receta.setIngredientes(RecipeParser.parseIngredientes(
                etIngredientes.getText().toString()));
        receta.setPasos(RecipeParser.parsePasos(
                etPasos.getText().toString()));

        // Guardar
        if (modoEdicion) {
            viewModel.actualizarReceta(receta);
        } else {
            viewModel.insertarReceta(receta);
        }

        Navigation.findNavController(requireView()).navigateUp();
    }

    private void observeViewModel() {
        viewModel.getProgresoSubida().observe(getViewLifecycleOwner(), progreso -> {
            if (progreso != null && progreso > 0) {
                progressBar.setProgress(progreso);
            }
        });

        observeUiState();
    }

    private void cargarRecetaParaEditar() {
        viewModel.getRecetaById(recetaIdEditar).observe(getViewLifecycleOwner(), receta -> {
            if (receta != null) {
                recetaEditar = receta;
                rellenarFormulario(receta);
            }
        });
    }

    private void rellenarFormulario(Receta r) {
        etNombre.setText(r.getNombre());
        etDescripcion.setText(r.getDescripcion());

        if (r.getTiempoPreparacion() > 0) {
            etTiempo.setText(String.valueOf(r.getTiempoPreparacion()));
        }
        if (r.getPorciones() > 0) {
            etPorciones.setText(String.valueOf(r.getPorciones()));
        }

        if (r.getDificultad() != null) etDificultad.setText(r.getDificultad(), false);
        if (r.getCategoria() != null) etCategoria.setText(r.getCategoria(), false);
        if (r.getOrigen() != null) etOrigen.setText(r.getOrigen());

        // Imagen
        if (r.getImagenPortadaURL() != null && !r.getImagenPortadaURL().isEmpty()) {
            imagenUrl = r.getImagenPortadaURL();
            ViewExtensions.setVisible(layoutAddImage, false);
            ViewExtensions.setVisible(ivPreview, true);
            ImageLoader.loadRecipeImage(requireContext(), r.getImagenPortadaURL(), ivPreview);
        }

        // Ingredientes y pasos
        etIngredientes.setText(RecipeParser.ingredientesToText(r.getIngredientes()));
        etPasos.setText(RecipeParser.pasosToText(r.getPasos()));
    }

    private void showImportDialog() {
        ImportDialogHelper.show(requireContext(), (url,dialog) -> {
            // Mostrar progreso mientras se extrae la receta
            ViewExtensions.setVisible(progressBar, true);
            ViewExtensions.setEnabled(btnGuardar, false);

            // Ejecutar en un hilo background para no bloquear la UI
            new Thread(() -> {
                WebScraperHelper.RecetaExtraida recetaExtraida = WebScraperHelper.extraerRecetaDesdeURL(url);

                // Volver al hilo principal para actualizar la UI
                requireActivity().runOnUiThread(() -> {
                    ViewExtensions.setVisible(progressBar, false);
                    ViewExtensions.setEnabled(btnGuardar, true);

                    if (recetaExtraida != null) {
                        rellenarFormularioDesdeWeb(recetaExtraida);
                        showToast("Receta importada exitosamente");

                        // CERRAR EL DIÁLOGO después de importar exitosamente
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                    } else {
                        showToast("No se pudo extraer la receta de la URL");
                    }
                });
            }).start();
        });
    }

    private void rellenarFormularioDesdeWeb(WebScraperHelper.RecetaExtraida r) {
        if (r == null) return;

        if (!r.nombre.isEmpty()) etNombre.setText(r.nombre);
        if (!r.descripcion.isEmpty()) etDescripcion.setText(r.descripcion);
        if (r.tiempoPreparacion > 0) etTiempo.setText(String.valueOf(r.tiempoPreparacion));
        if (r.porciones > 0) etPorciones.setText(String.valueOf(r.porciones));
        if (r.origen != null) etOrigen.setText(r.origen);

        if (!r.ingredientes.isEmpty()) {
            etIngredientes.setText(RecipeParser.ingredientesToText(r.ingredientes));
        }
        if (!r.pasos.isEmpty()) {
            etPasos.setText(RecipeParser.pasosToText(r.pasos));
        }
    }
}