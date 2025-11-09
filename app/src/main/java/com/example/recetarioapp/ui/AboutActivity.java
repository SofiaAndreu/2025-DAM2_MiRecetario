package com.example.recetarioapp.ui;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.recetarioapp.R;
import com.google.android.material.button.MaterialButton;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);

        // Configurar botón volver
        MaterialButton btnVolver = findViewById(R.id.btn_volver);
        btnVolver.setOnClickListener(v -> finish());

        // Mostrar versión real de la app
        TextView tvVersion = findViewById(R.id.tv_version);
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            tvVersion.setText("Versión " + versionName);
        } catch (Exception e) {
            tvVersion.setText("Versión 1.0");
        }
    }

}