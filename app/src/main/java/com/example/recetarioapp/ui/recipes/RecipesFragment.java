package com.example.recetarioapp.ui.recipes;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.chip.Chip;
import com.example.recetarioapp.R;
import com.example.recetarioapp.adapters.RecetaAdapter;
import com.example.recetarioapp.viewmodels.RecetaViewModel;

/**
 * Fragment que muestra todas las recetas con búsqueda y filtros
 */
public class RecipesFragment extends Fragment {

    private RecetaViewModel viewModel;

    // Views
    private EditText etBuscar;
    private ImageButton btnFiltros;
    private RecyclerView rvRecetas;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;

    // Chips de filtro
    private Chip chipTodas;
    private Chip chipPostres;
    private Chip chipPrincipales;
    private Chip chipRapidas;

    // Adapter
    private RecetaAdapter adapter;

    // Filtro actual
    private String categoriaActual = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recipes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar ViewModel
        viewModel = new ViewModelProvider(requireActivity()).get(RecetaViewModel.class);

        // Inicializar vistas
        initViews(view);

        // Configurar RecyclerView
        setupRecyclerView();

        // Configurar búsqueda
        setupSearch();

        // Configurar filtros
        setupFilters();

        // Configurar SwipeRefresh
        setupSwipeRefresh();

        // Observar datos
        observeData();
    }

    private void initViews(View view) {
        etBuscar = view.findViewById(R.id.et_buscar);
        btnFiltros = view.findViewById(R.id.btn_filtros);
        rvRecetas = view.findViewById(R.id.rv_recetas);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        layoutEmpty = view.findViewById(R.id.layout_empty);
        progressBar = view.findViewById(R.id.progress_bar);

        // Chips
        chipTodas = view.findViewById(R.id.chip_todas);
        chipPostres = view.findViewById(R.id.chip_postres);
        chipPrincipales = view.findViewById(R.id.chip_principales);
        chipRapidas = view.findViewById(R.id.chip_rapidas);
    }

    private void setupRecyclerView() {
        adapter = new RecetaAdapter();

        // GridLayout con 2 columnas (puedes cambiar a LinearLayout si prefieres)
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        rvRecetas.setLayoutManager(layoutManager);
        rvRecetas.setAdapter(adapter);

        // Click en receta
        adapter.setOnRecetaClickListener(receta -> {
            // TODO: Navegar al detalle
            Toast.makeText(getContext(), "Receta: " + receta.getNombre(), Toast.LENGTH_SHORT).show();
        });

        // Click en favorito
        adapter.setOnFavClickListener((receta, esFavorita) -> {
            viewModel.marcarFavorita(receta.getId(), esFavorita);
        });
    }

    private void setupSearch() {
        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.buscar(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFilters() {
        // Chip "Todas"
        chipTodas.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                categoriaActual = null;
                desmarcarOtrosChips(chipTodas);
                observeData();
            }
        });

        // Chip "Postres"
        chipPostres.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                categoriaActual = getString(R.string.categoria_postres);
                desmarcarOtrosChips(chipPostres);
                observarPorCategoria(categoriaActual);
            }
        });

        // Chip "Platos Principales"
        chipPrincipales.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                categoriaActual = getString(R.string.categoria_principales);
                desmarcarOtrosChips(chipPrincipales);
                observarPorCategoria(categoriaActual);
            }
        });

        // Chip "Rápidas"
        chipRapidas.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                desmarcarOtrosChips(chipRapidas);
                observarPorTiempo(30);
            }
        });

        // Botón de filtros avanzados
        btnFiltros.setOnClickListener(v -> {
            // TODO: Mostrar dialog con más opciones de filtro
            Toast.makeText(getContext(), "Filtros avanzados (próximamente)", Toast.LENGTH_SHORT).show();
        });
    }

    private void desmarcarOtrosChips(Chip chipSeleccionado) {
        if (chipSeleccionado != chipTodas) chipTodas.setChecked(false);
        if (chipSeleccionado != chipPostres) chipPostres.setChecked(false);
        if (chipSeleccionado != chipPrincipales) chipPrincipales.setChecked(false);
        if (chipSeleccionado != chipRapidas) chipRapidas.setChecked(false);
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setColorSchemeResources(R.color.color_primary);
        swipeRefresh.setOnRefreshListener(() -> {
            viewModel.sincronizar();
            swipeRefresh.setRefreshing(false);
        });
    }

    private void observeData() {
        // Observar todas las recetas (por defecto)
        viewModel.getTodasLasRecetas().observe(getViewLifecycleOwner(), recetas -> {
            if (recetas != null) {
                adapter.submitList(recetas);

                // Mostrar/ocultar empty state
                if (recetas.isEmpty()) {
                    rvRecetas.setVisibility(View.GONE);
                    layoutEmpty.setVisibility(View.VISIBLE);
                } else {
                    rvRecetas.setVisibility(View.VISIBLE);
                    layoutEmpty.setVisibility(View.GONE);
                }
            }
        });
    }

    private void observarPorCategoria(String categoria) {
        viewModel.filtrarPorCategoria(categoria).observe(getViewLifecycleOwner(), recetas -> {
            if (recetas != null) {
                adapter.submitList(recetas);
                actualizarEmptyState(recetas.isEmpty());
            }
        });
    }

    private void observarPorTiempo(int tiempoMax) {
        viewModel.filtrarPorTiempo(tiempoMax).observe(getViewLifecycleOwner(), recetas -> {
            if (recetas != null) {
                adapter.submitList(recetas);
                actualizarEmptyState(recetas.isEmpty());
            }
        });
    }

    private void actualizarEmptyState(boolean isEmpty) {
        if (isEmpty) {
            rvRecetas.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            rvRecetas.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
        }
    }
}