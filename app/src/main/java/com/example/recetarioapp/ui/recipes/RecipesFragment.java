package com.example.recetarioapp.ui.recipes;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.recetarioapp.utils.RecipeFilterDialog;
import com.example.recetarioapp.viewmodels.RecetaViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.example.recetarioapp.R;
import com.example.recetarioapp.adapters.RecetaAdapter;
import com.example.recetarioapp.models.Receta;
import com.example.recetarioapp.ui.base.BaseFragment;
import com.example.recetarioapp.utils.ViewExtensions;

import java.util.List;

/**
 * Fragment para mostrar y gestionar la lista de recetas.
 *
 * Funcionalidades incluidas:
 * - Búsqueda en tiempo real de recetas
 * - Filtrado por categorías y tiempo de preparación
 * - Gestión de estados vacíos
 * - Actualización mediante gesto pull-to-refresh
 * - Navegación a detalles de recetas
 */
public class RecipesFragment extends BaseFragment {

    // Componentes de la interfaz de usuario
    private EditText etBuscar;
    private RecyclerView rvRecetas;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout layoutEmpty;
    private RecetaAdapter adapter;
    private RecetaViewModel viewModel;

    // Vistas para estado vacío
    private TextView emptyIcon, emptyTitle, emptySubtitle;
    private MaterialButton btnLimpiarBusqueda;

    // Chips para filtros rápidos
    private Chip chipTodas, chipPostres, chipPrincipales, chipRapidas;

    // Control de estado de búsqueda y filtros
    private boolean isSearching = false;
    private String currentSearchQuery = "";

    /**
     * Inicializa el ViewModel específico para este fragmento.
     * Sobrescribe el método de la clase base para usar RecetaViewModel.
     */
    @Override
    public void initViewModel() {
        viewModel = new ViewModelProvider(this).get(RecetaViewModel.class);
    }

    /**
     * Crea la vista del fragmento inflando el layout correspondiente.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recipes, container, false);
    }

    /**
     * Configura la vista después de que ha sido creada.
     * Inicializa componentes y configura observadores de datos.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar todos los componentes
        initViewModel();
        initViews(view);
        setupRecyclerView();
        setupSearch();
        setupFilters();
        setupSwipeRefresh();
        observeData();
        setupFavoritoObserver();
    }

    /**
     * Configura el observador para actualizaciones de estado de favoritos.
     * Actualiza la interfaz cuando una receta cambia su estado de favorito.
     */
    private void setupFavoritoObserver() {
        viewModel.getFavoritoActualizado().observe(getViewLifecycleOwner(), recetaId -> {
            // Actualizar el item específico en el adapter cuando cambia un favorito
            if (adapter != null) {
                // Buscar la receta en la lista actual y actualizar su estado
                for (int i = 0; i < adapter.getItemCount(); i++) {
                    Receta receta = adapter.getRecetaEn(i);
                    if (receta != null && receta.getId() == recetaId) {
                        receta.setFav(!receta.isFav()); // Invertir estado
                        adapter.notifyItemChanged(i);
                        break;
                    }
                }
            }
        });
    }

    /**
     * Inicializa las referencias a las vistas del layout.
     *
     * @param view Vista raíz del fragmento
     */
    private void initViews(View view) {
        etBuscar = view.findViewById(R.id.et_buscar);
        rvRecetas = view.findViewById(R.id.rv_recetas);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        layoutEmpty = view.findViewById(R.id.layout_empty);

        // Configurar vistas de estado vacío
        emptyIcon = layoutEmpty.findViewById(R.id.empty_icon);
        emptyTitle = layoutEmpty.findViewById(R.id.empty_title);
        emptySubtitle = layoutEmpty.findViewById(R.id.empty_subtitle);
        btnLimpiarBusqueda = layoutEmpty.findViewById(R.id.btn_limpiar_busqueda);

        // Inicializar chips de filtro
        chipTodas = view.findViewById(R.id.chip_todas);
        chipPostres = view.findViewById(R.id.chip_postres);
        chipPrincipales = view.findViewById(R.id.chip_principales);
        chipRapidas = view.findViewById(R.id.chip_rapidas);

        // Configurar botón para limpiar búsqueda
        btnLimpiarBusqueda.setOnClickListener(v -> {
            limpiarBusqueda();
            etBuscar.clearFocus();
            hideKeyboard();
        });
    }

    /**
     * Configura el RecyclerView para mostrar la lista de recetas.
     * Establece el layout manager y el adapter.
     */
    private void setupRecyclerView() {
        adapter = new RecetaAdapter();
        rvRecetas.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRecetas.setAdapter(adapter);

        // Configurar listeners para interacciones con recetas
        adapter.setOnRecetaClickListener(this::openRecipeDetail);
        adapter.setOnFavClickListener(this::toggleFavorite);
    }

    /**
     * Configura el sistema de búsqueda en tiempo real.
     * Monitoriza cambios en el campo de búsqueda y actualiza resultados.
     */
    private void setupSearch() {
        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString();
                isSearching = !currentSearchQuery.trim().isEmpty();

                if (isSearching) {
                    // Activar modo búsqueda
                    viewModel.buscar(currentSearchQuery);
                    desmarcarTodosLosChips();
                    switchToSearchMode();
                } else {
                    // Volver al modo normal
                    viewModel.buscar("");
                    switchToNormalMode();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    //Cambia al modo de búsqueda observando recetas filtradas.
    private void switchToSearchMode() {
        viewModel.getTodasLasRecetas().removeObservers(getViewLifecycleOwner());
        viewModel.getRecetasFiltradas().observe(getViewLifecycleOwner(), this::actualizarLista);
    }

    /**
     * Cambia al modo normal observando todas las recetas.
     */
    private void switchToNormalMode() {
        viewModel.getRecetasFiltradas().removeObservers(getViewLifecycleOwner());
        viewModel.getTodasLasRecetas().observe(getViewLifecycleOwner(), this::actualizarLista);
    }

    /**
     * Configura los chips de filtro rápido.
     * Cada chip aplica un filtro diferente al ser seleccionado.
     */
    private void setupFilters() {
        // Chip para mostrar todas las recetas
        chipTodas.setOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked) {
                desmarcarOtrosChips(chipTodas);
                limpiarBusqueda();
                observeData();
            }
        });

        // Chip para filtrar por categoría postres
        chipPostres.setOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked) {
                desmarcarOtrosChips(chipPostres);
                limpiarBusqueda();
                filtrarPorCategoria(getString(R.string.categoria_postres));
            }
        });

        // Chip para filtrar por categoría platos principales
        chipPrincipales.setOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked) {
                desmarcarOtrosChips(chipPrincipales);
                limpiarBusqueda();
                filtrarPorCategoria(getString(R.string.categoria_principales));
            }
        });

        // Chip para filtrar por recetas rápidas (menos de 30 min)
        chipRapidas.setOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked) {
                desmarcarOtrosChips(chipRapidas);
                limpiarBusqueda();
                filtrarPorTiempo(30);
            }
        });

        // Botón para filtros avanzados
        requireView().findViewById(R.id.btn_filtros).setOnClickListener(v ->
                RecipeFilterDialog.show(requireContext(), viewModel, this::actualizarLista));
    }

    /**
     * Desmarca todos los chips excepto el seleccionado.
     *
     * @param seleccionado Chip que debe permanecer seleccionado
     */
    private void desmarcarOtrosChips(Chip seleccionado) {
        if (seleccionado != chipTodas) chipTodas.setChecked(false);
        if (seleccionado != chipPostres) chipPostres.setChecked(false);
        if (seleccionado != chipPrincipales) chipPrincipales.setChecked(false);
        if (seleccionado != chipRapidas) chipRapidas.setChecked(false);
    }

    /**
     * Desmarca todos los chips de filtro.
     */
    private void desmarcarTodosLosChips() {
        chipTodas.setChecked(false);
        chipPostres.setChecked(false);
        chipPrincipales.setChecked(false);
        chipRapidas.setChecked(false);
    }

    /**
     * Limpia la búsqueda actual y restablece el estado normal.
     */
    private void limpiarBusqueda() {
        isSearching = false;
        currentSearchQuery = "";
        if (etBuscar != null) {
            etBuscar.setText("");
        }
        viewModel.buscar("");
    }

    /**
     * Configura el gesto pull-to-refresh para actualizar datos.
     */
    private void setupSwipeRefresh() {
        swipeRefresh.setColorSchemeResources(R.color.color_primary);
        swipeRefresh.setOnRefreshListener(() -> {
            // Sincronizar datos y mantener el filtro actual
            viewModel.sincronizar();
            if (isSearching) {
                viewModel.buscar(currentSearchQuery);
            } else {
                // Reaplicar filtro actual después de sincronizar
                if (chipPostres.isChecked()) {
                    filtrarPorCategoria(getString(R.string.categoria_postres));
                } else if (chipPrincipales.isChecked()) {
                    filtrarPorCategoria(getString(R.string.categoria_principales));
                } else if (chipRapidas.isChecked()) {
                    filtrarPorTiempo(30);
                } else {
                    observeData();
                }
            }
            swipeRefresh.setRefreshing(false);
        });
    }

    /**
     * Inicia la observación de datos según el modo actual.
     */
    private void observeData() {
        switchToNormalMode();
    }

    /**
     * Filtra recetas por categoría específica.
     *
     * @param categoria Categoría por la que filtrar
     */
    private void filtrarPorCategoria(String categoria) {
        viewModel.filtrarPorCategoria(categoria).observe(getViewLifecycleOwner(),
                this::actualizarLista);
    }

    //Filtra recetas por tiempo máximo de preparación.
    private void filtrarPorTiempo(int tiempo) {
        viewModel.filtrarPorTiempo(tiempo).observe(getViewLifecycleOwner(),
                this::actualizarLista);
    }

    /**
     * Actualiza la lista de recetas en el adapter.
     * Maneja la visibilidad del estado vacío según los resultados.
     *
     * @param recetas Lista de recetas a mostrar
     */
    private void actualizarLista(List<Receta> recetas) {
        if (recetas == null) return;

        // Actualizar adapter y verificar si la lista está vacía
        adapter.submitList(recetas);
        boolean isEmpty = recetas.isEmpty();

        ViewExtensions.setVisible(rvRecetas, !isEmpty);
        ViewExtensions.setVisible(layoutEmpty, isEmpty);

        if (isEmpty) {
            actualizarEmptyState();
        }
    }

    /**
     * Actualiza el mensaje del estado vacío según el contexto actual.
     * Muestra mensajes específicos para búsquedas, filtros o estado inicial.
     */
    private void actualizarEmptyState() {
        // Mostrar/ocultar botón de limpiar según el contexto
        btnLimpiarBusqueda.setVisibility(isSearching ? View.VISIBLE : View.GONE);

        if (isSearching && !currentSearchQuery.trim().isEmpty()) {
            // Modo búsqueda con texto - mostrar mensaje de sin resultados
            emptyTitle.setText(getString(R.string.empty_title_sin_resultados));
            emptySubtitle.setText(String.format(getString(R.string.empty_subtitle_busqueda), currentSearchQuery));
        } else if (chipPostres.isChecked()) {
            // Filtro de postres sin resultados
            emptyTitle.setText(getString(R.string.empty_title_no_postres));
            emptySubtitle.setText(getString(R.string.empty_subtitle_postres));
        } else if (chipPrincipales.isChecked()) {
            // Filtro de platos principales sin resultados
            emptyTitle.setText(getString(R.string.empty_title_no_principales));
            emptySubtitle.setText(getString(R.string.empty_subtitle_principales));
        } else if (chipRapidas.isChecked()) {
            // Filtro de recetas rápidas sin resultados
            emptyTitle.setText(getString(R.string.empty_title_no_rapidas));
            emptySubtitle.setText(getString(R.string.empty_subtitle_rapidas));
        } else {
            // Estado por defecto - sin recetas en la aplicación
            emptyTitle.setText(getString(R.string.recetas_sin_resultados));
            emptySubtitle.setText(getString(R.string.empty_subtitle_default));
        }
    }

    /**
     * Oculta el teclado virtual cuando se completa una búsqueda.
     */
    private void hideKeyboard() {
        if (getContext() != null && etBuscar != null) {
            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager) getContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(etBuscar.getWindowToken(), 0);
        }
    }

    /**
     * Alterna el estado de favorito de una receta.
     * Delega la operación en el ViewModel.
     *
     * @param receta Receta a modificar
     * @param isFav Nuevo estado de favorito
     */
    @Override
    protected void toggleFavorite(Receta receta, boolean isFav) {
        viewModel.marcarFavorita(receta.getId(), isFav);
        // El observer maneja la actualización de la interfaz
    }

    /**
     * Reanuda la observación de datos cuando el fragmento vuelve a primer plano.
     * Restaura la búsqueda activa si existía.
     */
    @Override
    public void onResume() {
        super.onResume();
        if (isSearching) {
            viewModel.buscar(currentSearchQuery);
        }
    }
}