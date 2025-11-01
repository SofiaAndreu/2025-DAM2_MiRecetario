package com.example.recetarioapp.ui.recipes;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.recetarioapp.utils.RecipeFilterDialog;
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
    }

    private void initViews(View view) {
        etBuscar = view.findViewById(R.id.et_buscar);
        rvRecetas = view.findViewById(R.id.rv_recetas);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        layoutEmpty = view.findViewById(R.id.layout_empty);

        chipTodas = view.findViewById(R.id.chip_todas);
        chipPostres = view.findViewById(R.id.chip_postres);
        chipPrincipales = view.findViewById(R.id.chip_principales);
        chipRapidas = view.findViewById(R.id.chip_rapidas);
    }

    private void setupRecyclerView() {
        adapter = new RecetaAdapter();
        rvRecetas.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRecetas.setAdapter(adapter);

        // Usar los métodos de esta clase con @Override
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

                Log.d("BUSQUEDA", "Texto cambiado: '" + currentSearchQuery + "', isSearching: " + isSearching);

                if (isSearching) {
                    // Usar el método de diagnóstico
                    viewModel.debugBuscar(currentSearchQuery);
                    desmarcarTodosLosChips();
                    // Cambiar a observar recetas filtradas
                    switchToSearchMode();
                } else {
                    viewModel.debugBuscar("");
                    switchToNormalMode();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void switchToSearchMode() {
        Log.d("BUSQUEDA", "Cambiando a modo búsqueda");
        // Remover observadores anteriores
        viewModel.getTodasLasRecetas().removeObservers(getViewLifecycleOwner());

        // Observar recetas filtradas
        viewModel.getRecetasFiltradas().observe(getViewLifecycleOwner(), recetas -> {
            Log.d("BUSQUEDA", "Resultados de búsqueda: " + (recetas != null ? recetas.size() : 0) + " recetas");
            actualizarLista(recetas);
        });
    }

    private void switchToNormalMode() {
        Log.d("BUSQUEDA", "Cambiando a modo normal");
        // Remover observadores anteriores
        viewModel.getRecetasFiltradas().removeObservers(getViewLifecycleOwner());

        // Observar todas las recetas
        viewModel.getTodasLasRecetas().observe(getViewLifecycleOwner(), recetas -> {
            Log.d("BUSQUEDA", "Todas las recetas: " + (recetas != null ? recetas.size() : 0) + " recetas");
            actualizarLista(recetas);
        });
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
        // También limpiar la búsqueda en el ViewModel
        viewModel.buscar("");
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setColorSchemeResources(R.color.color_primary);
        swipeRefresh.setOnRefreshListener(() -> {
            viewModel.sincronizar();
            // Recargar datos según el estado actual
            if (isSearching) {
                viewModel.buscar(currentSearchQuery);
            } else {
                // Verificar si hay algún chip activo y recargar accordingly
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
        // Inicialmente en modo normal
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
        Log.d("BUSQUEDA", "Actualizando lista con: " + (recetas != null ? recetas.size() : 0) + " recetas");

        if (recetas == null) {
            Log.d("BUSQUEDA", "Lista de recetas es NULL");
            return;
        }

        adapter.submitList(recetas);
        boolean isEmpty = recetas.isEmpty();

        Log.d("BUSQUEDA", "Lista vacía: " + isEmpty);

        ViewExtensions.setVisible(rvRecetas, !isEmpty);
        ViewExtensions.setVisible(layoutEmpty, isEmpty);

        actualizarMensajeEmptyState(isEmpty);
    }

    private void actualizarMensajeEmptyState(boolean isEmpty) {
        if (!isEmpty) return;

        // Buscar el TextView correcto en tu layout_empty
        // Si tienes múltiples TextViews, ajusta el ID según tu layout
        TextView tvEmpty = layoutEmpty.findViewById(android.R.id.text1);
        if (tvEmpty == null) {
            // Si no encuentra con ese ID, busca cualquier TextView
            for (int i = 0; i < layoutEmpty.getChildCount(); i++) {
                if (layoutEmpty.getChildAt(i) instanceof TextView) {
                    tvEmpty = (TextView) layoutEmpty.getChildAt(i);
                    break;
                }
            }
        }

        if (tvEmpty != null) {
            if (isSearching && !currentSearchQuery.trim().isEmpty()) {
                tvEmpty.setText("No se encontraron recetas para: \"" + currentSearchQuery + "\"");
            } else if (chipPostres.isChecked()) {
                tvEmpty.setText("No hay recetas de postres");
            } else if (chipPrincipales.isChecked()) {
                tvEmpty.setText("No hay recetas principales");
            } else if (chipRapidas.isChecked()) {
                tvEmpty.setText("No hay recetas rápidas");
            } else {
                // Mensaje por defecto
                tvEmpty.setText(getString(R.string.recetas_sin_resultados));
            }
        }
    }

    // SOBRESCRIBIR los métodos de la clase base con el mismo modificador de acceso o más permisivo
    @Override
    protected void openRecipeDetail(Receta receta) {
        // Navegar al detalle de la receta
        // Implementa según tu navegación
        /* Ejemplo:
        NavController navController = Navigation.findNavController(requireView());
        Bundle bundle = new Bundle();
        bundle.putLong("recetaId", receta.getId());
        navController.navigate(R.id.action_recipesFragment_to_recipeDetailFragment, bundle);
        */

        // Llama al método de la clase base si es necesario
        super.openRecipeDetail(receta);
    }

    @Override
    protected void toggleFavorite(Receta receta, boolean isFav) {
        viewModel.marcarFavorita(receta.getId(), isFav);

        // Llama al método de la clase base si es necesario
        super.toggleFavorite(receta, isFav);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Asegurarse de que los datos estén actualizados al volver al fragment
        if (isSearching) {
            viewModel.buscar(currentSearchQuery);
        }
    }
}