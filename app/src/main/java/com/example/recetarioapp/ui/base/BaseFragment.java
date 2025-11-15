package com.example.recetarioapp.ui.base;

import android.content.Intent;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.recetarioapp.models.Receta;
import com.example.recetarioapp.ui.RecipeDetailActivity;
import com.example.recetarioapp.viewmodels.RecetaViewModel;

//Fragment base con funcionalidad común para todos los fragments
// - Inicialización de ViewModel compartido
// - Observación de estados de UI
// - Navegación a detalles de recetas
// - Gestión de favoritos
// - Utilidades para mostrar mensajes
public abstract class BaseFragment extends Fragment {

    //ViewModel compartido para operaciones con recetas
    protected RecetaViewModel viewModel;

    //Inicializa el ViewModel compartido entre fragments
    protected void initViewModel() {
        viewModel = new ViewModelProvider(requireActivity()).get(RecetaViewModel.class);
    }

    //Observa el estado de la UI y muestra mensajes al usuario
    protected void observeUiState() {
        //Observar mensajes de éxito - mostrar Toast y limpiar
        viewModel.getMensajeExito().observe(getViewLifecycleOwner(), mensaje -> {
            if (mensaje != null) {
                showToast(mensaje);
                viewModel.limpiarMensajesE();
            }
        });

        //Observar mensajes de error - mostrar Toast y limpiar
        viewModel.getMensajeError().observe(getViewLifecycleOwner(), mensaje -> {
            if (mensaje != null) {
                showToast(mensaje);
                viewModel.limpiarMensajesE();
            }
        });

        //Observar progreso de subida - para actualización de UI si es necesario
        viewModel.getProgresoSubida().observe(getViewLifecycleOwner(), progreso -> {
            if (progreso != null) {
                //Aquí se puede manejar el progreso para mostrar barra de progreso
            }
        });
    }

    //Abre la Activity de detalle para una receta específica
    protected void openRecipeDetail(Receta receta) {
        Intent intent = new Intent(getContext(), RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECETA_ID, receta.getId());
        startActivity(intent);
    }

    //Alterna el estado de favorito de una receta
    protected void toggleFavorite(Receta receta, boolean isFavorite) {
        viewModel.marcarFavorita(receta.getId(), isFavorite);
        showToast(isFavorite ? "Añadida a favoritos" : "Quitada de favoritos");
    }

    //Muestra un mensaje Toast de corta duración
    protected void showToast(String mensaje) {
        Toast.makeText(getContext(), mensaje, Toast.LENGTH_SHORT).show();
    }
}