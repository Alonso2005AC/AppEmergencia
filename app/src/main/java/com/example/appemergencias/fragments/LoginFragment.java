package com.example.appemergencias.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
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

        // ********** CÓDIGO CLAVE: OCULTAR TOOLBAR Y PANTALLA COMPLETA **********
        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().hide(); // 1. Oculta la Toolbar
            }

            // 2. Hace que el fondo se dibuje detrás de la barra de estado
            activity.getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        // ************************************************************************

        // Inicialización de Vistas
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        btnLogin = view.findViewById(R.id.btnLogin);
        tvRegister = view.findViewById(R.id.tvRegister);

        btnGuestReport = view.findViewById(R.id.btnGuestReport);
        btnViewEmergencies = view.findViewById(R.id.btnViewEmergencies);


        // Listener del botón de login
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

        // ********** CÓDIGO CLAVE: RESTAURAR TOOLBAR Y MODO NORMAL **********
        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();

            // 1. Restaura la visibilidad de la Toolbar y el título
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().show();
                activity.getSupportActionBar().setTitle(MainActivity.APP_TITLE);
            }

            // 2. Restaura el modo de pantalla completa a normal (contenido debajo de la barra)
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
        // *******************************************************************
        listener = null;
    }

    // =====================================================================
    // LÓGICA DE NEGOCIO Y API
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
                try {
                    String jsonResponse = new String(responseBody);
                    JSONObject json = new JSONObject(jsonResponse);

                    if (json.getString("status").equals("ok")) {

                        JSONObject data = json.getJSONObject("data");
                        String idUsuario = data.getString("id_usuario");
                        String nombreUsuario = data.getString("nombre"); // Opcional, pero útil

                        // 1. OBTENER Y EDITAR SharedPreferences
                        SharedPreferences prefs = requireActivity().getSharedPreferences("SESSION", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();

                        // 2. GUARDAR LOS DATOS CLAVE
                        editor.putString("id_usuario", idUsuario); // GUARDA LA CLAVE "id_usuario"
                        editor.putString("nombre_usuario", nombreUsuario); // Útil para el HomeFragment
                        editor.apply();

                        Toast.makeText(requireContext(), "¡Bienvenido, " + nombreUsuario + "!", Toast.LENGTH_SHORT).show();
                        // Redirigir al HomeActivity o Fragment
                        // ...

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
                Toast.makeText(requireContext(), "Error de conexión con el servidor", Toast.LENGTH_SHORT).show();
            }
        });
    }
}