package com.example.tiendamascotas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tiendamascotas.adapter.ProductosAdapter;
import com.example.tiendamascotas.model.Categoria;
import com.example.tiendamascotas.model.Producto;

import java.io.Serializable;

public class ProductosFragment extends Fragment {
    private static final String ARG_CATEGORIA = "categoria";
    private Categoria categoria;
    private RecyclerView recyclerView;

    public static ProductosFragment newInstance(Categoria categoria) {
        ProductosFragment fragment = new ProductosFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CATEGORIA,categoria);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoria = (Categoria) getArguments().getSerializable(ARG_CATEGORIA);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_productos, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewProductos);
        setupRecyclerView();
        return view;
    }

    private void setupRecyclerView() {
        ProductosAdapter adapter = new ProductosAdapter(categoria.getProductos(), this::onProductoClick);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setAdapter(adapter);
    }

    private void onProductoClick(Producto producto) {
        DetalleImagenFragment fragment = DetalleImagenFragment.newInstance(producto.getImagenUrl());
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }
}
