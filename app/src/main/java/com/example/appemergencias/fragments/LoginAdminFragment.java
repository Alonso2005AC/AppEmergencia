package com.example.appemergencias.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.appemergencias.Inicio.HomeAdminActivity;
import com.example.appemergencias.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class LoginAdminFragment extends Fragment {

    private EditText etCorreo, etPassword;
    private Button btnLoginAdmin;
    private OnAdminLoginSuccess listener;

    public interface OnAdminLoginSuccess {
        void onLoginAdminSuccess();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login_admin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etCorreo = view.findViewById(R.id.etCorreoAdmin);
        etPassword = view.findViewById(R.id.etPasswordAdmin);
        btnLoginAdmin = view.findViewById(R.id.btnLoginAdmin);

        btnLoginAdmin.setOnClickListener(v -> loginAdmin());
    }

    private void loginAdmin() {
        String correo = etCorreo.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if(correo.isEmpty() || password.isEmpty()){
            Toast.makeText(requireContext(), "Ingresa correo y contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://10.0.2.2/emergencias_api/usuarios/login_admin.php";
        RequestParams params = new RequestParams();
        params.put("correo", correo);
        params.put("password", password);

        AsyncHttpClient client = new AsyncHttpClient();
        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    String resp = new String(responseBody);
                    JSONObject json = new JSONObject(resp);

                    if (json.getBoolean("success")){
                        JSONObject data = json.getJSONObject("data");
                        String nombreAdmin = data.getString("nombre");

                        SharedPreferences prefs = requireActivity().getSharedPreferences("SESSION", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("id_usuario", data.getString("id_admin"));
                        editor.putString("nombre_usuario", nombreAdmin);
                        editor.putString("rol", "admin");
                        editor.apply();

                        Toast.makeText(requireContext(), "¡Bienvenido Admin " + nombreAdmin + "!", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(requireActivity(), HomeAdminActivity.class);
                        startActivity(intent);
                        requireActivity().finish();
                    } else {
                        Toast.makeText(requireContext(), json.getString("message"), Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e){
                    Toast.makeText(requireContext(), "Error procesando la respuesta", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(requireContext(), "Error de conexión con el servidor", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if(context instanceof OnAdminLoginSuccess){
            listener = (OnAdminLoginSuccess) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}
