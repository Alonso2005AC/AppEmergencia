package com.example.appemergencias.fragments;

import android.content.Context;
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

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class RegisterFragment extends Fragment {

    // Componentes de la interfaz de usuario
    // NOTA: etEmail, etTelefono, etPassword, y etConfirmPassword son variables de la clase.
    private EditText etNombre, etEmail, etTelefono, etPassword, etConfirmPassword;
    private Button btnRegister;

    // Listener para comunicar el éxito del registro a la Activity
    private OnRegistrationSuccessListener listener;
    private static final String TAG = "API_REGISTER";

    // Interfaz de comunicación con la Activity
    public interface OnRegistrationSuccessListener {
        void onRegistrationSuccessNavigateToLogin();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Infla el layout fragment_register.xml
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Inicialización de Vistas (¡Mapeado a tu XML!)
        // Los IDs deben coincidir exactamente con los de fragment_register.xml
        etNombre = view.findViewById(R.id.etNombre);
        etEmail = view.findViewById(R.id.etEmail); // Mapeado a etCorreo2
        etTelefono = view.findViewById(R.id.etTel); // Mapeado a etTel
        etPassword = view.findViewById(R.id.etPassword); // Mapeado a etContraseña
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword); // Mapeado a etConContra
        btnRegister = view.findViewById(R.id.btnRegister); // Mapeado a btn_registrar

        // 2. Establecer el Listener del botón
        btnRegister.setOnClickListener(v -> realizarRegistro());
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnRegistrationSuccessListener) {
            listener = (OnRegistrationSuccessListener) context;
        } else {
            throw new RuntimeException(context.toString() + " debe implementar OnRegistrationSuccessListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    // =====================================================================
    // LÓGICA DE REGISTRO Y LLAMADA A LA API
    // =====================================================================

    private void realizarRegistro() {
        // Obtener y sanitizar los datos de los campos de texto
        final String nombre = etNombre.getText().toString().trim();
        final String email = etEmail.getText().toString().trim();
        final String telefono = etTelefono.getText().toString().trim();
        final String password = etPassword.getText().toString().trim();
        final String confirmPass = etConfirmPassword.getText().toString().trim();

        // 1. Validaciones de la Interfaz
        if (nombre.isEmpty() || email.isEmpty() || telefono.isEmpty() || password.isEmpty() || confirmPass.isEmpty()) {
            Toast.makeText(requireContext(), "Todos los campos son obligatorios.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPass)) {
            Toast.makeText(requireContext(), "Las contraseñas no coinciden.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Configuración de la Petición
        String url = "http://10.0.2.2/emergencias_api/usuarios/register.php";

        RequestParams params = new RequestParams();
        // Las claves aquí DEBEN COINCIDIR con las que espera tu script PHP (register.php)
        params.put("nombre", nombre);
        params.put("correo", email); // PHP espera 'correo'
        params.put("password", password);
        params.put("telefono", telefono);

        // 3. Petición HTTP (POST)
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String jsonResponse = new String(responseBody);
                Log.e(TAG, "Respuesta del Servidor: " + jsonResponse);

                try {
                    JSONObject json = new JSONObject(jsonResponse);

                    if (json.has("success") && json.getBoolean("success")) {

                        Toast.makeText(requireContext(), "¡Registro exitoso! Por favor inicia sesión.", Toast.LENGTH_LONG).show();

                        // Navegación de vuelta al Login
                        if (listener != null) {
                            listener.onRegistrationSuccessNavigateToLogin();
                        }

                    } else if (json.has("message")) {
                        // Muestra el mensaje de error que viene del servidor (ej. "El correo ya está registrado")
                        Toast.makeText(requireContext(), "Error al registrar: " + json.getString("message"), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(requireContext(), "Error de formato de API inesperado.", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(requireContext(), "Error de API. Respuesta inválida.", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error de JSON parsing: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.e(TAG, "Fallo de conexión: Status " + statusCode, error);
                Toast.makeText(requireContext(), "Error de conexión con el servidor. Verifica tu API.", Toast.LENGTH_LONG).show();
            }
        });
    }
}