package app.arisoft_app;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import app.arisoft_app.Modelo.Serie;

public class InventarioListaSeriesDiferenciasAdapter extends BaseAdapter {
    private ArrayList<Serie> datos;
    Activity activity;
    LayoutInflater inflater;
    int red = Color.parseColor("#B00020");
    int green = Color.parseColor("#387002");

    public InventarioListaSeriesDiferenciasAdapter (Activity activity, ArrayList<Serie> datos) {
        // Guardamos los parámetros en variables de clase.
        this.activity = activity;
        this.datos = datos;
        inflater = activity.getLayoutInflater();
    }

    public void updateRecords(ArrayList<Serie> datos){
        this.datos = datos;
        notifyDataSetChanged();
    }

    class ViewHolder{
        LinearLayout linearLayout;
        TextView serie;
        TextView estado;
    }

    @Override
    public int getCount() {
        return datos.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        InventarioListaSeriesDiferenciasAdapter.ViewHolder holder = null;

        if (view == null) {
            view = inflater.inflate(R.layout.lista_serie_item, parent, false);
            holder = new InventarioListaSeriesDiferenciasAdapter.ViewHolder();
            holder.serie = (TextView) view.findViewById(R.id.tv_serie);
            holder.estado = (TextView) view.findViewById(R.id.tv_estado);
            view.setTag(holder);
        } else holder = (InventarioListaSeriesDiferenciasAdapter.ViewHolder)view.getTag();

        Serie model = datos.get(position);
        holder.serie.setText(model.getSerie());
        String codigo_estado = model.getEstado();
        String estado = "";
        holder.estado.setTextColor(activity.getResources().getColor(R.color.red));
        switch (codigo_estado){
            case "D":  estado = "Disponible";
                holder.estado.setTextColor(activity.getResources().getColor(R.color.green));
                break;
            case "V":  estado = "Vendida";
                break;
            case "AO":  estado = "En otro almacén /n No vendida";
                break;
            case "VO":  estado = "En otro almacén /n Vendida";
                break;
            case "NE":  estado = "No existe en MacroPro";
                break;
            case "N":  estado = "No Inventariadas";
                holder.estado.setTextColor(activity.getResources().getColor(R.color.colorAccent));
                break;
            default:  estado = "Sin estado";
                break;
        }
        holder.estado.setText(estado);
        return view;
    }
}
