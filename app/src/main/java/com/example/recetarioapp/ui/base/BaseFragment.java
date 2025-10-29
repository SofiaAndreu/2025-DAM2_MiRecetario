package com.example.recetarioapp.ui.base;


import android.content.Intent;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.recetarioapp.models.Receta;
import com.example.recetarioapp.ui.RecipeDetailActivity;
import com.example.recetarioapp.viewmodels.RecetaViewModel;

/**
 * Fragment base con funcionalidad común para todos los fragments
 */
public abstract class BaseFragment extends Fragment {

    protected RecetaViewModel viewModel;

    /**
     * Inicializa el ViewModel compartido
     */
    protected void initViewModel() {
        viewModel = new ViewModelProvider(requireActivity()).get(RecetaViewModel.class);
    }

    /**
     * Observa el estado de la UI y muestra mensajes
     */
    protected void observeUiState() {
        // Observa mensajes de éxito
        viewModel.getMensajeExito().observe(getViewLifecycleOwner(), mensaje -> {
            if (mensaje != null) {
                showToast(mensaje);
                viewModel.limpiarMensajesE();
            }
        });

        // Observa mensajes de error
        viewModel.getMensajeError().observe(getViewLifecycleOwner(), mensaje -> {
            if (mensaje != null) {
                showToast(mensaje);
                // No llamamos a limpiarEstado() porque no existe en tu ViewModel
                // En su lugar, limpiamos los mensajes
                viewModel.limpiarMensajesE();
            }
        });

        // Observa el progreso de subida (solo para logging o actualización de UI si es necesario)
        viewModel.getProgresoSubida().observe(getViewLifecycleOwner(), progreso -> {
            if (progreso != null) {
                // Aquí puedes manejar el progreso si necesitas mostrar una barra de progreso
                // Por ejemplo: actualizar una ProgressBar en algún fragment
            }
        });
    }

    /**
     * Abre el detalle de una receta
     */
    protected void openRecipeDetail(Receta receta) {
        Intent intent = new Intent(getContext(), RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECETA_ID, receta.getId());
        startActivity(intent);
    }

    /**
     * Alterna el estado de favorito
     */
    protected void toggleFavorite(Receta receta, boolean isFavorite) {
        viewModel.marcarFavorita(receta.getId(), isFavorite);
        showToast(isFavorite ? "Añadida a favoritos" : "Quitada de favoritos");
    }

    /**
     * Muestra un Toast
     */
    protected void showToast(String mensaje) {
        Toast.makeText(getContext(), mensaje, Toast.LENGTH_SHORT).show();
    }

    /**
     * Muestra un Toast largo
     */
    protected void showToastLong(String mensaje) {
        Toast.makeText(getContext(), mensaje, Toast.LENGTH_LONG).show();
    }
}