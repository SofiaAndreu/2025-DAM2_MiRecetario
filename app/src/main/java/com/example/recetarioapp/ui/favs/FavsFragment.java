package com.example.recetarioapp.ui.favs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.recetarioapp.R;
import com.example.recetarioapp.adapters.RecetaAdapter;
import com.example.recetarioapp.ui.base.BaseFragment;
import com.example.recetarioapp.utils.ViewExtensions;

//Fragment para mostrar la lista de recetas marcadas como favoritas
// - Muestra lista de recetas favoritas del usuario
// - Gestiona estados vacíos cuando no hay favoritos
// - Permite quitar recetas de favoritos directamente
// - Navegación a detalles de recetas favoritas
public class FavsFragment extends BaseFragment {

    //RecyclerView para mostrar la lista de recetas favoritas
    private RecyclerView rvFavoritas;

    //Layout que se muestra cuando no hay recetas favoritas
    private FrameLayout layoutEmpty;

    //Adaptador para gestionar la lista de recetas
    private RecetaAdapter adapter;

    //Crea la vista del fragmento inflando el layout correspondiente
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favs, container, false);
    }

    //Configura la vista después de que ha sido creada
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Inicializar todos los componentes
        initViewModel();
        initViews(view);
        setupRecyclerView();
        observeData();
    }

    //Inicializa las referencias a las vistas del layout
    private void initViews(View view) {
        rvFavoritas = view.findViewById(R.id.rv_favoritas);
        layoutEmpty = view.findViewById(R.id.layout_empty);
    }

    //Configura el RecyclerView para mostrar la lista de recetas favoritas
    private void setupRecyclerView() {
        adapter = new RecetaAdapter();
        rvFavoritas.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFavoritas.setAdapter(adapter);

        //Configurar listener para clic en receta - abre detalles
        adapter.setOnRecetaClickListener(this::openRecipeDetail);

        //Configurar listener para clic en favorito - permite quitar de favoritos
        adapter.setOnFavClickListener((receta, isFav) -> {
            toggleFavorite(receta, isFav);
            if (!isFav) showToast("Quitada de favoritos");
        });
    }

    //Observa los cambios en la lista de recetas favoritas
    private void observeData() {
        viewModel.getFavs().observe(getViewLifecycleOwner(), favoritas -> {
            if (favoritas == null) return;

            //Actualizar la lista del adaptador con las recetas favoritas
            adapter.submitList(favoritas);

            //Mostrar/u ocultar elementos según si hay favoritos
            ViewExtensions.setVisible(rvFavoritas, !favoritas.isEmpty());
            ViewExtensions.setVisible(layoutEmpty, favoritas.isEmpty());
        });
    }
}