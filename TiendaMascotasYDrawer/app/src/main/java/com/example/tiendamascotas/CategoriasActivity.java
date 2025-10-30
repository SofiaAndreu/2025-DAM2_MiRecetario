package com.example.tiendamascotas;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
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

public class CategoriasActivity extends AppCompatActivity {
    private Spinner spinnerCategorias;
    private List<Categoria> categorias;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categorias);

        spinnerCategorias = findViewById(R.id.spinnerCategorias);
        categorias = new ArrayList<>();

        setupSpinner();
        cargarDatosDesdePHP();
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

        Volley.newRequestQueue(this).add(request);
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

        // Mostrar mensaje de éxito
        Toast.makeText(this, "Datos cargados: " + categorias.size() + " categorías", Toast.LENGTH_SHORT).show();
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

            // Verificar si la imagen ya es una URL completa
            String imagenUrl;
            if (imagenNombre.startsWith("http://") || imagenNombre.startsWith("https://")) {
                // Ya es una URL completa, usar tal cual
                imagenUrl = imagenNombre;
                android.util.Log.d("TiendaMascotas", "URL completa detectada: " + imagenUrl);
            } else {
                // Es solo el nombre del archivo, construir URL completa
                imagenUrl = ApiConfig.BASE_URL + "imagenes/" + imagenNombre;
                android.util.Log.d("TiendaMascotas", "URL construida: " + imagenUrl);
            }

            Producto producto = new Producto(id, nombre, descripcion, precio, imagenUrl);
            productos.add(producto);
        }

        return productos;
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
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
                    // Si selecciona "Selecciona una categoría", limpiar fragmento
                    limpiarFragmento();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void actualizarSpinner() {
        runOnUiThread(() -> {
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerCategorias.getAdapter();
            adapter.clear();
            adapter.add("Selecciona una categoría");

            for (Categoria categoria : categorias) {
                adapter.add(categoria.getNombre());
            }

            adapter.notifyDataSetChanged();
        });
    }

    private void cargarFragmentoProductos(Categoria categoria) {
        ProductosFragment fragment = ProductosFragment.newInstance(categoria);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void limpiarFragmento() {
        // Remover cualquier fragmento actual
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .remove(fragment)
                    .commit();
        }
    }

    private void mostrarProgressDialog() {
        runOnUiThread(() -> {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Cargando categorías y productos...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        });
    }

    private void ocultarProgressDialog() {
        runOnUiThread(() -> {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        });
    }

    private void mostrarError(String mensaje) {
        runOnUiThread(() -> {
            Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
        });
    }
}