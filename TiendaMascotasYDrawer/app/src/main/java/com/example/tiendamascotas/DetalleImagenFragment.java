package com.example.tiendamascotas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.squareup.picasso.Picasso;

public class DetalleImagenFragment extends Fragment {
    private static final String ARG_IMAGEN_URL = "imagen_url";

    public static DetalleImagenFragment newInstance(String imagenUrl) {
        DetalleImagenFragment fragment = new DetalleImagenFragment();
        Bundle args = new Bundle();
        args.putString(ARG_IMAGEN_URL, imagenUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detalle_imagen, container, false);

        ImageView imageView = view.findViewById(R.id.imageViewDetalle);
        MaterialButton btnVolver = view.findViewById(R.id.btnVolver);

        // Cargar imagen (usar Picasso o Glide)
        String imagenUrl = getArguments().getString(ARG_IMAGEN_URL);
        Picasso.get().load(imagenUrl).into(imageView);

        btnVolver.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        return view;
    }
}
