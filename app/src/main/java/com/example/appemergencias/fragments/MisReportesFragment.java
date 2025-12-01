package com.example.appemergencias.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.appemergencias.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class MisReportesFragment extends Fragment {

    private Spinner spinnerPrioridad;
    private ListView listViewReportes;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<String> listaReportes;
    private ArrayAdapter<String> adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mis_reportes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        spinnerPrioridad = view.findViewById(R.id.spinnerPrioridad);
        listViewReportes = view.findViewById(R.id.listViewReportes);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        // Spinner con opciones de prioridad
        String[] prioridades = {"Todas", "Alta", "Media", "Baja"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, prioridades);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPrioridad.setAdapter(spinnerAdapter);

        // Lista de reportes
        listaReportes = new ArrayList<>();
        adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_list_item_1, listaReportes);
        listViewReportes.setAdapter(adapter);

        // Swipe to refresh
        swipeRefreshLayout.setOnRefreshListener(this::cargarReportes);

        // Cargar reportes al iniciar
        cargarReportes();
    }

    private void cargarReportes() {
        swipeRefreshLayout.setRefreshing(true); // Mostrar animación

        String url = "http://10.0.2.2/emergencias_api/emergencia/listar_emergencias.php";
        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();

        client.get(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                swipeRefreshLayout.setRefreshing(false);
                listaReportes.clear();

                try {
                    String jsonResponse = new String(responseBody);
                    JSONObject json = new JSONObject(jsonResponse);

                    if (json.getBoolean("success")) {
                        JSONArray data = json.getJSONArray("data");
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject obj = data.getJSONObject(i);
                            String reporte = "ID: " + obj.getInt("id_emergencia") +
                                    "\nTipo: " + obj.getString("tipo") +
                                    "\nPrioridad: " + obj.getString("prioridad") +
                                    "\nFecha: " + obj.getString("fecha_reporte") +
                                    "\nDescripción: " + obj.getString("descripcion");
                            listaReportes.add(reporte);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(requireContext(), json.getString("message"), Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    Toast.makeText(requireContext(), "Error procesando la respuesta del servidor.", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(requireContext(), "Error de conexión con el servidor", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
