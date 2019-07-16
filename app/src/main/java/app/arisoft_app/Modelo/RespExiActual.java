package app.arisoft_app.Modelo;

import java.util.ArrayList;

public class RespExiActual {
    private ArrayList<ExistenciaActual>existencia;
    private ArrayList<ExistenciaActual>data;
    private String items;

    public ArrayList<ExistenciaActual> getData() {
        return data;
    }

    public void setData(ArrayList<ExistenciaActual> data) {
        this.data = data;
    }

    public String getItems() {
        return items;
    }

    public void setItems(String items) {
        this.items = items;
    }

    public ArrayList<ExistenciaActual> getExistencia() {
        return existencia;
    }

    public void setExistencia(ArrayList<ExistenciaActual> existencia) {
        this.existencia = existencia;
    }
}
