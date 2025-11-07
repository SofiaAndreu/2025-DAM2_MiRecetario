package com.example.recetarioapp.adapters;
// -- IMPORTACIONES --
import android.graphics.Paint; //para efecto de texto tachado
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
 * IngredienteAdapter - Muestra lista de ingredientes con checkbox.
 * Permite marcarlos como "comprados"; al hacerlo, el texto se tacha visualmente.
 */
public class IngredienteAdapter extends RecyclerView.Adapter<IngredienteAdapter.ViewHolder> {
    private List<Ingrediente> ingredientes; //Lista de ingredientes a mostrar.

    //Constructor - Lista vacía por defecto.
    public IngredienteAdapter() { this.ingredientes = new ArrayList<>(); }

    //Actualiza los datos y refresca RecyclerView
    public void setIngredientes(List<Ingrediente> ingredientes) {
        this.ingredientes = ingredientes != null ? ingredientes : new ArrayList<>(); //Si es null -> lista vacía.
        notifyDataSetChanged(); //Notificar a RecyclerView que los datos cambiaron.
    }

    //Crear nuevo viewHolder inflando el layout del item.
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Convertir item_ingrediente.xml en View
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ingrediente, parent, false);
        return new ViewHolder(view);
    }

    //Asigna datos del ingrediente a las vistas del viewHolder.
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //obtiene ingrediente + posición -> lo pasa ViewHolder.
        Ingrediente ingrediente = ingredientes.get(position);
        holder.bind(ingrediente);
    }

    //Devuelve número total de ingredientes en la lista.
    @Override
    public int getItemCount() {
        return ingredientes.size();
    }


    // -- VIEWHOLDER INTERNO --
    static class ViewHolder extends RecyclerView.ViewHolder {
        private final CheckBox cbIngrediente; //checkbox para marcar ingrediente
        private final TextView tvIngrediente; //TV para texto con nombre y cantidad del ingrediente.

        //Constructor que enlaza vistas y configura el comportamiento del Checkbox
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cbIngrediente = itemView.findViewById(R.id.cb_ingrediente);
            tvIngrediente = itemView.findViewById(R.id.tv_ingrediente);

            //Listener para marcar/desmarcar el checkbox (aplica o quita el tachado)
            cbIngrediente.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    // Tachar el texto
                    tvIngrediente.setPaintFlags(tvIngrediente.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                } else {
                    // Quitar tachado
                    tvIngrediente.setPaintFlags(tvIngrediente.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                }
            });
        }

        //Asignar datos de ingrediente a vistas
        public void bind(Ingrediente ingrediente) {
            tvIngrediente.setText(ingrediente.getIngredienteCompleto()); //metodo que devuelve texto formateado [models.Ingrediente]
            cbIngrediente.setChecked(false); //Reiniciar checkbox
            tvIngrediente.setPaintFlags(tvIngrediente.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG); //asegurar que el texto se muestre normal
        }
    }
}