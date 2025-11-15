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
import java.util.List;

//Helper para mostrar diálogos de filtrado avanzado de recetas
// - Interfaz de usuario para aplicar múltiples filtros
// - Filtrado por categoría, dificultad y tiempo de preparación
// - Selectores desplegables para categorías y dificultades
// - Slider para tiempo máximo de preparación
// - Integración con ViewModel para aplicar filtros
public class RecipeFilterDialog {

    //Interfaz para manejar la aplicación de filtros
    public interface OnFilterAppliedListener {
        //Se llama cuando se aplican los filtros seleccionados
        void onFilterApplied(List<Receta> recetasFiltradas);
    }

    //Muestra un diálogo de filtrado avanzado para recetas
    public static void show(Context context, RecetaViewModel viewModel,
                            OnFilterAppliedListener listener) {
        //Inflar la vista del diálogo desde el layout
        View dialogView = LayoutInflater.from(context)
                .inflate(R.layout.dialog_filtros, null);

        //Obtener referencias a los controles de filtrado
        AutoCompleteTextView spinnerCategoria = dialogView.findViewById(R.id.spinner_categoria);
        AutoCompleteTextView spinnerDificultad = dialogView.findViewById(R.id.spinner_dificultad);
        Slider sliderTiempo = dialogView.findViewById(R.id.slider_tiempo);
        TextView tvTiempoSeleccionado = dialogView.findViewById(R.id.tv_tiempo_seleccionado);

        //Configurar los dropdowns de categoría y dificultad
        setupCategorias(context, spinnerCategoria);
        setupDificultades(context, spinnerDificultad);

        //Configurar listener para el slider de tiempo
        sliderTiempo.addOnChangeListener((slider, value, fromUser) -> {
            //Actualizar texto que muestra el tiempo seleccionado
            tvTiempoSeleccionado.setText(
                    value == 0 ? "Sin límite" : ((int) value + " minutos"));
        });

        //Crear y configurar el diálogo
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .create();

        //Configurar listener para el botón aplicar
        dialogView.findViewById(R.id.btn_aplicar).setOnClickListener(v -> {
            //Obtener valores seleccionados de los filtros
            String categoriaSeleccionada = spinnerCategoria.getText().toString();
            String dificultadSeleccionada = spinnerDificultad.getText().toString();
            int tiempoMax = (int) sliderTiempo.getValue();

            //Aplicar filtros según la selección del usuario
            if (!categoriaSeleccionada.equals("Todas")) {
                //Filtrar por categoría seleccionada
                viewModel.filtrarPorCategoria(categoriaSeleccionada)
                        .observeForever(listener::onFilterApplied);
            } else if (!dificultadSeleccionada.equals("Todas")) {
                //Filtrar por dificultad seleccionada
                viewModel.filtrarPorDificultad(dificultadSeleccionada)
                        .observeForever(listener::onFilterApplied);
            } else if (tiempoMax > 0) {
                //Filtrar por tiempo máximo de preparación
                viewModel.filtrarPorTiempo(tiempoMax)
                        .observeForever(listener::onFilterApplied);
            }

            //Cerrar el diálogo después de aplicar filtros
            dialog.dismiss();
        });

        //Mostrar el diálogo
        dialog.show();
    }

    //Configura el dropdown de categorías con las opciones disponibles
    private static void setupCategorias(Context context, AutoCompleteTextView view) {
        //Definir array de categorías disponibles
        String[] categorias = {
                "Todas", //Opción por defecto (sin filtro)
                context.getString(R.string.categoria_postres),
                context.getString(R.string.categoria_principales),
                //... más categorías según sea necesario
        };

        //Crear adaptador para el dropdown
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_dropdown_item_1line, categorias);
        view.setAdapter(adapter);
        view.setText("Todas", false); //Establecer valor por defecto
    }

    //Configura el dropdown de dificultades con las opciones disponibles
    private static void setupDificultades(Context context, AutoCompleteTextView view) {
        //Definir array de dificultades disponibles
        String[] dificultades = {
                "Todas", //Opción por defecto (sin filtro)
                context.getString(R.string.dificultad_facil),
                context.getString(R.string.dificultad_medio),
                context.getString(R.string.dificultad_dificil)
        };

        //Crear adaptador para el dropdown
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_dropdown_item_1line, dificultades);
        view.setAdapter(adapter);
        view.setText("Todas", false); //Establecer valor por defecto
    }
}