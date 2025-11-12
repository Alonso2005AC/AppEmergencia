package com.example.appemergencias.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.appemergencias.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class EditarPerfilFragment extends Fragment {

    // Vistas del layout fragment_editar_perfil.xml
    private EditText etNombre, etCorreo, etTelefono, etPassActual, etPassNueva;
    private Button btnGuardar;
    private SharedPreferences prefs;
    private String idUsuario;
    private static final String TAG = "API_PERFIL";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_editarperfil, container, false);

        // 1. Inicializar SharedPreferences y obtener ID del usuario
        prefs = requireActivity().getSharedPreferences("SESSION", Context.MODE_PRIVATE);
        idUsuario = prefs.getString("id_usuario", null);

        // 2. Inicializar Vistas (Asegúrate de que los IDs coincidan con tu XML)
        etNombre = view.findViewById(R.id.etNombrePerfil);
        etCorreo = view.findViewById(R.id.etCorreoPerfil);
        etTelefono = view.findViewById(R.id.etTelefonoPerfil);
        etPassActual = view.findViewById(R.id.etPasswordActual);
        etPassNueva = view.findViewById(R.id.etPasswordNueva);
        btnGuardar = view.findViewById(R.id.btnGuardarPerfil);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 3. Cargar datos del usuario al iniciar
        cargarDatosPerfil();

        // 4. Configurar el Listener del botón Guardar
        btnGuardar.setOnClickListener(v -> guardarCambios());
    }

    // Carga los datos guardados en SharedPreferences (Nombre, Correo, Teléfono)
    private void cargarDatosPerfil() {
        if (idUsuario == null) {
            Toast.makeText(requireContext(), "Error de sesión. Vuelve a iniciar.", Toast.LENGTH_LONG).show();
            return;
        }

        // Rellenar los campos
        etNombre.setText(prefs.getString("nombre", ""));
        etCorreo.setText(prefs.getString("correo", ""));
        etTelefono.setText(prefs.getString("telefono", ""));
    }

    private void guardarCambios() {
        // Obtener datos del formulario
        String nombre = etNombre.getText().toString().trim();
        String correo = etCorreo.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String passActual = etPassActual.getText().toString();
        String passNueva = etPassNueva.getText().toString();

        // Validación de campos obligatorios
        if (nombre.isEmpty() || correo.isEmpty() || telefono.isEmpty()) {
            Toast.makeText(requireContext(), "Nombre, correo y teléfono son obligatorios.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validación de Contraseña
        if (!passNueva.isEmpty() && passActual.isEmpty()) {
            Toast.makeText(requireContext(), "Debes ingresar tu Contraseña Actual para cambiarla.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Preparar la petición
        // URL apunta a emergencias_api/usuarios/update_perfil.php
        String url = "http://10.0.2.2/emergencias_api/usuarios/update_perfil.php";
        RequestParams params = new RequestParams();

        // Datos a enviar
        params.put("id_usuario", idUsuario);
        params.put("nombre", nombre);
        params.put("correo", correo);
        params.put("telefono", telefono);

        // Datos de contraseña (solo si se intenta cambiar)
        if (!passNueva.isEmpty()) {
            params.put("password_actual", passActual);
            params.put("password_nueva", passNueva);
        }

        AsyncHttpClient client = new AsyncHttpClient();
        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String jsonResponse = new String(responseBody);
                try {
                    JSONObject json = new JSONObject(jsonResponse);
                    Toast.makeText(requireContext(), json.getString("message"), Toast.LENGTH_LONG).show();

                    if (json.getBoolean("success")) {
                        // Limpia los campos de contraseña y actualiza la sesión
                        etPassActual.setText("");
                        etPassNueva.setText("");

                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("nombre", nombre);
                        editor.putString("correo", correo);
                        editor.putString("telefono", telefono);
                        editor.apply();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "JSON Error: ", e);
                    Toast.makeText(requireContext(), "Error al procesar la respuesta del servidor.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String errorMsg = "Error " + statusCode + ": Verifique la conexión o la ruta PHP.";
                Log.e(TAG, errorMsg, error);
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show();
            }
        });
    }
}