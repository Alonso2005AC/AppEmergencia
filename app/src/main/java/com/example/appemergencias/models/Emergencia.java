package com.example.appemergencias.models;

public class Emergencia {
    public int id_emergencia;
    public String tipo;
    public String descripcion;
    public String prioridad;
    public String fecha_reporte;

    public Emergencia(int id_emergencia, String tipo, String descripcion, String prioridad, String fecha_reporte) {
        this.id_emergencia = id_emergencia;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.prioridad = prioridad;
        this.fecha_reporte = fecha_reporte;
    }
}