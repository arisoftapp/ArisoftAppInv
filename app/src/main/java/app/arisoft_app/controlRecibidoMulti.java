package app.arisoft_app;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
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
import java.util.Locale;

import app.arisoft_app.Modelo.ModeloRecibido;
import app.arisoft_app.Tools.Database;

public class controlRecibidoMulti extends AppCompatActivity {

    String idalmacen,almacen,folioFac,cantidadG,recibidoG,diferenciaG,bus_cod_macro,idCodigo,CantidadAux,foliogrl;
    LinearLayout main, vertical, page;
    TextView tv_alm,tv_fol;
    EditText et_factura,et_codigo;
    LinearLayout ly_fac;
    private ListView lvItems;
    private RecibidoAdapter adaptador;
    private String URL;
    ArrayList<Recibido>Recibido_list;
    ArrayList<String>folios;
    private RecibidoAdapter Recibido_adap;
    MenuItem done, cancel, reset, menucod1, menucod2;
    Context contexto=this;
    int Surt;
    Button cambiar;


    public ArrayList<Recibido> GetArrayItems(String codigo,String descripcion, String cantidad, String recibido,String dif){
        ArrayList<Recibido> listItems=new ArrayList<>();
        listItems.add(new Recibido(codigo,descripcion,cantidad,recibido,dif,""));
        return listItems;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_recibido_multi);
        getDomain();
        et_factura = (EditText) findViewById(R.id.et1);
        et_factura.setFilters(new InputFilter[] {new InputFilter.AllCaps()});
        et_codigo = (EditText) findViewById(R.id.et2);
        et_codigo.setFilters(new InputFilter[] {new InputFilter.AllCaps()});
        lvItems=findViewById(R.id.lista_Recibido);
        tv_fol=(TextView)findViewById(R.id.tv_fol);
        ly_fac=(LinearLayout)findViewById(R.id.ly_fac);
        cambiar=(Button)findViewById(R.id.btn_cambiar);
        eliminarTabla("recibido");
        folios = new ArrayList<String>();
        tv_fol.setText("----");
        et_codigo.setEnabled(false);

        try{
            page =  ((LinearLayout)findViewById(R.id.linear_alm));
            Bundle bundle = getIntent().getExtras();
            idalmacen = bundle.getString("idalmacen");
            //Toast.makeText(this, "idalmacen:"+idalmacen, Toast.LENGTH_SHORT).show();
            almacen = bundle.getString("almacen");
            String selectedAlm = idalmacen + " - " + almacen;
            if(bundle!=null){
                tv_alm = page.findViewById(R.id.tv_alm);
                tv_alm.setText(almacen);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        et_factura.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                //ENTER EN FOLIO
                //buscar si ya se agrego factura
                folioFac=et_factura.getText().toString();
                String ruta="numsurtido/"+folioFac;
                if(buscarFolio()==false)
                {
                    new ConsultarSurt().execute(ruta);
                }
                else {
                    mensajes("","Factura ya agregada");
                }
                return false;
            }
        });

        et_codigo.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                //enter en codigo
                contarRecibido();
                return false;
            }

        });
        tv_fol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tablaVacia("recibido","folio")==false)
                {
                    String x="";
                    for(int i=0;i<folios.size();i++)
                    {
                       x=x+" "+folios.get(i);
                    }
                    mensajes("","FOLIOS: "+x);
                    //revisarfacturas();
                }
            }
        });
        lvItems.setLongClickable(true);
        lvItems.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                Log.i("comentarioLong",""+position);
                //mensajes("","xd"+position+" "+Recibido_list.get(position).getCodigo());
                LayoutInflater inflater = controlRecibidoMulti.this.getLayoutInflater();
                View v = inflater.inflate(R.layout.dialog_editar_surtidas, null);
                final EditText tvnuevo_conteo = v.findViewById(R.id.editTextConteo);
                final TextView tvcodigo = v.findViewById(R.id.tv_cod);
                final TextView tvdesc = v.findViewById(R.id.tv_descr);
                final ImageView menos = (ImageView) v.findViewById(R.id.imageView5);
                ImageView mas = (ImageView) v.findViewById(R.id.imageView4);
                menos.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Toast.makeText(getApplicationContext(), "menos", Toast.LENGTH_SHORT).show();
                        int nc=Integer.parseInt(tvnuevo_conteo.getText().toString());
                        if(nc<=0)
                        {
                            mensajes("","Surtidas es igual a 0");
                        }
                        else
                        {
                            nc=nc-1;
                            tvnuevo_conteo.setText(""+nc);
                        }

                    }
                });
                mas.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int cant=Integer.parseInt(Recibido_list.get(position).getCantidad());
                        int nc=Integer.parseInt(tvnuevo_conteo.getText().toString());
                        if(nc<cant)
                        {
                            nc=nc+1;
                            tvnuevo_conteo.setText(""+nc);
                        }
                        else
                        {
                            mensajes("","Surtidas igual que cantidad");
                        }

                    }
                });

                final String codigo = Recibido_list.get(position).getCodigo();
                //pendiente para traer folio
                final String folio=Recibido_list.get(position).getFolio();
                tvcodigo.setText(codigo);
                tvdesc.setText(Recibido_list.get(position).getDescripcion());
                tvnuevo_conteo.setText(Recibido_list.get(position).getRecibido());

                AlertDialog dialog = new AlertDialog.Builder(controlRecibidoMulti.this)
                        .setTitle("Editar Surtidas")
                        .setView(v)
                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int nuevo_conteo = Integer.parseInt(tvnuevo_conteo.getText().toString());
                                int cant=Integer.parseInt(Recibido_list.get(position).getCantidad());
                                //String task = String.valueOf(tvnuevo_conteo.getText());
                                //Integer nuevo_conteo = Integer.parseInt(task);
                                Log.i("EDITAR_CONTEO", "NUEVO CONTEO: " + codigo + " " + nuevo_conteo);
                                //String Id = getIdConteo(codigo);
                                modificarSurtida(codigo, nuevo_conteo,cant,folio);
                                actualizarLista();
                                Log.i("EDITAR_CONTEO", "SALIR DE POSITIVE BUTTON");
                            }
                        })
                        .setNegativeButton("Cancelar", null)
                        .create();
                dialog.show();

                return false;
            }
        });


    }
    public void modificarSurtida(String codigo,int nuevo, int cantidad,String folio)
    {
        int cant=cantidad-nuevo;
        //mensajes("modificar"," surtidas "+codigo+" "+nuevo);
        try{
            Database admin = new Database(this, null,1);
            SQLiteDatabase db = admin.getWritableDatabase();
            ContentValues r = new ContentValues();
            r.put("recibido",nuevo);
            r.put("estatus","M");
            r.put("diferencia",cant);
            db.update("recibido",r, "codigo='"+ codigo +"' AND folio='"+folio+"' ",null);

        }catch (Exception e)
        {
            mensajes("Error al actualizar Surtidas:",""+e.getMessage());
        }
    }
    public boolean buscarFolio()
    {
        boolean buscarFolio=false;
        try{
            Database admin = new Database(getApplicationContext(),null,1);
            SQLiteDatabase db = admin.getWritableDatabase();
            Cursor fila = db.rawQuery("SELECT * FROM recibido WHERE folio='"+folioFac+"'",null);
            if(fila.moveToFirst())
            {
                buscarFolio=true;
            }
            else {
                buscarFolio=false;
            }
            db.close();
        }catch (Exception e)
        {
            Toast.makeText(getApplicationContext(), "Error consulta bd:"+e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        return buscarFolio;
    }
    //CLICK EN BOTON CAMBIAR
    public void cambiar(View view)
    {

        if(tablaVacia("recibido","folio")==true)
        {
            mensajes("","sin facturas agregadas");
        }
        else
        {
            if(cambiar.getText().toString().equalsIgnoreCase("CODIGO"))
            {
                cambiar.setText("FACTURA");
                et_factura.setText("");
                et_factura.setEnabled(false);
                et_codigo.setEnabled(true);
                et_codigo.requestFocus();
                mostrarTeclado("CODIGO");
            }
            else
            {
                cambiar.setText("CODIGO");
                et_codigo.setText("");
                et_codigo.setEnabled(false);
                et_factura.setEnabled(true);
                et_factura.requestFocus();
                mostrarTeclado("FACTURA");

            }

        }
    }
    //MUESTRA TECLADO DEPENDIENDO DEL TEXTVIEW QUE OCUPES
    public void mostrarTeclado(String tv)
    {
        if(tv.equalsIgnoreCase("FACTURA"))
        {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(et_factura, InputMethodManager.SHOW_IMPLICIT);
        }
        else
        {
            if(tv.equalsIgnoreCase("CODIGO"))
            {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(et_codigo, InputMethodManager.SHOW_IMPLICIT);
            }
        }

    }
    //REVISA SI LA TABLA ESTA VACIA
    public boolean tablaVacia(String nomTabla, String columna){
        Boolean vacio = true;
        Database admin = new Database(this, null,1);
        SQLiteDatabase db = admin.getWritableDatabase();
        try {
            Cursor fila = db.rawQuery("SELECT " + columna + " FROM "+ nomTabla,null);
            if(fila.moveToFirst())
            {
                vacio=false;
                //tv1.setText(vacio.toString());
            }
            else
            {
                //Toast.makeText(this, "No existe un artículo con dicho código", Toast.LENGTH_SHORT).show();
            }

        }catch (SQLiteException sql){
            vacio = true;
        }

        db.close();
        return vacio;

    }
    //PETICION FACTURAS ASYNC TASK
    class cargarFacturaAT extends AsyncTask<String,Integer,String>
    {
        private ProgressDialog progreso;
        String validar;
        @Override
        protected void onPreExecute()
        {
            progreso = new ProgressDialog(contexto);
            progreso.setProgressStyle(ProgressDialog.
                    STYLE_HORIZONTAL);
            progreso.setMessage("Descargando Factura...");
            progreso.setCancelable(false);
            progreso.setMax(100);
            progreso.setProgress(0);
            progreso.show();
            super.onPreExecute();
            //barra("Consulta Conteo...");
            //Toast.makeText(getApplicationContext(), "Iniciando", Toast.LENGTH_SHORT).show();

        }
        @Override
        protected String doInBackground(String... params)
        {
            try {
                //Log.i("Async",params[0]+" "+params[1]+" "+params[2]+" "+params[3]);
                HttpClient cliente = new DefaultHttpClient();
                /* Definimos la ruta al servidor. */
                String value="Fallo";
                HttpParams httpParameters = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParameters, 900000);
                HttpConnectionParams.setSoTimeout(httpParameters, 900000);
                HttpGet htpoget = new HttpGet(URL+params[0]);
                org.apache.http.HttpResponse resx = cliente.execute(htpoget);
                BufferedReader bfr = new BufferedReader(new InputStreamReader(resx.getEntity().getContent()));
                StringBuffer stb = new StringBuffer("");
                String linea="";
                StringBuffer res = new StringBuffer();
                while ((linea =bfr.readLine())!=null)
                {
                    res.append(linea);
                    validar="OK";

                }
                String finalJSON = res.toString();

                int c=1;
                JSONObject jObject = new JSONObject(finalJSON); //Obtenemos el JSON global
                JSONArray jArray = jObject.getJSONArray("recibido"); //Obtenemos el array results
                progreso.setMax(jArray.length());
                Database admin=new Database(contexto,null,1);
                SQLiteDatabase db = admin.getWritableDatabase();
                ContentValues r = new ContentValues();
                Log.i("recibido",""+jArray.length());
                if(jArray.length()==0)
                {
                    validar="Factura No Encontrada";
                }
                for (int i=0; i < jArray.length(); i++) //Miramos en todos los objetos del array de objetos results
                {
                    publishProgress(i+1);
                    try {
                        JSONObject objeto = jArray.getJSONObject(i); //Obtenemos cada uno de los objetos del array results
                        if(objeto.getString("estatus").equalsIgnoreCase("A"))
                        {
                            r.put("folio",et_factura.getText().toString());
                            r.put("codigo",objeto.getString("articulo"));
                            r.put("descripcion",objeto.getString("descripcion"));
                            r.put("cantidad",objeto.getInt("cantidad"));
                            r.put("recibido",objeto.getInt("cantsurt"));
                            int dif=objeto.getInt("cantidad")-objeto.getInt("cantsurt");
                            r.put("diferencia",dif);
                            r.put("codigo2",objeto.getString("codigo2"));
                            r.put("posicion",objeto.getString("posicion"));
                            r.put("fechafactura",objeto.getString("fecha"));
                            r.put("cliente",objeto.getString("cliente"));
                            r.put("cantidadtmp",0);
                            r.put("surt",Surt);
                            db.insert("recibido",null,r);

                        }
                        else
                        {
                            validar="Factura Surtida";
                        }

                    } catch (JSONException e) {
                        Log.e("error",e.getMessage());
                    }
                }
                db.close();
                bfr.close();


            }
            catch (Exception e)
            {
                validar=e.getMessage();
                Log.e("Error",""+e.getMessage());
            }
            return validar;

        }

        protected void onProgressUpdate(Integer... i)
        {
            progreso.setProgress(i[0]);
        }
        protected void onPostExecute(String s)
        {

            if(s.equalsIgnoreCase("OK"))
            {
                folios.add(et_factura.getText().toString());
                Toast.makeText(getApplicationContext(), "Factura agregada con éxito",
                        Toast.LENGTH_LONG).show();
                actualizarLista();
                et_factura.setText("");
                /*
                tv_fol.setText(et_factura.getText());
                et_factura.setEnabled(false);
                et_codigo.setEnabled(true);
                et_codigo.requestFocus();
                mostrarTeclado("CODIGO");
                surtirCompleta();
                */
            }
            else
            {
                Toast.makeText(getApplicationContext(), ""+s,
                        Toast.LENGTH_LONG).show();
            }
            progreso.dismiss();
            super.onPostExecute(s);
        }
    }
    public void surtirCompleta()
    {
        new AlertDialog.Builder(this)
                .setMessage("¿Desea Surtir Factura Completa?")
                .setNegativeButton("No", null)
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        //mensajes("Surtir ","SI");
                        ArrayList<ModeloRecibido> ModeloRecibido = new ArrayList<ModeloRecibido>();
                        Database admin = new Database(contexto,null,1);
                        SQLiteDatabase db = admin.getWritableDatabase();
                        Cursor fila = db.rawQuery("SELECT codigo,cantidad,recibido,diferencia FROM recibido WHERE diferencia>0",null);
                        if(fila.moveToFirst())
                        {
                            do{
                                String codigo=fila.getString(0);
                                int cantidad=fila.getInt(1);
                                int recibido=fila.getInt(2);
                                int diferencia=fila.getInt(3);
                                ModeloRecibido.add(0,new ModeloRecibido( codigo,cantidad,recibido,diferencia));
                                Log.i("surtircompleto"," | "+fila.getString(0)+" | "+fila.getString(1)+" | "+fila.getString(2) +" | "+fila.getString(3));
                            }while (fila.moveToNext());

                        }

                        int size = ModeloRecibido.size();
                        for ( int i = 0; i < size; i++) {
                            ModeloRecibido model = ModeloRecibido.get(i);
                            Log.i("surtircompleto2",""+model.getCodigo()+" "+model.getCantidad()+" "+model.getDiferencias()+" "+model.getRecibido());
                            try{
                                ContentValues registro = new ContentValues();
                                registro.put("recibido",model.getCantidad());
                                registro.put("diferencia",0);
                                registro.put("estatus","M");
                                registro.put("cantidadtmp",model.getDiferencias());
                                db.update("recibido",registro, "codigo='"+ model.getCodigo() +"'",null);

                                actualizarLista();
                            }catch (Exception e)
                            {
                                Toast.makeText(contexto, "Error:"+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                        db.close();
                    }
                }).create().show();
    }
    public void guardarSurtir()
    {
        //consultar si hay diferencias
        boolean buscar_dif=false;
        try{
            Database admin = new Database(this,null,1);
            SQLiteDatabase db = admin.getWritableDatabase();
            Cursor fila = db.rawQuery("SELECT diferencia FROM recibido WHERE diferencia>0",null);
            if(fila.moveToFirst())
            {
                do{
                    Log.i("buscarDif"," | "+fila.getString(0));
                }while (fila.moveToNext());
                buscar_dif=true;
            }
            else {
                buscar_dif=false;
            }
            db.close();
        }catch (Exception e)
        {
            Log.e("Error:",""+e.getMessage());
            mensajes("Error al buscar diferencias:",""+e.getMessage());
        }

        if(buscarsimodifico()==true)
        {
                //consultar folios modificados
                //modificcar VENREN Y VENSRT
                new ModificarSurtido().execute();
                //new ModificarEstatus().execute();
                //MODIFICAR VENDOC, VENREN Y VENSRT
        }
        else
        {
            mensajes("Mensaje ","No modifico ningun articulo");
        }

    }
    public boolean buscarsimodifico()
    {
        boolean modifico=false;
        try{
            Database admin = new Database(getApplicationContext(),null,1);
            SQLiteDatabase db = admin.getWritableDatabase();
            Cursor fila = db.rawQuery("SELECT * FROM recibido WHERE estatus='M'",null);
            if(fila.moveToFirst())
            {
                modifico=true;
            }
            db.close();
        }catch (Exception e)
        {
            Toast.makeText(getApplicationContext(), "Error consulta bd:"+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return modifico;
    }
    class ConsultarSurt extends AsyncTask<String,Integer,String>
    {
        private ProgressDialog progreso;
        String validar;
        @Override
        protected void onPreExecute()
        {
            progreso = new ProgressDialog(contexto);
            progreso.setProgressStyle(ProgressDialog.
                    STYLE_HORIZONTAL);
            progreso.setMessage("Consultando Numero Surtido...");
            progreso.setCancelable(false);
            progreso.setMax(100);
            progreso.setProgress(0);
            progreso.show();
            super.onPreExecute();
            //barra("Consulta Conteo...");
            //Toast.makeText(getApplicationContext(), "Iniciando", Toast.LENGTH_SHORT).show();

        }
        @Override
        protected String doInBackground(String... params)
        {
            try {

                //Log.i("Async",params[0]+" "+params[1]+" "+params[2]+" "+params[3]);
                HttpClient cliente = new DefaultHttpClient();
                /* Definimos la ruta al servidor. */
                String value="Fallo";
                HttpParams httpParameters = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParameters, 900000);
                HttpConnectionParams.setSoTimeout(httpParameters, 900000);
                HttpGet htpoget = new HttpGet(URL+params[0]);
                org.apache.http.HttpResponse resx = cliente.execute(htpoget);
                BufferedReader bfr = new BufferedReader(new InputStreamReader(resx.getEntity().getContent()));
                String linea="";
                StringBuffer res = new StringBuffer();
                while ((linea =bfr.readLine())!=null)
                {
                    res.append(linea);
                    validar="OK";
                }
                String finalJSON = res.toString();

                int c=1;
                Surt=0;
                JSONObject jObject = new JSONObject(finalJSON); //Obtenemos el JSON global
                JSONArray jArray = jObject.getJSONArray("data"); //Obtenemos el array results
                progreso.setMax(jArray.length());
                Log.i("data",""+jArray.length());
                if(jArray.length()==0)
                {
                    Surt=0;
                    //validar="Factura Sin Surtir";
                }
                for (int i=0; i < jArray.length(); i++) //Miramos en todos los objetos del array de objetos results
                {
                    publishProgress(i+1);
                    try {
                        JSONObject objeto = jArray.getJSONObject(i); //Obtenemos cada uno de los objetos del array results
                        if(Surt<objeto.getInt("VSRT_SURT"))
                        {
                            Surt=objeto.getInt("VSRT_SURT");
                        }
                        Log.i("surt",""+objeto.getString("VSRT_SURT")+" "+Surt);
                    } catch (JSONException e) {
                        Log.e("error",e.getMessage());
                    }
                }
                Log.i("surt",""+Surt);
                bfr.close();
            }
            catch (Exception e)
            {
                validar=e.getMessage();
                Log.e("Error",""+e.getMessage());
            }
            return validar;

        }
        protected void onProgressUpdate(Integer... i)
        {
            progreso.setProgress(i[0]);
        }
        protected void onPostExecute(String s)
        {
            progreso.dismiss();
            if(s.equalsIgnoreCase("OK"))
            {
                //Toast.makeText(getApplicationContext(), "Obtuvo Surtido",
                  //      Toast.LENGTH_LONG).show();
                String ruta="RecibidoFolioFac/"+folioFac+"/"+idalmacen;
                new cargarFacturaAT().execute(ruta);
                //new ModificarSurtido().execute();
            }
            else
            {
                Toast.makeText(getApplicationContext(), ""+s,
                        Toast.LENGTH_LONG).show();
            }

            super.onPostExecute(s);
        }
    }
    class ModificarSurtido extends AsyncTask<String,Integer,String>
    {
        private ProgressDialog progreso;
        String validar="OK";
        @Override
        protected void onPreExecute()
        {
            progreso = new ProgressDialog(contexto);
            progreso.setProgressStyle(ProgressDialog.
                    STYLE_HORIZONTAL);
            progreso.setMessage("Modificando Surtido...");
            progreso.setCancelable(false);
            progreso.setMax(100);
            progreso.setProgress(0);
            progreso.show();
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(String... params)
        {
                //consultar articulos
                try{
                    Database admin = new Database(getApplicationContext(),null,1);
                    SQLiteDatabase db = admin.getWritableDatabase();
                    Cursor fila = db.rawQuery("SELECT folio,codigo,recibido FROM recibido WHERE estatus='M'",null);
                    if(fila.moveToFirst())
                    {
                        progreso.setMax(fila.getCount());
                        int i=0;
                        do{
                            publishProgress(i+1);
                            String folio = fila.getString(0);
                            String codigo = fila.getString(1);
                            String recibido = fila.getString(2);
                            //modificar web service
                            HttpClient cliente = new DefaultHttpClient();
                            HttpParams httpParameters = new BasicHttpParams();
                            HttpConnectionParams.setConnectionTimeout(httpParameters, 900000);
                            HttpConnectionParams.setSoTimeout(httpParameters, 900000);
                            HttpPost htppost=new HttpPost(URL+"cambiarsurtido/"+folio+"/"+codigo+"/"+recibido);
                            org.apache.http.HttpResponse resx = cliente.execute(htppost);
                            BufferedReader bfr = new BufferedReader(new InputStreamReader(resx.getEntity().getContent()));
                            String linea="";
                            StringBuffer res = new StringBuffer();
                            while ((linea =bfr.readLine())!=null)
                            {
                                res.append(linea);
                                validar="OK";
                            }
                            String finalJSON = res.toString();
                            JSONObject jObject = new JSONObject(finalJSON); //Obtenemos el JSON global
                            if(jObject.getBoolean("success")==true)
                            {
                                validar="OK";
                            }
                            bfr.close();
                            //Log.i("consultarecibido",""+folio+" "+codigo+" "+recibido);
                        }while (fila.moveToNext());
                    }
                    db.close();
                }catch (Exception e)
                {
                    validar=""+e.getMessage();
                }

            return validar;

        }

        protected void onProgressUpdate(Integer... i)
        {
            //progreso.setProgress(i[0]);
        }
        protected void onPostExecute(String s)
        {
            progreso.dismiss();
            if(s.equalsIgnoreCase("OK"))
            {
                Toast.makeText(getApplicationContext(), "Modifico Surtido",
                        Toast.LENGTH_LONG).show();
                new AgregarSurtido().execute();
            }
            else
            {
                Toast.makeText(getApplicationContext(), ""+s,
                        Toast.LENGTH_LONG).show();
            }

            super.onPostExecute(s);
        }
    }
    class AgregarSurtido extends AsyncTask<String,Integer,String>
    {
        private ProgressDialog progreso;
        String validar="OK";
        @Override
        protected void onPreExecute()
        {
            progreso = new ProgressDialog(contexto);
            progreso.setProgressStyle(ProgressDialog.
                    STYLE_HORIZONTAL);
            progreso.setMessage("Agregando Surtido...");
            progreso.setCancelable(false);
            progreso.setMax(100);
            progreso.setProgress(0);
            progreso.show();
            super.onPreExecute();
            //barra("Consulta Conteo...");
            //Toast.makeText(getApplicationContext(), "Iniciando", Toast.LENGTH_SHORT).show();

        }
        @Override
        protected String doInBackground(String... params)
        {
            //consultar articulos
            try{
                Database admin = new Database(getApplicationContext(),null,1);
                SQLiteDatabase db = admin.getWritableDatabase();
                Cursor fila = db.rawQuery("SELECT * FROM recibido WHERE estatus='M'",null);
                if(fila.moveToFirst())
                {
                    Surt=Surt+1;
                    progreso.setMax(fila.getCount());
                    int i=0;
                    do{

                        publishProgress(i+1);
                        String folio = fila.getString(1);
                        String cantidad = fila.getString(12);
                        String posicion = fila.getString(8);
                        String fechafactura = fila.getString(9);
                        String client = fila.getString(10);
                        //modificar web service

                        Log.i("agregosurtido",""+folio+" "+posicion+" "+Surt+" "+cantidad+" "+fecha(true).toString()+" "+fechafactura+" "+client+" "+fecha(false).toString());

                        HttpClient cliente = new DefaultHttpClient();
                        HttpParams httpParameters = new BasicHttpParams();
                        HttpConnectionParams.setConnectionTimeout(httpParameters, 900000);
                        HttpConnectionParams.setSoTimeout(httpParameters, 900000);
                        HttpPost htppost=new HttpPost(URL+"agregarsurtido/4/"+folio+"/"+posicion+"/"+Surt+"/"+cantidad+"/A/"+fecha(true).toString()+"/"+fechafactura+"/"+client+"/"+fecha(false).toString());
                        org.apache.http.HttpResponse resx = cliente.execute(htppost);
                        BufferedReader bfr = new BufferedReader(new InputStreamReader(resx.getEntity().getContent()));
                        String linea="";
                        StringBuffer res = new StringBuffer();
                        while ((linea =bfr.readLine())!=null)
                        {
                            res.append(linea);
                            validar="OK";
                        }
                        String finalJSON = res.toString();
                        JSONObject jObject = new JSONObject(finalJSON); //Obtenemos el JSON global
                        //JSONArray jArray = jObject.getJSONArray("success"); //Obtenemos el array results

                        //Log.i("success",""+jObject.toString()+" "+jObject.getBoolean("success"));
                        if(jObject.getBoolean("success")==true)
                        {
                            validar="OK";
                        }
                        else
                        {
                            validar="sin respuesta";
                        }
                        bfr.close();

                    }while (fila.moveToNext());
                }

                db.close();
            }catch (Exception e)
            {
                //Toast.makeText(getApplicationContext(), "Error consulta bd:"+e.getMessage(), Toast.LENGTH_SHORT).show();
                validar=""+e.getMessage();
            }
            return validar;

        }

        protected void onProgressUpdate(Integer... i)
        {
            progreso.setProgress(i[0]);
        }
        protected void onPostExecute(String s)
        {
            progreso.dismiss();
            if(s.equalsIgnoreCase("OK"))
            {
                Toast.makeText(getApplicationContext(), "Agrego Surtido con exito",
                        Toast.LENGTH_LONG).show();
                revisarfacturas();
                //new ModificarEstatus().execute();
                eliminarTabla("recibido");
                et_factura.setText("");
                et_codigo.setText("");
                et_codigo.setEnabled(false);
                et_factura.setEnabled(true);
                et_factura.requestFocus();
                actualizarLista();
                et_factura.requestFocus();
                mostrarTeclado("FACTURA");
                cambiar.setText("CODIGO");

            }
            else
            {
                Toast.makeText(getApplicationContext(), ""+s,
                        Toast.LENGTH_LONG).show();
            }

            super.onPostExecute(s);
        }
    }
    public void revisarfacturas()
    {

        for(int i=0;i<folios.size();i++)
        {
            Log.i("revisarfacturas ",""+folios.size());
            try{
                Database admin = new Database(this,null,1);
                SQLiteDatabase db = admin.getWritableDatabase();
                Cursor fila = db.rawQuery("SELECT folio,diferencia,estatus,codigo FROM recibido WHERE diferencia>0 AND folio='"+folios.get(i).toString()+"'",null);
                if(fila.moveToFirst())
                {
                    do{
                        String folio=fila.getString(0);
                        int dif=fila.getInt(1);
                        String estatus=fila.getString(2);
                        String codigo=fila.getString(3);
                        Log.i("revisarfacturas"," | "+fila.getString(0)+" | "+fila.getString(1)+" | "+fila.getString(2)+" | "+codigo);
                    }while (fila.moveToNext());
                }
                else
                {
                    Log.i("revisarfacturas ","mandar al ws");
                    new ModificarEstatus().execute(folios.get(i));
                }
                db.close();
            }catch (Exception e)
            {
                Log.e("revisarfacturasError:",""+e.getMessage());
                mensajes("Error al buscar diferencias:",""+e.getMessage());
            }
            //mensajes("revisarfactura","Click en folios"+folios.get(i)+" "+folios.size()+"  i"+i+ " "+ facturacompleta);
        }

        folios.clear();
    }
    class ModificarEstatus extends AsyncTask<String,Integer,String>
    {
        private ProgressDialog progreso;
        String validar="OK";
        @Override
        protected void onPreExecute()
        {
            progreso = new ProgressDialog(contexto);
            progreso.setProgressStyle(ProgressDialog.
                    STYLE_HORIZONTAL);
            progreso.setMessage("Modificando Estatus...");
            progreso.setCancelable(false);
            progreso.setMax(100);
            progreso.setProgress(0);
            progreso.show();
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(String... params)
        {
            //consultar articulos
            try{
                    //progreso.setMax(fila.getCount());
                    //int i=0;
                        //publishProgress(i+1);
                            //modificar web service
                            HttpClient cliente = new DefaultHttpClient();
                            HttpParams httpParameters = new BasicHttpParams();
                            HttpConnectionParams.setConnectionTimeout(httpParameters, 900000);
                            HttpConnectionParams.setSoTimeout(httpParameters, 900000);
                            HttpPost htppost=new HttpPost(URL+"cambiarestatus/"+params[0]+"/S");
                            org.apache.http.HttpResponse resx = cliente.execute(htppost);
                            BufferedReader bfr = new BufferedReader(new InputStreamReader(resx.getEntity().getContent()));
                            String linea="";
                            StringBuffer res = new StringBuffer();
                            while ((linea =bfr.readLine())!=null)
                            {
                                res.append(linea);
                                validar="OK";
                            }
                            String finalJSON = res.toString();
                            JSONObject jObject = new JSONObject(finalJSON); //Obtenemos el JSON global
                            if(jObject.getBoolean("success")==true)
                            {
                                validar="OK";
                            }
                            bfr.close();

                            //Log.i("consultarecibido",""+folio+" "+codigo+" "+recibido);
            }catch (Exception e)
            {
                validar=""+e.getMessage();
            }

            return validar;

        }

        protected void onProgressUpdate(Integer... i)
        {
            //progreso.setProgress(i[0]);
        }
        protected void onPostExecute(String s)
        {
            progreso.dismiss();
            if(s.equalsIgnoreCase("OK"))
            {
                Toast.makeText(getApplicationContext(), "Modifico Estatus",
                        Toast.LENGTH_LONG).show();
            }
            else
            {
                Toast.makeText(getApplicationContext(), ""+s,
                        Toast.LENGTH_LONG).show();
            }

            super.onPostExecute(s);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.contro_recibido_options, menu);
        menu.findItem(R.id.menucod1).setChecked(true);
        menucod1 = menu.findItem(R.id.menucod1);
        menucod2 = menu.findItem(R.id.menucod2);
        bus_cod_macro="1";
        //mensajes("creando","");
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem menu) {

        if (menu.getItemId() == R.id.done) {
            guardarSurtir();
            //mensajes("Opcion:","done");
        }
        if (menu.getItemId() == R.id.drop) {
            //onBackPressed();
            cambiarFactura();
        }
        if(menu.getItemId()==R.id.menucod1)
        {
            menucod2.setChecked(false);
            menucod1.setChecked(true);
            mensajes("Seleccionó", "código 1");
            bus_cod_macro="1";
        }
        if(menu.getItemId()==R.id.menucod2)
        {
            menucod1.setChecked(false);
            menucod2.setChecked(true);
            bus_cod_macro="2";
            mensajes("Seleccionó", "código 2");
        }
        return super.onOptionsItemSelected(menu);
    }
    //ELIMINA LA FACTURA YA CONSULTADA
    public void cambiarFactura()
    {
        if(tablaVacia("recibido","folio")==true)
        {
            mensajes("","Tabla vacia");
        }
        else
        {
            new AlertDialog.Builder(this)
                    .setMessage("¿Está seguro que desea eliminar la Factura?")
                    .setNegativeButton("Cancelar", null)
                    .setCancelable(false)
                    .setPositiveButton("SI", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            eliminarTabla("recibido");
                            et_factura.setText("");
                            et_codigo.setText("");
                            et_factura.setEnabled(true);
                            et_factura.requestFocus();
                            cambiar.setText("CODIGO");
                            tv_fol.setText("");
                            et_codigo.setEnabled(false);
                            mostrarTeclado("FACTURA");
                            folios.clear();
                            actualizarLista();
                        }
                    }).create().show();
        }



    }
    //CREA UN TOAST
    public void mensajes(String titulo, String mensaje) {
        Toast.makeText(getApplicationContext(),titulo +" "+mensaje,Toast.LENGTH_SHORT).show();
    }
    //CUENTA LOS CODIGOS QUE VAS INGRESANDO
    public void contarRecibido()
    {
        int rec,dif,cantidadaux;
        if(consultaCodigo(et_codigo.getText().toString())==true)
        {
            //encontro codigo
            //Toast.makeText(getApplicationContext(),"dio enter en codigo:"+cantidadG+"|"+recibidoG+"|"+diferenciaG,Toast.LENGTH_SHORT).show();
                rec=Integer.parseInt(recibidoG);
                dif=Integer.parseInt(diferenciaG);
                cantidadaux=Integer.parseInt(CantidadAux);
                if(dif!=0)
                {
                    cantidadaux=cantidadaux+1;
                    rec=rec+1;
                    dif=dif-1;
                    actualizarBD(rec,dif,et_codigo.getText().toString(),idCodigo,cantidadaux);
                    actualizarLista();
                    et_codigo.setText("");
                }
                else{
                    Toast.makeText(this, "codigo sin diferencia", Toast.LENGTH_SHORT).show();
                    et_codigo.setText("");
                }

        }

    }
    //ACTUALIZA LA BASE DE DATOS
    public void actualizarBD(int recibido, int dif, String codigo,String idcodigo,int cantidadAux)
    {
        try{
            Database admin=new Database(this,null,1);
            SQLiteDatabase db = admin.getWritableDatabase();
            ContentValues registro = new ContentValues();
            registro.put("recibido",recibido);
            registro.put("diferencia",dif);
            registro.put("estatus","M");
            registro.put("cantidadtmp",cantidadAux);
            if(bus_cod_macro.equalsIgnoreCase("1"))
            {
                db.update("recibido",registro, "codigo='"+ codigo +"' AND _id='"+idcodigo+"'",null);
            }
            else
            {
                db.update("recibido",registro, "codigo2='"+ codigo +"' AND _id='"+idcodigo+"' ",null);
            }

            db.close();
        }catch (Exception e)
        {
            Toast.makeText(this, "Error:"+e.getMessage(), Toast.LENGTH_SHORT).show();
        }




    }
    //CONSULTA CODIGO
    public boolean consultaCodigo(String codigo)
    {
        String busquedacod="codigo";
        boolean consulta;
        Database admin = new Database(this,null,1);
        SQLiteDatabase db = admin.getWritableDatabase();
        Cursor fila = null;
        if(bus_cod_macro.equalsIgnoreCase("2"))
        {
            busquedacod="codigo2";
        }
        fila = db.rawQuery("SELECT * FROM recibido WHERE "+busquedacod+"='"+codigo+"' AND diferencia>0",null);
        if(fila.moveToFirst())
        {
                    Log.i("buscarCod"," | "+fila.getString(0)+" | "+fila.getString(1)+" | "+fila.getString(2)+" | "+fila.getString(3)+" | "+fila.getString(4)+" | "+fila.getString(5)
                            +" | "+fila.getString(6)+" | "+fila.getString(7)+" | "+fila.getString(8)+fila.getString(9)+" | "+fila.getString(10)+" | "+fila.getString(11)+" | "+fila.getString(12)+" | "+fila.getInt(13));
                    idCodigo=fila.getString(0);
                    cantidadG = fila.getString(4);
                    recibidoG = fila.getString(5);
                    diferenciaG = fila.getString(6);
                    CantidadAux = fila.getString(12);
            consulta=true;
            //Toast.makeText(this, "codigo encontrado", Toast.LENGTH_SHORT).show();
        }
        else {
            consulta=false;
            Toast.makeText(this, "codigo no encontrado", Toast.LENGTH_SHORT).show();
            et_codigo.setText("");
        }
        db.close();
        return consulta;

    }
    //ACTUALIZA LA VISTA
    public void actualizarLista()
    {
        Recibido_list = new ArrayList<Recibido>();
        Database admin = new Database(this,null,1);
        String r;
        SQLiteDatabase db = admin.getWritableDatabase();
        Cursor fila = db.rawQuery("SELECT * FROM recibido",null);
        if(fila.moveToFirst())
        {
            do{
                String folio = fila.getString(1);
                String codigo = fila.getString(2);
                String descripcion = fila.getString(3);
                String cantidad = fila.getString(4);
                String recibido = fila.getString(5);
                String diferencia = fila.getString(6);
                //Toast.makeText(this, "con datos", Toast.LENGTH_SHORT).show();
                Recibido_list.add(new Recibido(codigo, descripcion, cantidad, recibido, diferencia,folio));
            }while (fila.moveToNext());
        }
        else {
            Toast.makeText(this, "sin datos", Toast.LENGTH_SHORT).show();
        }
        db.close();
        //Recibido_list.add(new Recibido("x", "xd", "asdfasd", "asdfasdl", "als"));
        //Recibido_list.add(new Recibido("x", "xd", "asdfasd", "asdfasdl", "als"));

        Recibido_adap = new RecibidoAdapter( Recibido_list,this);
        lvItems.setAdapter(Recibido_adap);
    }
    //OBTENER FECHA ACTUAL
    public String fecha(boolean g)
    {
        String valor="";
        if(g==true)
        {

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date date = new Date();
            valor=dateFormat.format(date);
        }
        else
        {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
            Date date = new Date();
            valor=dateFormat.format(date);
        }

        return valor;
    }
    //OPTIENE EL DOMINIO
    public void getDomain(){
        Database admin = new Database(this,null,1);
        this.URL = admin.getDomain();
        //Toast.makeText(getApplicationContext(),"Dominio :" + URL,Toast.LENGTH_SHORT).show();
        if (URL.equalsIgnoreCase("N")){
            new AlertDialog.Builder(this)
                    .setTitle("No se ha podido obtener el dominio")
                    .setMessage("Por favor, pongase en contacto con un asesor de Arisoft para poder resolver este problema")
                    .setPositiveButton("aceptar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                        }
                    }).create().show();
        }
    }
    //ELIMINAR TABLA DE BASE DE DATOS
    public void eliminarTabla(String tabla)
    {
        Database admin=new Database(this,null,1);
        SQLiteDatabase db = admin.getWritableDatabase();
        //db.execSQL("DROP TABLE IF EXISTS "+tabla);
        db.execSQL("DELETE FROM " + tabla);
        db.close();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("¿Está seguro que desea salir?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Salir", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        eliminarTabla("recibido");
                        controlRecibidoMulti.super.onBackPressed();
                    }
                }).create().show();
    }

}
