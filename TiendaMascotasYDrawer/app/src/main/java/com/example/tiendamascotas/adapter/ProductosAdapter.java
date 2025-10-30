package com.example.tiendamascotas.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tiendamascotas.R;
import com.example.tiendamascotas.model.Producto;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ProductosAdapter extends RecyclerView.Adapter<ProductosAdapter.ViewHolder> {
    private List<Producto> productos;
    private OnProductoClickListener listener;

    public interface OnProductoClickListener {
        void onProductoClick(Producto producto);
    }

    public ProductosAdapter(List<Producto> productos, OnProductoClickListener listener) {
        this.productos = productos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Producto producto = productos.get(position);
        holder.bind(producto, listener);
    }

    @Override
    public int getItemCount() {
        return productos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imagenProducto;
        private TextView nombreProducto;
        private TextView precioProducto;
        private TextView descripcionProducto;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imagenProducto = itemView.findViewById(R.id.imgProducto);
            nombreProducto = itemView.findViewById(R.id.tvNombreProducto);
            precioProducto = itemView.findViewById(R.id.tvPrecioProducto);
        }

        public void bind(Producto producto, OnProductoClickListener listener) {
            nombreProducto.setText(producto.getNombre());
            precioProducto.setText(producto.getPrecio());

            // LOG PARA DEPURAR
            android.util.Log.d("ProductosAdapter", "Intentando cargar: " + producto.getImagenUrl());

            Picasso.get()
                    .load(producto.getImagenUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(imagenProducto, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            android.util.Log.d("Picasso", "✓ Imagen cargada: " + producto.getNombre());
                        }

                        @Override
                        public void onError(Exception e) {
                            android.util.Log.e("Picasso", "✗ Error en " + producto.getNombre() + ": " + e.getMessage());
                        }
                    });

            itemView.setOnClickListener(v -> listener.onProductoClick(producto));
        }
    }
}