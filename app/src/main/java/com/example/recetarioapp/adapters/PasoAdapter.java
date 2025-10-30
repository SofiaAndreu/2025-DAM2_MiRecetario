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
 * Adapter para mostrar los pasos de preparación ✓
 */
public class PasoAdapter extends RecyclerView.Adapter<PasoAdapter.ViewHolder> {
    private List<Paso> pasos; //Lista de pasos
    public PasoAdapter() {
        this.pasos = new ArrayList<>();
    } //Constructor lista vacia

    //Actualizar datos
    public void setPasos(List<Paso> pasos) {
        this.pasos = pasos != null ? pasos : new ArrayList<>(); //Si la lista es null -> lista vacia
        notifyDataSetChanged();
    }
    // ------------------------------------------------------------------------------ //

    //Crear nuevas vistas/items
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()) //convierte item_paso.xml en objetos View
                .inflate(R.layout.item_paso, parent, false);
        return new ViewHolder(view);
    }

    //Asigna datos a una vista existente
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Paso paso = pasos.get(position); //obtiene paso en posicion actual
        holder.bind(paso);
    }

    //Devuelve cuantos items hay en la lista
    @Override
    public int getItemCount() {
        return pasos.size();
    }
    // ------------------------------------------------------------------------------ //

    //Clase interna ViewHolder
    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvNumero; //tv para mostrar numero de paso
        private final TextView tvDescripcion; //tv para mostrar descripcion

        //Constructor que recibe la vista del item
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNumero = itemView.findViewById(R.id.tv_numero);
            tvDescripcion = itemView.findViewById(R.id.tv_descripcion);
        }

        //Asigna los datos del paso a las vistas
        public void bind(Paso paso) {
            tvNumero.setText(String.valueOf(paso.getNumeroPaso())); //obtiene numero de paso -> convierte a String
            tvDescripcion.setText(paso.getDescripcion()); //obtiene descripcion
        }
    }
    // ------------------------------------------------------------------------------ //
}
