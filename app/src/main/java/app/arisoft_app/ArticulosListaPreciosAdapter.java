package app.arisoft_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import app.arisoft_app.Modelo.Articulo;

public class ArticulosListaPreciosAdapter extends ArrayAdapter<Articulo> {
    private Context context;
    private ArrayList<Articulo> datos;

    public ArticulosListaPreciosAdapter(Context context, ArrayList<Articulo> datos) {
        super(context, R.layout.lista_precios_item, datos);
        // Guardamos los parámetros en variables de clase.
        this.context = context;
        this.datos = datos;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // En primer lugar "inflamos" una nueva vista, que será la que se
        // mostrará en la celda del ListView. Para ello primero creamos el
        // inflater, y después inflamos la vista.
        LayoutInflater inflater = LayoutInflater.from(context);
        View item = inflater.inflate(R.layout.lista_precios_item, null);
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.CANADA);

        TextView articulo = (TextView) item.findViewById(R.id.tvArticulo);
        articulo.setText(datos.get(position).getCodigoArticulo());

        TextView desc = (TextView) item.findViewById(R.id.tvDesc);
        desc.setText(datos.get(position).getDescripcion());

        TextView p1 = (TextView) item.findViewById(R.id.precio1);
        String precio1 = datos.get(position).getPrecio01();
        double num = Double.parseDouble(precio1);
        String precio01 = format.format(num);
        p1.setText(precio01);

        /*TextView p2 = (TextView) item.findViewById(R.id.precio2);
        String precio2 = datos.get(position).getPrecio02();
        double num2 = Double.parseDouble(precio2);
        String precio02 = format.format(num2);
        p1.setText(precio02);

        TextView p3 = (TextView) item.findViewById(R.id.precio3);
        String precio3 = datos.get(position).getPrecio03();
        double num3 = Double.parseDouble(precio3);
        String precio03 = format.format(num3);
        p1.setText(precio03);*/

        return item;
    }
}
