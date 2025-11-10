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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.google.android.material.textfield.TextInputEditText;
import com.example.recetarioapp.R;
import com.example.recetarioapp.models.Receta;
import com.example.recetarioapp.ui.base.BaseFragment;
import com.example.recetarioapp.utils.*;

/**
 * Fragment para agregar nuevas recetas o editar recetas existentes.
 *
 * Funcionalidades principales:
 * - Formulario completo para crear nuevas recetas
 * - Edición de recetas existentes
 * - Selección de imágenes desde galería
 * - Importación de recetas desde URLs web
 * - Validación de campos del formulario
 * - Categorización y clasificación de recetas
 */
public class AddRecetasFragment extends BaseFragment {

    // Control de modo de operación (agregar vs editar)
    private boolean modoEdicion = false;
    private long recetaIdEditar = -1;
    private Receta recetaEditar;

    // Gestión de imágenes
    private Uri imagenUri;
    private String imagenUrl;

    // Componentes de la interfaz de usuario
    private FrameLayout cardImagen;
    private ImageView ivPreview;
    private LinearLayout layoutAddImage;
    private TextInputEditText etNombre, etDescripcion, etTiempo, etPorciones;
    private TextInputEditText etOrigen, etIngredientes, etPasos;
    private AutoCompleteTextView etDificultad, etCategoria;
    private FrameLayout btnCancelar, btnGuardar, btnImportarUrl;
    private ProgressBar progressBar;

    // Launcher para selección de imágenes
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private boolean observadoresConfigurados = false;

    /**
     * Método llamado al crear el fragmento.
     * Configura el launcher para selección de imágenes.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configurar launcher para selección de imágenes desde galería
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // Manejar resultado de selección de imagen
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        imagenUri = result.getData().getData();
                        mostrarImagenPreview();
                    }
                }
        );
    }

    /**
     * Crea la vista del fragmento inflando el layout correspondiente.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_recetas, container, false);
    }

    /**
     * Configura la vista después de que ha sido creada.
     * Inicializa componentes según el modo (agregar o editar).
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar todos los componentes
        initViewModel();
        checkEditMode();
        initViews(view);
        setupDropdowns();
        setupListeners();
        setupObservadoresGuardado();
       // observeViewModel();

        // Si está en modo edición, cargar datos de la receta
        if (modoEdicion) cargarRecetaParaEditar();
    }

    /**
     * Verifica si el fragmento está en modo edición.
     * Extrae el ID de receta de los argumentos si existe.
     */
    private void checkEditMode() {
        if (getArguments() != null) {
            recetaIdEditar = getArguments().getLong("receta_id", -1);
            modoEdicion = (recetaIdEditar != -1);
        }
    }

    /**
     * Inicializa las referencias a las vistas del layout.
     *
     * @param view Vista raíz del fragmento
     */
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

    /**
     * Configura los dropdowns (AutoCompleteTextView) para dificultad y categoría.
     * Carga las opciones desde recursos de strings.
     */
    private void setupDropdowns() {
        // Configurar dropdown de dificultad
        setupDropdown(etDificultad, R.string.dificultad_facil,
                R.string.dificultad_medio, R.string.dificultad_dificil);

        // Configurar dropdown de categoría
        setupDropdown(etCategoria, R.string.categoria_postres,
                R.string.categoria_principales, R.string.categoria_aperitivos,
                R.string.categoria_panaderia, R.string.categoria_bebidas,
                R.string.categoria_ensaladas, R.string.categoria_sopas,
                R.string.categoria_otros);
    }

    /**
     * Configura un AutoCompleteTextView con opciones desde recursos de strings.
     *
     * @param view AutoCompleteTextView a configurar
     * @param stringResIds Array de IDs de recursos de strings para las opciones
     */
    private void setupDropdown(AutoCompleteTextView view, int... stringResIds) {
        String[] items = new String[stringResIds.length];
        for (int i = 0; i < stringResIds.length; i++) {
            items[i] = getString(stringResIds[i]);
        }

        // Crear adaptador para las opciones del dropdown
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                items
        );
        view.setAdapter(adapter);
    }

    /**
     * Configura los listeners de interacción para los botones y elementos de la UI.
     */
    private void setupListeners() {
        // Listener para seleccionar imagen
        cardImagen.setOnClickListener(v -> seleccionarImagen());

        // Listener para cancelar - navega hacia atrás
        btnCancelar.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        // Listener para guardar receta
        btnGuardar.setOnClickListener(v -> guardarReceta());

        // Listener para importar desde URL
        btnImportarUrl.setOnClickListener(v -> showImportDialog());
    }

    /**
     * Configura observadores para el proceso de guardado.
     * Maneja navegación automática después de guardar exitosamente.
     */
    private void setupObservadoresGuardado() {
        if (observadoresConfigurados) return;

        // Observar mensajes de éxito - navegar atrás cuando se guarda exitosamente
        viewModel.getMensajeExito().observe(getViewLifecycleOwner(), mensaje -> {
            if (mensaje != null && (mensaje.contains("guardada") || mensaje.contains("actualizada"))) {
                navegarAtrasSeguro();
            }
        });

        // Observar mensajes de error - habilitar botón y mostrar error
        viewModel.getMensajeError().observe(getViewLifecycleOwner(), mensaje -> {
            if (mensaje != null) {
                ViewExtensions.setEnabled(btnGuardar, true);
                ViewExtensions.setVisible(progressBar, false);
                showToast("Error: " + mensaje);
            }
        });

        observadoresConfigurados = true;
    }

    /**
     * Inicia la actividad para seleccionar imagen desde la galería.
     */
    private void seleccionarImagen() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    /**
     * Muestra la vista previa de la imagen seleccionada.
     * Oculta el layout de agregar imagen y muestra la imagen.
     */
    private void mostrarImagenPreview() {
        ViewExtensions.setVisible(layoutAddImage, false);
        ViewExtensions.setVisible(ivPreview, true);
        ImageLoader.loadImage(requireContext(), imagenUri, ivPreview);
    }

    /**
     * Valida y guarda la receta en la base de datos.
     * Maneja subida de imagen si es necesario.
     */
    private void guardarReceta() {
        // Validar campos obligatorios del formulario
        if (!RecipeValidator.validateRecipeForm(etNombre, etIngredientes, etPasos)) {
            return;
        }

        // Deshabilitar botón y mostrar progreso durante el guardado
        ViewExtensions.setEnabled(btnGuardar, false);
        ViewExtensions.setVisible(progressBar, true);

        // Si hay imagen seleccionada, subirla primero
        if (imagenUri != null) {
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
            // Si no hay imagen, guardar directamente
            crearYGuardarReceta();
        }
    }

    /**
     * Crea el objeto Receta y lo guarda en la base de datos.
     * Diferencia entre creación nueva y actualización.
     */
    private void crearYGuardarReceta() {
        // Usar receta existente si está en modo edición, sino crear nueva
        Receta receta = modoEdicion && recetaEditar != null ? recetaEditar : new Receta();

        // Establecer datos básicos de la receta
        receta.setNombre(etNombre.getText().toString().trim());
        receta.setDescripcion(etDescripcion.getText().toString().trim());
        receta.setTiempoPreparacion(RecipeValidator.parseIntOrDefault(etTiempo, 0));
        receta.setPorciones(RecipeValidator.parseIntOrDefault(etPorciones, 0));
        receta.setDificultad(etDificultad.getText().toString());
        receta.setCategoria(etCategoria.getText().toString());
        receta.setOrigen(etOrigen.getText().toString().trim());

        // Establecer URL de imagen si existe
        if (imagenUrl != null) {
            receta.setImagenPortadaURL(imagenUrl);
        }

        // Parsear y establecer ingredientes y pasos
        receta.setIngredientes(RecipeParser.parseIngredientes(
                etIngredientes.getText().toString()));
        receta.setPasos(RecipeParser.parsePasos(
                etPasos.getText().toString()));

        // Guardar en base de datos según el modo
        if (modoEdicion) {
            viewModel.actualizarReceta(receta);
        } else {
            viewModel.insertarReceta(receta);
        }
    }

    /**
     * Navega de forma segura hacia atrás después de guardar.
     * Maneja casos donde el fragmento podría no estar disponible.
     */
    private void navegarAtrasSeguro() {
        if (!isAdded() || getView() == null) return;

        requireActivity().runOnUiThread(() -> {
            try {
                NavController navController = Navigation.findNavController(getView());
                // Verificar que aún estamos en este fragmento antes de navegar
                if (navController.getCurrentDestination() != null &&
                        navController.getCurrentDestination().getId() == R.id.addRecipeFragment) {
                    navController.popBackStack();
                }
            } catch (Exception e) {
                // Fallback seguro si hay error en navegación
                if (!requireActivity().isFinishing()) {
                    requireActivity().onBackPressed();
                }
            }
        });
    }

    /**
     * Carga los datos de la receta para editar desde la base de datos.
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
     * Rellena el formulario con los datos de una receta existente.
     *
     * @param r Receta con los datos a mostrar en el formulario
     */
    private void rellenarFormulario(Receta r) {
        // Rellenar campos básicos
        etNombre.setText(r.getNombre());
        etDescripcion.setText(r.getDescripcion());

        // Rellenar campos numéricos si tienen valor
        if (r.getTiempoPreparacion() > 0) {
            etTiempo.setText(String.valueOf(r.getTiempoPreparacion()));
        }
        if (r.getPorciones() > 0) {
            etPorciones.setText(String.valueOf(r.getPorciones()));
        }

        // Rellenar dropdowns
        if (r.getDificultad() != null) etDificultad.setText(r.getDificultad(), false);
        if (r.getCategoria() != null) etCategoria.setText(r.getCategoria(), false);
        if (r.getOrigen() != null) etOrigen.setText(r.getOrigen());

        // Cargar imagen si existe
        if (r.getImagenPortadaURL() != null && !r.getImagenPortadaURL().isEmpty()) {
            imagenUrl = r.getImagenPortadaURL();
            ViewExtensions.setVisible(layoutAddImage, false);
            ViewExtensions.setVisible(ivPreview, true);
            ImageLoader.loadRecipeImage(requireContext(), r.getImagenPortadaURL(), ivPreview);
        }

        // Rellenar ingredientes y pasos
        etIngredientes.setText(RecipeParser.ingredientesToText(r.getIngredientes()));
        etPasos.setText(RecipeParser.pasosToText(r.getPasos()));
    }

    /**
     * Muestra diálogo para importar receta desde URL web.
     * Ejecuta web scraping en segundo plano para extraer datos.
     */
    private void showImportDialog() {
        ImportDialogHelper.show(requireContext(), (url, dialog) -> {
            // Mostrar progreso durante la importación
            ViewExtensions.setVisible(progressBar, true);
            ViewExtensions.setEnabled(btnGuardar, false);

            // Ejecutar web scraping en hilo secundario
            new Thread(() -> {
                WebScraperHelper.RecetaExtraida recetaExtraida =
                        WebScraperHelper.extraerRecetaDesdeURL(url, requireContext());

                requireActivity().runOnUiThread(() -> {
                    // Ocultar progreso y habilitar botón
                    ViewExtensions.setVisible(progressBar, false);
                    ViewExtensions.setEnabled(btnGuardar, true);

                    if (recetaExtraida != null) {
                        // Rellenar formulario con datos extraídos
                        rellenarFormularioDesdeWeb(recetaExtraida);
                        showToast("Receta importada exitosamente");

                        // Cerrar diálogo si está abierto
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

    /**
     * Rellena el formulario con datos extraídos desde una URL web.
     *
     * @param r Objeto con datos de receta extraídos
     */
    private void rellenarFormularioDesdeWeb(WebScraperHelper.RecetaExtraida r) {
        if (r == null) return;

        // Rellenar campos con datos extraídos
        if (!r.nombre.isEmpty()) etNombre.setText(r.nombre);
        if (!r.descripcion.isEmpty()) etDescripcion.setText(r.descripcion);
        if (r.tiempoPreparacion > 0) etTiempo.setText(String.valueOf(r.tiempoPreparacion));
        if (r.porciones > 0) etPorciones.setText(String.valueOf(r.porciones));
        if (r.origen != null) etOrigen.setText(r.origen);

        // Asignar categoría normalizada desde datos extraídos
        if (r.categoria != null && !r.categoria.isEmpty()) {
            etCategoria.setText(r.categoria, false);
        }

        // Rellenar ingredientes y pasos
        if (!r.ingredientes.isEmpty()) {
            etIngredientes.setText(RecipeParser.ingredientesToText(r.ingredientes));
        }
        if (!r.pasos.isEmpty()) {
            etPasos.setText(RecipeParser.pasosToText(r.pasos));
        }
    }

    /**
     * Limpia mensajes del ViewModel al destruir la vista.
     * Previene fugas de memoria y mensajes duplicados.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewModel.limpiarMensajesE();
    }
}