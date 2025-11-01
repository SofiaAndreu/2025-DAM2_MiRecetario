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

public class RecipesFragment extends BaseFragment {

    private EditText etBuscar;
    private RecyclerView rvRecetas;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout layoutEmpty;
    private RecetaAdapter adapter;
    private RecetaViewModel viewModel;

    // Empty state views
    private TextView emptyIcon, emptyTitle, emptySubtitle;
    private MaterialButton btnLimpiarBusqueda;

    // Chips de filtro
    private Chip chipTodas, chipPostres, chipPrincipales, chipRapidas;

    // Para controlar si estamos en modo búsqueda o filtro
    private boolean isSearching = false;
    private String currentSearchQuery = "";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recipes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViewModel();
        initViews(view);
        setupRecyclerView();
        setupSearch();
        setupFilters();
        setupSwipeRefresh();
        observeData();
        setupFavoritoObserver();
    }

    private void setupFavoritoObserver() {
        viewModel.getFavoritoActualizado().observe(getViewLifecycleOwner(), recetaId -> {
            // Actualizar el item específico en el adapter
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

    private void initViews(View view) {
        etBuscar = view.findViewById(R.id.et_buscar);
        rvRecetas = view.findViewById(R.id.rv_recetas);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        layoutEmpty = view.findViewById(R.id.layout_empty);

        // Empty state views
        emptyIcon = layoutEmpty.findViewById(R.id.empty_icon);
        emptyTitle = layoutEmpty.findViewById(R.id.empty_title);
        emptySubtitle = layoutEmpty.findViewById(R.id.empty_subtitle);
        btnLimpiarBusqueda = layoutEmpty.findViewById(R.id.btn_limpiar_busqueda);

        // Chips
        chipTodas = view.findViewById(R.id.chip_todas);
        chipPostres = view.findViewById(R.id.chip_postres);
        chipPrincipales = view.findViewById(R.id.chip_principales);
        chipRapidas = view.findViewById(R.id.chip_rapidas);

        // Configurar botón de limpiar búsqueda
        btnLimpiarBusqueda.setOnClickListener(v -> {
            limpiarBusqueda();
            etBuscar.clearFocus();
            hideKeyboard();
        });
    }

    private void setupRecyclerView() {
        adapter = new RecetaAdapter();
        rvRecetas.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRecetas.setAdapter(adapter);

        adapter.setOnRecetaClickListener(this::openRecipeDetail);
        adapter.setOnFavClickListener(this::toggleFavorite);
    }

    private void setupSearch() {
        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString();
                isSearching = !currentSearchQuery.trim().isEmpty();

                if (isSearching) {
                    viewModel.buscar(currentSearchQuery);
                    desmarcarTodosLosChips();
                    switchToSearchMode();
                } else {
                    viewModel.buscar("");
                    switchToNormalMode();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void switchToSearchMode() {
        viewModel.getTodasLasRecetas().removeObservers(getViewLifecycleOwner());
        viewModel.getRecetasFiltradas().observe(getViewLifecycleOwner(), this::actualizarLista);
    }

    private void switchToNormalMode() {
        viewModel.getRecetasFiltradas().removeObservers(getViewLifecycleOwner());
        viewModel.getTodasLasRecetas().observe(getViewLifecycleOwner(), this::actualizarLista);
    }

    private void setupFilters() {
        chipTodas.setOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked) {
                desmarcarOtrosChips(chipTodas);
                limpiarBusqueda();
                observeData();
            }
        });

        chipPostres.setOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked) {
                desmarcarOtrosChips(chipPostres);
                limpiarBusqueda();
                filtrarPorCategoria(getString(R.string.categoria_postres));
            }
        });

        chipPrincipales.setOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked) {
                desmarcarOtrosChips(chipPrincipales);
                limpiarBusqueda();
                filtrarPorCategoria(getString(R.string.categoria_principales));
            }
        });

        chipRapidas.setOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked) {
                desmarcarOtrosChips(chipRapidas);
                limpiarBusqueda();
                filtrarPorTiempo(30);
            }
        });

        requireView().findViewById(R.id.btn_filtros).setOnClickListener(v ->
                RecipeFilterDialog.show(requireContext(), viewModel, this::actualizarLista));
    }

    private void desmarcarOtrosChips(Chip seleccionado) {
        if (seleccionado != chipTodas) chipTodas.setChecked(false);
        if (seleccionado != chipPostres) chipPostres.setChecked(false);
        if (seleccionado != chipPrincipales) chipPrincipales.setChecked(false);
        if (seleccionado != chipRapidas) chipRapidas.setChecked(false);
    }

    private void desmarcarTodosLosChips() {
        chipTodas.setChecked(false);
        chipPostres.setChecked(false);
        chipPrincipales.setChecked(false);
        chipRapidas.setChecked(false);
    }

    private void limpiarBusqueda() {
        isSearching = false;
        currentSearchQuery = "";
        if (etBuscar != null) {
            etBuscar.setText("");
        }
        viewModel.buscar("");
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setColorSchemeResources(R.color.color_primary);
        swipeRefresh.setOnRefreshListener(() -> {
            viewModel.sincronizar();
            if (isSearching) {
                viewModel.buscar(currentSearchQuery);
            } else {
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

    private void observeData() {
        switchToNormalMode();
    }

    private void filtrarPorCategoria(String categoria) {
        viewModel.filtrarPorCategoria(categoria).observe(getViewLifecycleOwner(),
                this::actualizarLista);
    }

    private void filtrarPorTiempo(int tiempo) {
        viewModel.filtrarPorTiempo(tiempo).observe(getViewLifecycleOwner(),
                this::actualizarLista);
    }

    private void actualizarLista(List<Receta> recetas) {
        if (recetas == null) return;

        adapter.submitList(recetas);
        boolean isEmpty = recetas.isEmpty();

        ViewExtensions.setVisible(rvRecetas, !isEmpty);
        ViewExtensions.setVisible(layoutEmpty, isEmpty);

        if (isEmpty) {
            actualizarEmptyState();
        }
    }

    private void actualizarEmptyState() {
        // Mostrar/ocultar botón de limpiar según el contexto
        btnLimpiarBusqueda.setVisibility(isSearching ? View.VISIBLE : View.GONE);

        if (isSearching && !currentSearchQuery.trim().isEmpty()) {
            // Modo búsqueda con texto
            emptyTitle.setText(getString(R.string.empty_title_sin_resultados));
            emptySubtitle.setText(String.format(getString(R.string.empty_subtitle_busqueda), currentSearchQuery));
        } else if (chipPostres.isChecked()) {
            emptyTitle.setText(getString(R.string.empty_title_no_postres));
            emptySubtitle.setText(getString(R.string.empty_subtitle_postres));
        } else if (chipPrincipales.isChecked()) {
            emptyTitle.setText(getString(R.string.empty_title_no_principales));
            emptySubtitle.setText(getString(R.string.empty_subtitle_principales));
        } else if (chipRapidas.isChecked()) {
            emptyTitle.setText(getString(R.string.empty_title_no_rapidas));
            emptySubtitle.setText(getString(R.string.empty_subtitle_rapidas));
        } else {
            // Estado por defecto
            emptyTitle.setText(getString(R.string.recetas_sin_resultados));
            emptySubtitle.setText(getString(R.string.empty_subtitle_default));
        }
    }

    private void hideKeyboard() {
        if (getContext() != null && etBuscar != null) {
            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager) getContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(etBuscar.getWindowToken(), 0);
        }
    }

    @Override
    protected void openRecipeDetail(Receta receta) {
        // Tu implementación de navegación al detalle
        super.openRecipeDetail(receta);
    }

    @Override
    protected void toggleFavorite(Receta receta, boolean isFav) {
        viewModel.marcarFavorita(receta.getId(), isFav);
        //observer maneja actualizacion
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isSearching) {
            viewModel.buscar(currentSearchQuery);
        }
    }
}