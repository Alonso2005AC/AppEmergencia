package com.example.appemergencias.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.appemergencias.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class AdminChartsFragment extends Fragment {

    private WebView webView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_charts, container, false);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        webView = view.findViewById(R.id.webViewCharts);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);

        cargarDatosDesdeAPI();
    }

    private void cargarDatosDesdeAPI() {
        String url = "http://10.0.2.2/emergencias_api/usuarios/reportes_por_tipo.php";

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    String resp = new String(responseBody);
                    JSONObject json = new JSONObject(resp);

                    if (json.getBoolean("success")) {
                        JSONArray data = json.getJSONArray("data");
                        cargarGraficosEnWebView(data);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                error.printStackTrace();
            }
        });
    }

    private void cargarGraficosEnWebView(JSONArray data) {
        StringBuilder arrayJS = new StringBuilder();

        arrayJS.append("['Tipo', 'Cantidad'],");
        for (int i = 0; i < data.length(); i++) {
            try {
                JSONObject obj = data.getJSONObject(i);
                String tipo = obj.getString("tipo");
                int cantidad = obj.getInt("cantidad");

                arrayJS.append("['").append(tipo).append("', ").append(cantidad).append("],");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String html = "<html>" +
                "<head>" +
                "  <script type='text/javascript' src='https://www.gstatic.com/charts/loader.js'></script>" +
                "  <script type='text/javascript'>" +
                "    google.charts.load('current', {'packages':['corechart']});" +
                "    google.charts.setOnLoadCallback(drawCharts);" +

                "    function drawCharts() {" +
                "      var data = google.visualization.arrayToDataTable([" +
                arrayJS.toString() +
                "      ]);" +

                "      var optionsBar = {title: 'Reportes por Tipo', legend: { position: 'none' }};" +
                "      var barChart = new google.visualization.ColumnChart(document.getElementById('bar_chart'));" +
                "      barChart.draw(data, optionsBar);" +

                "      var optionsPie = {title: 'Distribución de Reportes'};" +
                "      var pieChart = new google.visualization.PieChart(document.getElementById('pie_chart'));" +
                "      pieChart.draw(data, optionsPie);" +
                "    }" +
                "  </script>" +
                "</head>" +
                "<body>" +
                "  <h3>Gráfico de Barras</h3>" +
                "  <div id='bar_chart' style='width: 100%; height: 350px;'></div>" +
                "  <h3>Gráfico de Torta</h3>" +
                "  <div id='pie_chart' style='width: 100%; height: 350px;'></div>" +
                "</body>" +
                "</html>";

        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
    }
}
