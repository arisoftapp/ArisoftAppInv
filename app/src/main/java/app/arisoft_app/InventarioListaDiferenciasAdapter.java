package app.arisoft_app;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;

import app.arisoft_app.Modelo.Inventario;
import app.arisoft_app.Tools.Database;

public class InventarioListaDiferenciasAdapter extends BaseAdapter {
    private ArrayList<Inventario> datos;
    Activity activity;
    LayoutInflater inflater;
    int red = Color.parseColor("#B00020");
    int green = Color.parseColor("#387002");
    int blue=Color.parseColor("#03A9F4");
    String idalmacen;
    Integer view;

    public InventarioListaDiferenciasAdapter(Activity activity) {
        this.activity = activity;
    }

    public InventarioListaDiferenciasAdapter(Activity activity, ArrayList<Inventario> datos, String almacen, Integer vista) {
        // Guardamos los parámetros en variables de clase.
        this.activity = activity;
        this.datos = datos;
        this.idalmacen = almacen;
        this.view = vista;
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
        TextView diferencia;
        TextView serie;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {

        InventarioListaDiferenciasAdapter.ViewHolder holder = null;

        if (view == null) {
            view = inflater.inflate(R.layout.lista_diferencias_item, viewGroup, false);
            holder = new ViewHolder();
            holder.articulo = (TextView) view.findViewById(R.id.tv_cod);
            holder.descripcion = (TextView) view.findViewById(R.id.tv_desc);
            holder.existencia = (TextView) view.findViewById(R.id.tv_existencia);
            holder.conteo = (TextView) view.findViewById(R.id.tv_conteo);
            holder.diferencia = (TextView) view.findViewById(R.id.tv_diferencia);
            holder.linearLayout = (LinearLayout) view.findViewById(R.id.linear_diferencias_item);
            holder.serie = view.findViewById(R.id.tv_serie);
            view.setTag(holder);
        } else holder = (ViewHolder)view.getTag();

        Inventario model = datos.get(position);
        Float dife = model.getDiferencia();

        if (this.view == 0 ){
            //SI SE SELECCIONÓ TODOS LOS ARTICULOS

        } else {
            //SI SE SELECCIONÓ SÓLO DIFERENCIAS
            if (dife != 0){

            }
        }

        holder.articulo.setText(model.getCodigoArticulo());
        holder.descripcion.setText(model.getDescripcion());
        String exist = Float.toString(model.getExistencia());
        holder.existencia.setText(exist);
        String cont = Float.toString(model.getConteo());
        holder.conteo.setText(cont);
        String dif = Float.toString(model.getDiferencia());
        if (Float.parseFloat(dif) > 0){
            dif = "+" + dif;
            holder.diferencia.setTextColor(green);
        }else if (Float.parseFloat(dif) < 0) {
            holder.diferencia.setTextColor(red);
        }
        if(Float.parseFloat(dif)==0)
        {
            if(model.getSerie().equalsIgnoreCase("S"))
            {
                Database admin = new Database(this.activity,null,1);
                SQLiteDatabase db = admin.getWritableDatabase();
                Cursor fila2 = db.rawQuery("SELECT serie,estatus FROM series WHERE almacen = '" + idalmacen+"' AND codigo= '"+model.getCodigoArticulo()+"' AND estatus='D' ",null);
                int contserie=fila2.getCount();
                if(model.getExistencia()!=contserie)
                {
                    holder.diferencia.setTextColor(red);
                }
                Log.i("XD","series");
            }
            else
            {
                holder.diferencia.setTextColor(blue);
            }

        }
        //DecimalFormat df = new DecimalFormat("#.######");
        //holder.diferencia.setText(""+df.format(dife));
        holder.diferencia.setText(""+dife);
        holder.serie.setText(model.getSerie());

        if (model.isSelected())
            holder.linearLayout.setBackgroundResource(R.color.selected_item);
        else
            holder.linearLayout.setBackgroundColor(Color.WHITE);

        //String id = getIdConteo(model.getCodigoArticulo());
        //putDiferencia(id, Integer.parseInt(dif));
        return view;
    }

    public void updateRecords(ArrayList<Inventario> datos){
        this.datos = datos;
        notifyDataSetChanged();
    }

    public String getIdConteo(String codigo) {
        String id_conteo = "", r;
        Database admin = new Database( activity, null, 1);
        SQLiteDatabase db = admin.getWritableDatabase();
        Cursor fila = db.rawQuery("SELECT * FROM conteo WHERE idalmacen = '" + idalmacen+"'",null);
        if(fila.moveToFirst())
        {
            do{
                r=fila.getString(1);
                if(r.equalsIgnoreCase(codigo))
                {
                    id_conteo=fila.getString(0);
                    //Toast.makeText(this,r+" "+codigo+" encontro codigo igual", Toast.LENGTH_SHORT).show();
                }

            }while (fila.moveToNext());

        }
        db.close();
        return id_conteo;
    }

    //HACE LA ACTUALIZACION DE LAS DIFERENCIAS DE LOS ARTÍCULOS DEL CONTEO EN LA BASE DE DATOS DE SQLITE
    public void putDiferencia(String Id, int diferencia)
    {
        Database admin=new Database(activity,null,1);
        SQLiteDatabase db = admin.getWritableDatabase();
        ContentValues registro = new ContentValues();
        registro.put("diferencia",diferencia);
        db.update("conteo",registro,"_id="+ Id +" AND idalmacen='"+ idalmacen +"'",null);
        db.close();
    }
}
