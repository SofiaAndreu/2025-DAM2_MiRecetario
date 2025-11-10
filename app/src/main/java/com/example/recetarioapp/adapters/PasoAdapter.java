package com.example.recetarioapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recetarioapp.R;
import com.example.recetarioapp.models.Paso;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter para mostrar la lista de pasos de preparación de una receta.
 *
 * Funcionalidades principales:
 * - Muestra pasos numerados secuencialmente
 * - Presenta descripción detallada de cada paso
 * - Gestión eficiente de listas mediante RecyclerView
 * - Layout optimizado para lectura de instrucciones
 */
public class PasoAdapter extends RecyclerView.Adapter<PasoAdapter.ViewHolder> {

    // Lista de pasos de preparación a mostrar
    private List<Paso> pasos;

    /**
     * Constructor que inicializa el adapter con lista vacía.
     * Garantiza que la lista nunca sea null.
     */
    public PasoAdapter() {
        this.pasos = new ArrayList<>();
    }

    /**
     * Actualiza la lista de pasos y notifica al RecyclerView del cambio.
     * Maneja casos donde la lista proporcionada es null.
     *
     * @param pasos Nueva lista de pasos a mostrar
     */
    public void setPasos(List<Paso> pasos) {
        // Usar lista vacía si se proporciona null para evitar errores
        this.pasos = pasos != null ? pasos : new ArrayList<>();
        // Notificar al RecyclerView que todos los datos cambiaron
        notifyDataSetChanged();
    }

    /**
     * Crea nuevas vistas (ViewHolders) cuando el RecyclerView los necesita.
     * Infla el layout del item de paso.
     *
     * @param parent ViewGroup padre al que se añadirá la nueva vista
     * @param viewType Tipo de vista (no utilizado en este adapter)
     * @return Nuevo ViewHolder para el item de paso
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Convertir el layout XML item_paso en un objeto View
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_paso, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Asigna los datos del paso a las vistas del ViewHolder.
     * Se llama para cada item visible en el RecyclerView.
     *
     * @param holder ViewHolder que contiene las vistas a actualizar
     * @param position Posición del item en la lista de datos
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Obtener el paso en la posición actual
        Paso paso = pasos.get(position);
        // Vincular datos del paso con las vistas del ViewHolder
        holder.bind(paso);
    }

    /**
     * Retorna el número total de pasos en la lista.
     * Determina cuántos items mostrará el RecyclerView.
     *
     * @return Número de pasos en la lista
     */
    @Override
    public int getItemCount() {
        return pasos.size();
    }

    // ==================== CLASE INTERNA VIEWHOLDER ====================

    /**
     * ViewHolder que representa cada item de paso en el RecyclerView.
     * Mantiene referencias a las vistas para acceso eficiente.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        // TextView para mostrar el número del paso (1, 2, 3...)
        private final TextView tvNumero;
        // TextView para mostrar la descripción detallada del paso
        private final TextView tvDescripcion;

        /**
         * Constructor que inicializa las referencias a las vistas.
         *
         * @param itemView Vista del item de paso
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Obtener referencias a las vistas del layout
            tvNumero = itemView.findViewById(R.id.tv_numero);
            tvDescripcion = itemView.findViewById(R.id.tv_descripcion);
        }

        /**
         * Vincula los datos de un paso con las vistas del ViewHolder.
         * Establece el número y descripción del paso.
         *
         * @param paso Paso con los datos a mostrar
         */
        public void bind(Paso paso) {
            // Convertir número de paso a String y mostrar
            tvNumero.setText(String.valueOf(paso.getNumeroPaso()));
            // Mostrar descripción del paso
            tvDescripcion.setText(paso.getDescripcion());
        }
    }
}