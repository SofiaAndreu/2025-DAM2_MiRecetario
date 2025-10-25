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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.example.recetarioapp.R;
import com.example.recetarioapp.models.Ingrediente;
import com.example.recetarioapp.models.Paso;
import com.example.recetarioapp.models.Receta;
import com.example.recetarioapp.viewmodels.RecetaViewModel;
import com.example.recetarioapp.utils.WebScraperHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Fragment para añadir una nueva receta
 */
public class AddRecetasFragment extends Fragment {

    private RecetaViewModel viewModel;

    // Modo edición
    private boolean modoEdicion = false;
    private long recetaIdEditar = -1;
    private Receta recetaEditar;

    // Views
    private MaterialCardView cardImagen;
    private ImageView ivPreview;
    private LinearLayout layoutAddImage;
    private TextInputEditText etNombre;
    private TextInputEditText etDescripcion;
    private TextInputEditText etTiempo;
    private TextInputEditText etPorciones;
    private AutoCompleteTextView etDificultad;
    private AutoCompleteTextView etCategoria;
    private TextInputEditText etOrigen;
    private TextInputEditText etIngredientes;
    private TextInputEditText etPasos;
    private MaterialButton btnCancelar;
    private MaterialButton btnGuardar;
    private ProgressBar progressBar;

    // Imagen seleccionada
    private Uri imagenUri;
    private Uri imagenUriTemporal; // Para fotos de cámara
    private String imagenUrl;

    // Launcher para seleccionar imagen de galería
    private ActivityResultLauncher<Intent> pickImageLauncher;

    // Launcher para foto con cámara
    private ActivityResultLauncher<Intent> takePictureLauncher;

    //
    private MaterialButton btnImportarUrl;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configurar launcher para seleccionar imagen
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        imagenUri = result.getData().getData();
                        mostrarImagenPreview(imagenUri);
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_recetas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(RecetaViewModel.class);

        // Verificar si estamos en modo edición
        if (getArguments() != null) {
            recetaIdEditar = getArguments().getLong("receta_id", -1);
            if (recetaIdEditar != -1) {
                modoEdicion = true;
            }
        }

        // Inicializar vistas
        initViews(view);

        // Configurar dropdowns
        setupDropdowns();

        // Configurar listeners
        setupListeners();

        // Observar progreso de subida
        observeViewModel();

        // Si estamos en modo edición, cargar la receta
        if (modoEdicion) {
            cargarRecetaParaEditar();
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
        progressBar = view.findViewById(R.id.progress_bar);
        btnImportarUrl = view.findViewById(R.id.btn_importar_url);
    }

    private void setupDropdowns() {
        // Dificultades
        String[] dificultades = {
                getString(R.string.dificultad_facil),
                getString(R.string.dificultad_medio),
                getString(R.string.dificultad_dificil)
        };
        ArrayAdapter<String> dificultadAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                dificultades
        );
        etDificultad.setAdapter(dificultadAdapter);

        // Categorías
        String[] categorias = {
                getString(R.string.categoria_postres),
                getString(R.string.categoria_principales),
                getString(R.string.categoria_aperitivos),
                getString(R.string.categoria_panaderia),
                getString(R.string.categoria_bebidas),
                getString(R.string.categoria_ensaladas),
                getString(R.string.categoria_sopas),
                getString(R.string.categoria_otros)
        };
        ArrayAdapter<String> categoriaAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                categorias
        );
        etCategoria.setAdapter(categoriaAdapter);
    }

    private void setupListeners() {
        // Click en card de imagen
        cardImagen.setOnClickListener(v -> seleccionarImagen());

        // Botón cancelar
        btnCancelar.setOnClickListener(v -> {
            Navigation.findNavController(v).navigateUp();
        });

        // Botón guardar
        btnGuardar.setOnClickListener(v -> guardarReceta());
        // Botón importar desde URL
        btnImportarUrl.setOnClickListener(v -> mostrarDialogImportarURL());
    }

    private void seleccionarImagen() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    private void mostrarImagenPreview(Uri uri) {
        layoutAddImage.setVisibility(View.GONE);
        ivPreview.setVisibility(View.VISIBLE);

        Glide.with(this)
                .load(uri)
                .centerCrop()
                .into(ivPreview);
    }

    private void guardarReceta() {
        // Validar campos obligatorios
        String nombre = etNombre.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String ingredientesTexto = etIngredientes.getText().toString().trim();
        String pasosTexto = etPasos.getText().toString().trim();

        if (nombre.isEmpty()) {
            etNombre.setError("Campo obligatorio");
            etNombre.requestFocus();
            return;
        }

        if (ingredientesTexto.isEmpty()) {
            etIngredientes.setError("Campo obligatorio");
            etIngredientes.requestFocus();
            return;
        }

        if (pasosTexto.isEmpty()) {
            etPasos.setError("Campo obligatorio");
            etPasos.requestFocus();
            return;
        }

        // Deshabilitar botón mientras se guarda
        btnGuardar.setEnabled(false);

        // Si hay imagen, subirla primero
        if (imagenUri != null) {
            progressBar.setVisibility(View.VISIBLE);
            viewModel.guardarImagenLocal(imagenUri, new RecetaViewModel.OnImagenSubidaListener() {
                @Override
                public void onImagenSubida(String path) {
                    // CORREGIDO: Ejecutar en UI Thread
                    requireActivity().runOnUiThread(() -> {
                        imagenUrl = path;
                        crearYGuardarReceta();
                    });
                }

                @Override
                public void onError(String mensaje) {
                    // CORREGIDO: Ejecutar en UI Thread
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), mensaje, Toast.LENGTH_SHORT).show();
                        btnGuardar.setEnabled(true);
                        progressBar.setVisibility(View.GONE);
                    });
                }
            });
        } else {
            crearYGuardarReceta();
        }
    }

    private void crearYGuardarReceta() {
        // Crear o actualizar objeto Receta
        Receta receta;

        if (modoEdicion && recetaEditar != null) {
            // Modo edición: usar la receta existente
            receta = recetaEditar;
        } else {
            // Modo nuevo: crear receta nueva
            receta = new Receta();
        }

        receta.setNombre(etNombre.getText().toString().trim());
        receta.setDescripcion(etDescripcion.getText().toString().trim());

        // Tiempo
        String tiempoStr = etTiempo.getText().toString().trim();
        if (!tiempoStr.isEmpty()) {
            receta.setTiempoPreparacion(Integer.parseInt(tiempoStr));
        }

        // Porciones
        String porcionesStr = etPorciones.getText().toString().trim();
        if (!porcionesStr.isEmpty()) {
            receta.setPorciones(Integer.parseInt(porcionesStr));
        }

        // Dificultad y Categoría
        receta.setDificultad(etDificultad.getText().toString());
        receta.setCategoria(etCategoria.getText().toString());
        receta.setOrigen(etOrigen.getText().toString().trim());

        // Imagen (si se subió una nueva, o mantener la existente)
        if (imagenUrl != null) {
            receta.setImagenPortadaURL(imagenUrl);
        }

        // Parsear ingredientes (separados por líneas)
        List<Ingrediente> ingredientes = parsearIngredientes(
                etIngredientes.getText().toString()
        );
        receta.setIngredientes(ingredientes);

        // Parsear pasos (separados por líneas)
        List<Paso> pasos = parsearPasos(
                etPasos.getText().toString()
        );
        receta.setPasos(pasos);

        // Guardar o actualizar en ViewModel
        if (modoEdicion) {
            viewModel.actualizarReceta(receta);
            // CORREGIDO: Toast en UI Thread
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), "Receta actualizada", Toast.LENGTH_SHORT).show();
            });
        } else {
            viewModel.insertarReceta(receta);
            // CORREGIDO: Toast en UI Thread
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), "Receta guardada", Toast.LENGTH_SHORT).show();
            });
        }

        // Volver atrás (esto debe ejecutarse en UI Thread)
        requireActivity().runOnUiThread(() -> {
            Navigation.findNavController(requireView()).navigateUp();
        });
    }

    private List<Ingrediente> parsearIngredientes(String texto) {
        List<Ingrediente> ingredientes = new ArrayList<>();
        String[] lineas = texto.split("\n");

        for (String linea : lineas) {
            linea = linea.trim();
            if (!linea.isEmpty()) {
                // Intenta separar cantidad y nombre (ej: "200g harina")
                String[] partes = linea.split(" ", 2);
                if (partes.length == 2) {
                    ingredientes.add(new Ingrediente(partes[1], partes[0]));
                } else {
                    ingredientes.add(new Ingrediente(linea, ""));
                }
            }
        }

        return ingredientes;
    }

    private List<Paso> parsearPasos(String texto) {
        List<Paso> pasos = new ArrayList<>();
        String[] lineas = texto.split("\n");

        int numero = 1;
        for (String linea : lineas) {
            linea = linea.trim();
            if (!linea.isEmpty()) {
                // Quitar numeración si existe (ej: "1. Mezclar...")
                linea = linea.replaceFirst("^\\d+\\.\\s*", "");
                pasos.add(new Paso(numero++, linea));
            }
        }

        return pasos;
    }

    private void observeViewModel() {
        // Observar progreso de subida de imagen
        viewModel.getProgresoSubida().observe(getViewLifecycleOwner(), progreso -> {
            if (progreso != null && progreso > 0) {
                progressBar.setProgress(progreso);
            }
        });

        // Observar mensajes de éxito
        viewModel.getMensajeExito().observe(getViewLifecycleOwner(), mensaje -> {
            if (mensaje != null && !mensaje.isEmpty()) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Carga una receta existente para editarla
     */
    private void cargarRecetaParaEditar() {
        viewModel.getRecetaById(recetaIdEditar).observe(getViewLifecycleOwner(), receta -> {
            if (receta != null) {
                recetaEditar = receta;
                rellenarFormulario(receta);
            }
        });
    }

    /**
     * Rellena el formulario con los datos de la receta
     */
    private void rellenarFormulario(Receta receta) {
        // Nombre y descripción
        etNombre.setText(receta.getNombre());
        etDescripcion.setText(receta.getDescripcion());

        // Tiempo y porciones
        if (receta.getTiempoPreparacion() > 0) {
            etTiempo.setText(String.valueOf(receta.getTiempoPreparacion()));
        }
        if (receta.getPorciones() > 0) {
            etPorciones.setText(String.valueOf(receta.getPorciones()));
        }

        // Dificultad, categoría y origen
        if (receta.getDificultad() != null) {
            etDificultad.setText(receta.getDificultad(), false);
        }
        if (receta.getCategoria() != null) {
            etCategoria.setText(receta.getCategoria(), false);
        }
        if (receta.getOrigen() != null) {
            etOrigen.setText(receta.getOrigen());
        }

        // Imagen existente
        if (receta.getImagenPortadaURL() != null && !receta.getImagenPortadaURL().isEmpty()) {
            imagenUrl = receta.getImagenPortadaURL();
            File imageFile = new File(receta.getImagenPortadaURL());
            layoutAddImage.setVisibility(View.GONE);
            ivPreview.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(imageFile)
                    .centerCrop()
                    .into(ivPreview);
        }

        // Ingredientes (convertir lista a texto)
        StringBuilder ingredientesTexto = new StringBuilder();
        if (receta.getIngredientes() != null) {
            for (Ingrediente ing : receta.getIngredientes()) {
                ingredientesTexto.append(ing.getIngredienteCompleto()).append("\n");
            }
        }
        etIngredientes.setText(ingredientesTexto.toString().trim());

        // Pasos (convertir lista a texto)
        StringBuilder pasosTexto = new StringBuilder();
        if (receta.getPasos() != null) {
            for (Paso paso : receta.getPasos()) {
                pasosTexto.append(paso.getNumeroPaso()).append(". ")
                        .append(paso.getDescripcion()).append("\n\n");
            }
        }
        etPasos.setText(pasosTexto.toString().trim());

        // Cambiar texto del botón
        btnGuardar.setText("Actualizar Receta");
    }

    /**
     * Muestra dialog para pegar URL
     */
    private void mostrarDialogImportarURL() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_importar_url, null);

        TextInputEditText etUrl = dialogView.findViewById(R.id.et_url);

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Importar receta desde URL")
                .setView(dialogView)
                .setPositiveButton("Importar", null)
                .setNegativeButton("Cancelar", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String url = etUrl.getText().toString().trim();
                if (url.isEmpty()) {
                    etUrl.setError("Introduce una URL");
                    return;
                }

                if (!url.startsWith("http")) {
                    url = "https://" + url;
                }

                dialog.dismiss();
                importarRecetaDesdeURL(url);
            });
        });

        dialog.show();
    }

    /**
     * Importa receta desde URL
     */
    private void importarRecetaDesdeURL(String url) {
        progressBar.setVisibility(View.VISIBLE);
        btnImportarUrl.setEnabled(false);

        // Ejecutar en background thread
        new Thread(() -> {
            com.example.recetarioapp.utils.WebScraperHelper.RecetaExtraida recetaExtraida =
                    com.example.recetarioapp.utils.WebScraperHelper.extraerRecetaDesdeURL(url);

            requireActivity().runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                btnImportarUrl.setEnabled(true);

                if (recetaExtraida != null && !recetaExtraida.nombre.isEmpty()) {
                    rellenarFormularioDesdeWeb(recetaExtraida);
                    Toast.makeText(getContext(), "Receta importada correctamente", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "No se pudo extraer la receta. Verifica la URL.", Toast.LENGTH_LONG).show();
                }
            });
        }).start();
    }

    /**
     * Rellena el formulario con datos extraídos de la web
     */
    private void rellenarFormularioDesdeWeb(com.example.recetarioapp.utils.WebScraperHelper.RecetaExtraida recetaExtraida) {
        // Nombre
        if (!recetaExtraida.nombre.isEmpty()) {
            etNombre.setText(recetaExtraida.nombre);
        }

        // Descripción
        if (!recetaExtraida.descripcion.isEmpty()) {
            etDescripcion.setText(recetaExtraida.descripcion);
        }

        // Tiempo
        if (recetaExtraida.tiempoPreparacion > 0) {
            etTiempo.setText(String.valueOf(recetaExtraida.tiempoPreparacion));
        }

        // Porciones
        if (recetaExtraida.porciones > 0) {
            etPorciones.setText(String.valueOf(recetaExtraida.porciones));
        }

        // Origen
        if (recetaExtraida.origen != null) {
            etOrigen.setText(recetaExtraida.origen);
        }

        // Ingredientes
        if (!recetaExtraida.ingredientes.isEmpty()) {
            StringBuilder ingredientesTexto = new StringBuilder();
            for (Ingrediente ing : recetaExtraida.ingredientes) {
                ingredientesTexto.append(ing.getIngredienteCompleto()).append("\n");
            }
            etIngredientes.setText(ingredientesTexto.toString().trim());
        }

        // Pasos
        if (!recetaExtraida.pasos.isEmpty()) {
            StringBuilder pasosTexto = new StringBuilder();
            for (Paso paso : recetaExtraida.pasos) {
                pasosTexto.append(paso.getNumeroPaso()).append(". ")
                        .append(paso.getDescripcion()).append("\n\n");
            }
            etPasos.setText(pasosTexto.toString().trim());
        }
    }
}