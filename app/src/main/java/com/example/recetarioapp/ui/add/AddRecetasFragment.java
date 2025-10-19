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

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment para añadir una nueva receta
 */
public class AddRecetasFragment extends Fragment {

    private RecetaViewModel viewModel;

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
    private String imagenUrl;

    // Launcher para seleccionar imagen de galería
    private ActivityResultLauncher<Intent> pickImageLauncher;

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

        // Inicializar vistas
        initViews(view);

        // Configurar dropdowns
        setupDropdowns();

        // Configurar listeners
        setupListeners();

        // Observar progreso de subida
        observeViewModel();
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
            viewModel.subirImagen(imagenUri, new RecetaViewModel.OnImagenSubidaListener() {
                @Override
                public void onImagenSubida(String url) {
                    imagenUrl = url;
                    crearYGuardarReceta();
                }

                @Override
                public void onError(String mensaje) {
                    Toast.makeText(getContext(), mensaje, Toast.LENGTH_SHORT).show();
                    btnGuardar.setEnabled(true);
                    progressBar.setVisibility(View.GONE);
                }
            });
        } else {
            crearYGuardarReceta();
        }
    }

    private void crearYGuardarReceta() {
        // Crear objeto Receta
        Receta receta = new Receta();
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

        // Imagen (si se subió)
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

        // Guardar en ViewModel
        viewModel.insertarReceta(receta);

        // Mostrar mensaje
        Toast.makeText(getContext(), "Receta guardada", Toast.LENGTH_SHORT).show();

        // Volver atrás
        Navigation.findNavController(requireView()).navigateUp();
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
}