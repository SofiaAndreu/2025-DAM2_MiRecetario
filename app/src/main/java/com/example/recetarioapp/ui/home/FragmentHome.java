package com.example.recetarioapp.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.recetarioapp.R;
import com.example.recetarioapp.adapters.RecetaAdapter;
import com.example.recetarioapp.ui.base.BaseFragment;
import com.example.recetarioapp.utils.ViewExtensions;
import java.util.List;

public class FragmentHome extends BaseFragment {

    private TextView tvTotalRecetas, tvTotalFavoritas, tvTotalCategorias;
    private RecyclerView rvRecetasRecientes;
    private LinearLayout layoutEmptyState;
    private RecetaAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViewModel();
        initViews(view);
        setupRecyclerView();
        observeData();
        observeUiState();
    }

    private void initViews(View view) {
        tvTotalRecetas = view.findViewById(R.id.tv_total_recetas);
        tvTotalFavoritas = view.findViewById(R.id.tv_total_favoritas);
        tvTotalCategorias = view.findViewById(R.id.tv_total_categorias);
        rvRecetasRecientes = view.findViewById(R.id.rv_recetas_recientes);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
    }

    private void setupRecyclerView() {
        adapter = new RecetaAdapter();
        rvRecetasRecientes.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRecetasRecientes.setAdapter(adapter);

        adapter.setOnRecetaClickListener(this::openRecipeDetail);
        adapter.setOnFavClickListener(this::toggleFavorite);
    }

    private void observeData() {
        // Recetas recientes (primeras 5)
        viewModel.getTodasLasRecetas().observe(getViewLifecycleOwner(), recetas -> {
            if (recetas == null) return;

            tvTotalRecetas.setText(String.valueOf(recetas.size()));

            List<?> recetasRecientes = recetas.size() > 5
                    ? recetas.subList(0, 5)
                    : recetas;
            adapter.submitList((List) recetasRecientes);

            ViewExtensions.setVisible(rvRecetasRecientes, !recetas.isEmpty());
            ViewExtensions.setVisible(layoutEmptyState, recetas.isEmpty());
        });

        // Favoritas
        viewModel.getFavs().observe(getViewLifecycleOwner(), favoritas -> {
            if (favoritas != null) {
                tvTotalFavoritas.setText(String.valueOf(favoritas.size()));
            }
        });

        // Categorías
        viewModel.getCategorias().observe(getViewLifecycleOwner(), categorias -> {
            if (categorias != null) {
                tvTotalCategorias.setText(String.valueOf(categorias.size()));
            }
        });
    }
}
