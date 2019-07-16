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

public class InventarioListaSeriesAdapter extends BaseAdapter {
    private ArrayList<Serie> datos;
    Activity activity;
    LayoutInflater inflater;
    int red = Color.parseColor("#B00020");
    int green = Color.parseColor("#387002");

    public InventarioListaSeriesAdapter (Activity activity, ArrayList<Serie> datos) {
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
        InventarioListaSeriesAdapter.ViewHolder holder = null;

        if (view == null) {
            view = inflater.inflate(R.layout.lista_serie_item, parent, false);
            holder = new ViewHolder();
            holder.serie = (TextView) view.findViewById(R.id.tv_serie);
            holder.estado = (TextView) view.findViewById(R.id.tv_estado);
            view.setTag(holder);
        } else holder = (ViewHolder)view.getTag();

        Serie model = datos.get(position);
        holder.serie.setText(model.getSerie());
        holder.estado.setText(model.getEstado());
        return view;
    }
}
