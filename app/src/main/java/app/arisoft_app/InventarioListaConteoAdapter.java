package app.arisoft_app;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import app.arisoft_app.Modelo.Inventario;

public class InventarioListaConteoAdapter extends BaseAdapter {
    private ArrayList<Inventario> datos;
    private ArrayList<Inventario>auxDatos;
    Activity activity;
    LayoutInflater inflater;

    public InventarioListaConteoAdapter(Activity activity) {
        this.activity = activity;
    }

    public InventarioListaConteoAdapter(Activity activity, ArrayList<Inventario> datos) {
        // Guardamos los parámetros en variables de clase.
        this.activity = activity;
        this.datos = datos;
        this.auxDatos=datos;
        inflater = activity.getLayoutInflater();
    }


    @Override
    public int getCount() {
        return datos.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    class ViewHolder{
        LinearLayout linearLayout;
        TextView articulo;
        TextView descripcion;
        TextView existencia;
        TextView conteo;
        TextView serie;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {

        ViewHolder holder = null;

        if (view == null) {

            view = inflater.inflate(R.layout.lista_conteo_item, viewGroup, false);

            holder = new ViewHolder();
            holder.articulo = view.findViewById(R.id.tv_cod);
            holder.descripcion = view.findViewById(R.id.tv_desc);
            holder.existencia = view.findViewById(R.id.tv_existencia);
            holder.conteo = view.findViewById(R.id.tv_conteo);
            holder.serie = view.findViewById(R.id.tv_serie);
            holder.linearLayout = view.findViewById(R.id.linear_conteo_item);
            view.setTag(holder);

        } else holder = (ViewHolder)view.getTag();

        Inventario model = datos.get(position);

        holder.articulo.setText(model.getCodigoArticulo());
        holder.descripcion.setText(model.getDescripcion());
        String exist = Float.toString(model.getExistencia());
        holder.existencia.setText(exist);
        String cont = Float.toString(model.getConteo());
        holder.conteo.setText(cont);
        holder.serie.setText(model.getSerie());

        if (model.isSelected())
            holder.linearLayout.setBackgroundResource(R.color.selected_item);
        else
            holder.linearLayout.setBackgroundColor(Color.WHITE);

        return view;
    }

    public void updateRecords(ArrayList<Inventario> datos){
        this.datos = datos;
        notifyDataSetChanged();
    }


}
