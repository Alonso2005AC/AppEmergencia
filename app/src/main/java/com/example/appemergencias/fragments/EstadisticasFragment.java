package com.example.appemergencias.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.appemergencias.R;

import org.json.JSONArray;
import org.json.JSONObject;

public class EstadisticasFragment extends Fragment {

    private WebView webView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_estadisticas, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        webView = view.findViewById(R.id.webViewEstadisticas);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webView.setWebChromeClient(new android.webkit.WebChromeClient());


        cargarDatos();
    }

    private void cargarDatos() {
        String url = "http://10.0.2.2/emergencias_api/stats/estadisticas_emergencias.php";

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);

                        if (!json.getBoolean("success")) return;

                        JSONArray dataArray = json.getJSONArray("data");

                        String html = generarHTML(dataArray);

                        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                Throwable::printStackTrace
        );

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        queue.add(request);
    }

    private String generarHTML(JSONArray dataArray) {

        String html = "<html>" +
                "<head>" +
                "<script type='text/javascript' src='https://www.gstatic.com/charts/loader.js'></script>" +
                "<script type='text/javascript'>" +
                "google.charts.load('current', {'packages':['corechart','bar']});" +
                "google.charts.setOnLoadCallback(drawCharts);" +

                "function drawCharts() {" +
                "var data = google.visualization.arrayToDataTable([" +
                "['Prioridad', 'Cantidad'], ['Alta', 10], ['Media', 5], ['Baja', 3]" +
                "]);" +

                "var barChart = new google.charts.Bar(document.getElementById('barChart'));" +
                "barChart.draw(data);" +

                "var pieChart = new google.visualization.PieChart(document.getElementById('pieChart'));" +
                "pieChart.draw(data);" +
                "}" +
                "</script>" +
                "</head>" +
                "<body>" +
                "<h2 style='text-align:center;'>PRUEBA GOOGLE CHARTS</h2>" +
                "<div id='barChart' style='width:100%; height:300px;'></div>" +
                "<br>" +
                "<div id='pieChart' style='width:100%; height:300px;'></div>" +
                "</body>" +
                "</html>";

        return html;
    }
}
