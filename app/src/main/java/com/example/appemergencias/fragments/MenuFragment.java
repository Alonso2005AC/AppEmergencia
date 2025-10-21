package com.example.appemergencias.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.appemergencias.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import cz.msebera.android.httpclient.Header;

public class MenuFragment extends Fragment {

    // --- Elementos del formulario ---
    private Spinner spinnerTipo;
    private Spinner spinnerPrioridad; // NUEVO SPINNER
    private EditText etIncidentDetails;
    private Button btnNext;

    // Adjuntos
    // etAudio y etVideo ya no se referencian
    private EditText etImage;

    private SharedPreferences prefs;

    // URL de tu API
    private static final String API_URL_REGISTRO = "http://10.0.2.2/emergencias_api/emergencia/insert_emergencia.php";

    public MenuFragment() {
        // Constructor público requerido
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefs = requireActivity().getSharedPreferences("SESSION", Context.MODE_PRIVATE);

        // 1. Inicializar Vistas
        spinnerTipo = view.findViewById(R.id.spinner_tipo);
        spinnerPrioridad = view.findViewById(R.id.spinner_prioridad); // INICIALIZACIÓN
        etIncidentDetails = view.findViewById(R.id.et_incident_details);
        btnNext = view.findViewById(R.id.btn_next);

        // Inicializar campo de imagen restante
        etImage = view.findViewById(R.id.et_image);

        // 2. Configurar Spinners
        setupSpinners();

        // 3. Configurar Listener del Botón
        btnNext.setOnClickListener(v -> registrarEmergencia());
    }

    private void setupSpinners() {
        // Spinner Tipo de Emergencia
        String[] tiposEmergencia = new String[]{
                "Seleccione un tipo...",
                "Incendio 🔥",
                "Accidente de tránsito 💥",
                "Actividad sospechosa 🚨",
                "Emergencia médica 🚑",
                "Desastre natural 🌊",
                "Otro ❔"
        };
        ArrayAdapter<String> tipoAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, tiposEmergencia);
        tipoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipo.setAdapter(tipoAdapter);

        // Spinner Prioridad
        String[] prioridades = new String[]{
                "Seleccione Prioridad...", // Opción inicial para forzar selección
                "Baja",
                "Media",
                "Alta"
        };
        ArrayAdapter<String> prioridadAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, prioridades);
        prioridadAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPrioridad.setAdapter(prioridadAdapter);
    }

    private void registrarEmergencia() {
        // 1. Obtener datos
        String tipoSeleccionado = spinnerTipo.getSelectedItem().toString();
        String prioridadSeleccionada = spinnerPrioridad.getSelectedItem().toString().toLowerCase(); // A minúsculas para coincidir con el ENUM de la DB
        String descripcionCompleta = etIncidentDetails.getText().toString().trim();
        String idUsuario = prefs.getString("id_usuario", null);

        // 2. Separar Descripción y Ubicación
        String descripcion = descripcionCompleta;
        String ubicacion = descripcionCompleta.split("\n")[0];
        if (ubicacion.length() > 255) {
            ubicacion = ubicacion.substring(0, 255);
        }

        // 3. Validación
        if (spinnerTipo.getSelectedItemPosition() == 0) {
            Toast.makeText(requireContext(), "Por favor, seleccione el tipo de emergencia.", Toast.LENGTH_LONG).show();
            return;
        }
        if (spinnerPrioridad.getSelectedItemPosition() == 0) {
            Toast.makeText(requireContext(), "Por favor, seleccione la prioridad.", Toast.LENGTH_LONG).show();
            return;
        }
        if (descripcionCompleta.isEmpty()) {
            Toast.makeText(requireContext(), "Por favor, agregue el detalle del incidente y la ubicación.", Toast.LENGTH_LONG).show();
            return;
        }
        if (idUsuario == null || idUsuario.isEmpty()) {
            Toast.makeText(requireContext(), "Error de sesión. No se encontró ID de usuario.", Toast.LENGTH_LONG).show();
            return;
        }

        // 4. Preparación de Parámetros
        RequestParams params = new RequestParams();
        params.put("id_usuario", idUsuario);
        params.put("tipo", tipoSeleccionado);
        params.put("descripcion", descripcion);
        params.put("ubicacion", ubicacion);
        params.put("prioridad", prioridadSeleccionada); // CAMPO AGREGADO
        params.put("foto", "");

        // 5. Ejecución del POST
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(API_URL_REGISTRO, params, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String respuesta = new String(responseBody);
                if (respuesta.contains("success")) {
                    Toast.makeText(requireContext(), "✅ Emergencia registrada con éxito.", Toast.LENGTH_LONG).show();
                    limpiarFormulario();

                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.nav_host_fragment, new HomeFragment())
                            .commit();

                } else {
                    Toast.makeText(requireContext(), "❌ Error al registrar: " + respuesta, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(requireContext(), "⚠️ Error de conexión al servidor. Código: (" + statusCode + ")", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void limpiarFormulario() {
        spinnerTipo.setSelection(0);
        spinnerPrioridad.setSelection(0);
        etIncidentDetails.setText("");
    }
}