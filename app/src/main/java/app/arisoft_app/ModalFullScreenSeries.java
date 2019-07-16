package app.arisoft_app;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import app.arisoft_app.Interfaz.ServiceExiActual;
import app.arisoft_app.Modelo.ExistenciaActual;
import app.arisoft_app.Modelo.Inventario;
import app.arisoft_app.Modelo.RespExiActual;
import app.arisoft_app.Modelo.Serie;
import app.arisoft_app.Tools.Database;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ModalFullScreenSeries extends DialogFragment {

    public static String TAG = "FullScreenDialog";
    ArrayList<String> arraySerie = new ArrayList<>();
    ListView lv_series;
    ArrayList<Serie> Series;
    private InventarioListaSeriesAdapter seriesAdapter;
    String idalmacen;
    String codigo, nombre;
    TextView codigoArt, nombreArt;
    EditText et_series;
    private String URL;
    LayoutInflater inflater;
    Context contexto=getActivity() ;
    private ProgressDialog progreso;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);
        Bundle bundle = getArguments();
        idalmacen = bundle.getString("idalmacen");
        codigo = bundle.getString("codigo");
        nombre = bundle.getString("desc");
        //mensajes("idalmacen valor ",idalmacen);
        getDomain();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        final Activity activity = getActivity();
        if (activity instanceof DialogInterface.OnDismissListener) {
            ((DialogInterface.OnDismissListener) activity).onDismiss(dialog);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new Dialog(getActivity(), getTheme()){
            @Override
            public void onBackPressed() {
                cerrar();
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        this.inflater = inflater;
        View view = inflater.inflate(R.layout.full_screen_dialog_series, container, false);
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setTitle("Agregar series");
        toolbar.setNavigationIcon(R.drawable.ic_done);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cerrar();
            }
        });

        lv_series = view.findViewById(R.id.lv_series);
        codigoArt = view.findViewById(R.id.tv_codigoArt);
        nombreArt = view.findViewById(R.id.tv_nombreArt);
        et_series = view.findViewById(R.id.et_serie);
        codigoArt.setText(codigo);
        nombreArt.setText(nombre);
        et_series.requestFocus();
        lv_series.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //CLICK EN SERIE
                Log.i("SERIES_POS", "POS " + position + " serie " + Series.get(position).getSerie());
                verSerie(Series.get(position), position);
            }
        });

        et_series.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                //ENTER EN EDITTEXT SERIES
                if(et_series.getText().toString().equalsIgnoreCase("")) {
                    mensajes("Ingresar Serie","");
                }
                else {
                    if(existeSerie(et_series.getText().toString())) {
                        if(serieEstatus(et_series.getText().toString()).equalsIgnoreCase("N"))
                        {
                            compararSerie(et_series.getText().toString(),true);
                        }
                        else
                        {
                            mensajes("Serie ya ha sido capturada.","");
                        }
                        //validar si ya existe serie

                    }
                    else {
                        compararSerie(et_series.getText().toString(),false);
                    }
                }
                return false;
            }
        });
        return view;
    }

    private void cancelUpload() {
    }

    public void cerrar(){
        actualizarConteo();
        getDialog().dismiss();
    }


    public void consultaInicial() {
        Series = new ArrayList<>();
        Database admin = new Database(getActivity(),null,1);
        SQLiteDatabase db = admin.getWritableDatabase();
        Cursor fila = db.rawQuery("SELECT * FROM series WHERE almacen = '"+ idalmacen + "' AND codigo = '" + codigo + "' AND estatus!='N' ",null);
        if(fila.moveToFirst()) {
            do{
                String id = fila.getString(0);
                String serie = fila.getString(1);
                String codigo = fila.getString(2);
                String almacen = fila.getString(3);
                String estado = fila.getString(4);
                Series.add(new Serie(id, serie, codigo, almacen, estado));
            }while (fila.moveToNext());
        }
        else {
            //Toast.makeText(this, "Almacen sin conteo", Toast.LENGTH_SHORT).show();
        }
        db.close();
        seriesAdapter = new InventarioListaSeriesAdapter(getActivity(), Series);
        lv_series.setAdapter(seriesAdapter);
        getDomain();
    }
    public String serieEstatus(String serie) {
        String serieEstatus = "";
        Database admin = new Database(getActivity(),null,1);
        SQLiteDatabase db = admin.getWritableDatabase();
        Cursor fila = db.rawQuery("SELECT estatus FROM series WHERE serie = '"+serie+"' AND codigo = '"+codigo+"' AND almacen = '"+ idalmacen +"' ",null);
        if(fila.moveToFirst()) {
            do{
                serieEstatus=fila.getString(0);

            } while (fila.moveToNext());
        }
        else {
            //Toast.makeText(this, "Almacen sin conteo", Toast.LENGTH_SHORT).show();
        }
        db.close();
        return serieEstatus;
    }

    public boolean existeSerie(String serie) {
        boolean existeSerie = false;
        Database admin = new Database(getActivity(),null,1);
        SQLiteDatabase db = admin.getWritableDatabase();
        Cursor fila = db.rawQuery("SELECT * FROM series WHERE serie = '"+serie+"' AND codigo = '"+codigo+"' AND almacen = '"+ idalmacen +"' ",null);
        if(fila.moveToFirst()) {
            do{
                existeSerie=true;

            } while (fila.moveToNext());
        }
        else {
            //Toast.makeText(this, "Almacen sin conteo", Toast.LENGTH_SHORT).show();
        }
        db.close();
        return existeSerie;
    }

    public void compararSerie(final String serie,final Boolean ExisSerie) {
        final String[] estatus = new String[1];
        ServiceExiActual service=retrofit().create(ServiceExiActual.class);
        Call<RespExiActual> call=service.compSerie(serie,codigo);
        call.enqueue(new retrofit2.Callback<RespExiActual>() {
            @Override
            public void onResponse(Call<RespExiActual> call, Response<RespExiActual> response) {

                if(response.isSuccessful()) {
                    RespExiActual r=response.body();
                    ArrayList<ExistenciaActual> l = r.getExistencia();
                    for (int i=0;i<l.size();i++) {
                        //encontro
                        ExistenciaActual e=l.get(i);
                        String fventa,almacen;
                        fventa=String.valueOf(e.getFventa());
                        almacen=String.valueOf(e.getAlmacen());
                        Log.i("almacen:","mismo almacen "+e.getFventa()+" "+e.getAlmacen()+" "+fventa);
                        if(idalmacen.equalsIgnoreCase(almacen)) {
                            //si almacen es igual
                            if(fventa.equalsIgnoreCase("null")) {
                                //sin fecha de venta//disponible
                                // Log.i("serie:","no vendida");
                                //insertarSerie(serie,"D");
                                estatus[0] ="D";
                            }
                            else {
                                //vendida
                                //con fecha de venta. serie vendida
                                // Log.i("serie:"," vendida");
                                //insertarSerie(serie,"V");
                                estatus[0] ="V";
                            }
                        }
                        else {
                            //si almacen es diferente
                            Log.i("ALMACEN:","almacen diferente " + e.getFventa());
                            if( (e.getFventa() == null) ||  e.getFventa().equalsIgnoreCase("") ) {
                                //insertarSerie(serie,"AO");
                                //sin fecha de venta//otro almacen
                                //  Log.i("serie:","no vendida");
                                estatus[0] ="AO";
                            }
                            else {
                                //insertarSerie(serie,"VO");
                                estatus[0] ="VO";
                                //otro almacen vendida
                                //con fecha de venta. serie vendida
                                //  Log.i("serie:","vendida");
                            }
                        }
                        Log.i("almacen:","|" + idalmacen);
                        Log.i("existencia"," "+e.getFventa()+"|"+e.getAlmacen()+"|");
                    }
                    if(l.size()<1) {
                        //si no encuentra nada
                        //Log.i("serie:","no encontrada");
                        //insertarSerie(serie,"NE");
                        estatus[0] ="NE";
                        //Toast.makeText(getApplicationContext(),"consulta vacia",Toast.LENGTH_SHORT).show();
                    }
                    if(ExisSerie==true)
                    {
                        //modificar
                        modificarSerie(serie,estatus[0].toString());

                    }
                    else
                    {
                        insertarSerie(serie,estatus[0].toString());
                    }

                }
                else {
                    Toast.makeText(getActivity(),"no se encontro",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RespExiActual> call, Throwable t) {
                Log.i("Error:no se encontro ",t.getMessage());
                Toast.makeText(getActivity(),"no se encontro el articulo",Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void insertarSerie(String serie,String estatus) {
        try{
            Database admin=new Database(getActivity(),null,1);
            SQLiteDatabase db = admin.getWritableDatabase();
            ContentValues registro = new ContentValues();
            //String descrip = getDescripcion(codigo);
            //registro.put("id","NULL");
            registro.put("serie",serie);
            registro.put("codigo",codigo);
            registro.put("almacen",idalmacen);
            registro.put("estatus",estatus);
            db.insert("series",null,registro);
            db.close();
            Log.i("INSERTAR_SERIE", ""+idalmacen+" - "+serie+" - "+codigo);
            consultaInicial();
            et_series.setText("");
            et_series.requestFocus();
        }catch (SQLiteException e){
            mensajes("Error", "No se ha podido registrar la serie");
        }

    }
    public void modificarSerie(String serie,String estatus) {
        try{
            Database admin=new Database(getActivity(),null,1);
            SQLiteDatabase db = admin.getWritableDatabase();
            ContentValues registro = new ContentValues();

            registro.put("serie",serie);
            registro.put("estatus",estatus);
            db.update("series",registro,"serie = '"+ serie +"'",null);
            db.close();
            Log.i("ACTUALIZAR_SERIE", ""+idalmacen+" - "+serie+" - "+codigo);
            consultaInicial();
            et_series.setText("");
            et_series.requestFocus();
        }catch (SQLiteException e){
            mensajes("Error", "No se ha podido registrar la serie");
        }

    }

    public void verSerie(Serie articulo, int pos) {
        View v = inflater.inflate(R.layout.dialog_eliminar_serie, null);
        final TextView tvcodigo = v.findViewById(R.id.tv_cod);
        final TextView tvdesc = v.findViewById(R.id.tv_descr);
        final TextView tvserie = v.findViewById(R.id.tv_serie);
        final TextView tvestado = v.findViewById(R.id.tv_estado);
        final Button btn_aceptar = v.findViewById(R.id.btn_aceptar);
        final CheckBox check_eliminar = v.findViewById(R.id.check_drop);
        tvcodigo.setText(codigo);
        tvdesc.setText(nombre);
        final String codigo_estado = articulo.getEstado();
        String estado = "";
        switch (codigo_estado){
            case "D":  estado = "Disponible";
                break;
            case "V":  estado = "Vendida";
                break;
            case "AO":  estado = "En otro almacén. No vendida";
                break;
            case "VO":  estado = "En otro almacén. Vendida";
                break;
            case "NE":  estado = "No existe en MacroPro";
                break;
            default:  estado = "Sin estado";
                break;
        }
        tvestado.setText(estado);
        final String serie = articulo.getSerie();
        final String id = articulo.getId();
        tvserie.setText(serie);
        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(v)
                .create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
        btn_aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (check_eliminar.isChecked()){
                    eliminarSerie(id);
                    dialog.dismiss();
                    consultaInicial();
                }else dialog.dismiss();
            }
        });
        consultaInicial();
    }

    public void eliminarSerie(String id){
        try {
            Database admin = new Database(getActivity(),null,1);
            SQLiteDatabase db = admin.getWritableDatabase();
            db.execSQL("DELETE FROM series WHERE _id = " + Integer.parseInt(id));
            db.close();
        } catch (SQLiteException e){
            Toast.makeText(getActivity().getApplicationContext(),"Error al eliminar serie" + e, Toast.LENGTH_LONG).show();
        }
        Toast.makeText(getActivity().getApplicationContext(),"Serie eliminada", Toast.LENGTH_SHORT).show();
    }

    public void actualizarConteo(){
        try {
            int conteo = 0;
            Database admin = new Database(getActivity(),null,1);
            SQLiteDatabase db = admin.getWritableDatabase();
            ContentValues registro = new ContentValues();
            registro.put("conteo", Series.size());
            db.update("conteo",registro,"codigo = '"+ codigo +"' AND idalmacen = '"+ idalmacen +"'",null);
            db.close();
        } catch (SQLiteException e){
            Toast.makeText(getActivity().getApplicationContext(),"No se pudo actualizar el conteo" + e, Toast.LENGTH_LONG).show();
        }
        Toast.makeText(getActivity().getApplicationContext(),"Conteo actualizado", Toast.LENGTH_LONG).show();
    }


    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        consultaInicial();
        if (dialog != null) {
            //int width = ViewGroup.LayoutParams.MATCH_PARENT;
            //int height = ViewGroup.LayoutParams.MATCH_PARENT;
            //dialog.getWindow().setLayout();
        }
        //seriesAdapter.updateRecords(Series);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater2) {
        inflater2.inflate(R.menu.series_save, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.save) {
            Toast.makeText(getActivity(),"Guardar", Toast.LENGTH_SHORT).show();
            dismiss();
            return true;
        } else if (id == android.R.id.home) {
            /*AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Descartar series");
            builder.setMessage("Los cambios no se guardarán")
                    .setPositiveButton("Descartar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dismiss();
                        }
                    })
                    .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog salir = builder.create();
            salir.show();*/
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void mensajes(String titulo, String mensaje) {
        Toast.makeText(getActivity(),titulo +" "+mensaje,Toast.LENGTH_SHORT).show();
    }

    public Retrofit retrofit() {
      /*  OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();*/

        Retrofit retrofit;
        retrofit=new Retrofit.Builder()
                .baseUrl(URL)
                .addConverterFactory(GsonConverterFactory.create())
                // .client(client)
                .build();
        return retrofit;
    }

    public void getDomain(){
        Database admin = new Database(getActivity(),null,1);
        this.URL = admin.getDomain();
        if (URL.equalsIgnoreCase("N")){
            new AlertDialog.Builder(getActivity())
                    .setTitle("No se ha podido obtener el dominio")
                    .setMessage("Por favor, pongase en contacto con un asesor de Arisoft para poder resolver este problema")
                    .setPositiveButton("aceptar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                        }
                    }).create().show();
        }
    }
    /*@NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }*/
}
