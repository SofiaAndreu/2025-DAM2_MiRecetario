package com.example.recetarioapp.ui.recipes;


import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.recetarioapp.utils.RecipeFilterDialog;
import com.google.android.material.chip.Chip;
import com.example.recetarioapp.R;
import com.example.recetarioapp.adapters.RecetaAdapter;
import com.example.recetarioapp.ui.base.BaseFragment;
import com.example.recetarioapp.utils.ViewExtensions;

public class RecipesFragment extends BaseFragment {

    private EditText etBuscar;
    private RecyclerView rvRecetas;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout layoutEmpty;
    private RecetaAdapter adapter;

    // Chips de filtro
    private Chip chipTodas, chipPostres, chipPrincipales, chipRapidas;

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

        adapter.setOnRecetaClickListener(this::openRecipeDetail);
        adapter.setOnFavClickListener(this::toggleFavorite);
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
        chipTodas.setOnCheckedChangeListener((b, isChecked) -> {
            if (isChecked) {
                desmarcarOtrosChips(chipTodas);
                observeData();
            }
        });

        chipPostres.setOnCheckedChangeListener((b, isChecked) -> {
            if (isChecked) {
                desmarcarOtrosChips(chipPostres);
                filtrarPorCategoria(getString(R.string.categoria_postres));
            }
        });

        chipPrincipales.setOnCheckedChangeListener((b, isChecked) -> {
            if (isChecked) {
                desmarcarOtrosChips(chipPrincipales);
                filtrarPorCategoria(getString(R.string.categoria_principales));
            }
        });

        chipRapidas.setOnCheckedChangeListener((b, isChecked) -> {
            if (isChecked) {
                desmarcarOtrosChips(chipRapidas);
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

    private void setupSwipeRefresh() {
        swipeRefresh.setColorSchemeResources(R.color.color_primary);
        swipeRefresh.setOnRefreshListener(() -> {
            viewModel.sincronizar();
            swipeRefresh.setRefreshing(false);
        });
    }

    private void observeData() {
        viewModel.getTodasLasRecetas().observe(getViewLifecycleOwner(),
                this::actualizarLista);
    }

    private void filtrarPorCategoria(String categoria) {
        viewModel.filtrarPorCategoria(categoria).observe(getViewLifecycleOwner(),
                this::actualizarLista);
    }

    private void filtrarPorTiempo(int tiempo) {
        viewModel.filtrarPorTiempo(tiempo).observe(getViewLifecycleOwner(),
                this::actualizarLista);
    }

    private void actualizarLista(java.util.List recetas) {
        if (recetas == null) return;

        adapter.submitList(recetas);
        ViewExtensions.setVisible(rvRecetas, !recetas.isEmpty());
        ViewExtensions.setVisible(layoutEmpty, recetas.isEmpty());
    }
}