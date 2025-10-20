package com.example.appemergencias.Menu;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.appemergencias.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MenuActivity extends AppCompatActivity {

    private Spinner spinnerTipo;
    private EditText etDescripcion;
    private Button btnNext;

    private RequestQueue requestQueue;
    private ProgressDialog progressDialog;

    private String URL_TIPOS = "http://10.0.2.2/emergencias_api/emergencia/get_tipos.php";
    private String URL_INSERT = "http://10.0.2.2/emergencias_api/emergencia/insert_emergencia.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        spinnerTipo = findViewById(R.id.spinner_tipo);
        etDescripcion = findViewById(R.id.et_incident_details);
        btnNext = findViewById(R.id.btn_next);

        requestQueue = Volley.newRequestQueue(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Cargando...");

        cargarTipos();

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registrarEmergencia();
            }
        });
    }

    private void cargarTipos() {
        progressDialog.show();
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                URL_TIPOS,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        progressDialog.dismiss();
                        ArrayList<String> tipos = new ArrayList<>();
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject obj = response.getJSONObject(i);
                                tipos.add(obj.getString("tipo"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(MenuActivity.this,
                                android.R.layout.simple_spinner_item, tipos);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerTipo.setAdapter(adapter);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Toast.makeText(MenuActivity.this, "Error al cargar tipos", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        requestQueue.add(jsonArrayRequest);
    }

    private void registrarEmergencia() {
        final String tipo = spinnerTipo.getSelectedItem().toString();
        final String descripcion = etDescripcion.getText().toString();

        if (descripcion.isEmpty()) {
            etDescripcion.setError("Ingrese la descripción");
            return;
        }

        progressDialog.setMessage("Registrando emergencia...");
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_INSERT,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String status = jsonObject.getString("status");
                            String message = jsonObject.getString("message");
                            Toast.makeText(MenuActivity.this, message, Toast.LENGTH_SHORT).show();

                            if(status.equals("ok")){
                                etDescripcion.setText("");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MenuActivity.this, "Respuesta inválida del servidor", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Toast.makeText(MenuActivity.this, "Error al registrar emergencia", Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("tipo", tipo);
                params.put("descripcion", descripcion);
                params.put("id_usuario", "1"); // usuario de ejemplo
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }
}
