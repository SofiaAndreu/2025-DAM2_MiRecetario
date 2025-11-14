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
 * Adapter para mostrar lista de recetas en RecyclerView con funcionalidades avanzadas.
 *
 * Características principales:
 * - Muestra información completa de recetas (imagen, nombre, descripción, etc.)
 * - Gestión eficiente de actualizaciones con DiffUtil
 * - Soporte para marcado de favoritos
 * - Carga optimizada de imágenes con Glide
 * - Interfaz de usuario rica con chips y elementos visuales
 */
public class RecetaAdapter extends ListAdapter<Receta, RecetaAdapter.RecetaViewHolder> {

    // Listeners para manejar interacciones del usuario
    private OnRecetaClickListener listener;
    private OnFavClickListener favListener;

    /**
     * Constructor que inicializa el adapter con DiffUtil para optimización.
     * Utiliza ListAdapter en lugar de RecyclerView.Adapter para mejor performance.
     */
    public RecetaAdapter() {
        super(DIFF_CALLBACK);
    }

    /**
     * Callback de DiffUtil para comparar y actualizar items eficientemente.
     * Determina qué items cambiaron y actualiza solo esos.
     */
    private static final DiffUtil.ItemCallback<Receta> DIFF_CALLBACK = new DiffUtil.ItemCallback<Receta>() {
        /**
         * Verifica si dos items representan el mismo objeto.
         * Compara por ID único de receta.
         */
        @Override
        public boolean areItemsTheSame(@NonNull Receta oldItem, @NonNull Receta newItem) {
            return oldItem.getId() == newItem.getId();
        }
        /**
         * Verifica si los contenidos de dos items son iguales.
         * Compara nombre y estado de favorito para detectar cambios.
         */
        @Override
        public boolean areContentsTheSame(@NonNull Receta oldItem, @NonNull Receta newItem) {
            return Objects.equals(oldItem.getNombre(), newItem.getNombre()) &&
                    oldItem.isFav() == newItem.isFav();
        }
    };

    /**
     * Crea nuevas vistas (ViewHolders) cuando el RecyclerView los necesita.
     * Infla el layout del item de receta.
     *
     * @param parent ViewGroup padre al que se añadirá la nueva vista
     * @param viewType Tipo de vista (no utilizado en este adapter)
     * @return Nuevo ViewHolder para el item de receta
     */
    @NonNull
    @Override
    public RecetaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Convertir el layout XML item_receta en un objeto View
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_receta, parent, false);
        return new RecetaViewHolder(view);
    }

    /**
     * Asigna los datos de la receta a las vistas del ViewHolder.
     * Se llama para cada item visible en el RecyclerView.
     *
     * @param holder ViewHolder que contiene las vistas a actualizar
     * @param position Posición del item en la lista de datos
     */
    @Override
    public void onBindViewHolder(@NonNull RecetaViewHolder holder, int position) {
        // Obtener la receta en la posición actual
        Receta receta = getItem(position);
        // Vincular datos de la receta con las vistas del ViewHolder
        holder.bind(receta);
    }

    /**
     * Obtiene la receta en una posición específica.
     * Método de utilidad para acceso externo a los datos.
     *
     * @param position Posición de la receta a obtener
     * @return Receta en la posición especificada
     */
    public Receta getRecetaEn(int position) {
        return getItem(position);
    }

    // ==================== CLASE INTERNA VIEWHOLDER ====================

    /**
     * ViewHolder que representa cada item de receta en el RecyclerView.
     * Mantiene referencias a todas las vistas para acceso eficiente.
     */
    class RecetaViewHolder extends RecyclerView.ViewHolder {
        // Referencias a todas las vistas del layout de receta
        private final ImageView ivImagen;
        private final TextView tvNombre;
        private final TextView tvDescripcion;
        private final TextView tvTiempo;
        private final TextView tvDificultad;
        private final TextView tvPorciones;
        private final Chip chipCategoria;
        private final ImageView btnFavorito;

        /**
         * Constructor que inicializa las vistas y configura listeners.
         *
         * @param itemView Vista del item de receta
         */
        public RecetaViewHolder(@NonNull View itemView) {
            super(itemView);
            // Obtener referencias a todas las vistas del layout
            ivImagen = itemView.findViewById(R.id.iv_receta_imagen);
            tvNombre = itemView.findViewById(R.id.tv_receta_nombre);
            tvDescripcion = itemView.findViewById(R.id.tv_receta_descripcion);
            tvTiempo = itemView.findViewById(R.id.tv_receta_tiempo);
            tvDificultad = itemView.findViewById(R.id.tv_receta_dificultad);
            tvPorciones = itemView.findViewById(R.id.tv_receta_porciones);
            chipCategoria = itemView.findViewById(R.id.chip_categoria);
            btnFavorito = itemView.findViewById(R.id.btn_favorito);

            /**
             * Listener para clic en toda la tarjeta de receta.
             * Navega a los detalles de la receta seleccionada.
             */
            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                // Verificar que la posición es válida y existe listener
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onRecetaClick(getItem(position));
                }
            });

            /**
             * Listener para clic en el botón de favorito.
             * Alterna el estado de favorito de la receta.
             */
            btnFavorito.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                // Verificar que la posición es válida y existe listener
                if (favListener != null && position != RecyclerView.NO_POSITION) {
                    // Obtener receta y alternar estado de favorito
                    Receta receta = getItem(position);
                    favListener.onFavClick(receta, !receta.isFav());
                }
            });
        }

        /**
         * Vincula los datos de una receta con las vistas del ViewHolder.
         * Actualiza todos los elementos visuales con la información de la receta.
         *
         * @param receta Receta con los datos a mostrar
         */
        public void bind(Receta receta) {
            // Mostrar nombre de la receta
            tvNombre.setText(receta.getNombre());

            // Mostrar descripción si existe, ocultar si está vacía
            if (receta.getDescripcion() != null && !receta.getDescripcion().isEmpty()) {
                tvDescripcion.setText(receta.getDescripcion());
                tvDescripcion.setVisibility(View.VISIBLE);
            } else {
                tvDescripcion.setVisibility(View.GONE);
            }

            // Mostrar tiempo de preparación formateado
            tvTiempo.setText(receta.getTiempoPrepFormateado());

            // Mostrar dificultad o guión si no está definida
            String dificultad = receta.getDificultad() != null ? receta.getDificultad() : "-";
            tvDificultad.setText(dificultad);

            // Mostrar número de porciones o guión si no está definido
            if (receta.getPorciones() > 0) {
                tvPorciones.setText(receta.getPorciones() + " porc.");
            } else {
                tvPorciones.setText("-");
            }

            // Mostrar chip de categoría solo si existe
            if (receta.getCategoria() != null && !receta.getCategoria().isEmpty()) {
                chipCategoria.setText(receta.getCategoria());
                chipCategoria.setVisibility(View.VISIBLE);
            } else {
                chipCategoria.setVisibility(View.GONE);
            }

            // Cargar imagen de la receta usando Glide
            if (receta.getImagenPortadaURL() != null && !receta.getImagenPortadaURL().isEmpty()) {
                // Crear objeto File desde la ruta de la imagen
                File imageFile = new File(receta.getImagenPortadaURL());
                // Configurar carga de imagen con Glide
                Glide.with(itemView.getContext())
                        .load(imageFile) // Archivo a cargar
                        .placeholder(R.drawable.placeholder_receta) // Imagen mientras carga
                        .error(R.drawable.placeholder_receta) // Imagen si hay error
                        .centerCrop() // Recortar al centro
                        .into(ivImagen); // ImageView destino
            } else {
                // Usar imagen placeholder si no hay imagen
                ivImagen.setImageResource(R.drawable.placeholder_receta);
            }

            // Actualizar icono de favorito según el estado
            if (receta.isFav()) {
                btnFavorito.setImageResource(R.drawable.ic_fav);
            } else {
                btnFavorito.setImageResource(R.drawable.ic_fav_no);
            }
        }
    }

    // ==================== INTERFACES PARA CALLBACKS ====================

    /**
     * Interfaz para manejar clics en recetas.
     * Permite navegar a los detalles de la receta seleccionada.
     */
    public interface OnRecetaClickListener {
        void onRecetaClick(Receta receta);
    }

    /**
     * Interfaz para manejar clics en botones de favorito.
     * Permite agregar o quitar recetas de favoritos.
     */
    public interface OnFavClickListener {
        void onFavClick(Receta receta, boolean isFav);
    }

    // ==================== SETTERS PARA LISTENERS ====================

    /**
     * Establece el listener para clics en recetas.
     *
     * @param listener Listener a establecer
     */
    public void setOnRecetaClickListener(OnRecetaClickListener listener) {
        this.listener = listener;
    }

    /**
     * Establece el listener para clics en favoritos.
     *
     * @param listener Listener a establecer
     */
    public void setOnFavClickListener(OnFavClickListener listener) {
        this.favListener = listener;
    }
}