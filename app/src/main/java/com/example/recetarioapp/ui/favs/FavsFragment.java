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

public class FavsFragment extends BaseFragment {

    private RecyclerView rvFavoritas;
    private FrameLayout layoutEmpty;
    private RecetaAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favs, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViewModel();
        initViews(view);
        setupRecyclerView();
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

        adapter.setOnRecetaClickListener(this::openRecipeDetail);
        adapter.setOnFavClickListener((receta, isFav) -> {
            toggleFavorite(receta, isFav);
            if (!isFav) showToast("Quitada de favoritos");
        });
    }

    private void observeData() {
        viewModel.getFavs().observe(getViewLifecycleOwner(), favoritas -> {
            if (favoritas == null) return;

            adapter.submitList(favoritas);
            ViewExtensions.setVisible(rvFavoritas, !favoritas.isEmpty());
            ViewExtensions.setVisible(layoutEmpty, favoritas.isEmpty());
        });
    }
}