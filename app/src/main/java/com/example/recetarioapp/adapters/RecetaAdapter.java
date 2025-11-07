package com.example.recetarioapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.Objects;

/**
 * Adapter para mostrar la lista de recetas en RecyclerView
 * puente entre objetos receta y RecyclerView
 */
public class RecetaAdapter extends ListAdapter<Receta, RecetaAdapter.RecetaViewHolder> {

    //Variables para Listeners
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
        @Override
        public boolean areContentsTheSame(@NonNull Receta oldItem, @NonNull Receta newItem) {
            return Objects.equals(oldItem.getNombre(), newItem.getNombre()) &&
                    oldItem.isFav() == newItem.isFav();
        }
    };
    // ---------------------------------------------------------------------------------- //

    //Creacion de vista de cada item - item_receta.xml
    @NonNull
    @Override
    public RecetaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_receta, parent, false);
        return new RecetaViewHolder(view);
    }
    // ------------------------------------------------------------------------------------ //

    //Asignacion datos de receta al ViewHolder correspondiente
    @Override
    public void onBindViewHolder(@NonNull RecetaViewHolder holder, int position) {
        Receta receta = getItem(position);
        holder.bind(receta);
    }
    // ---------------------------------------------------------------------- //

    public Receta getRecetaEn(int position) {
        return getItem(position);
    }
    //------------------------------------------------------------------------------------ //

    //REFERENCIAS elementos visuales (img, txt, btn) para evitar busqueda en cada actualizacion
    class RecetaViewHolder extends RecyclerView.ViewHolder {
        //Referencias
        private final ImageView ivImagen;
        private final TextView tvNombre;
        private final TextView tvDescripcion;
        private final TextView tvTiempo;
        private final TextView tvDificultad;
        private final TextView tvPorciones;
        private final Chip chipCategoria;
        private final ImageView btnFavorito;

        //Constructor que recibe la vista de item y llama a constructor padre
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
                int position = getBindingAdapterPosition(); //obtiene posicion actual
                //verifica que existe y es valida
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onRecetaClick(getItem(position)); //ejecuta callback
                }
            });

            // Click en el botón de favorito
            btnFavorito.setOnClickListener(v -> {
                int position = getBindingAdapterPosition(); //obtiene posicion y receta
                if (favListener != null && position != RecyclerView.NO_POSITION) {
                    //cambia el estado actual al opuesto
                    Receta receta = getItem(position);
                    favListener.onFavClick(receta, !receta.isFav());
                }
            });
        } //------------------------------------------------------------------------------------ //

        //ASIGNAR DATOS - Recibe Receta y muestra sus datos
        public void bind(Receta receta) {
            tvNombre.setText(receta.getNombre()); //nombre

            // -- Muestra descripción si existe texto --
            if (receta.getDescripcion() != null && !receta.getDescripcion().isEmpty()) {
                tvDescripcion.setText(receta.getDescripcion());
                tvDescripcion.setVisibility(View.VISIBLE);
            } else {
                tvDescripcion.setVisibility(View.GONE);
            }

            // -- Tiempo de preparacion formateado --
            tvTiempo.setText(receta.getTiempoPrepFormateado());

            // -- Muestra Dificultad/"-" si es null --
            String dificultad = receta.getDificultad() != null ? receta.getDificultad() : "-";
            tvDificultad.setText(dificultad);

            // -- Muestra porciones/si no hay "-" --
            if (receta.getPorciones() > 0) {
                tvPorciones.setText(receta.getPorciones() + " porc.");
            } else {
                tvPorciones.setText("-");
            }

            // -- Muestra chip solo si existe categoria --
            if (receta.getCategoria() != null && !receta.getCategoria().isEmpty()) {
                chipCategoria.setText(receta.getCategoria());
                chipCategoria.setVisibility(View.VISIBLE);
            } else {
                chipCategoria.setVisibility(View.GONE);
            }

            // -- Carga imagen con GLIDE --
            if (receta.getImagenPortadaURL() != null && !receta.getImagenPortadaURL().isEmpty()) { //verificar URL de Imagen
                File imageFile = new File(receta.getImagenPortadaURL()); //objeto File
                //Carga imagen
                Glide.with(itemView.getContext()) //contexto
                        .load(imageFile)//archivo a cargar
                        .placeholder(R.drawable.placeholder_receta) //mientras carga...
                        .error(R.drawable.placeholder_receta)//si falla
                        .centerCrop() //recorta al centro
                        .into(ivImagen); //imageView de destino (donde se muestra)
            } else {
                ivImagen.setImageResource(R.drawable.placeholder_receta); //Si no hay imagen -> placeholder_receta.xml
            } // ----------------------------------------------------------------- //

            // -- Botón Favorito -- Cambia icono
            if (receta.isFav()) { //SI es fav. -> drawable/ic_star_filled.xml
                btnFavorito.setImageResource(R.drawable.ic_fav);
            } else { //NO es fav. -> drawable/ic_star_outline.xml
                btnFavorito.setImageResource(R.drawable.ic_fav_no);
            }
        }
    }
    // ------------------------------------------------------------------------- //

    // -- INTERFACES PARA CALLBACKS --

    //Click en receta
    public interface OnRecetaClickListener { void onRecetaClick(Receta receta);}

    //Click en favorito
    public interface OnFavClickListener { void onFavClick(Receta receta, boolean isFav);}

    // -- SETTERS PARA LISTENERS -- (Receta y Favorto)
    public void setOnRecetaClickListener(OnRecetaClickListener listener) {this.listener = listener;}
    public void setOnFavClickListener(OnFavClickListener listener) {
        this.favListener = listener;
    }
}