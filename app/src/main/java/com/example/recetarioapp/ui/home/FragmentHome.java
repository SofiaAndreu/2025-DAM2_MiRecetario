package com.example.recetarioapp.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.recetarioapp.R;
import com.example.recetarioapp.adapters.RecetaAdapter;
import com.example.recetarioapp.models.Receta;
import com.example.recetarioapp.ui.AboutActivity;
import com.example.recetarioapp.ui.base.BaseFragment;
import com.example.recetarioapp.utils.ViewExtensions;
import java.util.List;

public class FragmentHome extends BaseFragment {

    private TextView tvTotalRecetas, tvTotalFavoritas, tvTotalCategorias;
    private RecyclerView rvRecetasRecientes;
    private LinearLayout layoutEmptyState;
    private RecetaAdapter adapter;
    private FrameLayout btnAbout; // Añadido

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
        setupAboutButton(); // Añadido
        observeData();
        observeUiState();
    }

    private void initViews(View view) {
        tvTotalRecetas = view.findViewById(R.id.tv_total_recetas);
        tvTotalFavoritas = view.findViewById(R.id.tv_total_favoritas);
        rvRecetasRecientes = view.findViewById(R.id.rv_recetas_recientes);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        btnAbout = view.findViewById(R.id.btn_about); // Añadido
    }

    // NUEVO MÉTODO: Configurar el botón Acerca de
    private void setupAboutButton() {
        if (btnAbout != null) {
            btnAbout.setOnClickListener(v -> {
                // Navegar a AboutActivity
                Intent intent = new Intent(getActivity(), AboutActivity.class);
                startActivity(intent);

                // Opcional: Añadir animación si quieres
                // getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            });
        }
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

            List<Receta> recetasRecientes = recetas.size() > 5
                    ? recetas.subList(0, 5)
                    : recetas;
            adapter.submitList(recetasRecientes);

            ViewExtensions.setVisible(rvRecetasRecientes, !recetas.isEmpty());
            ViewExtensions.setVisible(layoutEmptyState, recetas.isEmpty());
        });

        // Favoritas
        viewModel.getFavs().observe(getViewLifecycleOwner(), favoritas -> {
            if (favoritas != null) {
                tvTotalFavoritas.setText(String.valueOf(favoritas.size()));
            }
        });

    }
}