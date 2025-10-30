package com.example.tiendamascotas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

public class InicioFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inicio, container, false);

        MaterialButton btnVerCategorias = view.findViewById(R.id.btnVerCategorias);
        MaterialButton btnVerTodosProductos = view.findViewById(R.id.btnVerTodosProductos);

        btnVerCategorias.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, new CategoriasFragment())
                    .addToBackStack(null)
                    .commit();
        });

        btnVerTodosProductos.setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, new TodosProductosFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }
}