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
 * Adapter para mostrar los pasos de preparación
 */
public class PasoAdapter extends RecyclerView.Adapter<PasoAdapter.ViewHolder> {

    private List<Paso> pasos;

    public PasoAdapter() {
        this.pasos = new ArrayList<>();
    }

    public void setPasos(List<Paso> pasos) {
        this.pasos = pasos != null ? pasos : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_paso, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Paso paso = pasos.get(position);
        holder.bind(paso);
    }

    @Override
    public int getItemCount() {
        return pasos.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvNumero;
        private final TextView tvDescripcion;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNumero = itemView.findViewById(R.id.tv_numero);
            tvDescripcion = itemView.findViewById(R.id.tv_descripcion);
        }

        public void bind(Paso paso) {
            tvNumero.setText(String.valueOf(paso.getNumeroPaso()));
            tvDescripcion.setText(paso.getDescripcion());
        }
    }
}
