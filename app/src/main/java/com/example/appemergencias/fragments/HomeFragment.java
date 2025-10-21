package com.example.appemergencias.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.appemergencias.R;

public class HomeFragment extends Fragment {

    private TextView tvWelcomeMessage;
    private TextView tvTotalReports;
    private TextView tvPendingStatus;
    private SharedPreferences prefs;

    public HomeFragment() {
        // Constructor público vacío requerido
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializar SharedPreferences
        prefs = requireActivity().getSharedPreferences("SESSION", Context.MODE_PRIVATE);

        // Inicializar Vistas
        tvWelcomeMessage = view.findViewById(R.id.tv_welcome_message);
        tvTotalReports = view.findViewById(R.id.tv_total_reports);
        tvPendingStatus = view.findViewById(R.id.tv_pending_status);

        // -----------------------------------------------------
        // 1. Personalizar el mensaje de bienvenida
        // -----------------------------------------------------
        String nombreUsuario = prefs.getString("usuario", "Usuario");
        tvWelcomeMessage.setText("Bienvenido(a), " + nombreUsuario);

        // -----------------------------------------------------
        // 2. Cargar datos del Dashboard (ACTUALIZAR CON LÓGICA REAL)
        // -----------------------------------------------------

        // Por ahora, solo mostramos 0. Aquí iría la llamada a la API
        // para obtener el resumen de reportes del usuario.

        tvTotalReports.setText("Reportes Enviados: 0");
        tvPendingStatus.setText("Emergencias Pendientes: 0");

        // Ejemplo de cómo cargarías datos reales más tarde:
        // loadDashboardData(prefs.getString("id_usuario", ""));
    }

    // (Opcional) Crea esta función si planeas cargar los datos reales del usuario más adelante
    /*
    private void loadDashboardData(String userId) {
        // Implementar aquí la llamada a Volley o Retrofit para obtener el resumen
        // y actualizar tvTotalReports y tvPendingStatus.
    }
    */
}