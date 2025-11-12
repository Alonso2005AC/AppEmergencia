package com.example.appemergencias.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView; // <-- ¡IMPORTACIÓN CRÍTICA!

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.appemergencias.R;
import com.example.appemergencias.models.Emergencia;

import java.util.List;

public class EmergenciaAdapter extends ArrayAdapter<Emergencia> {

    public EmergenciaAdapter(Context context, List<Emergencia> emergencias) {
        super(context, R.layout.list_item_emergencia, emergencias);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Emergencia emergencia = getItem(position);
        ViewHolder viewHolder;

        // Usar patrón ViewHolder
        if (convertView == null) {
            viewHolder = new ViewHolder();

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_emergencia, parent, false);

            // Asignar referencias
            viewHolder.tvTipo = convertView.findViewById(R.id.tv_emergencia_tipo);
            viewHolder.tvDescripcion = convertView.findViewById(R.id.tv_emergencia_descripcion);
            viewHolder.tvPrioridad = convertView.findViewById(R.id.tv_emergencia_prioridad);
            viewHolder.tvFecha = convertView.findViewById(R.id.tv_emergencia_fecha);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (emergencia != null) {
            // Asignar texto (usando el ViewHolder)
            viewHolder.tvTipo.setText(emergencia.tipo);
            viewHolder.tvDescripcion.setText(emergencia.descripcion);
            viewHolder.tvFecha.setText("Reportado: " + emergencia.fecha_reporte);

            // Manejar Prioridad y Color
            String prioridad = emergencia.prioridad.toUpperCase();
            viewHolder.tvPrioridad.setText(prioridad);

            int color;
            switch (prioridad) {
                case "ALTA":
                    color = Color.parseColor("#C62828"); // Rojo
                    break;
                case "MEDIA":
                    color = Color.parseColor("#FFC107"); // Ámbar
                    break;
                case "BAJA":
                    color = Color.parseColor("#4CAF50"); // Verde
                    break;
                default:
                    color = Color.GRAY;
                    break;
            }
            viewHolder.tvPrioridad.setBackgroundColor(color);
        }

        return convertView;
    }

    // CLASE VIEWHOLDER: Debe ser estática (static)
    private static class ViewHolder {
        // Declaración de variables: asegúrate de que sean de tipo TextView
        TextView tvTipo;
        TextView tvDescripcion;
        TextView tvPrioridad;
        TextView tvFecha;
    }
}