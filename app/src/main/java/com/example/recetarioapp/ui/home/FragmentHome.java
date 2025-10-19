package com.example.recetarioapp.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recetarioapp.R;
import com.example.recetarioapp.adapters.RecetaAdapter;
import com.example.recetarioapp.models.Receta;
import com.example.recetarioapp.viewmodels.RecetaViewModel;

import java.util.List;

/**
 * Fragment de la pantalla de inicio
 * Muestra un resumen y las recetas más recientes
 */
public class FragmentHome extends Fragment {

    private RecetaViewModel viewModel;

    // Views
    private TextView tvTotalRecetas;
    private TextView tvTotalFavoritas;
    private TextView tvTotalCategorias;
    private RecyclerView rvRecetasRecientes;
    private LinearLayout layoutEmptyState;

    // Adapter
    private RecetaAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
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

        // Click en receta
        adapter.setOnRecetaClickListener(receta -> {
            // TODO: Navegar al detalle de la receta
            Toast.makeText(getContext(), "Receta: " + receta.getNombre(), Toast.LENGTH_SHORT).show();
        });

        // Click en favorito
        adapter.setOnFavClickListener((receta, esFavorita) -> {
            viewModel.marcarFavorita(receta.getId(), esFavorita);
            Toast.makeText(getContext(),
                    esFavorita ? "Añadida a favoritos" : "Quitada de favoritos",
                    Toast.LENGTH_SHORT).show();
        });
    }

    private void observeData() {
        // Observar todas las recetas
        viewModel.getTodasLasRecetas().observe(getViewLifecycleOwner(), recetas -> {
            if (recetas != null) {
                // Actualizar contador
                tvTotalRecetas.setText(String.valueOf(recetas.size()));

                // Mostrar las 5 más recientes
                List<Receta> recetasRecientes = recetas.size() > 5
                        ? recetas.subList(0, 5)
                        : recetas;

                adapter.submitList(recetasRecientes);

                // Mostrar/ocultar empty state
                if (recetas.isEmpty()) {
                    rvRecetasRecientes.setVisibility(View.GONE);
                    layoutEmptyState.setVisibility(View.VISIBLE);
                } else {
                    rvRecetasRecientes.setVisibility(View.VISIBLE);
                    layoutEmptyState.setVisibility(View.GONE);
                }
            }
        });

        // Observar favoritas
        viewModel.getFavs().observe(getViewLifecycleOwner(), favoritas -> {
            if (favoritas != null) {
                tvTotalFavoritas.setText(String.valueOf(favoritas.size()));
            }
        });

        // Observar categorías
        viewModel.getCategorias().observe(getViewLifecycleOwner(), categorias -> {
            if (categorias != null) {
                tvTotalCategorias.setText(String.valueOf(categorias.size()));
            }
        });

        // Observar mensajes de error
        viewModel.getMensajeError().observe(getViewLifecycleOwner(), mensaje -> {
            if (mensaje != null && !mensaje.isEmpty()) {
                Toast.makeText(getContext(), mensaje, Toast.LENGTH_SHORT).show();
                viewModel.limpiarMensajesE();
            }
        });
    }
}