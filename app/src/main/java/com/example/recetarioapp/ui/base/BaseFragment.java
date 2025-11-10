package com.example.recetarioapp.ui.base;

import android.content.Intent;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.recetarioapp.models.Receta;
import com.example.recetarioapp.ui.RecipeDetailActivity;
import com.example.recetarioapp.viewmodels.RecetaViewModel;

/**
 * Fragment base con funcionalidad común para todos los fragments.
 *
 * Proporciona funcionalidades compartidas:
 * - Inicialización de ViewModel compartido
 * - Observación de estados de UI
 * - Navegación a detalles de recetas
 * - Gestión de favoritos
 * - Utilidades para mostrar mensajes
 */
public abstract class BaseFragment extends Fragment {

    // ViewModel compartido para operaciones con recetas
    protected RecetaViewModel viewModel;

    /**
     * Inicializa el ViewModel compartido entre fragments.
     * Obtiene la instancia del ViewModel desde la Activity principal.
     */
    protected void initViewModel() {
        viewModel = new ViewModelProvider(requireActivity()).get(RecetaViewModel.class);
    }

    /**
     * Observa el estado de la UI y muestra mensajes al usuario.
     * Maneja mensajes de éxito, error y progreso de operaciones.
     */
    protected void observeUiState() {
        // Observar mensajes de éxito - mostrar Toast y limpiar
        viewModel.getMensajeExito().observe(getViewLifecycleOwner(), mensaje -> {
            if (mensaje != null) {
                showToast(mensaje);
                viewModel.limpiarMensajesE();
            }
        });

        // Observar mensajes de error - mostrar Toast y limpiar
        viewModel.getMensajeError().observe(getViewLifecycleOwner(), mensaje -> {
            if (mensaje != null) {
                showToast(mensaje);
                viewModel.limpiarMensajesE();
            }
        });

        // Observar progreso de subida - para actualización de UI si es necesario
        viewModel.getProgresoSubida().observe(getViewLifecycleOwner(), progreso -> {
            if (progreso != null) {
                // Aquí se puede manejar el progreso para mostrar barra de progreso
            }
        });
    }

    /**
     * Abre la Activity de detalle para una receta específica.
     *
     * @param receta Receta de la que se mostrarán los detalles
     */
    protected void openRecipeDetail(Receta receta) {
        Intent intent = new Intent(getContext(), RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECETA_ID, receta.getId());
        startActivity(intent);
    }

    /**
     * Alterna el estado de favorito de una receta.
     * Actualiza el estado en la base de datos y muestra feedback.
     *
     * @param receta Receta a modificar
     * @param isFavorite Nuevo estado de favorito (true para favorito, false para quitar)
     */
    protected void toggleFavorite(Receta receta, boolean isFavorite) {
        viewModel.marcarFavorita(receta.getId(), isFavorite);
        showToast(isFavorite ? "Añadida a favoritos" : "Quitada de favoritos");
    }

    /**
     * Muestra un mensaje Toast de corta duración.
     *
     * @param mensaje Mensaje a mostrar al usuario
     */
    protected void showToast(String mensaje) {
        Toast.makeText(getContext(), mensaje, Toast.LENGTH_SHORT).show();
    }

    /**
     * Muestra un mensaje Toast de larga duración.
     *
     * @param mensaje Mensaje a mostrar al usuario
     */
    protected void showToastLong(String mensaje) {
        Toast.makeText(getContext(), mensaje, Toast.LENGTH_LONG).show();
    }
}