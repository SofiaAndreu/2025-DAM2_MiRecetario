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
 * Adapter para mostrar ingredientes con checkbox
 */
public class IngredienteAdapter extends RecyclerView.Adapter<IngredienteAdapter.ViewHolder> {

    private List<Ingrediente> ingredientes;

    public IngredienteAdapter() {
        this.ingredientes = new ArrayList<>();
    }

    public void setIngredientes(List<Ingrediente> ingredientes) {
        this.ingredientes = ingredientes != null ? ingredientes : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ingrediente, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Ingrediente ingrediente = ingredientes.get(position);
        holder.bind(ingrediente);
    }

    @Override
    public int getItemCount() {
        return ingredientes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final CheckBox cbIngrediente;
        private final TextView tvIngrediente;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cbIngrediente = itemView.findViewById(R.id.cb_ingrediente);
            tvIngrediente = itemView.findViewById(R.id.tv_ingrediente);

            // Listener para el checkbox
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

        public void bind(Ingrediente ingrediente) {
            tvIngrediente.setText(ingrediente.getIngredienteCompleto());
            cbIngrediente.setChecked(false);
            tvIngrediente.setPaintFlags(tvIngrediente.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        }
    }
}