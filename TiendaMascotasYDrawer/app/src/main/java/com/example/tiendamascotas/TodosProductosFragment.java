package com.example.tiendamascotas;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.tiendamascotas.adapter.ProductosAdapter;
import com.example.tiendamascotas.model.Producto;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TodosProductosFragment extends Fragment {
    private RecyclerView recyclerView;
    private List<Producto> todosProductos;
    private ProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_productos, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewProductos);
        todosProductos = new ArrayList<>();

        cargarTodosLosProductos();

        return view;
    }

    private void cargarTodosLosProductos() {
        mostrarProgressDialog();

        StringRequest request = new StringRequest(
                Request.Method.GET,
                ApiConfig.API_PRODUCTOS,
                response -> {
                    try {
                        procesarTodosProductos(response);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        mostrarError("Error al procesar productos");
                    }
                },
                error -> {
                    ocultarProgressDialog();
                    mostrarError("Error de conexión: " + error.getMessage());
                }
        );

        Volley.newRequestQueue(requireContext()).add(request);
    }

    private void procesarTodosProductos(String response) throws JSONException {
        JSONArray jsonArray = new JSONArray(response);
        todosProductos.clear();

        // Recorrer todas las categorías y extraer todos los productos
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject categoria = jsonArray.getJSONObject(i);
            JSONArray productosArray = categoria.getJSONArray("productos");

            for (int j = 0; j < productosArray.length(); j++) {
                JSONObject productoJson = productosArray.getJSONObject(j);

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
                todosProductos.add(producto);
            }
        }

        setupRecyclerView();
        ocultarProgressDialog();
        Toast.makeText(getContext(), "Total: " + todosProductos.size() + " productos", Toast.LENGTH_SHORT).show();
    }

    private void setupRecyclerView() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                ProductosAdapter adapter = new ProductosAdapter(todosProductos, this::onProductoClick);
                recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
                recyclerView.setAdapter(adapter);
            });
        }
    }

    private void onProductoClick(Producto producto) {
        DetalleImagenFragment fragment = DetalleImagenFragment.newInstance(producto.getImagenUrl());
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void mostrarProgressDialog() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                progressDialog = new ProgressDialog(getContext());
                progressDialog.setMessage("Cargando productos...");
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