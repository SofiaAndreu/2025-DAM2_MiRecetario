package com.example.recetarioapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.recetarioapp.R;
import com.example.recetarioapp.models.Receta;
import com.google.android.material.chip.Chip;

import java.io.File;


/**
 * Adapter para mostrar la lista de recetas en RecyclerView
 * puente entre objetos receta y RecyclerView
 */
public class RecetaAdapter extends ListAdapter<Receta, RecetaAdapter.RecetaViewHolder> {

    private OnRecetaClickListener listener;
    private OnFavClickListener favListener;

    //Inicializar adapter - DiffUtil.ItemCallback para optimizar actualizacion
    public RecetaAdapter() {
        super(DIFF_CALLBACK);
    }

    //Compara recetas y actualiza solo los items que cambian
    private static final DiffUtil.ItemCallback<Receta> DIFF_CALLBACK = new DiffUtil.ItemCallback<Receta>() {
        @Override //Compara si son mismo objeto (ID)
        public boolean areItemsTheSame(@NonNull Receta oldItem, @NonNull Receta newItem) {
            return oldItem.getId() == newItem.getId();
        }
        @Override //Compara mismo contenido (nombre y fav)
        public boolean areContentsTheSame(@NonNull Receta oldItem, @NonNull Receta newItem) {
            return oldItem.getNombre().equals(newItem.getNombre()) &&
                    oldItem.isFav() == newItem.isFav();
        }
    };

    //Creacion de vista de cada item - item_receta.xml
    @NonNull
    @Override
    public RecetaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_receta, parent, false);
        return new RecetaViewHolder(view);
    }

    //Asignacion datos de receta al ViewHolder correspondiente
    @Override
    public void onBindViewHolder(@NonNull RecetaViewHolder holder, int position) {
        Receta receta = getItem(position);
        holder.bind(receta);
    }

    public Receta getRecetaEn(int position) {
        return getItem(position);
    }


    //------------------------------------------------------------------------------------
    // REFERENCIAS elementos visuales (img, txt, btn) para evitar busqueda en cada actualizacion
    class RecetaViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivImagen;
        private final TextView tvNombre;
        private final TextView tvDescripcion;
        private final TextView tvTiempo;
        private final TextView tvDificultad;
        private final TextView tvPorciones;
        private final Chip chipCategoria;
        private final ImageView btnFavorito;

        public RecetaViewHolder(@NonNull View itemView) {
            super(itemView);

            ivImagen = itemView.findViewById(R.id.iv_receta_imagen);
            tvNombre = itemView.findViewById(R.id.tv_receta_nombre);
            tvDescripcion = itemView.findViewById(R.id.tv_receta_descripcion);
            tvTiempo = itemView.findViewById(R.id.tv_receta_tiempo);
            tvDificultad = itemView.findViewById(R.id.tv_receta_dificultad);
            tvPorciones = itemView.findViewById(R.id.tv_receta_porciones);
            chipCategoria = itemView.findViewById(R.id.chip_categoria);
            btnFavorito = itemView.findViewById(R.id.btn_favorito);

            // Click en la card completa
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onRecetaClick(getItem(position));
                }
            });

            // Click en el botón de favorito
            btnFavorito.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (favListener != null && position != RecyclerView.NO_POSITION) {
                    Receta receta = getItem(position);
                    favListener.onFavClick(receta, !receta.isFav());
                }
            });
        }

        public void bind(Receta receta) {
            // Nombre
            tvNombre.setText(receta.getNombre());

            // Descripción
            if (receta.getDescripcion() != null && !receta.getDescripcion().isEmpty()) {
                tvDescripcion.setText(receta.getDescripcion());
                tvDescripcion.setVisibility(View.VISIBLE);
            } else {
                tvDescripcion.setVisibility(View.GONE);
            }

            // Tiempo
            tvTiempo.setText(receta.getTiempoPrepFormateado());

            // Dificultad
            String dificultad = receta.getDificultad() != null ? receta.getDificultad() : "Media";
            tvDificultad.setText(dificultad);

            // Porciones
            if (receta.getPorciones() > 0) {
                tvPorciones.setText(receta.getPorciones() + " porc.");
            } else {
                tvPorciones.setText("-");
            }

            // Categoría
            if (receta.getCategoria() != null && !receta.getCategoria().isEmpty()) {
                chipCategoria.setText(receta.getCategoria());
                chipCategoria.setVisibility(View.VISIBLE);
            } else {
                chipCategoria.setVisibility(View.GONE);
            }

            // Imagen
            if (receta.getImagenPortadaURL() != null && !receta.getImagenPortadaURL().isEmpty()) {
                // Cargar imagen local usando File
                File imageFile = new File(receta.getImagenPortadaURL());

                Glide.with(itemView.getContext())
                        .load(imageFile)
                        .placeholder(R.drawable.placeholder_receta)
                        .error(R.drawable.placeholder_receta)
                        .centerCrop()
                        .into(ivImagen);
            } else {
                ivImagen.setImageResource(R.drawable.placeholder_receta);
            }

            // Favorito
            if (receta.isFav()) {
                btnFavorito.setImageResource(android.R.drawable.star_big_on);
            } else {
                btnFavorito.setImageResource(android.R.drawable.star_big_off);
            }
        }
    }



    // Interfaces para callbacks
    public interface OnRecetaClickListener {
        void onRecetaClick(Receta receta);
    }

    public interface OnFavClickListener {
        void onFavClick(Receta receta, boolean isFav);
    }

    // Setters para listeners
    public void setOnRecetaClickListener(OnRecetaClickListener listener) {
        this.listener = listener;
    }

    public void setOnFavClickListener(OnFavClickListener listener) {
        this.favListener = listener;
    }
}