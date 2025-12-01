package com.example.appemergencias.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
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
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

public class MisReportesAdminFragment extends Fragment {

    private ListView listView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ArrayList<HashMap<String, String>> listaEmergencias = new ArrayList<>();
    private SimpleAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mis_reportes_admin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listView = view.findViewById(R.id.listViewReportesAdmin);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshAdmin);

        // Configurar adapter
        adapter = new SimpleAdapter(getContext(), listaEmergencias,
                android.R.layout.simple_list_item_2,
                new String[]{"tipo", "descripcion"},
                new int[]{android.R.id.text1, android.R.id.text2}) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View viewItem = super.getView(position, convertView, parent);
                Button btnMarcar = new Button(getContext());
                btnMarcar.setText("Marcar como resuelta");

                String estado = listaEmergencias.get(position).get("estado");
                if ("resuelta".equals(estado)) {
                    btnMarcar.setEnabled(false);
                    btnMarcar.setText("Resuelta");
                } else {
                    btnMarcar.setOnClickListener(v -> marcarComoResuelta(position));
                }

                // Añadir botón al layout del item
                ((ViewGroup) viewItem).addView(btnMarcar);
                return viewItem;
            }
        };

        listView.setAdapter(adapter);

        // Swipe to refresh
        swipeRefreshLayout.setOnRefreshListener(this::cargarEmergencias);

        // Cargar emergencias inicial
        cargarEmergencias();
    }

    private void cargarEmergencias() {
        swipeRefreshLayout.setRefreshing(true);

        AsyncHttpClient client = new AsyncHttpClient();
        client.get("http://10.0.2.2/emergencias_api/emergencia/listar_emergencias.php", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                swipeRefreshLayout.setRefreshing(false);
                listaEmergencias.clear();
                try {
                    String jsonResponse = new String(responseBody);
                    JSONObject json = new JSONObject(jsonResponse);

                    if (json.getBoolean("success")) {
                        JSONArray data = json.getJSONArray("data");
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject obj = data.getJSONObject(i);
                            HashMap<String, String> emergencia = new HashMap<>();
                            emergencia.put("id_emergencia", obj.getString("id_emergencia"));
                            emergencia.put("tipo", obj.getString("tipo") + " - " + obj.getString("prioridad"));
                            emergencia.put("descripcion", obj.getString("descripcion") + "\nFecha: " + obj.getString("fecha_reporte"));
                            emergencia.put("estado", obj.getString("estado"));
                            listaEmergencias.add(emergencia);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getContext(), json.getString("message"), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Error procesando los datos.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(getContext(), "Error de conexión al servidor.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void marcarComoResuelta(int position) {
        String idEmergencia = listaEmergencias.get(position).get("id_emergencia");

        AsyncHttpClient client = new AsyncHttpClient();
        RequestParams params = new RequestParams();
        params.put("id_emergencia", idEmergencia);
        params.put("estado", "resuelta");

        client.post("http://10.0.2.2/emergencias_api/emergencia/actualizar_emergencia.php", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                Toast.makeText(getContext(), "Emergencia marcada como resuelta", Toast.LENGTH_SHORT).show();
                cargarEmergencias();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(getContext(), "Error al actualizar el estado", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
