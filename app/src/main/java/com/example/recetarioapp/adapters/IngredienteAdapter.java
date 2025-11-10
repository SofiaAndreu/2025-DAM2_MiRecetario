package com.example.recetarioapp.adapters;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recetarioapp.R;
import com.example.recetarioapp.models.Ingrediente;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter para mostrar lista de ingredientes con funcionalidad de marcado.
 *
 * Características principales:
 * - Muestra cada ingrediente con un checkbox
 * - Permite marcar ingredientes como "comprados"
 * - Aplica efecto de texto tachado cuando se marca
 * - Gestión eficiente de listas mediante RecyclerView
 */
public class IngredienteAdapter extends RecyclerView.Adapter<IngredienteAdapter.ViewHolder> {

    // Lista de ingredientes a mostrar en el RecyclerView
    private List<Ingrediente> ingredientes;

    /**
     * Constructor que inicializa el adapter con lista vacía.
     * Previene NullPointerException al usar la lista.
     */
    public IngredienteAdapter() {
        this.ingredientes = new ArrayList<>();
    }

    /**
     * Actualiza los datos del adapter y notifica cambios.
     * Maneja casos donde la lista proporcionada es null.
     *
     * @param ingredientes Nueva lista de ingredientes a mostrar
     */
    public void setIngredientes(List<Ingrediente> ingredientes) {
        // Usar lista vacía si se proporciona null para evitar errores
        this.ingredientes = ingredientes != null ? ingredientes : new ArrayList<>();
        // Notificar al RecyclerView que todos los datos cambiaron
        notifyDataSetChanged();
    }

    /**
     * Crea nuevas vistas (ViewHolders) cuando el RecyclerView los necesita.
     * Infla el layout del item de ingrediente.
     *
     * @param parent ViewGroup padre al que se añadirá la nueva vista
     * @param viewType Tipo de vista (no utilizado en este adapter)
     * @return Nuevo ViewHolder para el item de ingrediente
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Convertir el layout XML item_ingrediente en un objeto View
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ingrediente, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Asigna los datos del ingrediente a las vistas del ViewHolder.
     * Se llama para cada item visible en el RecyclerView.
     *
     * @param holder ViewHolder que contiene las vistas a actualizar
     * @param position Posición del item en la lista de datos
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Obtener el ingrediente en la posición actual
        Ingrediente ingrediente = ingredientes.get(position);
        // Vincular datos del ingrediente con las vistas del ViewHolder
        holder.bind(ingrediente);
    }

    /**
     * Retorna el número total de items en la lista de datos.
     * Determina cuántos items mostrará el RecyclerView.
     *
     * @return Número de ingredientes en la lista
     */
    @Override
    public int getItemCount() {
        return ingredientes.size();
    }

    // ==================== CLASE INTERNA VIEWHOLDER ====================

    /**
     * ViewHolder que representa cada item de ingrediente en el RecyclerView.
     * Mantiene referencias a las vistas para acceso eficiente.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        // Checkbox para marcar/desmarcar el ingrediente como comprado
        private final CheckBox cbIngrediente;
        // TextView para mostrar el nombre y cantidad del ingrediente
        private final TextView tvIngrediente;

        /**
         * Constructor que inicializa las vistas y configura listeners.
         *
         * @param itemView Vista del item de ingrediente
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Obtener referencias a las vistas del layout
            cbIngrediente = itemView.findViewById(R.id.cb_ingrediente);
            tvIngrediente = itemView.findViewById(R.id.tv_ingrediente);

            /**
             * Listener para cambios en el estado del checkbox.
             * Aplica o remueve el efecto de texto tachado según el estado.
             */
            cbIngrediente.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    // Aplicar flag de texto tachado cuando está marcado
                    tvIngrediente.setPaintFlags(tvIngrediente.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                } else {
                    // Remover flag de texto tachado cuando está desmarcado
                    tvIngrediente.setPaintFlags(tvIngrediente.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                }
            });
        }

        /**
         * Vincula los datos de un ingrediente con las vistas del ViewHolder.
         * Establece el texto y resetea el estado visual del item.
         *
         * @param ingrediente Ingrediente con los datos a mostrar
         */
        public void bind(Ingrediente ingrediente) {
            // Establecer texto formateado del ingrediente (nombre + cantidad)
            tvIngrediente.setText(ingrediente.getIngredienteCompleto());
            // Reiniciar checkbox a estado desmarcado
            cbIngrediente.setChecked(false);
            // Asegurar que el texto se muestre sin tachado inicialmente
            tvIngrediente.setPaintFlags(tvIngrediente.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        }
    }
}