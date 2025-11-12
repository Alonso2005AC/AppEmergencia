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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.appemergencias.R;
import com.example.appemergencias.Inicio.MainActivity;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class LoginFragment extends Fragment {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private Button btnGuestReport, btnViewEmergencies;
    private OnLoginSuccessListener listener;
    private static final String TAG = "API_LOGIN"; // Usamos una etiqueta clara

    public interface OnLoginSuccessListener {
        void onLoginSuccessNavigateToMenu();
        void onRegisterClicked();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ocultar Toolbar (código sin cambios)
        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().hide();
            }
            activity.getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        // Inicialización de Vistas
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        btnLogin = view.findViewById(R.id.btnLogin);
        tvRegister = view.findViewById(R.id.tvRegister);
        btnGuestReport = view.findViewById(R.id.btnGuestReport);
        btnViewEmergencies = view.findViewById(R.id.btnViewEmergencies);

        btnLogin.setOnClickListener(v -> realizarLogin());

        tvRegister.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRegisterClicked();
            }
        });

        btnGuestReport.setOnClickListener(v -> Toast.makeText(requireContext(), "Reportar Invitado (Pendiente)", Toast.LENGTH_SHORT).show());
        btnViewEmergencies.setOnClickListener(v -> Toast.makeText(requireContext(), "Ver Emergencias (Pendiente)", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnLoginSuccessListener) {
            listener = (OnLoginSuccessListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // Restaurar Toolbar (código sin cambios)
        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().show();
                activity.getSupportActionBar().setTitle(MainActivity.APP_TITLE);
            }
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
        listener = null;
    }

    // =====================================================================
    // LÓGICA DE NEGOCIO Y API (CORREGIDA PARA JSON Y ROLES)
    // =====================================================================

    private void realizarLogin() {
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(requireContext(), "Ingresa correo y contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://10.0.2.2/emergencias_api/usuarios/login.php";
        RequestParams params = new RequestParams();
        params.put("email", email);
        params.put("password", pass);

        AsyncHttpClient client = new AsyncHttpClient();
        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String jsonResponse = new String(responseBody);
                Log.d(TAG, "Respuesta del Servidor: " + jsonResponse);

                try {
                    JSONObject json = new JSONObject(jsonResponse);

                    // 1. CORRECCIÓN: Verifica "success" (no "status")
                    if (json.has("success") && json.getBoolean("success")) {

                        JSONObject data = json.getJSONObject("data");
                        String idUsuario = data.getString("id_usuario");
                        String nombreUsuario = data.getString("nombre");
                        String rolUsuario = data.getString("rol"); // <-- Lee el Rol

                        SharedPreferences prefs = requireActivity().getSharedPreferences("SESSION", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();

                        // 2. GUARDA EL ROL
                        editor.putString("id_usuario", idUsuario);
                        editor.putString("usuario", nombreUsuario); // Clave para MainActivity
                        editor.putString("rol", rolUsuario); // <-- Guarda el Rol
                        editor.apply();

                        Toast.makeText(requireContext(), "¡Bienvenido, " + nombreUsuario + "!", Toast.LENGTH_SHORT).show();

                        // Llama al listener para la navegación
                        if (listener != null) {
                            listener.onLoginSuccessNavigateToMenu();
                        }

                    } else if (json.has("message")) {
                        // Si "success" es false, muestra el mensaje de error.
                        Toast.makeText(requireContext(), json.getString("message"), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(requireContext(), "Error de formato de API inesperado.", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // Este catch se activa por el error de JSON mal formado (espacios, warnings PHP)
                    Toast.makeText(requireContext(), "Error procesando la respuesta del servidor (JSON inválido).", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error de JSON parsing: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(requireContext(), "Error de conexión con el servidor", Toast.LENGTH_SHORT).show();
            }
        });
    }
}