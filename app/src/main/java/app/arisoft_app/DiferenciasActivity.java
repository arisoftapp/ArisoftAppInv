package app.arisoft_app;

import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import app.arisoft_app.Interfaz.ServiceExiActual;
import app.arisoft_app.Interfaz.ServiceModificar;
import app.arisoft_app.Modelo.Costo;
import app.arisoft_app.Modelo.ExistenciaActual;
import app.arisoft_app.Modelo.Inventario;
import app.arisoft_app.Modelo.RespExiActual;
import app.arisoft_app.Modelo.RespuestaExistencia;
import app.arisoft_app.Modelo.respuestaCosto;
import app.arisoft_app.Tools.Database;
import app.arisoft_app.Tools.ISelectedData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DiferenciasActivity extends AppCompatActivity implements ISelectedData {
    public int opc;
    String almacen;
    static String idalmacen;
    String folEntrada;
    String folSalida;
    String params;
    TextView tv_alm,tv_part;
    EditText et_busqueda;
    LinearLayout page,layoutbusqueda;
    ArrayList<String> arrayList=new ArrayList<>();
    ArrayList<Inventario> Inventario;
    InventarioListaDiferenciasAdapter dif_adapter;
    ListView lv_dif;
    MenuItem save, ajustar, drop, edit_cont, opciones,busquedaFiltro;
    private ProgressDialog progreso,progresoc;
    private RequestQueue requestQueue;
    private String serieG,part_dif="";
    private String codigoG;

    //private static final String URL = "http://192.168.1.65:3000/conteo";
    private static final String admin_URL = "http://wsar.homelinux.com:3000/conteo";
    private static final String sinConteo_URL="http://wsar.homelinux.com:3000/conteo";
    private static String client_URL;
    static String strUrl="http://192.168.1.65:3001/almacenes";
    int posPartEnt;
    int posPartSal,consultaAlmView=0;
    boolean validarEntrada,validarSalida;
    protected PowerManager.WakeLock wakelock;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diferencias);
        //evitar que pantalla se apague
        final PowerManager pm=(PowerManager)getSystemService(this.POWER_SERVICE);
        this.wakelock=pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "etiqueta");
        wakelock.acquire();
        //ttermina

        eliminarTabla("sinconteo");
        optionsDialog();
        getDomain();

        requestQueue = Volley.newRequestQueue(this);
        lv_dif = (ListView) findViewById(R.id.lista_diferencias);
        tv_part = (TextView)findViewById(R.id.tv_part);
        layoutbusqueda=(LinearLayout)findViewById(R.id.layoutbusqueda);
        et_busqueda=(EditText)findViewById(R.id.et_busqueda);
        page =  (findViewById(R.id.linear_alm));
        try{
            Bundle bundle = getIntent().getExtras();
            idalmacen = bundle.getString("idalmacen");
            almacen = bundle.getString("almacen");
            if(bundle!=null){
                tv_alm = page.findViewById(R.id.tv_alm);
                tv_alm.setText(almacen);
            }
        }catch (Exception e){
            e.printStackTrace();
        }


        lv_dif.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Inventario model = Inventario.get(position);
                if (model.getSerie().equalsIgnoreCase("S")){
                    FragmentDetalleSeriesDiferenciasModal dialog = new FragmentDetalleSeriesDiferenciasModal();
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    Bundle b = new Bundle();
                    b.putString("idalmacen", idalmacen);
                    b.putString("codigo", model.getCodigoArticulo());
                    b.putString("desc", model.getDescripcion());
                    b.putFloat("exist", model.getExistencia());
                    b.putFloat("cont", model.getConteo());
                    b.putFloat("dif", getDiferencia(model.getCodigoArticulo()));
                    Log.i("serie",""+idalmacen+" "+model.getCodigoArticulo()+" "+model.getConteo()+" "+getDiferencia(model.getCodigoArticulo()));
                    dialog.setArguments(b);
                    dialog.show(ft, ModalFullScreenSeries.TAG);
                }
            }
        });

        lv_dif.setLongClickable(true);
        lv_dif.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                dialogComentarios(position);
                Inventario.get(position).getComentario();
                Log.i("comentarioLong",""+Inventario.get(position).getComentario());
                return false;
            }
        });

        et_busqueda.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtroDiferencias(et_busqueda.getText().toString().trim());
/*
                if(et_busqueda.getText().toString().trim().equalsIgnoreCase(""))
                {
                    obtenerDatos();
                }
                else
                {
                    filtroDiferencias(et_busqueda.getText().toString().trim());
                }
                */
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    public void filtroDiferencias(String texto)
    {
        //Toast.makeText(getApplicationContext(),""+texto,Toast.LENGTH_SHORT).show();

        try {
            Inventario = new ArrayList<Inventario>();
            Database admin = new Database(this,null,1);
            String r;
            SQLiteDatabase db = admin.getWritableDatabase();
            Cursor fila = db.rawQuery("SELECT * FROM conteo WHERE idalmacen='"+idalmacen+"'",null);
            if(fila.moveToFirst())
            {
                do{

                    String codigo = fila.getString(1);
                    arrayList.add(codigo);
                    String descrip = fila.getString(2);
                    String conteo = fila.getString(3);
                    arrayList.add(conteo);
                    String existencia = fila.getString(4);
                    arrayList.add(existencia);
                    String serie = fila.getString(6);
                    String comentario= fila.getString(9);
                    if(codigo.contains(texto))
                    {
                        Log.i("x",""+codigo);
                        Inventario.add(0,new Inventario(false, codigo, descrip, conteo, existencia, serie,comentario));
                    }

                }while (fila.moveToNext());
            }
            Cursor fila2 = db.rawQuery("SELECT * FROM sinconteo WHERE idalmacen='"+idalmacen+"'",null);
            if(fila2.moveToFirst())
            {
                do{

                    String codigo = fila2.getString(1);
                    arrayList.add(codigo);
                    String descrip = fila2.getString(2);
                    String conteo = fila2.getString(3);
                    arrayList.add(conteo);
                    String existencia = fila2.getString(4);
                    arrayList.add(existencia);
                    String serie = fila2.getString(6);
                    String comentario= fila2.getString(9);
                    if(codigo.contains(texto))
                    {
                        Log.i("x",""+codigo);
                        Inventario.add(0,new Inventario(false, codigo, descrip, conteo, existencia, serie,comentario));
                    }

                }while (fila2.moveToNext());
            }
            db.close();
            dif_adapter = new InventarioListaDiferenciasAdapter(DiferenciasActivity.this, Inventario, idalmacen, 0);
            lv_dif.setAdapter(dif_adapter);
        }catch (Exception e)
        {
            Toast.makeText(getApplicationContext(),""+e.getMessage(),Toast.LENGTH_SHORT).show();
        }


    }
    protected void onDestroy(){
        super.onDestroy();

        this.wakelock.release();
    }
    protected void onResume(){
        super.onResume();
        wakelock.acquire();
    }
    public void onSaveInstanceState(Bundle icicle) {
        super.onSaveInstanceState(icicle);
        this.wakelock.release();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater infalter = getMenuInflater();
        infalter.inflate(R.menu.diferencias_options, menu);
        menu.findItem(R.id.drop).setVisible(false);
        edit_cont = menu.findItem(R.id.edit);
        opciones = menu.findItem(R.id.opciones);
        busquedaFiltro = menu.findItem(R.id.busqueda) ;
        return true;
    }

    public void dialogComentarios(int position)
    {
        final int pos=position;
        Log.i("comentario"," tamaño:"+pos);

            LayoutInflater inflater = DiferenciasActivity.this.getLayoutInflater();
            View v = inflater.inflate(R.layout.dialog_comentarios, null);
            final TextView tvcodigo = v.findViewById(R.id.tv_cod);
            final TextView tvdesc = v.findViewById(R.id.tv_desc);
            final EditText coment=v.findViewById(R.id.comentarios);
            final ImageButton ibLimpiar=v.findViewById(R.id.ib_limpiar);
            final Inventario model = Inventario.get(pos);

            ibLimpiar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    coment.setText("");
                }
            });


                final String codigo = model.getCodigoArticulo();
                final String desc=model.getDescripcion();
                tvcodigo.setText(codigo);
                tvdesc.setText(desc);
                Database admin = new Database(this,null,1);
                SQLiteDatabase db = admin.getWritableDatabase();
                Cursor fila = db.rawQuery("SELECT comentarios FROM conteo WHERE idalmacen ='"+idalmacen+"' AND codigo='"+codigo+"'  ",null);
                if(fila.moveToFirst())
                {

                    Log.i("comentarios:","conteo");
                    do{
                        coment.setText(fila.getString(0));
                    }while (fila.moveToNext());
                    db.close();
                }
                else
                {
                    fila = db.rawQuery("SELECT comentarios FROM sinconteo WHERE idalmacen ='"+idalmacen+"' AND codigo='"+codigo+"'  ",null);
                    if(fila.moveToFirst())
                    {

                        Log.i("comentarios:","sinconteo");
                        do{
                            coment.setText(fila.getString(0));
                        }while (fila.moveToNext());
                        db.close();
                    }
                }


                AlertDialog dialog = new AlertDialog.Builder(DiferenciasActivity.this)
                        .setTitle("Comentarios")
                        .setView(v)
                        .setPositiveButton("Guardar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Database admin=new Database(DiferenciasActivity.this,null,1);
                                SQLiteDatabase db = admin.getWritableDatabase();
                                ContentValues registro = new ContentValues();
                                registro.put("comentarios",coment.getText().toString());
                                Inventario.get(pos).setComentario(coment.getText().toString());
                                db.update("conteo",registro,"codigo='"+ codigo +"' AND idalmacen='"+ idalmacen +"'",null);
                                db.update("sinconteo",registro,"codigo='"+ codigo +"' AND idalmacen='"+ idalmacen +"'",null);
                                db.close();
                                Log.i("EDITAR_CONTEO", "SALIR DE POSITIVE BUTTON");
                            }
                        })
                        .setNegativeButton("Cancelar", null)
                        .create();
                dialog.show();



    }
    public void optionsDialog() {
            FragmentOpcionesDiferenciasDialog dialog = new FragmentOpcionesDiferenciasDialog();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            dialog.show(ft, ModalFullScreenSeries.TAG);
    }

    public void obtenerDatos() {

        //Toast.makeText(this,"obtener datos" , Toast.LENGTH_SHORT).show();
        switch (params){
            case "00":  Toast.makeText(this,"Sólo conteo - mostrar todo" , Toast.LENGTH_SHORT).show();
                        //consultaConteo(0);//Sólo conteo - mostrar todo
                        consultaAlmView=0;
                        new Tarea3().execute();
                        break;
            case "01":  Toast.makeText(this,"Sólo conteo - sólo diferencias" , Toast.LENGTH_SHORT).show();
                        //consultaConteo(1);//Sólo conteo - sólo diferencias
                        consultaAlmView=1;
                        new Tarea3().execute();
                        break;
            case "10":  Toast.makeText(this,"Conteo + ALM - Mostrar todo" , Toast.LENGTH_SHORT).show();
                        if(validarSinConteo()==true)
                        {
                            consultaAlmView=0;
                            new Tarea2().execute();
                        }
                        else
                        {
                            consultaAlmView=0;
                            new Tarea1().execute();
                        }

                        //consultaAlm(0);//Conteo + ALM - Mostrar todo
                        break;
            case "11":  Toast.makeText(this,"Conteo + ALM - Sólo diferencias" , Toast.LENGTH_SHORT).show();
                        if(validarSinConteo()==true)
                        {
                            consultaAlmView=1;

                            new Tarea2().execute();
                        }
                        else
                        {
                            consultaAlmView=1;
                            new Tarea1().execute();
                        }
                        //consultaAlm(1);//Conteo + ALM - Sólo diferencias
                        break;//
        }
    }
    public boolean validarSinConteo()
    {
        boolean validar=false;
        Database admin = new Database(this,null,1);
        SQLiteDatabase db = admin.getWritableDatabase();
        Cursor fila = db.rawQuery("select * from sinconteo where idalmacen='"+idalmacen+"'",null);
        if(fila.moveToFirst())
        {
            validar=true;
        }
        else
        {
            validar=false;
            //tv1.setText("No datos");
            //Toast.makeText(this, "No existe un artículo con el código indicado", Toast.LENGTH_SHORT).show();
        }
        db.close();
        return validar;

    }

    public void sinConteo()
    {
        ServiceExiActual service=retrofit().create(ServiceExiActual.class);
        Call<RespExiActual> call=service.soloExi("1");
        call.enqueue(new retrofit2.Callback<RespExiActual>() {
            @Override
            public void onResponse(Call<RespExiActual> call, Response<RespExiActual> response) {
                if(response.isSuccessful())
                {
                    Toast.makeText(getApplicationContext(),"sin conteo"+" "+response.code(),Toast.LENGTH_SHORT).show();
                    RespExiActual r=response.body();
                    ArrayList<ExistenciaActual> l = r.getExistencia();
                    for (int i=0;i<l.size();i++) {
                        ExistenciaActual e=l.get(i);
                        //Log.i("sinconteo:"," "+e.getCodigo()+"|"+e.getDescripcion()+"|"+e.getSerie()+"|"+e.getExistenciaActual());
                      //  if(consultaSinConteo(e.getCodigo())==false)
                        //{
                            //insertar en tabla sin conteo
                            Log.i("sincon:",e.getCodigo()+" falso "+i);
                            float exiMacro=Float.parseFloat(e.getExistenciaActual());
                            insertarSinConteo(e.getCodigo(),0,exiMacro,e.getDescripcion(),e.getSerie());

                        //}
                       /* if(i==l.size()-1)
                        {
                            consultaAlm(0);
                        }*/
                    }
                }
            }

            @Override
            public void onFailure(Call<RespExiActual> call, Throwable t) {
                Toast.makeText(getApplicationContext(),"Error modexi:"+t.getMessage(),Toast.LENGTH_SHORT).show();
                Log.i("Error modexi:",t.getMessage());
            }
        });
    }
    public boolean consultaSinConteo(String codigo)
    {
        Boolean existe=false;
        Database admin = new Database(this,null,1);
        SQLiteDatabase db = admin.getWritableDatabase();
        Cursor fila = db.rawQuery("SELECT * FROM conteo WHERE codigo = '" + codigo + "' AND idalmacen = '" + idalmacen+"'",null);
        if(fila.moveToFirst()) {
            existe=true;
             //Log.i("sinconteoconsulta:",""+ fila.getString(1)+" |"+existe);
        }
        else {
            existe=false;
            //Toast.makeText(this, "No se encontró diferencia de serie", Toast.LENGTH_SHORT).show();
            //Log.i("sinconteoconsulta:",""+existe);
        }

        return existe;

    }
    public void insertarSinConteo(String codigo, float conteo, float existencia, String descrip, String serie) {
        Database admin=new Database(this,null,1);
        SQLiteDatabase db = admin.getWritableDatabase();
        ContentValues registro = new ContentValues();
        //String descrip = getDescripcion(codigo);
        //registro.put("id","NULL");
        registro.put("codigo",codigo);
        registro.put("descripcion",descrip);
        registro.put("conteo",conteo);
        registro.put("existencia",existencia);
        registro.put("idalmacen", idalmacen);
        registro.put("serie",serie);
        registro.put("estatus","S");
        registro.put("diferencia",existencia);
        db.insert("sinconteo",null,registro);
        db.close();
        //Log.i("INSERTAR_CONTEO", ""+codigo+" - "+descrip+" - "+conteo+" - "+existencia+" - "+serie);
    }

    public void consultaConteo(Integer view) {
        //progreso.setMessage("Consultando Almacen conteo...");
        //progreso.setProgress(0);
        //pruebaDialog();
        Inventario = new ArrayList<Inventario>();
        Database admin = new Database(this,null,1);
        String Id;
        int con=1;
        SQLiteDatabase db = admin.getWritableDatabase();
        Cursor fila = db.rawQuery("SELECT * FROM conteo WHERE idalmacen = '" + idalmacen +"' ",null);
        if(fila.moveToFirst()) {
            do{
                //progreso.setMax(fila.getCount());
                //progreso.setProgress(con);
                con++;
                String id = fila.getString(0);
                String codigo = fila.getString(1);
                arrayList.add(codigo);
                String descrip = fila.getString(2);
                String conteo = fila.getString(3);
                arrayList.add(conteo);
                String existencia = fila.getString(4);
                arrayList.add(existencia);
                String serie=fila.getString(6);
                String comentario=fila.getString(9);
                //Toast.makeText(this, "codigo con serie "+serie, Toast.LENGTH_SHORT).show();
                Log.i("solo conteo: ",codigo+""+con);
                app.arisoft_app.Modelo.Inventario Articulo;
                if(serie.equalsIgnoreCase("S"))
                {
                    Integer cont = consultaSeries(codigo, idalmacen);
                    Articulo = new Inventario(false, codigo, descrip, cont.toString(), existencia, serie,comentario);
                    putDiferencia(id, Articulo.getDiferencia());
                } else {
                    Articulo = new Inventario(false, codigo, descrip, conteo, existencia, serie,comentario);
                    putDiferencia(id, Articulo.getDiferencia());
                }

                if (view == 1){ //SOLO DIFERENCIAS
                    Log.i("solo dif conteo: ",""+Articulo.getDiferencia());
                    if (Articulo.getDiferencia() != 0){
                        Inventario.add(Articulo);
                    }
                }
                else
                {
                    Inventario.add(Articulo);
                }
            }while (fila.moveToNext());
        }
        else {
            Toast.makeText(this, "Almacen sin conteo", Toast.LENGTH_SHORT).show();
        }
        db.close();
        dif_adapter = new InventarioListaDiferenciasAdapter(this, Inventario, idalmacen, 0);
        lv_dif.setAdapter(dif_adapter);
        consultaTablaSerie();
        //progresoc.dismiss();
        //peticion
    }

    public void consultaAlm(Integer view){

        int con=1;
        consultaConteo(view);
        //progreso.setMessage("Consultando Almacen sinconte");
        //progreso.setProgress(0);
        //pruebaDialog();
        Database admin = new Database(this,null,1);
        String Id;
        SQLiteDatabase db = admin.getWritableDatabase();
        Cursor fila = db.rawQuery("SELECT * FROM sinconteo WHERE idalmacen = '" + idalmacen +"'",null);
        if(fila.moveToFirst()) {
            //barra("prueba");
            do{
                //progreso.setMax(fila.getCount());
                //progreso.setProgress(con);
                con++;
                String id = fila.getString(0);
                String codigo = fila.getString(1);
                arrayList.add(codigo);
                String descrip = fila.getString(2);
                String conteo = fila.getString(3);
                arrayList.add(conteo);
                String existencia = fila.getString(4);
                arrayList.add(existencia);
                String serie=fila.getString(6);
                String comentario=fila.getString(9);
                //Toast.makeText(this, "codigo con serie "+serie, Toast.LENGTH_SHORT).show();
                //Log.i("solo : ",codigo+""+con);
                app.arisoft_app.Modelo.Inventario Articulo;
                if(serie.equalsIgnoreCase("S"))
                {
                    Integer cont = consultaSeries(codigo, idalmacen);
                    Articulo = new Inventario(false, codigo, descrip, cont.toString(), existencia, serie,comentario);
                    putDiferencia(id, Articulo.getDiferencia());
                } else {
                    Articulo = new Inventario(false, codigo, descrip, conteo, existencia, serie,comentario);
                    putDiferencia(id, Articulo.getDiferencia());
                }

                if (view == 1){ //SOLO DIFERENCIAS
                    //Log.i("solo dif: ",""+Articulo.getDiferencia());
                    if (Articulo.getDiferencia() != 0){
                        Inventario.add(Articulo);
                    }
                }
                else
                {
                    Inventario.add(Articulo);
                }
            }while (fila.moveToNext());
        }
        else {
            Toast.makeText(this, "Almacen sin conteo", Toast.LENGTH_SHORT).show();
        }
        db.close();
        dif_adapter = new InventarioListaDiferenciasAdapter(this, Inventario, idalmacen, 0);
        lv_dif.setAdapter(dif_adapter);
        consultaTablaSerie();
        //progresoc.dismiss();
    }

    public void putDiferencia(String Id, Float diferencia) {
        Database admin=new Database(this,null,1);
        SQLiteDatabase db = admin.getWritableDatabase();
        ContentValues registro = new ContentValues();
        registro.put("diferencia",diferencia);
        db.update("conteo",registro,"_id="+ Id +" AND idalmacen='"+ idalmacen +"'",null);
        db.close();
    }
    public Integer consultaSeriesReal(String codigo, String idalmacen) {
        Integer conteo = 0;
        Database admin = new Database(this,null,1);
        SQLiteDatabase db = admin.getWritableDatabase();
        Cursor fila = db.rawQuery("SELECT estatus FROM series WHERE codigo = '" + codigo + "' AND almacen = " + idalmacen + "",null);
        if(fila.moveToFirst()) {
            do{
                String status = fila.getString(0);
                if (status.equalsIgnoreCase("D")){
                    conteo = conteo + 1;
                }

            } while (fila.moveToNext());
        }
        else {
            //Toast.makeText(this, "Almacen sin conteo", Toast.LENGTH_SHORT).show();
        }
        return conteo;
    }

    public Integer consultaSeries(String codigo, String idalmacen) {
        Integer conteo = 0;
        Database admin = new Database(this,null,1);
        SQLiteDatabase db = admin.getWritableDatabase();
        Cursor fila = db.rawQuery("SELECT estatus FROM series WHERE codigo = '" + codigo + "' AND almacen = '" + idalmacen + "' AND estatus!='N' ",null);
        if(fila.moveToFirst()) {
            do{
                String status = fila.getString(0);
                /*
                if (status.equalsIgnoreCase("D")){
                    conteo = conteo + 1;
                }*/
                conteo=conteo+1;
            } while (fila.moveToNext());
        }
        else {
            //Toast.makeText(this, "Almacen sin conteo", Toast.LENGTH_SHORT).show();
        }
        return conteo;
    }

    public int getDiferencia(String codigo){
        int dif = 0;
        Database admin = new Database(this,null,1);
        SQLiteDatabase db = admin.getWritableDatabase();
        Cursor fila = db.rawQuery("SELECT diferencia FROM conteo WHERE codigo = '" + codigo + "' AND idalmacen = '" + idalmacen+"'",null);
        if(fila.moveToFirst()) {
            dif = fila.getInt(0);

        }
        else {
            Toast.makeText(this, "No se encontró diferencia de serie", Toast.LENGTH_SHORT).show();
        }
        return dif;
    }

    public void compararSeries(String serie, String codigo) {
        ServiceExiActual service=retrofit().create(ServiceExiActual.class);

        Call<RespExiActual> call=service.compSerie(serie,codigo);
        call.enqueue(new retrofit2.Callback<RespExiActual>() {
            @Override
            public void onResponse(Call<RespExiActual> call, Response<RespExiActual> response) {
                //String x="";
                if(response.isSuccessful())
                {
                    //encontrarExiMacro = true;
                    RespExiActual r=response.body();
                    ArrayList<ExistenciaActual> l = r.getExistencia();
                    for (int i=0;i<l.size();i++)
                    {
                        //encontro
                        ExistenciaActual e=l.get(i);

                        if(idalmacen==e.getAlmacen())
                        {
                            //si almacen es igual
                           // Log.i("almacen:","mismo almacen");
                            if(e.getFventa().equalsIgnoreCase(""))
                            {
                                //sin fecha de venta//disponible
                               // Log.i("serie:","no vendida");
                                estatusSerie("D");
                            }
                            else
                            {
                                //vendida
                                //con fecha de venta. serie vendida
                               // Log.i("serie:"," vendida");
                                estatusSerie("V");
                            }
                        }
                        else
                        {
                            //si almacen es diferente
                          //  Log.i("almacen:","almacen diferente");
                            if(e.getFventa().equalsIgnoreCase(""))
                            {
                                estatusSerie("AD");
                                //sin fecha de venta//otro almacen
                              //  Log.i("serie:","no vendida");
                            }
                            else
                            {
                                estatusSerie("AD");
                                //otro almacen vendida
                                //con fecha de venta. serie vendida
                              //  Log.i("serie:","vendida");
                            }
                        }
                        Log.i("almacen:",almacen+"|"+idalmacen);
                        Log.i("existencia"," "+e.getFventa()+"|"+e.getAlmacen()+"|");


                    }
                    if(l.size()<1)
                    {
                        //si no encuentra nada
                        Log.i("serie:","no encontrada");
                        //Toast.makeText(getApplicationContext(),"consulta vacia",Toast.LENGTH_SHORT).show();
                    }


                }
                else
                {
                    Toast.makeText(getApplicationContext(),"no se encontro",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RespExiActual> call, Throwable t) {

                Log.i("Error:no se encontro ",t.getMessage());
                Toast.makeText(getApplicationContext(),"no se encontro el articulo",Toast.LENGTH_SHORT).show();
            }

        });

    }

    public void estatusSerie(String estatus) {
        Toast.makeText(this, "modificar status "+estatus, Toast.LENGTH_SHORT).show();
        Database admin=new Database(this,null,1);
        SQLiteDatabase db = admin.getWritableDatabase();
        ContentValues r = new ContentValues();
        r.put("estatus",estatus);
        //tv1.setText(codProd+"/"+codProd2+"/"+codDesc+"/"+clas+"/"+fechaAlta+"/"+fechaMod+"/"+idalmacen+"/"+nomAlmacen+"/"+existenciaActual);
        db.update("series",r,"serie='"+serieG+"' AND codigo='"+codigoG+"'",null);
        db.close();
    }

    public void consultaTablaSerie() {
        Database admin = new Database(this,null,1);
        SQLiteDatabase db = admin.getWritableDatabase();
        Cursor fila = db.rawQuery("SELECT * FROM series ",null);
        if(fila.moveToFirst())
        {
            do{
                String serie = fila.getString(0);
                String estatus=fila.getString(3);
                Log.i("Serie:",serie+" |"+estatus+" |"+fila.getString(1)+" |"+fila.getString(2));
            }while (fila.moveToNext());
        }
        else
        {
            //Toast.makeText(this, "Almacen sin conteo", Toast.LENGTH_SHORT).show();
        }
    }

    public Retrofit retrofit() {
      /*  OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();*/

        Retrofit retrofit;
        retrofit=new Retrofit.Builder()
                .baseUrl(client_URL)
                .addConverterFactory(GsonConverterFactory.create())
                // .client(client)
                .build();
        return retrofit;
    }

    public void getDomain(){
        Database admin = new Database(this,null,1);
        this.client_URL = admin.getDomain();
        if (client_URL.equalsIgnoreCase("N")){
            new AlertDialog.Builder(this)
                    .setTitle("No se pudo obtener el dominio")
                    .setMessage("Por favor, pongase en contacto con un asesor de Arisoft para poder resolver este problema")
                    .setPositiveButton("aceptar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                        }
                    }).create().show();
        }
    }

    public String getToken(){
        String token;
        Database admin = new Database(this,null,1);
        token = admin.getToken();
        if (token.equalsIgnoreCase("N")){
            new AlertDialog.Builder(this)
                    .setTitle("No se pudo obtener el token")
                    .setMessage("Por favor, pongase en contacto con un asesor de Arisoft para poder resolver este problema")
                    .setPositiveButton("aceptar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                        }
                    }).create().show();
        }
        return token;
    }

    public void GuardarConteo(){
        progreso = new ProgressDialog(this);
        progreso.setMessage("Iniciando...");
        progreso.show();
        final String token = getToken();
        JSONArray conteo = new JSONArray();
        JSONArray series = new JSONArray();
        String tipoinv="";
        String idinv="";

        for (int i=0; i < Inventario.size(); i++){
            JSONObject producto = new JSONObject();
            Inventario model = Inventario.get(i);
            try {
                producto.put("cod_prod", model.getCodigoArticulo());
                producto.put("descripcion", model.getDescripcion());
                producto.put("existencia", model.getExistencia());
                producto.put("conteo", model.getConteo());
                producto.put("diferencia", model.getDiferencia());
                producto.put("es_series", model.getSerie());
                producto.put("comentario",model.getComentario());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            conteo.put(producto);
        }
        Database admin = new Database(this.getApplicationContext(),null,1);
        SQLiteDatabase db = admin.getWritableDatabase();
        Cursor fila = db.rawQuery("SELECT * FROM series WHERE almacen = '" + idalmacen+"'" ,null);
        if(fila.moveToFirst()) {
            do{
                JSONObject serie = new JSONObject();
                try {
                    serie.put("cod_prod", fila.getString(2));
                    serie.put("serie", fila.getString(1));
                    serie.put("estatus", fila.getString(4));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                series.put(serie);

            }while (fila.moveToNext());
        }
        else {
            //Toast.makeText(this, "Almacen sin conteo", Toast.LENGTH_SHORT).show();
        }
        fila = db.rawQuery("SELECT tipo,id FROM tipoInventario WHERE idalmacen = '" + idalmacen+"'" ,null);
        if(fila.moveToNext())
        {
            tipoinv=fila.getString(0);
            idinv=fila.getString(1);
            Log.i("tipoinv: ",""+tipoinv+" "+idinv);
            //Toast.makeText(getApplicationContext(),"" + tipoinv ,Toast.LENGTH_LONG).show();
        }
        db.close();


        final JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("alm", Integer.parseInt(idalmacen));
            jsonParams.put("nombre_alm", almacen);
            jsonParams.put("params", conteo);
            jsonParams.put("series", series);
            jsonParams.put("tipo_inv",tipoinv);
            jsonParams.put("id_inv",idinv);

        }catch (JSONException e) {
            e.printStackTrace();
        }

        Log.i("PARAMETROS", "" + jsonParams);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, admin_URL, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("RESPUESTA JSON: ",""+ response);
                try {
                    JSONObject json = new JSONObject(response);
                    if (json.names().get(0).equals("success")) {
                        if (json.getBoolean("success")) {
                            Log.i("LOGIN_SUCCESS","CAN GET SUCCESS");
                            Toast.makeText(getApplicationContext(),"" + json.getString("message") ,Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                            startActivity(intent);
                            progreso.dismiss();
                            finish();
                        } else{
                            Log.i("LOGIN_ERROR","CAN NOT GET SUCCESS");
                            Toast.makeText(getApplicationContext(),"" + json.getString("message") ,Toast.LENGTH_LONG).show();
                            progreso.dismiss();
                        }
                    }
                } catch (JSONException e) {
                    Log.i("LOGIN_ERROR","JSON EXCEPTION " + e);
                    progreso.dismiss();
                    Toast.makeText(getApplicationContext(),"" + e ,Toast.LENGTH_LONG).show();
                }
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) { progreso.dismiss(); }
        })  {
            @Override
            public byte[] getBody() throws AuthFailureError { return jsonParams.toString().getBytes(); }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("x-access-token", token);
                headers.put("Content-Type", "application/json");
                return headers;
            }


            @Override
            public String getBodyContentType() { return "application/json"; }
        };
        requestQueue.add(stringRequest);
        requestQueue.start();
    }

    public void sinConteoVolley()
    {
        Toast.makeText(this, "entro sin conteo", Toast.LENGTH_SHORT).show();
        Log.i("sinconteo",client_URL+"sinconteo");
        String codigosW="";
        progreso = new ProgressDialog(this);
        progreso.setMessage("Iniciando...");
        progreso.show();
        JSONArray conteo = new JSONArray();
        Database admin = new Database(this.getApplicationContext(),null,1);
        SQLiteDatabase db = admin.getWritableDatabase();
        Cursor fila = db.rawQuery("SELECT codigo FROM conteo WHERE idalmacen='"+idalmacen+"'" ,null);
        int i=0;
        if(fila.moveToFirst()) {

            do{
                JSONObject articulos = new JSONObject();
                try {
                    i++;
                    articulos.put("cod_prod", fila.getString(0));
                    codigosW=codigosW+"'"+fila.getString(0)+"'";
                    if(i<fila.getCount())
                    {
                        codigosW=codigosW+",";
                    }

                    //Log.i("json:",fila.getString(0));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                conteo.put(articulos);
            }while (fila.moveToNext());
            Log.i("StringCodigo",""+fila.getCount()+"||"+codigosW);

        }
        else {
            //Toast.makeText(this, "Almacen sin conteo", Toast.LENGTH_SHORT).show();
        }
        db.close();
        //progreso.dismiss();
        final JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("alm", 1);//Integer.parseInt(idalmacen));
            jsonParams.put("params", conteo);
        }catch (JSONException e) {
            e.printStackTrace();
        }
        String URL_sin=client_URL+"sinconteo";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL_sin+"/"+idalmacen+"/"+codigosW, new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("RESPUESTA JSON: ",""+ response);
                try {
                    JSONObject json = new JSONObject(response);
                    if (json.names().get(0).equals("success")) {
                        if (json.getBoolean("success")) {
                            JSONArray sinconteo=json.getJSONArray("data");
                            Log.i("LOGIN_SUCCESS","CAN GET SUCCESS");

                            for(int i=0;i<sinconteo.length();i++)
                            {
                                JSONObject json_data = sinconteo.getJSONObject(i);
                                //Log.i("jsonArray:",sinconteo.getString(i));
                                //Log.i("json",""+json_data.getString("codigo")+"   "+i);
                                insertarSinConteo(json_data.getString("codigo"),0,json_data.getLong("existenciaActual"),json_data.getString("descripcion"),json_data.getString("serie"));
                            }
                            switch (params){
                                case "10":
                                    consultaAlm(0);
                                    break;
                                case "11":
                                    consultaAlm(1);//Conteo + ALM - Sólo diferencias
                                    break;//
                            }

                            Toast.makeText(getApplicationContext(),"" + json.getString("message") ,Toast.LENGTH_LONG).show();
                            progreso.dismiss();
                            //finish();
                        } else{
                            Log.i("LOGIN_ERROR","CAN NOT GET SUCCESS");
                            Toast.makeText(getApplicationContext(),"" + json.getString("message") ,Toast.LENGTH_LONG).show();
                            progreso.dismiss();
                        }
                    }
                } catch (JSONException e) {
                    Log.i("LOGIN_ERROR","JSON EXCEPTION " + e);
                    progreso.dismiss();
                    Toast.makeText(getApplicationContext(),"" + e ,Toast.LENGTH_LONG).show();
                }
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) { progreso.dismiss(); }
        })  {
            @Override
            public byte[] getBody() throws AuthFailureError { return jsonParams.toString().getBytes(); }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }


            @Override
            public String getBodyContentType() { return "application/json"; }
        };
        try{
            requestQueue.add(stringRequest);
            requestQueue.start();
        }catch (Exception ex)
        {
            Log.e("peticion",""+ex);
        }



    }

    class Tarea1 extends AsyncTask<String,String,String>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            barra("Consultando...");
            //Toast.makeText(getApplicationContext(), "Iniciando", Toast.LENGTH_SHORT).show();

        }
        @Override
        protected String doInBackground(String... params)
        {
            try {
/*
                 URL url = new URL(client_URL+"sinconteo/"+idalmacen+"/"+codigosW);
                 HttpURLConnection con = (HttpURLConnection) url.openConnection();
                 con.setRequestMethod("GET");
                 con.connect();
                 BufferedReader bf= new BufferedReader(new InputStreamReader(con.getInputStream()));
                 String value = bf.readLine();
                 */
                HttpClient cliente = new DefaultHttpClient();
                /* Definimos la ruta al servidor. */
                String value="Fallo";
                HttpParams httpParameters = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParameters, 900000);
                HttpConnectionParams.setSoTimeout(httpParameters, 900000);
                HttpGet htpoget = new HttpGet(client_URL+"SoloExistencia/"+idalmacen);
                //HttpGet htpoget = new HttpGet(URL+"almacenes");
                org.apache.http.HttpResponse resx = cliente.execute(htpoget);
                BufferedReader bfr = new BufferedReader(new InputStreamReader(resx.getEntity().getContent()));
                StringBuffer stb = new StringBuffer("");
                String linea="";
                StringBuffer res = new StringBuffer();
                while ((linea =bfr.readLine())!=null)
                {
                    res.append(linea);
                }
                String finalJSON = res.toString();
                int c=1;
                JSONObject jObject = new JSONObject(finalJSON); //Obtenemos el JSON global
                JSONArray jArray = jObject.getJSONArray("existencia"); //Obtenemos el array results
                progreso.setMax(jArray.length());
                for (int i=0; i < jArray.length(); i++) //Miramos en todos los objetos del array de objetos results
                {
                    try {
                        //Log.i("array",jArray.getJSONObject(i).toString());
                        JSONObject objeto = jArray.getJSONObject(i); //Obtenemos cada uno de los objetos del array results
                        //Log.i("array2",objeto.getString("codigo")+" | "+c++);
                        if (buscarcodigoconteo(objeto.getString("codigo"),idalmacen)==false)
                        {
                            progreso.setProgress(i+1);
                            insertarSinConteo(objeto.getString("codigo"),0,objeto.getInt("existenciaActual"),objeto.getString("descripcion"),objeto.getString("serie"));
                        }
                    } catch (JSONException e) {
                        Log.e("error",e.getMessage());
                    }
                }
                //String codigo = json.getString("codigo");


                bfr.close();
                value=stb.toString();

                Log.i("valor",""+value);
                //consulta conteo
                //consulta sin conteo

            }
            catch (Exception e)
            {
                progreso.dismiss();
                Log.e("Error",""+e.getMessage());
            }
            return null;
        }

        protected void onPostExecute(String s)
        {
            progreso.dismiss();
            //barra("prueba");
            new Tarea2().execute();
            super.onPostExecute(s);
            //Toast.makeText(getApplicationContext(), "finalizo", Toast.LENGTH_SHORT).show();
        }
    }

    class Tarea2 extends AsyncTask<String,String,String>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            barra("Consulta Conteo...");
            //Toast.makeText(getApplicationContext(), "Iniciando", Toast.LENGTH_SHORT).show();

        }
        @Override
        protected String doInBackground(String... params)
        {
            try {
                Inventario = new ArrayList<Inventario>();
                Database admin = new Database(getApplicationContext(),null,1);
                String Id;
                int con=1;
                int con_part=0;
                SQLiteDatabase db = admin.getWritableDatabase();

                Cursor fila = db.rawQuery("SELECT * FROM conteo WHERE idalmacen = '" + idalmacen+"'",null);
                if(fila.moveToFirst()) {
                    progreso.setMax(fila.getCount());
                    //part_dif=""+fila.getCount();
                    do{
                        progreso.setProgress(con);
                        con++;
                        String id = fila.getString(0);
                        String codigo = fila.getString(1);
                        arrayList.add(codigo);
                        String descrip = fila.getString(2);
                        String conteo = fila.getString(3);
                        arrayList.add(conteo);
                        String existencia = fila.getString(4);
                        arrayList.add(existencia);
                        String serie=fila.getString(6);
                        String comentario=fila.getString(9);
                        //Toast.makeText(this, "codigo con serie "+serie, Toast.LENGTH_SHORT).show();
                        Log.i("solo conteo: ",codigo+" "+descrip+" "+con);
                        app.arisoft_app.Modelo.Inventario Articulo;
                        if(serie.equalsIgnoreCase("S"))
                        {
                            Integer cont = consultaSeries(codigo, idalmacen);
                            Articulo = new Inventario(false, codigo, descrip, cont.toString(), existencia, serie,comentario);
                            putDiferencia(id, Articulo.getDiferencia());
                        } else {
                            Articulo = new Inventario(false, codigo, descrip, conteo, existencia, serie,comentario);
                            putDiferencia(id, Articulo.getDiferencia());
                        }

                        if (consultaAlmView == 1){ //SOLO DIFERENCIAS
                            Log.i("solo dif conteo: ",""+Articulo.getDiferencia());
                            if (Articulo.getDiferencia() != 0){
                                con_part++;
                                Inventario.add(Articulo);
                            }
                        }
                        else
                        {
                            con_part++;
                            Inventario.add(Articulo);
                        }
                    }while (fila.moveToNext());
                }
                else {
                    Toast.makeText(getApplicationContext(), "Almacen sin conteo", Toast.LENGTH_SHORT).show();
                }

                //progreso.dismiss();
                /*
                dif_adapter = new InventarioListaDiferenciasAdapter(DiferenciasActivity.this, Inventario, idalmacen, 0);
                lv_dif.setAdapter(dif_adapter);
                consultaTablaSerie();
                */

                //barra("Consultando Sinconteo");

                fila = db.rawQuery("SELECT * FROM sinconteo WHERE idalmacen = '" + idalmacen+"'",null);
                if(fila.moveToFirst()) {
                    //progreso.setMessage("Consultando Sinconteo");
                    //con=con+fila.getCount();
                    //part_dif=""+con;
                    progreso.setMax(fila.getCount());
                    con=1;
                    do{
                        progreso.setProgress(con);
                        con++;
                        String id = fila.getString(0);
                        String codigo = fila.getString(1);
                        arrayList.add(codigo);
                        String descrip = fila.getString(2);
                        String conteo = fila.getString(3);
                        arrayList.add(conteo);
                        String existencia = fila.getString(4);
                        arrayList.add(existencia);
                        String serie=fila.getString(6);
                        String comentario=fila.getString(9);
                        //Toast.makeText(this, "codigo con serie "+serie, Toast.LENGTH_SHORT).show();
                        Log.i("solo conteo: ",codigo+" "+descrip+" "+con);
                        app.arisoft_app.Modelo.Inventario Articulo;
                        if(serie.equalsIgnoreCase("S"))
                        {
                            Integer cont = consultaSeries(codigo, idalmacen);
                            Articulo = new Inventario(false, codigo, descrip, cont.toString(), existencia, serie,comentario);
                            putDiferencia(id, Articulo.getDiferencia());
                        } else {
                            Articulo = new Inventario(false, codigo, descrip, conteo, existencia, serie,comentario);
                            putDiferencia(id, Articulo.getDiferencia());
                        }

                        if (consultaAlmView == 1){ //SOLO DIFERENCIAS
                            Log.i("solo dif conteo: ",""+Articulo.getDiferencia());
                            if (Articulo.getDiferencia() != 0){
                                con_part++;
                                Inventario.add(Articulo);
                            }
                        }
                        else
                        {
                            con_part++;
                            Inventario.add(Articulo);
                        }
                    }while (fila.moveToNext());
                }
                else {
                    Toast.makeText(getApplicationContext(), "Almacen sin conteo", Toast.LENGTH_SHORT).show();
                }
                part_dif=""+con_part;
                db.close();

            }
            catch (Exception e)
            {
                Log.e("Error",""+e.getMessage());
            }
            return null;
        }
        protected void onPostExecute(String s)
        {
            super.onPostExecute(s);
            tv_part.setText(part_dif);
            dif_adapter = new InventarioListaDiferenciasAdapter(DiferenciasActivity.this, Inventario, idalmacen, 0);
            lv_dif.setAdapter(dif_adapter);
            consultaTablaSerie();
            progreso.dismiss();
            //new Tarea3().execute();
            //Toast.makeText(getApplicationContext(), "finalizo", Toast.LENGTH_SHORT).show();
        }
    }
    class Tarea3 extends AsyncTask<String,String,String>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            barra("Consulta Conteo...");
            //Toast.makeText(getApplicationContext(), "Iniciando", Toast.LENGTH_SHORT).show();

        }
        @Override
        protected String doInBackground(String... params)
        {
            try {
                Inventario = new ArrayList<Inventario>();
                Database admin = new Database(getApplicationContext(),null,1);
                String Id;
                int con_part=0;
                int con=1;
                SQLiteDatabase db = admin.getWritableDatabase();
                Cursor fila = db.rawQuery("SELECT * FROM conteo WHERE idalmacen = '" + idalmacen+"'",null);
                if(fila.moveToFirst()) {
                    //con_part=fila.getCount();
                    progreso.setMax(fila.getCount());
                    do{
                        progreso.setProgress(con);
                        con++;
                        String id = fila.getString(0);
                        String codigo = fila.getString(1);
                        arrayList.add(codigo);
                        String descrip = fila.getString(2);
                        String conteo = fila.getString(3);
                        arrayList.add(conteo);
                        String existencia = fila.getString(4);
                        arrayList.add(existencia);
                        String serie=fila.getString(6);
                        String comentario=fila.getString(9);
                        //Toast.makeText(this, "codigo con serie "+serie, Toast.LENGTH_SHORT).show();
                        Log.i("solo conteo: ",codigo+" "+descrip+" "+con);
                        app.arisoft_app.Modelo.Inventario Articulo;
                        if(serie.equalsIgnoreCase("S"))
                        {
                            Integer cont = consultaSeries(codigo, idalmacen);
                            Integer consul=consultaSeriesReal(codigo,idalmacen);
                            Articulo = new Inventario(false, codigo, descrip, cont.toString(), existencia, serie,comentario);
                            putDiferencia(id,Articulo.getDiferencia() );

                        } else {
                            Articulo = new Inventario(false, codigo, descrip, conteo, existencia, serie,comentario);
                            putDiferencia(id, Articulo.getDiferencia());
                        }

                        if (consultaAlmView == 1){ //SOLO DIFERENCIAS
                            Log.i("solo dif conteo: ",""+Articulo.getDiferencia());
                            if (Articulo.getDiferencia() != 0){
                                //tiene diferencias
                                if(Articulo.getSerie().equalsIgnoreCase("S"))
                                {
                                    Cursor fila2 = db.rawQuery("SELECT serie,estatus FROM series WHERE almacen = '" + idalmacen+"' AND codigo= '"+Articulo.getCodigoArticulo()+"' AND estatus='D' ",null);
                                    int cont=fila2.getCount();
                                    if(Articulo.getExistencia()!=cont)
                                    {
                                        Inventario.add(Articulo);
                                        con_part++;
                                    }
                                }
                                else
                                {

                                    Inventario.add(Articulo);
                                }

                            }
                            else
                            {
                                if(Articulo.getSerie().equalsIgnoreCase("S"))
                                {
                                    Cursor fila2 = db.rawQuery("SELECT serie,estatus FROM series WHERE almacen = '" + idalmacen+"' AND codigo= '"+Articulo.getCodigoArticulo()+"' AND estatus='D' ",null);
                                    int cont=fila2.getCount();
                                    if(Articulo.getExistencia()!=cont)
                                    {
                                        Inventario.add(Articulo);
                                        con_part++;
                                    }
                                }
                            }


                        }
                        else
                        {
                            con_part++;
                            Inventario.add(Articulo);
                        }
                    }while (fila.moveToNext());
                }
                else {
                    Toast.makeText(getApplicationContext(), "Almacen sin conteo", Toast.LENGTH_SHORT).show();
                }
                part_dif=""+con_part;
                /*
                dif_adapter = new InventarioListaDiferenciasAdapter(DiferenciasActivity.this, Inventario, idalmacen, 0);
                lv_dif.setAdapter(dif_adapter);
                consultaTablaSerie();
                */
                db.close();
            }
            catch (Exception e)
            {
                Log.e("Error",""+e.getMessage());
            }
            return null;
        }
        protected void onPostExecute(String s)
        {
            super.onPostExecute(s);
            tv_part.setText(part_dif);
            dif_adapter = new InventarioListaDiferenciasAdapter(DiferenciasActivity.this, Inventario, idalmacen, 0);
            lv_dif.setAdapter(dif_adapter);
            //consultaTablaSerie();
            progreso.dismiss();
        }
    }
    public boolean buscarcodigoconteo(String codigo,String almacen)
    {
        boolean resultado=false;
        Database admin = new Database(this,null,1);
        SQLiteDatabase db = admin.getWritableDatabase();
        Cursor fila = db.rawQuery("select * from conteo where idalmacen='"+idalmacen+"' and codigo='" + codigo + "'",null);
        if(fila.moveToFirst())
        {
            do{
                resultado=true;
                Log.i("consulta",fila.getString(0)+"|"+fila.getString(1)+"|"+fila.getString(2)+"|"+fila.getString(3)+"|"+fila.getString(4));
            }while (fila.moveToNext());
            //tv1.setText("consulta:|"+respuesta);
        }
        else
        {
            resultado=false;
            //tv1.setText("No datos");
            //Toast.makeText(this, "No existe un artículo con el código indicado", Toast.LENGTH_SHORT).show();
        }
        db.close();


        return resultado;
    }
    public void barra(String mensaje)
    {

        progreso = new ProgressDialog(this);
        progreso.setMessage(mensaje);
        progreso.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        //progreso.setIndeterminate(true);
        progreso.setProgress(0);
        progreso.setMax(100);
        progreso.setCancelable(false);
        progreso.show();

    }
    public void sinconteoRetrofit()
    {
        Toast.makeText(this, "entro sin conteo", Toast.LENGTH_SHORT).show();
        Log.i("sinconteo",client_URL+"sinconteo");
        String codigosW="";
        progreso = new ProgressDialog(this);
        progreso.setMessage("Iniciando...");
        progreso.show();
        JSONArray conteo = new JSONArray();
        Database admin = new Database(this.getApplicationContext(),null,1);
        SQLiteDatabase db = admin.getWritableDatabase();
        Cursor fila = db.rawQuery("SELECT codigo FROM conteo WHERE idalmacen='"+idalmacen+"'" ,null);
        int i=0;
        if(fila.moveToFirst()) {

            do{
                JSONObject articulos = new JSONObject();
                try {
                    i++;
                    articulos.put("cod_prod", fila.getString(0));
                    codigosW=codigosW+"'"+fila.getString(0)+"'";
                    if(i<fila.getCount())
                    {
                        codigosW=codigosW+",";
                    }

                    //Log.i("json:",fila.getString(0));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                conteo.put(articulos);
            }while (fila.moveToNext());
            Log.i("StringCodigo",""+fila.getCount()+"||"+codigosW);

        }
        else {
            //Toast.makeText(this, "Almacen sin conteo", Toast.LENGTH_SHORT).show();
        }
        db.close();
        //progreso.dismiss();
        ServiceExiActual service=retrofit().create(ServiceExiActual.class);
        Call<RespExiActual>call=service.rdata(idalmacen,codigosW);
        call.enqueue(new Callback<RespExiActual>() {
            @Override
            public void onResponse(Call<RespExiActual> call, Response<RespExiActual> response) {
                if(response.isSuccessful())
                {
                    Log.i("retrofit",response.message());
                    RespExiActual r=response.body();
                    ArrayList<ExistenciaActual> l = r.getData();
                    for (int i=0;i<l.size();i++) {
                        ExistenciaActual e=l.get(i);
                        Log.i("retrofit:"," "+e.getCodigo()+"|"+e.getDescripcion()+"|"+e.getSerie()+"|"+e.getExistenciaActual());
                        //  if(consultaSinConteo(e.getCodigo())==false)
                        //Log.i("sincon:",e.getCodigo()+" falso "+i);
                        float exi_sc=Float.parseFloat(e.getExistenciaActual());
                        insertarSinConteo(e.getCodigo(),0,exi_sc,e.getDescripcion(),e.getSerie());
                        if(i==l.size()-1)
                        {
                            progreso.dismiss();
                        }
                        switch (params){
                            case "10":
                                consultaAlm(0);
                                break;
                            case "11":
                                consultaAlm(1);//Conteo + ALM - Sólo diferencias
                                break;//
                        }
                    }
                    //Toast.makeText(getApplicationContext(),"Articulo modificado:"+response.code(),Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RespExiActual> call, Throwable t) {
                Toast.makeText(getApplicationContext(),"Error retrofit:"+t.getMessage(),Toast.LENGTH_SHORT).show();
                Log.i("Error retrofit:",t.getMessage());
                progreso.dismiss();
            }
        });

    }

    public void ajusteMacro() {
        validarEntrada=false;
        validarSalida=false;
        posPartEnt=0;
        posPartSal=0;
        Toast.makeText(DiferenciasActivity.this, "AJUSTAR REGISTROS EN MACROPRO ", Toast.LENGTH_SHORT).show();
        //actualizar existencia y valor de inventario.
        //recorrer las diferencias
        for (int i=0; i < Inventario.size(); i++) {
            final Inventario model = Inventario.get(i);
            estatusConteo(model.getCodigoArticulo());
            if(estatusConteo(model.getCodigoArticulo()).equalsIgnoreCase("S")){
            if (model.getSerie().equalsIgnoreCase("N")) {

                //cargar costo y valor
                ServiceModificar service = retrofit().create(ServiceModificar.class);
                Call<respuestaCosto> call = service.costoValor(idalmacen, model.getCodigoArticulo());
                call.enqueue(new Callback<respuestaCosto>() {
                    @Override
                    public void onResponse(Call<respuestaCosto> call, Response<respuestaCosto> response) {
                        if (response.isSuccessful()) {
                            respuestaCosto r = response.body();
                            ArrayList<Costo> l = r.getCosto();
                            float auxcostoUnitario = 0;
                            float auxdif = 0;
                            float auxvalor = 0;
                            float valor = 0;
                            int movimiento = 0;
                            String folio = "XD-00002";
                            String fecha = fechaActual("yyyy-MM-dd");
                            int fechaSys = Integer.parseInt(fechaActual("yyyyMMdd"));
                            int hora = Integer.parseInt(fechaActual("HHmmss"));
                            float costeo = 0;
                            Float cantidad;
                            boolean ent=false,sal=false;
                            for (int i = 0; i < l.size(); i++) {
                                cantidad=model.getDiferencia() * (-1);
                                Costo e = l.get(i);
                                Log.i("costo:", e.getCosto() + " " + e.getValor() + " " + i);
                                auxcostoUnitario = Float.parseFloat(e.getCosto());
                                valor = Float.parseFloat(e.getValor());
                                auxdif = model.getDiferencia();
                                costeo = cantidad*(valor / model.getExistencia());
                                if (model.getDiferencia() < 0) {
                                    validarSalida=true;
                                    posPartSal++;
                                    movimiento = 7;
                                    auxvalor = valor - (auxdif * auxcostoUnitario);
                                    //salida
                                    Log.i("recorrer diferencias:", "diferencia menor que 0 es una salida movmimiento tipo 7 ");
                                    Log.i("recorrer diferencias ", "codigo:" + model.getCodigoArticulo() + "|dif:" + model.getDiferencia() + "|almacen:" + idalmacen + " |nueva exis:" + model.getConteo() + " |estatus:" + model.getSerie() + " |pospart:");
                                    //Log.i("diferencia","valor total:"+auxvalor+" dif:"+auxdif+" costo:"+auxcostoUnitario);
                                    //modificar existencia
                                    modificarExistencia(idalmacen, model.getCodigoArticulo(), model.getConteo(), auxvalor);
                                    Ajuste(movimiento, folSalida, posPartSal, fecha, idalmacen, cantidad, model.getCodigoArticulo(), auxcostoUnitario, costeo, fechaSys, hora, fechaSys);
                                    modificarEC(model.getCodigoArticulo());
                                    //mandar salida
                                } else {
                                    if (model.getDiferencia() > 0) {
                                        validarEntrada=true;
                                        posPartEnt++;
                                        movimiento = 6;
                                        auxvalor = valor + (auxdif * auxcostoUnitario);
                                        //entrada
                                        Log.i("recorrer diferencias:", "diferencia mayor que 0 es una entrada movimiento tipo 6 ");
                                        Log.i("recorrer diferencias ", "codigo:" + model.getCodigoArticulo() + "|dif:" + model.getDiferencia() + "|almacen:" + idalmacen + " |nueva exis:" + model.getConteo() + " |estatus:" + model.getSerie() + " |pospart:");
                                        //Log.i("diferencia","valor total:"+auxvalor+" dif:"+auxdif+" costo:"+auxcostoUnitario);
                                        //modificar existencia
                                        modificarExistencia(idalmacen, model.getCodigoArticulo(), model.getConteo(), auxvalor);
                                        //mandar entrada
                                        Ajuste(movimiento, folEntrada, posPartEnt, fecha, idalmacen, model.getDiferencia(), model.getCodigoArticulo(), auxcostoUnitario, 0, fechaSys, hora, fechaSys);
                                        modificarEC(model.getCodigoArticulo());
                                    }
                                }
                                if(i==l.size()-1)
                                {
                                    ServiceModificar service=retrofit().create(ServiceModificar.class);
                                    Call<RespuestaExistencia>call2=service.folios(idalmacen,nvoFolio(folEntrada),nvoFolio(folSalida));
                                    call2.enqueue(new Callback<RespuestaExistencia>() {
                                        @Override
                                        public void onResponse(Call<RespuestaExistencia> call, Response<RespuestaExistencia> response) {
                                            if(response.isSuccessful())
                                            {
                                                Toast.makeText(getApplicationContext(),"folios modificado:"+response.code(),Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<RespuestaExistencia> call, Throwable t) {
                                            Toast.makeText(getApplicationContext(),"Error modexi:"+t.getMessage(),Toast.LENGTH_SHORT).show();
                                            Log.i("Error modexi:",t.getMessage());
                                        }
                                    });
                                }

                            }



                            //mandar actualizar folio
                        }
                    }

                    @Override
                    public void onFailure(Call<respuestaCosto> call, Throwable t) {
                        Toast.makeText(getApplicationContext(), "Error costo:" + t.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.i("Error costo:", t.getMessage());
                    }
                });

            }
            //if de estatus
        }
        else
            {
                //cuando ya esta ajustado
                Toast.makeText(getApplicationContext(), "Articulo ya ajustado" , Toast.LENGTH_SHORT).show();
            }

        }
        //modificar folio y actualizar
    }

    public void modificarEC(String codigo) {
        Database admin=new Database(this,null,1);
        SQLiteDatabase db = admin.getWritableDatabase();
        ContentValues registro = new ContentValues();
        //String descrip = getDescripcion(codigo);
        //registro.put("id","NULL");
        registro.put("estatus","N");
        //db.insert("conteo",null,registro);
        db.update("conteo",registro,"codigo = '"+ codigo +"' AND idalmacen = '"+ idalmacen +"'",null);
        db.close();
    }

    public void consulSinConteo() {
        //Log.i("consultasinconteo","entro a cosulta");
        Database admin = new Database(this,null,1);
        SQLiteDatabase db = admin.getWritableDatabase();
        Cursor fila = db.rawQuery("select * from sinconteo",null);
        if(fila.moveToFirst())
        {
            do{
                Log.i("consultasinconteo",fila.getString(0)+"|"+fila.getString(1)+"|"+fila.getString(2)+"|"+fila.getString(3)+"|"+fila.getString(4));
            }while (fila.moveToNext());
            //tv1.setText("consulta:|"+respuesta);
        }
        else
        {
            //tv1.setText("No datos");
            //Toast.makeText(this, "No existe un artículo con el código indicado", Toast.LENGTH_SHORT).show();
        }
        db.close();

    }

    public String estatusConteo(String articulo) {
        String estatus="";
        Database admin = new Database(this,null,1);
        String respuesta="";
        SQLiteDatabase db = admin.getWritableDatabase();
        Cursor fila = db.rawQuery("select estatus from conteo where codigo='"+articulo+"' and idalmacen='"+idalmacen+"'",null);
        if(fila.moveToFirst())
        {
            do{
                //respuesta += fila.getString(0)+"|"+fila.getString(1);
                //respuesta += "\n";
                estatus=fila.getString(0);
            }while (fila.moveToNext());
            //tv1.setText("consulta:|"+respuesta);
        }
        else
        {
            //tv1.setText("No datos");
            //Toast.makeText(this, "No existe un artículo con el código indicado", Toast.LENGTH_SHORT).show();
        }
        db.close();
        Log.i("estatus conteo:",estatus);
        return estatus;
    }

    public String fechaActual(String formato) {
        //SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat(formato, Locale.getDefault());
        Date date = new Date();
        String fecha = dateFormat.format(date);
        return fecha;
    }

    public void modificarExistencia(String almacen,String codigo,float conteo,float valor) {
        ServiceModificar service=retrofit().create(ServiceModificar.class);
        Call<RespuestaExistencia>call=service.respuesta(almacen,codigo,conteo,valor);
        call.enqueue(new Callback<RespuestaExistencia>() {
            @Override
            public void onResponse(Call<RespuestaExistencia> call, Response<RespuestaExistencia> response) {
                if(response.isSuccessful())
                {
                    Toast.makeText(getApplicationContext(),"Articulo modificado:"+response.code(),Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RespuestaExistencia> call, Throwable t) {
                Toast.makeText(getApplicationContext(),"Error modexi:"+t.getMessage(),Toast.LENGTH_SHORT).show();
                Log.i("Error modexi:",t.getMessage());
            }
        });

    }

    public String nvoFolio(String folio) {
        String[] parts = folio.split("-");
        String xd="1";
        xd=xd+parts[1];
        int x=Integer.parseInt(xd);
        x=x+1;
        xd=String.valueOf(x);
        xd=xd.substring(1,xd.length());
        xd=parts[0]+"-"+xd;
        return xd;
    }

    public void Ajuste(int movimiento, String folio, int posicion, String fecha, String almacen, float cantidad, final String articulo, float costoUni, float costeo, int fechaSys, int hora, int fechaMod)  {
        //Toast.makeText(getApplicationContext(),"entrada",Toast.LENGTH_SHORT).show();
        ServiceModificar service=retrofit().create(ServiceModificar.class);
        Call<RespuestaExistencia>call=service.movimiento(movimiento,folio,posicion,fecha,almacen,cantidad,articulo,costoUni,costeo,fechaSys,hora,fechaMod);
        call.enqueue(new Callback<RespuestaExistencia>() {
            @Override
            public void onResponse(Call<RespuestaExistencia> call, Response<RespuestaExistencia> response) {
                if(response.isSuccessful())
                {
                    Toast.makeText(getApplicationContext(),"Entrada:"+articulo+" "+response.code(),Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RespuestaExistencia> call, Throwable t) {
                Toast.makeText(getApplicationContext(),"Error modexi:"+t.getMessage(),Toast.LENGTH_SHORT).show();
                Log.i("Error modexi:",t.getMessage());
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menu) {

        if (menu.getItemId() == R.id.save) {
            GuardarConteo();
        }
       /* if (menu.getItemId() == R.id.ajustar) {
            //ajusteMacro();
        }*/
        if (menu.getItemId() == R.id.edit_cont){
            onBackPressed();
        }
        if (menu.getItemId() == R.id.opciones){
            optionsDialog();
        }
        if (menu.getItemId() == R.id.busqueda){
            //Toast.makeText(getApplicationContext(),"check",Toast.LENGTH_SHORT).show();
            if(busquedaFiltro.isChecked())
            {
                busquedaFiltro.setChecked(false);
                layoutbusqueda.setVisibility(View.GONE);
                et_busqueda.setText("");
            }
            else
            {
                busquedaFiltro.setChecked(true);
                layoutbusqueda.setVisibility(View.VISIBLE);
                et_busqueda.requestFocus();

            }

        }

        return super.onOptionsItemSelected(menu);
    }

    public void eliminarTabla(String tabla) {
        Database admin=new Database(this,null,1);
        SQLiteDatabase db = admin.getWritableDatabase();
        //db.execSQL("DROP TABLE IF EXISTS "+tabla);
        db.execSQL("DELETE FROM " + tabla);
        db.close();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("\n¿Desea regresar a editar el conteo?\n")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Regresar", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        eliminarTabla("sinconteo");
                        DiferenciasActivity.super.onBackPressed();
                    }
                }).create().show();
    }

    @Override
    public void onSelectedData(String string) {
        params = string;
        obtenerDatos();
        //adicionales();
    }
}
