package com.example.appemergencias.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.appemergencias.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class MisReportesFragment extends Fragment {

    private ListView listViewReportes;
    private Spinner spinnerPrioridad;
    private ArrayList<Reporte> listaObjetosReportesCompleta;
    private SharedPreferences prefs;
    private static final String TAG = "API_REPORTES";

    // CLASE REPORTANTE: DEBE ser pública y estática
    public static class Reporte {
        String id;
        String tipo;
        String descripcion;
        String fecha;
        String nombreReportante;
        String prioridad;

        public Reporte(String id, String tipo, String descripcion, String fecha, String nombreReportante, String prioridad) {
            this.id = id;
            this.tipo = tipo;
            this.descripcion = descripcion;
            this.fecha = fecha;
            this.nombreReportante = nombreReportante;
            this.prioridad = prioridad;
        }

        // toString() AHORA SIN EMOJIS, SOLO TEXTO CON PRIORIDAD
        public String toString(SharedPreferences prefs) {
            String rolUsuario = prefs.getString("rol", "usuario");

            // Formatear la prioridad (Alta, Media, Baja)
            String prioridadFormateada = "";
            if (this.prioridad != null && !this.prioridad.isEmpty()) {
                prioridadFormateada = this.prioridad.substring(0, 1).toUpperCase() + this.prioridad.substring(1).toLowerCase();
            }

            StringBuilder displayString = new StringBuilder();

            if (rolUsuario.equals("administrador")) {
                // Admin: Tipo (Por: Nombre) (Prioridad)
                displayString.append(this.tipo)
                        .append(" (Por: ").append(this.nombreReportante).append(")")
                        .append(" (").append(prioridadFormateada).append(")");
            } else {
                // Usuario: #ID: Tipo (Prioridad)
                displayString.append("#").append(id).append(": ")
                        .append(this.tipo)
                        .append(" (").append(prioridadFormateada).append(")");
            }
            return displayString.toString();
        }

        @NonNull
        @Override
        public String toString() {
            return tipo + " (" + prioridad + ")";
        }
    }

    // ======================================================================
    // CLASE ADAPTADOR ANIDADA (Aplica el color según la prioridad)
    // ======================================================================
    private class ListaReporteAdapter extends ArrayAdapter<Reporte> {

        public ListaReporteAdapter(Context context, int resource, List<Reporte> objects) {
            super(context, resource, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            }

            Reporte reporte = getItem(position);
            TextView tvReporte = convertView.findViewById(android.R.id.text1);

            if (reporte != null && tvReporte != null) {

                // 1. Establecer el texto sin emojis
                tvReporte.setText(reporte.toString(prefs));

                // 2. Lógica de Colores Condicionales
                int colorResId;
                String prioridad = reporte.prioridad.toLowerCase();

                if (prioridad.contains("alta")) {
                    colorResId = Color.RED; // ROJO
                } else if (prioridad.contains("media")) {
                    colorResId = Color.parseColor("#FFA500"); // NARANJA
                } else if (prioridad.contains("baja")) {
                    colorResId = Color.parseColor("#388E3C"); // VERDE
                } else {
                    colorResId = Color.BLACK;
                }

                // 3. Aplicar el color a la letra
                tvReporte.setTextColor(colorResId);
            }

            return convertView;
        }
    }
    // ======================================================================


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mis_reportes, container, false);
        listViewReportes = view.findViewById(R.id.listViewReportes);
        spinnerPrioridad = view.findViewById(R.id.spinnerPrioridad);
        prefs = requireActivity().getSharedPreferences("SESSION", Context.MODE_PRIVATE);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupSpinner();
        cargarMisReportes();
    }

    private void setupSpinner() {
        String[] prioridades = new String[]{"Todas las Prioridades", "Alta", "Media", "Baja"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, prioridades);
        spinnerPrioridad.setAdapter(adapter);

        spinnerPrioridad.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String prioridadSeleccionada = parent.getItemAtPosition(position).toString();
                if (listaObjetosReportesCompleta != null) {
                    filtrarLista(prioridadSeleccionada);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void filtrarLista(String prioridad) {
        if (prioridad.equals("Todas las Prioridades")) {
            actualizarListView(listaObjetosReportesCompleta);
        } else {
            ArrayList<Reporte> listaFiltrada = new ArrayList<>();
            for (Reporte r : listaObjetosReportesCompleta) {
                if (r.prioridad.equalsIgnoreCase(prioridad)) {
                    listaFiltrada.add(r);
                }
            }
            actualizarListView(listaFiltrada);
        }
    }

    private void setupListViewClickListener() {
        listViewReportes.setOnItemClickListener((parent, view, position, id) -> {
            Reporte reporteSeleccionado = (Reporte) parent.getItemAtPosition(position);
            mostrarDetalleReporte(reporteSeleccionado);
        });
    }

    private void mostrarDetalleReporte(Reporte r) {
        String rolUsuario = prefs.getString("rol", "usuario");
        String reportanteInfo = "";

        if (rolUsuario.equals("administrador")) {
            reportanteInfo = "Reportado por: " + r.nombreReportante + "\n";
        }

        String prioridadFormateada = r.prioridad != null ? r.prioridad.substring(0, 1).toUpperCase() + r.prioridad.substring(1).toLowerCase() : "";

        String mensaje = reportanteInfo +
                "Prioridad: " + prioridadFormateada + "\n" +
                "Tipo de Incidente: " + r.tipo + "\n" +
                "Fecha de Reporte: " + r.fecha + "\n\n" +
                "Descripción Completa:\n" + r.descripcion;

        new AlertDialog.Builder(requireContext())
                .setTitle("Detalle del Reporte #" + r.id)
                .setMessage(mensaje)
                .setPositiveButton("Cerrar", null)
                .show();
    }

    private void cargarMisReportes() {
        String idUsuario = prefs.getString("id_usuario", null);
        String rolUsuario = prefs.getString("rol", null);

        if (idUsuario == null || rolUsuario == null) {
            Toast.makeText(requireContext(), "Error: No se pudo obtener la sesión (ID o Rol).", Toast.LENGTH_LONG).show();
            return;
        }

        String url = "http://10.0.2.2/emergencias_api/emergencia/listar_reportes.php";
        RequestParams params = new RequestParams();
        params.put("id_usuario", idUsuario);
        params.put("rol", rolUsuario);

        AsyncHttpClient client = new AsyncHttpClient();
        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String jsonResponse = new String(responseBody);
                try {
                    JSONObject json = new JSONObject(jsonResponse);
                    if (json.getBoolean("success")) {
                        JSONArray reportesArray = json.getJSONArray("data");
                        procesarReportes(reportesArray);
                    } else {
                        Toast.makeText(requireContext(), json.getString("message"), Toast.LENGTH_LONG).show();
                        procesarReportes(new JSONArray());
                    }
                } catch (Exception e) {
                    Toast.makeText(requireContext(), "Error de procesamiento de datos.", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error JSON: ", e);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(requireContext(), "Error de conexión con el servidor.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void procesarReportes(JSONArray reportesArray) throws Exception {
        listaObjetosReportesCompleta = new ArrayList<>();

        for (int i = 0; i < reportesArray.length(); i++) {
            JSONObject reporte = reportesArray.getJSONObject(i);

            Reporte r = new Reporte(
                    reporte.getString("id_emergencia"),
                    reporte.getString("tipo"),
                    reporte.getString("descripcion"),
                    reporte.getString("fecha_reporte"),
                    reporte.getString("nombre_reportante"),
                    reporte.getString("prioridad")
            );
            listaObjetosReportesCompleta.add(r);
        }

        actualizarListView(listaObjetosReportesCompleta);

        String prioridadInicial = (String) spinnerPrioridad.getSelectedItem();
        filtrarLista(prioridadInicial);
    }

    private void actualizarListView(List<Reporte> listaParaMostrar) {
        // Usar la clase anidada ListaReporteAdapter
        ListaReporteAdapter adapter = new ListaReporteAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                listaParaMostrar
        );

        listViewReportes.setAdapter(adapter);
        setupListViewClickListener();

        if (listaParaMostrar.isEmpty()) {
            Toast.makeText(requireContext(), "No hay reportes para mostrar con ese filtro.", Toast.LENGTH_SHORT).show();
        }
    }
}