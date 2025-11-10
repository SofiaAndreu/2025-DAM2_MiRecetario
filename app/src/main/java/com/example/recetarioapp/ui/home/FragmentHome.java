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

/**
 * Fragment principal que muestra el dashboard de la aplicación.
 *
 * Funcionalidades principales:
 * - Estadísticas de recetas (total, favoritas, categorías)
 * - Lista de recetas recientes
 * - Acceso rápido a información "Acerca de"
 * - Navegación a detalles de recetas
 * - Gestión de favoritos desde la vista principal
 */
public class FragmentHome extends BaseFragment {

    // Vistas para mostrar estadísticas
    private TextView tvTotalRecetas, tvTotalFavoritas;

    // RecyclerView para mostrar recetas recientes
    private RecyclerView rvRecetasRecientes;

    // Layout que se muestra cuando no hay recetas
    private LinearLayout layoutEmptyState;

    // Adaptador para la lista de recetas
    private RecetaAdapter adapter;

    // Botón para acceder a la información "Acerca de"
    private FrameLayout btnAbout;

    /**
     * Crea la vista del fragmento inflando el layout correspondiente.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
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
        setupAboutButton();
        observeData();
        observeUiState();
    }

    /**
     * Inicializa las referencias a las vistas del layout.
     *
     * @param view Vista raíz del fragmento
     */
    private void initViews(View view) {
        tvTotalRecetas = view.findViewById(R.id.tv_total_recetas);
        tvTotalFavoritas = view.findViewById(R.id.tv_total_favoritas);
        rvRecetasRecientes = view.findViewById(R.id.rv_recetas_recientes);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        btnAbout = view.findViewById(R.id.btn_about);
    }

    /**
     * Configura el botón "Acerca de" para navegar a la Activity correspondiente.
     */
    private void setupAboutButton() {
        if (btnAbout != null) {
            btnAbout.setOnClickListener(v -> {
                // Navegar a AboutActivity
                Intent intent = new Intent(getActivity(), AboutActivity.class);
                startActivity(intent);
            });
        }
    }

    /**
     * Configura el RecyclerView para mostrar las recetas recientes.
     * Establece el layout manager, adapter y listeners de interacción.
     */
    private void setupRecyclerView() {
        adapter = new RecetaAdapter();
        rvRecetasRecientes.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRecetasRecientes.setAdapter(adapter);

        // Configurar listeners para interacciones con recetas
        adapter.setOnRecetaClickListener(this::openRecipeDetail);
        adapter.setOnFavClickListener(this::toggleFavorite);
    }

    /**
     * Observa los cambios en los datos de recetas y favoritos.
     * Actualiza las estadísticas y la lista de recetas recientes.
     */
    private void observeData() {
        // Observar todas las recetas para estadísticas y lista reciente
        viewModel.getTodasLasRecetas().observe(getViewLifecycleOwner(), recetas -> {
            if (recetas == null) return;

            // Actualizar contador de recetas totales
            tvTotalRecetas.setText(String.valueOf(recetas.size()));

            // Obtener las 5 recetas más recientes (o todas si hay menos de 5)
            List<Receta> recetasRecientes = recetas.size() > 5
                    ? recetas.subList(0, 5)
                    : recetas;

            // Actualizar lista de recetas recientes
            adapter.submitList(recetasRecientes);

            // Mostrar/ocultar elementos según si hay recetas
            ViewExtensions.setVisible(rvRecetasRecientes, !recetas.isEmpty());
            ViewExtensions.setVisible(layoutEmptyState, recetas.isEmpty());
        });

        // Observar recetas favoritas para actualizar contador
        viewModel.getFavs().observe(getViewLifecycleOwner(), favoritas -> {
            if (favoritas != null) {
                tvTotalFavoritas.setText(String.valueOf(favoritas.size()));
            }
        });
    }
}