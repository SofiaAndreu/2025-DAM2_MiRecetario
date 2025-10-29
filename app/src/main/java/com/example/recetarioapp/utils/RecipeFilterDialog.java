package com.example.recetarioapp.utils;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import com.example.recetarioapp.R;
import com.example.recetarioapp.models.Receta;
import com.example.recetarioapp.viewmodels.RecetaViewModel;
import com.google.android.material.slider.Slider;
import java.util.ArrayList;
import java.util.List;

public class RecipeFilterDialog {

    public interface OnFilterAppliedListener {
        void onFilterApplied(List<Receta> recetasFiltradas);
    }

    public static void show(Context context, RecetaViewModel viewModel,
                            OnFilterAppliedListener listener) {
        View dialogView = LayoutInflater.from(context)
                .inflate(R.layout.dialog_filtros, null);

        AutoCompleteTextView spinnerCategoria = dialogView.findViewById(R.id.spinner_categoria);
        AutoCompleteTextView spinnerDificultad = dialogView.findViewById(R.id.spinner_dificultad);
        Slider sliderTiempo = dialogView.findViewById(R.id.slider_tiempo);
        TextView tvTiempoSeleccionado = dialogView.findViewById(R.id.tv_tiempo_seleccionado);

        // Setup dropdowns...
        setupCategorias(context, spinnerCategoria);
        setupDificultades(context, spinnerDificultad);

        sliderTiempo.addOnChangeListener((slider, value, fromUser) -> {
            tvTiempoSeleccionado.setText(
                    value == 0 ? "Sin límite" : ((int) value + " minutos"));
        });

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .create();

        dialogView.findViewById(R.id.btn_aplicar).setOnClickListener(v -> {
            // Aplicar filtros...
            dialog.dismiss();
        });

        dialog.show();
    }

    private static void setupCategorias(Context context, AutoCompleteTextView view) {
        String[] categorias = {
                "Todas",
                context.getString(R.string.categoria_postres),
                context.getString(R.string.categoria_principales),
                // ... más categorías
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_dropdown_item_1line, categorias);
        view.setAdapter(adapter);
        view.setText("Todas", false);
    }

    private static void setupDificultades(Context context, AutoCompleteTextView view) {
        String[] dificultades = {
                "Todas",
                context.getString(R.string.dificultad_facil),
                context.getString(R.string.dificultad_medio),
                context.getString(R.string.dificultad_dificil)
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_dropdown_item_1line, dificultades);
        view.setAdapter(adapter);
        view.setText("Todas", false);
    }
}
