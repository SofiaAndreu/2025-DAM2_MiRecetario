package com.example.recetarioapp.ui;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.recetarioapp.R;
import com.google.android.material.button.MaterialButton;

//Activity "Acerca de" que muestra información sobre la aplicación
// - Versión actual de la aplicación
// - Información del desarrollador
// - Enlaces de contacto (si están implementados)
// - Créditos y licencias
public class AboutActivity extends AppCompatActivity {

    //Método principal de inicialización
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        //Configurar botón de volver - cierra la activity
        MaterialButton botonVolver = findViewById(R.id.btn_volver);
        botonVolver.setOnClickListener(v -> finish());

        //Mostrar versión real de la aplicación
        TextView textoVersion = findViewById(R.id.tv_version);
        try {
            //Obtener nombre de versión desde el manifest
            String nombreVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            textoVersion.setText("Versión " + nombreVersion);
        } catch (Exception excepcion) {
            //Valor por defecto en caso de error
            textoVersion.setText("Versión 1.0");
        }
    }
}