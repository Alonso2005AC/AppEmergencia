package com.example.appemergencias.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.appemergencias.R;

public class HomeAdminFragment extends Fragment {

    private TextView tvWelcomeAdmin;
    private Button btnVerReportes, btnCerrarSesion;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflamos el layout nuevo con fondo, overlay y CardView
        return inflater.inflate(R.layout.fragment_home_admin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Referencias a los elementos del layout
        tvWelcomeAdmin = view.findViewById(R.id.tvWelcomeAdmin);
        btnVerReportes = view.findViewById(R.id.btnVerReportesAdmin);
        btnCerrarSesion = view.findViewById(R.id.btnCerrarSesionAdmin);

        // Mostrar nombre del admin desde SharedPreferences
        SharedPreferences prefs = requireActivity().getSharedPreferences("SESSION", getActivity().MODE_PRIVATE);
        String nombreAdmin = prefs.getString("nombre_usuario", "Admin");
        tvWelcomeAdmin.setText("¡Bienvenido, " + nombreAdmin + "!");

        // Botón Ver Reportes
        btnVerReportes.setOnClickListener(v -> {
            // Cargar fragmento MisReportesAdminFragment dentro del contenedor de la Activity
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.admin_fragment_container, new MisReportesAdminFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Botón Cerrar Sesión
        btnCerrarSesion.setOnClickListener(v -> {
            // Limpiar sesión
            prefs.edit().clear().apply();
            // Cargar fragmento LoginAdminFragment
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.admin_fragment_container, new LoginAdminFragment())
                    .commit();
            Toast.makeText(requireContext(), "Sesión cerrada", Toast.LENGTH_SHORT).show();
        });
    }
}
