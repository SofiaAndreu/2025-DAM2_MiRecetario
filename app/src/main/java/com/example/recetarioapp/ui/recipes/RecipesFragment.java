package com.example.recetarioapp.ui.recipes;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.content.Intent;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.recetarioapp.models.Receta;
import com.google.android.material.chip.Chip;
import com.example.recetarioapp.R;
import com.example.recetarioapp.adapters.RecetaAdapter;
import com.example.recetarioapp.viewmodels.RecetaViewModel;

import java.util.ArrayList;
import java.util.List;

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

        // LinearLayout vertical (1 columna)
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        rvRecetas.setLayoutManager(layoutManager);
        rvRecetas.setAdapter(adapter);

        // Click en receta
        adapter.setOnRecetaClickListener(receta -> {
            Intent intent = new Intent(getContext(), com.example.recetarioapp.ui.RecipeDetailActivity.class);
            intent.putExtra(com.example.recetarioapp.ui.RecipeDetailActivity.EXTRA_RECETA_ID, receta.getId());
            startActivity(intent);
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
            mostrarDialogFiltros();
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

    /**
     * Muestra el diálogo de filtros avanzados
     */
    private void mostrarDialogFiltros() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_filtros, null);

        // Obtener vistas del diálogo
        AutoCompleteTextView spinnerCategoria = dialogView.findViewById(R.id.spinner_categoria);
        AutoCompleteTextView spinnerDificultad = dialogView.findViewById(R.id.spinner_dificultad);
        com.google.android.material.slider.Slider sliderTiempo = dialogView.findViewById(R.id.slider_tiempo);
        TextView tvTiempoSeleccionado = dialogView.findViewById(R.id.tv_tiempo_seleccionado);
        com.google.android.material.checkbox.MaterialCheckBox cbSoloFavoritas = dialogView.findViewById(R.id.cb_solo_favoritas);

        // Configurar categorías
        String[] categorias = {
                "Todas",
                getString(R.string.categoria_postres),
                getString(R.string.categoria_principales),
                getString(R.string.categoria_aperitivos),
                getString(R.string.categoria_panaderia),
                getString(R.string.categoria_bebidas),
                getString(R.string.categoria_ensaladas),
                getString(R.string.categoria_sopas),
                getString(R.string.categoria_otros)
        };
        ArrayAdapter<String> categoriaAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                categorias
        );
        spinnerCategoria.setAdapter(categoriaAdapter);
        spinnerCategoria.setText("Todas", false);

        // Configurar dificultades
        String[] dificultades = {
                "Todas",
                getString(R.string.dificultad_facil),
                getString(R.string.dificultad_medio),
                getString(R.string.dificultad_dificil)
        };
        ArrayAdapter<String> dificultadAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                dificultades
        );
        spinnerDificultad.setAdapter(dificultadAdapter);
        spinnerDificultad.setText("Todas", false);

        // Listener para el slider de tiempo
        sliderTiempo.addOnChangeListener((slider, value, fromUser) -> {
            if (value == 0) {
                tvTiempoSeleccionado.setText("Sin límite");
            } else {
                tvTiempoSeleccionado.setText((int) value + " minutos");
            }
        });

        // Crear diálogo
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        // Botón limpiar
        dialogView.findViewById(R.id.btn_limpiar).setOnClickListener(v -> {
            spinnerCategoria.setText("Todas", false);
            spinnerDificultad.setText("Todas", false);
            sliderTiempo.setValue(60);
            cbSoloFavoritas.setChecked(false);
        });

        // Botón aplicar
        dialogView.findViewById(R.id.btn_aplicar).setOnClickListener(v -> {
            aplicarFiltros(
                    spinnerCategoria.getText().toString(),
                    spinnerDificultad.getText().toString(),
                    (int) sliderTiempo.getValue(),
                    cbSoloFavoritas.isChecked()
            );
            dialog.dismiss();
        });

        dialog.show();
    }

    /**
     * Aplica los filtros seleccionados
     */
    private void aplicarFiltros(String categoria, String dificultad, int tiempoMax, boolean soloFavoritas) {
        // Si solo favoritas
        if (soloFavoritas) {
            viewModel.getFavs().observe(getViewLifecycleOwner(), recetas -> {
                if (recetas != null) {
                    List<Receta> recetasFiltradas = filtrarRecetas(recetas, categoria, dificultad, tiempoMax);
                    adapter.submitList(recetasFiltradas);
                    actualizarEmptyState(recetasFiltradas.isEmpty());
                }
            });
            return;
        }

        // Si filtro por categoría específica
        if (!categoria.equals("Todas")) {
            viewModel.filtrarPorCategoria(categoria).observe(getViewLifecycleOwner(), recetas -> {
                if (recetas != null) {
                    List<Receta> recetasFiltradas = filtrarRecetas(recetas, "Todas", dificultad, tiempoMax);
                    adapter.submitList(recetasFiltradas);
                    actualizarEmptyState(recetasFiltradas.isEmpty());
                }
            });
            return;
        }

        // Si filtro por dificultad específica
        if (!dificultad.equals("Todas")) {
            viewModel.filtrarPorDificultad(dificultad).observe(getViewLifecycleOwner(), recetas -> {
                if (recetas != null) {
                    List<Receta> recetasFiltradas = filtrarRecetas(recetas, "Todas", "Todas", tiempoMax);
                    adapter.submitList(recetasFiltradas);
                    actualizarEmptyState(recetasFiltradas.isEmpty());
                }
            });
            return;
        }

        // Si filtro por tiempo
        if (tiempoMax > 0) {
            viewModel.filtrarPorTiempo(tiempoMax).observe(getViewLifecycleOwner(), recetas -> {
                if (recetas != null) {
                    adapter.submitList(recetas);
                    actualizarEmptyState(recetas.isEmpty());
                }
            });
            return;
        }

        // Si no hay filtros, mostrar todas
        observeData();
    }

    /**
     * Filtra una lista de recetas según criterios
     */
    private List<Receta> filtrarRecetas(List<Receta> recetas, String categoria, String dificultad, int tiempoMax) {
        List<Receta> resultado = new ArrayList<>(recetas);

        // Filtrar por categoría
        if (!categoria.equals("Todas")) {
            resultado.removeIf(r -> !categoria.equals(r.getCategoria()));
        }

        // Filtrar por dificultad
        if (!dificultad.equals("Todas")) {
            resultado.removeIf(r -> !dificultad.equals(r.getDificultad()));
        }

        // Filtrar por tiempo
        if (tiempoMax > 0) {
            resultado.removeIf(r -> r.getTiempoPreparacion() > tiempoMax);
        }

        return resultado;
    }


}