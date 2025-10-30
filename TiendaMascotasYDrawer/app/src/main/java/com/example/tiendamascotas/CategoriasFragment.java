package com.example.tiendamascotas;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.tiendamascotas.model.Categoria;
import com.example.tiendamascotas.model.Producto;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CategoriasFragment extends Fragment {
    private Spinner spinnerCategorias;
    private List<Categoria> categorias;
    private ProgressDialog progressDialog;
    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_categorias, container, false);

        spinnerCategorias = rootView.findViewById(R.id.spinnerCategorias);
        categorias = new ArrayList<>();

        setupSpinner();
        cargarDatosDesdePHP();

        return rootView;
    }

    private void cargarDatosDesdePHP() {
        mostrarProgressDialog();

        StringRequest request = new StringRequest(
                Request.Method.GET,
                ApiConfig.API_PRODUCTOS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            procesarRespuestaCompleta(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            mostrarError("Error en formato de respuesta");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        ocultarProgressDialog();
                        mostrarError("Error de conexión: " + error.getMessage());
                    }
                }
        );

        Volley.newRequestQueue(requireContext()).add(request);
    }

    private void procesarRespuestaCompleta(String response) throws JSONException {
        JSONArray jsonArray = new JSONArray(response);
        categorias.clear();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject item = jsonArray.getJSONObject(i);
            String nombreCategoria = item.getString("categoria");
            JSONArray productosArray = item.getJSONArray("productos");
            List<Producto> productos = procesarProductos(productosArray);
            Categoria categoria = new Categoria(nombreCategoria, productos);
            categorias.add(categoria);
        }

        actualizarSpinner();
        ocultarProgressDialog();
        Toast.makeText(getContext(), "Datos cargados: " + categorias.size() + " categorías", Toast.LENGTH_SHORT).show();
    }

    private List<Producto> procesarProductos(JSONArray productosArray) throws JSONException {
        List<Producto> productos = new ArrayList<>();

        for (int i = 0; i < productosArray.length(); i++) {
            JSONObject productoJson = productosArray.getJSONObject(i);
            int id = productoJson.getInt("id");
            String nombre = productoJson.getString("nombre");
            String descripcion = productoJson.getString("descripcion");
            String precio = productoJson.getString("precio");
            String imagenNombre = productoJson.getString("imagen_url");

            String imagenUrl;
            if (imagenNombre.startsWith("http://") || imagenNombre.startsWith("https://")) {
                imagenUrl = imagenNombre;
            } else {
                imagenUrl = ApiConfig.BASE_URL + "imagenes/" + imagenNombre;
            }

            Producto producto = new Producto(id, nombre, descripcion, precio, imagenUrl);
            productos.add(producto);
        }

        return productos;
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, new ArrayList<>());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategorias.setAdapter(adapter);

        spinnerCategorias.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0 && categorias.size() > position - 1) {
                    Categoria categoriaSeleccionada = categorias.get(position - 1);
                    cargarFragmentoProductos(categoriaSeleccionada);
                } else if (position == 0) {
                    limpiarFragmento();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void actualizarSpinner() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerCategorias.getAdapter();
                adapter.clear();
                adapter.add("Selecciona una categoría");

                for (Categoria categoria : categorias) {
                    adapter.add(categoria.getNombre());
                }

                adapter.notifyDataSetChanged();
            });
        }
    }

    private void cargarFragmentoProductos(Categoria categoria) {
        ProductosFragment fragment = ProductosFragment.newInstance(categoria);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerCategorias, fragment)
                .commit();
    }

    private void limpiarFragmento() {
        Fragment fragment = getParentFragmentManager().findFragmentById(R.id.fragmentContainerCategorias);
        if (fragment != null) {
            getParentFragmentManager().beginTransaction()
                    .remove(fragment)
                    .commit();
        }
    }

    private void mostrarProgressDialog() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                progressDialog = new ProgressDialog(getContext());
                progressDialog.setMessage("Cargando categorías...");
                progressDialog.setCancelable(false);
                progressDialog.show();
            });
        }
    }

    private void ocultarProgressDialog() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            });
        }
    }

    private void mostrarError(String mensaje) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), mensaje, Toast.LENGTH_LONG).show();
            });
        }
    }
}