package com.example.recetarioapp.ui.favs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recetarioapp.R;
import com.example.recetarioapp.adapters.RecetaAdapter;
import com.example.recetarioapp.viewmodels.RecetaViewModel;

/**
 * Fragment que muestra las recetas favoritas
 */
public class FavsFragment extends Fragment {

    private RecetaViewModel viewModel;

    // Views
    private RecyclerView rvFavoritas;
    private LinearLayout layoutEmpty;

    // Adapter
    private RecetaAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favs, container, false);
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

        // Observar datos
        observeData();
    }

    private void initViews(View view) {
        rvFavoritas = view.findViewById(R.id.rv_favoritas);
        layoutEmpty = view.findViewById(R.id.layout_empty);
    }

    private void setupRecyclerView() {
        adapter = new RecetaAdapter();
        rvFavoritas.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFavoritas.setAdapter(adapter);

        // Click en receta
        adapter.setOnRecetaClickListener(receta -> {
            Intent intent = new Intent(getContext(), com.example.recetarioapp.ui.RecipeDetailActivity.class);
            intent.putExtra(com.example.recetarioapp.ui.RecipeDetailActivity.EXTRA_RECETA_ID, receta.getId());
            startActivity(intent);
        });

        // Click en favorito
        adapter.setOnFavClickListener((receta, esFavorita) -> {
            viewModel.marcarFavorita(receta.getId(), esFavorita);
            if (!esFavorita) {
                Toast.makeText(getContext(), "Quitada de favoritos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void observeData() {
        // Observar recetas favoritas
        viewModel.getFavs().observe(getViewLifecycleOwner(), favoritas -> {
            if (favoritas != null) {
                adapter.submitList(favoritas);

                // Mostrar/ocultar empty state
                if (favoritas.isEmpty()) {
                    rvFavoritas.setVisibility(View.GONE);
                    layoutEmpty.setVisibility(View.VISIBLE);
                } else {
                    rvFavoritas.setVisibility(View.VISIBLE);
                    layoutEmpty.setVisibility(View.GONE);
                }
            }
        });
    }
}