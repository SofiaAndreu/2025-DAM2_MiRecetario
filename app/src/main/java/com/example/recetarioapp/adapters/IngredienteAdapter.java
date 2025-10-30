package com.example.recetarioapp.adapters;

import android.graphics.Paint; //para texto tachado
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
 * Adapter para mostrar ingredientes con checkbox ✓
 */
public class IngredienteAdapter extends RecyclerView.Adapter<IngredienteAdapter.ViewHolder> {
    //List de datos
    private List<Ingrediente> ingredientes;

    //Constructor Lista vacía
    public IngredienteAdapter() {
        this.ingredientes = new ArrayList<>();
    }

    //Actualizar datos
    public void setIngredientes(List<Ingrediente> ingredientes) {
        this.ingredientes = ingredientes != null ? ingredientes : new ArrayList<>(); //si es null -> lista vacía
        notifyDataSetChanged(); //Notificar a RecyclerView que los datos cambiaron
    }
    // -------------------------------------------------------------------------------- //

    //Crear vistas/items cuando se necesitan
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Convertir item_ingrediente.xml en View
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ingrediente, parent, false);
        return new ViewHolder(view);
    }

    //Asigna datos a vista existente
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //obtiene ingrediente + posición -> lo pasa ViewHolder
        Ingrediente ingrediente = ingredientes.get(position);
        holder.bind(ingrediente);
    }

    //Devuelve cuántos items hay en la lista
    @Override
    public int getItemCount() {
        return ingredientes.size();
    }

    // -------------------------------------------------------------------------------- //

    //Clase interna - ViewHolder
    static class ViewHolder extends RecyclerView.ViewHolder {
        private final CheckBox cbIngrediente; //checkbox
        private final TextView tvIngrediente; //TV para texto

        //Constructor que recibe vista del item
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cbIngrediente = itemView.findViewById(R.id.cb_ingrediente);
            tvIngrediente = itemView.findViewById(R.id.tv_ingrediente);

            // Listener para marcar/desmarcar checkbox
            cbIngrediente.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    // Tachar el texto
                    tvIngrediente.setPaintFlags(tvIngrediente.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                } else {
                    // Quitar tachado
                    tvIngrediente.setPaintFlags(tvIngrediente.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                }
            });
        } // ---------------------------------------------------------------------------------------------- //

        //Asignar datos de ingrediente a vistas
        public void bind(Ingrediente ingrediente) {
            tvIngrediente.setText(ingrediente.getIngredienteCompleto()); //metodo que devuelve texto formateado [models.Ingrediente]
            cbIngrediente.setChecked(false); //Reiniciar checkbox
            tvIngrediente.setPaintFlags(tvIngrediente.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG); //asegurar que el texto se muestre normal
        }
    }
}