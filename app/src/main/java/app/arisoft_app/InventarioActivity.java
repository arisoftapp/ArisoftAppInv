package app.arisoft_app;

import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
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
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import app.arisoft_app.Interfaz.ServiceExiActual;
import app.arisoft_app.Modelo.Articulo;
import app.arisoft_app.Modelo.ExistenciaActual;
import app.arisoft_app.Modelo.Inventario;
import app.arisoft_app.Modelo.RespExiActual;
import app.arisoft_app.Tools.Database;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class InventarioActivity extends AppCompatActivity implements DialogInterface.OnDismissListener {
    private EditText et_codigo;
    ArrayList<String> arrayList = new ArrayList<>();
    ArrayList<String> arraySerie = new ArrayList<>();
    ArrayList<Inventario> Inventario;
    private InventarioListaConteoAdapter inv_adapter;
    private GridView gridView;
    float conteobusqueda;
    int partidas=0;
    String idbusqueda;
    Boolean encontrarBusqueda;
    Boolean encontrarExiMacro;
    Boolean mod_con_ser;
    String almacen;
    String idalmacen;
    CheckBox check_cantidad,check_busqueda;
    EditText et_cantidad;
    LinearLayout main, vertical, page;
    private ListView lv_conteo;
    MenuItem done, cancel, reset, menucod1, menucod2;
    TextView tv_alm,tv_partidas;
    private float exiAct;
    private Handler mhandler = new Handler();
    String codigoexi;
    float conteoexi;
    String bus_cod_macro = "";
    private String URL;
    Button btn_borrar;
    MediaPlayer mp,mpError;
    EditText sv_busqueda;
    private ProgressDialog progreso;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventario);
        getDomain();

        page =  ((LinearLayout)findViewById(R.id.linear_alm));
        lv_conteo = (ListView) findViewById(R.id.lista_conteo);
        //lv_conteo.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        lv_conteo.setLongClickable(true);
        check_cantidad = (CheckBox) findViewById(R.id.check_cantidad);
        check_busqueda = (CheckBox) findViewById(R.id.check_busqueda);
        et_cantidad = (EditText) findViewById(R.id.et_cantidad);
        et_codigo = (EditText) findViewById(R.id.et1);
        btn_borrar=(Button) findViewById(R.id.btn_borrar);
        tv_partidas=(TextView)findViewById(R.id.tv_partidas);
        mp=MediaPlayer.create(this,R.raw.beepmicro);
        mpError=MediaPlayer.create(this,R.raw.error);
        sv_busqueda=(EditText) findViewById(R.id.sv);
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(sv_busqueda.getWindowToken(), 0);
        //mp.start();
        et_codigo.setFilters(new InputFilter[] {new InputFilter.AllCaps()});
        sv_busqueda.setFilters(new InputFilter[] {new InputFilter.AllCaps()});
        sv_busqueda.setText("");
        sv_busqueda.setEnabled(false);
        try{
            Bundle bundle = getIntent().getExtras();
            idalmacen = bundle.getString("idalmacen");
            almacen = bundle.getString("almacen");
            String selectedAlm = idalmacen + " - " + almacen;
            if(bundle!=null){
                tv_alm = page.findViewById(R.id.tv_alm);
                tv_alm.setText(almacen);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        contarExistencia();
        checkConteo();

        lv_conteo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Inventario model = Inventario.get(position);
                if (model.getSerie().equalsIgnoreCase("S")){
                    ModalFullScreenSeries dialog = new ModalFullScreenSeries();
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    Bundle b = new Bundle();
                    b.putString("idalmacen", idalmacen);
                    b.putString("codigo", model.getCodigoArticulo());
                    b.putString("desc", model.getDescripcion());
                    dialog.setArguments(b);
                    dialog.show(ft, ModalFullScreenSeries.TAG);
                }
            }
        });


        lv_conteo.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        lv_conteo.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                final int checkedItemCount = lv_conteo.getCheckedItemCount();
                mode.setTitle(Integer.toString(checkedItemCount));

                    Inventario model = Inventario.get(position);

                    if (model.isSelected()) {
                        model.setSelected(false);
                    }
                    else{
                        model.setSelected(true);
                    }
                    Inventario.set(position, model);
                    //now update adapter
                    inv_adapter.updateRecords(Inventario);
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.conteo_options_am, menu);
                //mode.getMenuInflater().inflate(R.menu.conteo_options, menu);
                done = menu.findItem(R.id.done);
                cancel = menu.findItem(R.id.cancel);
                reset = menu.findItem(R.id.reset);
                done.setVisible(false);
                cancel.setVisible(false);
                reset.setVisible(false);
                //menucod1.setVisible(false);
                //menucod2.setVisible(false);
                //mensajes("creando"," el menu");
                //menucod1.isChecked();
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
                if (item.getItemId() == R.id.edit) {
                    editarConteo();
                    mode.finish();
                }
                if (item.getItemId() == R.id.drop) {
                    //Toast.makeText(InventarioActivity.this, "ELIMINAR REGISTROS", Toast.LENGTH_SHORT).show();

                    eliminarArticulo();
                    consultaPartidas();
                    mode.finish();

                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                int size = Inventario.size();
                for ( int i = 0; i < size; i++){
                    Inventario model = Inventario.get(i);
                    model.setSelected(false);
                    Inventario.set(i, model);
                    inv_adapter.updateRecords(Inventario);
                    done.setVisible(true);
                    cancel.setVisible(true);
                    reset.setVisible(true);
                    //menucod1.setVisible(true);
                    //menucod2.setVisible(true);
                }
            }
        });
        //filtrado
        sv_busqueda.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                consultaFiltro(sv_busqueda.getText().toString().trim());

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        et_codigo.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                //accion se activa al dar enter en edit text codigo
                //SI EL CHECK CANTIDAD ESTÁ ACTIVADO LA FUNCIÓN DE ENTER NO FUNCIONARÁ
                //ESTE EVENTO PASARÁ AL EDIT TEXT DE CANTIDAD DE ACUERDO AL FLUJO DE TRABAJO.
                if (!check_cantidad.isChecked()) {
                    //si check cantidad esta desactivado
                    //conteo();
                    conteoAR();
                    actualizarGrid();
                    InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(et_codigo.getWindowToken(), 0);
                    //check_cantidad.setChecked(false);
                    et_codigo.setText("");
                    et_codigo.requestFocus();

                } else{
                    //si check cantidad esta activo
                    et_cantidad.requestFocus();
                    prueba();
                }
                return false;
            }
        });


        et_cantidad.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (check_cantidad.isChecked()) {
                    //conteo();
                    conteoAR();
                    //actualizarGrid();
                    InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(et_codigo.getWindowToken(), 0);
                }
                et_codigo.setText("");
                et_cantidad.setText("1");
                et_codigo.requestFocus();
                return false;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.conteo_options, menu);
        menu.findItem(R.id.drop).setVisible(false);
        menu.findItem(R.id.edit).setVisible(false);
        menu.findItem(R.id.menucod1).setChecked(true);
        menucod1 = menu.findItem(R.id.menucod1);
        menucod2 = menu.findItem(R.id.menucod2);
        bus_cod_macro="1";

        //mensajes("creando","");
        return true;
    }
    public void borrarCodigo(View v){
        et_codigo.setText("");
        //mensajes("xd","xd");
    }
    public void prueba()
    {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                // acciones que se ejecutan tras los milisegundos
                //mensajes("Probando:","entro a prueba");
                et_cantidad.requestFocus();
            }
        }, 500);

    }
    public void consultaPartidas()
    {


        Database admin = new Database(this,null,1);
        SQLiteDatabase db = admin.getWritableDatabase();
        Cursor fila = db.rawQuery("SELECT * FROM conteo WHERE idalmacen='"+idalmacen+"'",null);
            partidas=fila.getCount();
            Log.i("partidas",""+partidas);
            if(tv_partidas.getText().toString().equalsIgnoreCase("TextView"))
            {

            }
            else
            {

                String aux;
                aux=tv_partidas.getText().toString();
                String[] parts = aux.split("/");
                tv_partidas.setText(partidas+"/"+parts[1]);
            }
        db.close();
    }
    public void contarExistencia()
    {

        ServiceExiActual service=retrofit().create(ServiceExiActual.class);
        Call<RespExiActual> call=service.soloEx(idalmacen);
        call.enqueue(new retrofit2.Callback<RespExiActual>() {
            @Override
            public void onResponse(Call<RespExiActual> call, Response<RespExiActual> response) {
                if(response.isSuccessful())
                {
                    //Toast.makeText(getApplicationContext(),"sin conteo"+" "+response.code(),Toast.LENGTH_SHORT).show();
                    RespExiActual r=response.body();
                    ArrayList<ExistenciaActual> l = r.getExistencia();
                    consultaPartidas();
                    tv_partidas.setText( partidas+"/"+r.getItems());

                        Log.i("sincon:",""+" "+idalmacen+" |"+r.getItems()+" | "+response.code());

                }
            }

            @Override
            public void onFailure(Call<RespExiActual> call, Throwable t) {
                Toast.makeText(getApplicationContext(),"Error modexi:"+t.getMessage(),Toast.LENGTH_SHORT).show();
                Log.i("Error modexi:",t.getMessage());
            }
        });
    }

    //CONDICION SI HAY CONTEO GUARDADO
    public void checkConteo(){
        boolean conteo = getConteoAlm();
        if (conteo == true){
            conteoGuardado();
            Log.i("CHECK_CONTEO", "CONTEO GUARDADO");
        } else {
            nuevoConteo();
            Log.i("CHECK_CONTEO", "NO HAY CONTEO GUARDADO");
        }
    }

    //BUSCA SI HAY UN CONTEO EXISTENTE EN ESE ALMACEN
    public boolean getConteoAlm(){
        Boolean exist = false;
        Database admin = new Database(this, null,1);
        SQLiteDatabase db = admin.getWritableDatabase();
        try {
            Cursor fila = db.rawQuery("SELECT * FROM conteo WHERE idalmacen='"+ idalmacen+"'",null);
            if(fila.moveToFirst()) {
                exist = true;
            }
        }catch (SQLiteException sql){
            exist = false;
        }
        db.close();
        return exist;
    }

    public void onCheckboxClicked(View v) {
        if (check_cantidad.isChecked()) {
            et_cantidad.setEnabled(true);
        } else {
            if(et_cantidad.getText().toString().equalsIgnoreCase(""))
            {
                et_cantidad.setText("1");
            }
            et_cantidad.setEnabled(false);
            et_codigo.requestFocus();
        }

    }
    public void onCheckboxbusqueda(View v){
        if(check_busqueda.isChecked())
        {
            et_codigo.setEnabled(false);
            sv_busqueda.setEnabled(true);
            sv_busqueda.requestFocus();
        }else {
            et_codigo.setEnabled(true);
            et_codigo.requestFocus();
            sv_busqueda.setText("");
            sv_busqueda.setEnabled(false);
        }
    }

    //CONTINUAR CON EL CONTEO GUARDADO O COMENZAR UNO NUEVO
    public void conteoGuardado(){
        AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this);
        dialogo1.setTitle("Conteo encontrado");
        dialogo1.setMessage("\nSe ha detectado que ya cuenta con un conteo iniciado en el almacen seleccionado.\n \n¿Desea continuar con el conteo?\n");
        dialogo1.setCancelable(false);
        dialogo1.setPositiveButton("CONTINUAR", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {
                actualizarGrid();
            }
        });
        dialogo1.setNegativeButton("NUEVO CONTEO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {
                eliminarConteoAlm();
                nuevoConteo();
                //actualizarGrid();
            }
        });
        dialogo1.show();
    }

    //CREAR NUEVO CONTEO
    public void nuevoConteo(){
        arrayList.clear();
        arrayList.add("Código");
        arrayList.add("Conteo");
        arrayList.add("Existencia");
        Inventario = new ArrayList<>();
        //mensajes("","tipo de inventario");
        //gridAdapter = new GridAdapter(this, arrayList);
        if(revisarConfig()==true)
        {
            try{
                Database admin = new Database(getApplicationContext(), null, 1);
                SQLiteDatabase db = admin.getWritableDatabase();
                ContentValues r = new ContentValues();
                r.put("idalmacen", idalmacen);
                r.put("almacen", almacen);
                r.put("tipo", "A");
                db.insert("tipoInventario", null, r);
                db.close();
            }catch (Exception e)
            {
                mensajes("","Error al insertar tabla tipoInventario:"+e.getMessage());
            }
        }
        else
        {
            eliminarTipoInventario();
            tipoInventario();
        }



    }
    public boolean revisarConfig()
    {
        boolean paramactivo=false;
        try {
            Database admin = new Database(getApplicationContext(), null,1);
            SQLiteDatabase db = admin.getWritableDatabase();
            Cursor fila = db.rawQuery("SELECT dialog_tipo_inv FROM  configuracion where id=1",null);
            if(fila.moveToFirst())
            {
                if(fila.getString(0).equalsIgnoreCase("true"))
                {
                    paramactivo=true;
                }
                else
                {
                    paramactivo=false;
                }

                Log.i("config",""+fila.getString(0));

            }
            else
            {
                Log.i("config","no encontro nada");
            }
            db.close();
        }catch (SQLiteException sql){
            Toast.makeText(getApplicationContext(),"Error en consulta configuracion:"+sql.getMessage(),Toast.LENGTH_SHORT).show();
        }
        return paramactivo;
    }
    public void eliminarTipoInventario()
    {
        try{
            Database admin=new Database(this,null,1);
            SQLiteDatabase db = admin.getWritableDatabase();
            db.execSQL("DELETE FROM tipoInventario WHERE idalmacen='" + idalmacen+"'");
            db.close();
        }catch (Exception e)
        {
            mensajes("","Error al eliminar tabla tipoInventario:"+e.getMessage());
        }

    }
    public void tipoInventario()
    {
        eliminarTipoInventario();
        LayoutInflater inflater = InventarioActivity.this.getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_opc_tipoinventario, null);
        final RadioButton rd_pa=(RadioButton) v.findViewById(R.id.rb_pa);
        final RadioButton rd_pc=(RadioButton) v.findViewById(R.id.rb_pc);
        final EditText et_id=(EditText) v.findViewById(R.id.et_id);
        final LinearLayout ll_id=(LinearLayout) v.findViewById(R.id.ll_id);
        final RadioGroup tipo_opc=(RadioGroup) v.findViewById(R.id.tipo_opc);
        final Button btn_aceptar=(Button) v.findViewById(R.id.btn_aceptar);


        tipo_opc.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(rd_pc.isChecked()==true)
                {
                    ll_id.setVisibility(View.VISIBLE);
                }
                else
                {
                    ll_id.setVisibility(View.GONE);
                }
            }
        });



        final AlertDialog dialog = new AlertDialog.Builder(InventarioActivity.this)
                .setTitle("Tipo de Inventario")
                .setView(v)
                .setCancelable(false)
                .create();
        dialog.show();
        btn_aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                try{
                    Database admin = new Database(getApplicationContext(), null, 1);
                    SQLiteDatabase db = admin.getWritableDatabase();
                    ContentValues r = new ContentValues();
                    r.put("idalmacen", idalmacen);
                    r.put("almacen", almacen);

                    if (rd_pa.isChecked()==true)
                    {
                        //mensajes("","abierto");
                        r.put("tipo", "A");
                        dialog.dismiss();
                    }
                    else {
                        if(rd_pc.isChecked()==true)
                        {
                            //mensajes("","cerrado");
                            String et=et_id.getText().toString();
                            if(et.equalsIgnoreCase(""))
                            {
                             mensajes("","Campo Id en blanco");
                             et_id.requestFocus();
                            }
                            else
                            {
                                r.put("tipo", "C");
                                r.put("id", et_id.getText().toString());
                                dialog.dismiss();
                            }

                        }
                    }
                    db.insert("tipoInventario", null, r);
                    db.close();


                }catch (Exception e)
                {
                    mensajes("","Error al insertar tabla tipoInventario:"+e.getMessage());
                }

            }
        });

    }


    //ELIMINAR EL CONTEO ANTERIOR
    public void eliminarConteoAlm() {
        Database admin=new Database(this,null,1);
        SQLiteDatabase db = admin.getWritableDatabase();
        //db.execSQL("DROP TABLE IF EXISTS "+tabla);
        db.execSQL("DELETE FROM conteo WHERE idalmacen='" + idalmacen+"'");
        db.execSQL("DELETE FROM series WHERE almacen = '" + idalmacen+"'");
        db.close();
    }

    //LLAMAR CONSULTA INICIAL PARA ACTUALIZAR LA LISTA
    //DE ARTICULOS DEL CONTEO
    public void actualizarGrid() {
        arrayList.clear();
        arrayList.add("Código");
        arrayList.add("Conteo");
        arrayList.add("Existencia");
        consultaInicial();

            et_codigo.requestFocus();
        //mp.start();
           // mensajes("check","no check activado");
    }

    //AL INGRESAR EL CODIGO DEL ARTICULO EN EL TEXTVIEW
    //VERIFICA SI EL ARTICULO INGRESADO YA EXISTE EN EL CONTEO
    public void conteoAR() {
        mod_con_ser=false;
        conteoexi = Float.parseFloat(et_cantidad.getText().toString());
        codigoexi = et_codigo.getText().toString().trim();
        //TABLAVACIA VERIFICA SI EL ARTICULO YA EXISTE EN EL CONTEO
        if(tablaVacia("conteo","codigo"))
        {
            existenciaAR();
        }
        else
        {
            //tabla con datos buscar codigo
            Float bus_cod=Float.parseFloat(bus_cod_macro);
            if(bus_cod==1)
            {
                if(busqueda(codigoexi)) //OBTENER EL CONTEO
                {
                    mod_con_ser=true;
                    //modificar aumento
                    buscar(codigoexi);
                    conteobusqueda += conteoexi;
                    //modificarConteo(idbusqueda,conteobusqueda);
                    //actualizarGrid();
                    //pedir serie
                    existenciaAR();
                }
                else
                {
                    existenciaAR();
                    //insertar nuevo
                    //insertarConteo(codigo,conteo,existenciaMacro);
                }
            }
            else
            {
                if(bus_cod==2)
                {
                    ServiceExiActual service=retrofit().create(ServiceExiActual.class);
                    Call<RespExiActual> call=service.r1(idalmacen,codigoexi,bus_cod_macro);
                    call.enqueue(new retrofit2.Callback<RespExiActual>() {
                        @Override
                        public void onResponse(Call<RespExiActual> call, Response<RespExiActual> response) {
                            //String x="";
                            if(response.isSuccessful()) {
                                RespExiActual r = response.body();
                                ArrayList<ExistenciaActual> l = r.getExistencia();
                                for (int i=0; i < l.size(); i++) {
                                    ExistenciaActual e = l.get(i);
                                    if(busqueda(e.getCodigo())) //OBTENER EL CONTEO
                                    {
                                        mod_con_ser=true;
                                        //modificar aumento
                                        buscar(e.getCodigo());
                                        conteobusqueda += conteoexi;
                                        //modificarConteo(idbusqueda,conteobusqueda);
                                        //actualizarGrid();
                                        //pedir serie
                                        existenciaAR();
                                    }
                                    else
                                    {
                                        existenciaAR();
                                        //insertar nuevo
                                        //insertarConteo(codigo,conteo,existenciaMacro);
                                    }

                                }
                                if(l.size()<1) {
                                    Toast.makeText(getApplicationContext(),"Articulo no encontrado en la base de datos",Toast.LENGTH_SHORT).show();

                                }
                                //Log.i("existencia",""+exiAct)
                            }
                            else {
                                Toast.makeText(getApplicationContext(),"Error al buscar el artículo",Toast.LENGTH_SHORT).show();
                            }
                            actualizarGrid();
                        }

                        @Override
                        public void onFailure(Call<RespExiActual> call, Throwable t) {
                            Log.i("Error:no se encontro ",t.getMessage());
                            Toast.makeText(getApplicationContext(),"No se encontró el articulo",Toast.LENGTH_SHORT).show();
                        }
                    });


                }
            }

        }
    }

    public void conteo() {
        float conteo = Float.parseFloat(et_cantidad.getText().toString());
        String codigo = et_codigo.getText().toString();
        float existenciaMacro = consultaExistencia(codigo);
  //      String x=existenciaMacroAR(codigo);
   /*     existenciaMacroAR(codigo);
        Log.i("prueba","pruebaxd");
        String xd="11";

        int existenciaMacro = Integer.parseInt(xd);
*/
       if(encontrarExiMacro==true)
        {
            if(tablaVacia("conteo","codigo"))
            {

                //insertarConteo(codigo,conteo,existenciaMacro);
                Toast.makeText(this, ""+existenciaMacro+" |", Toast.LENGTH_SHORT).show();
            }
            else
            {
                //tabla con datos buscar codigo
                if(busqueda(codigo)==true)
                {
                    //modificar aumento
                    buscar(codigo);
                    conteobusqueda+=conteo;
                    modificarConteo(idbusqueda,conteobusqueda);
                    actualizarGrid();
                }
                else
                {
                    //insertar nuevo
                    //insertarConteo(codigo,conteo,existenciaMacro);
                }
            }
        }
        else
        {
            Toast.makeText(this, "Articulo no encontrado en MacroPro", Toast.LENGTH_SHORT).show();
        }
    }

    public String getIdConteo(String codigo) {
        String id_conteo = "", r;
        Database admin = new Database(this,null,1);
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

    public void buscar(String codigo) {
        String r;
        boolean encontrar=false;
        Database admin = new Database(this,null,1);
        SQLiteDatabase db = admin.getWritableDatabase();
        Cursor fila = db.rawQuery("SELECT * FROM conteo where idalmacen='"+idalmacen+"'",null);
        if(fila.moveToFirst())
        {
            do{
                //conteobusqueda=fila.getInt(1);
                r=fila.getString(1);
                //Toast.makeText(this,r+" "+codigo, Toast.LENGTH_SHORT).show();
                if(r.equalsIgnoreCase(codigo))
                {
                    encontrar=true;
                    encontrarBusqueda=true;
                    conteobusqueda=fila.getFloat(3);
                    idbusqueda=fila.getString(0);
                    //Toast.makeText(this,r+" "+codigo+" encontro codigo igual", Toast.LENGTH_SHORT).show();
                }

            }while (fila.moveToNext() && encontrar==false);

        }
        else
        {
            encontrarBusqueda=false;
            Toast.makeText(this, "No existe un artículo con el código indicado", Toast.LENGTH_SHORT).show();
        }
        db.close();
    }

    //PROCESO DE EDITAR CONTEO EN UN DIALOG
    public void editarConteo() {
        int size = Inventario.size();
        for ( int i = 0; i < size; i++){
            LayoutInflater inflater = InventarioActivity.this.getLayoutInflater();
            View v = inflater.inflate(R.layout.dialog_editar_conteo, null);
            final EditText tvnuevo_conteo = v.findViewById(R.id.editTextConteo);
            final TextView tvcodigo = v.findViewById(R.id.tv_cod);
            final TextView tvdesc = v.findViewById(R.id.tv_descr);
            final TextView tvexi= v.findViewById(R.id.tv_exiact);
            ImageView menos = (ImageView) v.findViewById(R.id.imageView5);
            ImageView mas = (ImageView) v.findViewById(R.id.imageView4);
            Button btnActualizar=(Button) v.findViewById(R.id.btnactualizar);

            final Inventario model = Inventario.get(i);

            btnActualizar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //new EditarConteoExi().execute(model.getCodigoArticulo().toString());
                    EditarConteoExi t=new EditarConteoExi();
                    t.execute(model.getCodigoArticulo().toString());
                    String r="";
                    try{
                        r=t.get(10,TimeUnit.SECONDS).toString();
                        tvexi.setText(r);
                    } catch(TimeoutException e) {
                        Toast.makeText(getApplicationContext(), "Tiempo excedido al validar",
                                Toast.LENGTH_LONG).show();
                    } catch(CancellationException e) {
                        Toast.makeText(getApplicationContext(), "Error al conectar con servidor",
                                Toast.LENGTH_LONG).show();
                    } catch(Exception e) {
                        Toast.makeText(getApplicationContext(), "Error con tarea asíncrona",
                                Toast.LENGTH_LONG).show();
                    }
                    //Toast.makeText(getApplicationContext(), "actualizar " , Toast.LENGTH_SHORT).show();

                }
            });
            menos.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Toast.makeText(getApplicationContext(), "menos", Toast.LENGTH_SHORT).show();
                    float nc=Float.parseFloat(tvnuevo_conteo.getText().toString());
                    nc=nc-1;
                    tvnuevo_conteo.setText(""+nc);
                }
            });
            mas.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Toast.makeText(getApplicationContext(), "mas", Toast.LENGTH_SHORT).show();
                    Float nc=Float.parseFloat(tvnuevo_conteo.getText().toString());
                    nc=nc+1;
                    //Toast.makeText(getApplicationContext(), "mas "+nc , Toast.LENGTH_SHORT).show();
                    tvnuevo_conteo.setText(""+nc);
                }
            });


            if (model.isSelected()){
                final String codigo = model.getCodigoArticulo();
                tvcodigo.setText(codigo);
                tvdesc.setText(model.getDescripcion());
                tvnuevo_conteo.setText(Float.toString(model.getConteo()));
                tvexi.setText(Float.toString(model.getExistencia()));
                AlertDialog dialog = new AlertDialog.Builder(InventarioActivity.this)
                        .setTitle("Editar conteo")
                        .setView(v)
                        .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                float nuevo_conteo = Float.parseFloat(tvnuevo_conteo.getText().toString());
                                //String task = String.valueOf(tvnuevo_conteo.getText());
                                //Integer nuevo_conteo = Integer.parseInt(task);
                                Log.i("EDITAR_CONTEO", "NUEVO CONTEO: " + codigo + " " + nuevo_conteo);
                                String Id = getIdConteo(codigo);
                                modificarConteo(Id, nuevo_conteo);
                                actualizarGrid();
                                Log.i("EDITAR_CONTEO", "SALIR DE POSITIVE BUTTON");
                            }
                        })
                        .setNegativeButton("Cancelar", null)
                        .create();
                dialog.show();
            }
        }
        actualizarGrid();
    }
    class EditarConteoExi extends AsyncTask<String,Integer,String>
    {
        float exi_act=0;
        private ProgressDialog progreso;
        @Override
        protected void onPreExecute()
        {
            progreso = new ProgressDialog(InventarioActivity.this);
            progreso.setProgressStyle(ProgressDialog.
                    STYLE_HORIZONTAL);
            progreso.setMessage("Calculando...");
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
                //Log.i("Async",params[0]+" "+params[1]);
                HttpClient cliente = new DefaultHttpClient();
                /* Definimos la ruta al servidor. */
                String value="Fallo";
                HttpParams httpParameters = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParameters, 900000);
                HttpConnectionParams.setSoTimeout(httpParameters, 900000);
                HttpGet htpoget = new HttpGet(URL+"existencia/"+idalmacen+"/"+params[0]+"/"+"1");
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
                    publishProgress(i+1);
                    try {
                        JSONObject objeto = jArray.getJSONObject(i); //Obtenemos cada uno de los objetos del array results
                        exi_act=Float.parseFloat(objeto.getString("existenciaActual"));
                        Log.i("array2",exi_act+" | "+c++);
                        Database admin=new Database(getApplicationContext(),null,1);
                        SQLiteDatabase db = admin.getWritableDatabase();
                        ContentValues registro = new ContentValues();
                        registro.put("existencia",exi_act);
                        db.update("conteo",registro,"codigo='"+ params[0] +"' AND idalmacen='"+ idalmacen +"'",null);
                        db.close();
                    } catch (JSONException e) {
                        Log.e("error",e.getMessage());
                    }
                }
                bfr.close();
            }
            catch (Exception e)
            {
                Log.e("Error",""+e.getMessage());
            }
            return ""+exi_act;

        }

        protected void onProgressUpdate(Integer... i)
        {

            progreso.setProgress(i[0]);
        }
        protected void onPostExecute(String s)
        {
            Toast.makeText(getApplicationContext(), "Existencia actualizada",
                    Toast.LENGTH_LONG).show();
            progreso.dismiss();
            super.onPostExecute(s);

        }
    }

    //HACE LA ACTUALIZACION DEL CONTEO EN LA BASE DE DATOS DE SQLITE
    public void modificarConteo(String Id, float conteo) {
        Database admin=new Database(this,null,1);
        SQLiteDatabase db = admin.getWritableDatabase();
        ContentValues registro = new ContentValues();
        registro.put("conteo",conteo);
        db.update("conteo",registro,"_id="+ Id +" AND idalmacen='"+ idalmacen +"'",null);
        db.close();
    }

    public float consultaExistencia(String codigo) {

        Database admin = new Database(this,null,1);
        float respuesta=0;
        SQLiteDatabase db = admin.getWritableDatabase();
        Cursor fila = db.rawQuery("SELECT existenciaActual FROM existencia WHERE codProd='"+codigo+"'"+"AND idalmacen='"+idalmacen+"'",null);
        if(fila.moveToFirst())
        {
            do{
                encontrarExiMacro=true;
                respuesta=Float.parseFloat(fila.getString(0));
            }while (fila.moveToNext());
        }
        else
        {
            encontrarExiMacro=false;
            //Toast.makeText(this, "Articulo no encontrado en Existencia", Toast.LENGTH_SHORT).show();
        }
        db.close();
        return respuesta;
    }

    Context contexto;
    public void barra(String mensaje)
    {

        progreso = new ProgressDialog(this);
        progreso.setMessage(mensaje);
        progreso.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progreso.setIndeterminate(true);
        //progreso.setProgress(0);
        //progreso.setMax(100);
        progreso.setCancelable(false);
        progreso.show();

    }

    //CONSULTA AL WEBSERVICE, PIDE DETALLES DEL ARTICULO
    public void existenciaAR() {
        //String buscar="1";
        barra("Consultando Articulo ...");
        ServiceExiActual service=retrofit().create(ServiceExiActual.class);
        Call<RespExiActual> call=service.r1(idalmacen,codigoexi,bus_cod_macro);
        call.enqueue(new retrofit2.Callback<RespExiActual>() {
            @Override
            public void onResponse(Call<RespExiActual> call, Response<RespExiActual> response) {
                //String x="";
                    if(response.isSuccessful()) {
                        float bus_cod=Float.parseFloat(bus_cod_macro);
                        //encontrarExiMacro = true;
                        RespExiActual r = response.body();
                        ArrayList<ExistenciaActual> l = r.getExistencia();
                        for (int i=0; i < l.size(); i++) {
                            ExistenciaActual e = l.get(i);
                            exiAct  = Float.parseFloat(e.getExistenciaActual());
                            if(bus_cod==1)
                            {
                                mp.start();
                                mensajes("Se Agrego codigo: ",codigoexi);
                                if(e.getSerie().equalsIgnoreCase("S")) { //SI ARTICULO ES CON SERIES
                                    if(mod_con_ser) { //SI EL ARTICULO CON SERIES YA EXISTE EN EL CONTEO
                                        //new cargarSerieAT().execute(codigoexi);
                                        dialogSeries(codigoexi,e.getDescripcion(),idalmacen);
                                        progreso.dismiss();
                                    }
                                    else { //SI EL ARTICULO CON SERIES NO EXISTE EN EL CONTEO
                                        insertarConteo(codigoexi,conteoexi,exiAct,e.getDescripcion(),e.getSerie());
                                        Log.i("existencia",""+exiAct+" |"+codigoexi+" |"+conteoexi+" |"+e.getSerie()+" |"+i);
                                        new cargarSerieAT().execute(codigoexi);
                                        dialogSeries(codigoexi,e.getDescripcion(),idalmacen);
                                        progreso.dismiss();
                                    }
                                }
                                else { //SI EL ARTICULO NO TIENE SERIES
                                    if(!mod_con_ser) { //SI EL ARTICULO NO EXISTE EN EL CONTEO
                                        insertarConteo(codigoexi,conteoexi,exiAct,e.getDescripcion(),e.getSerie());
                                        Log.i("existencia",""+exiAct+" |"+codigoexi+" |"+conteoexi+" |"+e.getSerie()+" |"+i);
                                        //actualizarGrid();
                                        progreso.dismiss();

                                    }else modificarConteo(idbusqueda,conteobusqueda);
                                }
                            }
                            else
                            {
                                if(bus_cod==2)
                                {
                                    mp.start();
                                    mensajes("busqueda:","codigo2:"+e.getCodigo());
                                    if(e.getSerie().equalsIgnoreCase("S")) { //SI ARTICULO ES CON SERIES
                                        if(mod_con_ser) { //SI EL ARTICULO CON SERIES YA EXISTE EN EL CONTEO
                                            //new cargarSerieAT().execute(codigoexi);
                                            dialogSeries(e.getCodigo(),e.getDescripcion(),idalmacen);
                                            progreso.dismiss();
                                        }
                                        else { //SI EL ARTICULO CON SERIES NO EXISTE EN EL CONTEO
                                            insertarConteo(e.getCodigo(),conteoexi,exiAct,e.getDescripcion(),e.getSerie());
                                            Log.i("existencia",""+exiAct+" |"+e.getCodigo()+" |"+conteoexi+" |"+e.getSerie()+" |"+i);
                                            new cargarSerieAT().execute(codigoexi);
                                            dialogSeries(e.getCodigo(),e.getDescripcion(),idalmacen);
                                            progreso.dismiss();
                                        }
                                    }
                                    else { //SI EL ARTICULO NO TIENE SERIES
                                        if(!mod_con_ser) { //SI EL ARTICULO NO EXISTE EN EL CONTEO
                                            insertarConteo(e.getCodigo(),conteoexi,exiAct,e.getDescripcion(),e.getSerie());
                                            progreso.dismiss();
                                            actualizarGrid();
                                            Log.i("existencia",""+exiAct+" |"+e.getCodigo()+" |"+conteoexi+" |"+e.getSerie()+" |"+i);
                                            //actualizarGrid();

                                        }else modificarConteo(idbusqueda,conteobusqueda);
                                    }
                                }
                            }


                        }
                        if(l.size()<1) {
                            progreso.dismiss();
                            Toast.makeText(getApplicationContext(),"Articulo no encontrado en la base de datos",Toast.LENGTH_SHORT).show();
                            mpError.start();
                        }
                        //Log.i("existencia",""+exiAct)
                    }

                    else {
                        progreso.dismiss();
                        Toast.makeText(getApplicationContext(),"Error al buscar el artículo",Toast.LENGTH_SHORT).show();
                        mpError.start();
                    }
                    progreso.dismiss();
                actualizarGrid();

            }

            @Override
            public void onFailure(Call<RespExiActual> call, Throwable t) {
                progreso.dismiss();
                Log.i("Error:no se encontro ",t.getMessage());
                Toast.makeText(getApplicationContext(),"No se encontró el articulo",Toast.LENGTH_SHORT).show();
            }
        });
        //Integer exi=Integer.parseInt(exiAct);
        //Toast.makeText(getApplicationContext(),"ar:"+exiAct,Toast.LENGTH_SHORT).show();
        //RETORNAR EL VALOR OBTENIDO DE LA LLAMADA AL WEB SERVICE.
        //return exiAct;
    }

    class cargarSerieAT extends AsyncTask<String,Integer,String>
    {
        String validar;
        private ProgressDialog progreso;
        @Override
        protected void onPreExecute()
        {
            eliminarSeries(codigoexi);
            //barra("Descargando Serie");
            progreso = new ProgressDialog(InventarioActivity.this);
            progreso.setMessage("Descargando Serie");
            progreso.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            //progreso.setIndeterminate(true);
            progreso.setProgress(0);
            progreso.setMax(100);
            progreso.setCancelable(false);
            progreso.show();
            super.onPreExecute();

        }
        @Override
        protected String doInBackground(String... params)
        {
            String codigo=params[0];
            try {

                //Log.i("Async",params[0]+" "+params[1]+" "+params[2]+" "+params[3]);
                HttpClient cliente = new DefaultHttpClient();
                /* Definimos la ruta al servidor. */
                String value="Fallo";
                HttpParams httpParameters = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParameters, 900000);
                HttpConnectionParams.setSoTimeout(httpParameters, 900000);
                HttpGet htpoget = new HttpGet(URL+"seriescompletas/"+codigo+"/"+idalmacen);
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

                JSONObject jObject = new JSONObject(finalJSON); //Obtenemos el JSON global
                JSONArray jArray = jObject.getJSONArray("series"); //Obtenemos el array results
                //progreso.setMax(jArray.length());
                /*
                Database admin=new Database(getActivity(),null,1);
                SQLiteDatabase db = admin.getWritableDatabase();
                ContentValues r = new ContentValues();
                */
                if(jArray.length()==0)
                {
                    validar="Articulo Sin Series Disponibles";
                }
                for (int i=0; i < jArray.length(); i++) //Miramos en todos los objetos del array de objetos results
                {
                    publishProgress(i+1);
                    try {
                        JSONObject objeto = jArray.getJSONObject(i); //Obtenemos cada uno de los objetos del array results
                        String alm=objeto.getString("almacen");
                        String serie=objeto.getString("serie");
                        Log.i("cargarseries",""+codigo+" "+alm+" "+serie);
                        insertarSerie(serie,"N");

                    } catch (JSONException e) {
                        Log.e("cargarserieserror",e.getMessage());
                    }
                }
                bfr.close();


            }
            catch (Exception e)
            {
                validar=e.getMessage();
                Log.e("cargarseriesError",""+e.getMessage());
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
                mensajes("","Series agregadas con exito");
                // Toast.makeText(contexto, "Series agregadas con éxito",
                //       Toast.LENGTH_LONG).show();

            }
            else
            {
                mensajes("",""+s.toString());
                //  Toast.makeText(contexto, ""+s,
                //        Toast.LENGTH_LONG).show();
            }
            progreso.dismiss();
            super.onPostExecute(s);
        }
    }


    public void dialogSeries(String codigo, String desc, String id){
        ModalFullScreenSeries dialog = new ModalFullScreenSeries();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Bundle b = new Bundle();
        b.putString("idalmacen", id);
        b.putString("codigo", codigo);
        b.putString("desc", desc);
        dialog.setArguments(b);
        dialog.show(ft, ModalFullScreenSeries.TAG);
    }

    public void dialogoSeries(int i) {
        AlertDialog.Builder builder= new AlertDialog.Builder(this);
       // builder.setTitle("Series")
        //        .setMessage("Ingresar Serie:");
        //personalizado
        builder.setCancelable(false);
        LayoutInflater inflater=this.getLayoutInflater();
        View v=inflater.inflate(R.layout.cuadro_series,null);
        builder.setView(v);
        TextView tvSeries=(TextView)v.findViewById(R.id.tv_series);
        final EditText etSeries=(EditText)v.findViewById(R.id.et_series);
        ListView lv_series=(ListView)v.findViewById(R.id.lv_series);
        etSeries.setFilters(new InputFilter[] {new InputFilter.AllCaps()});
        tvSeries.setText(tvSeries.getText() + "(" + i + ")");
        arraySerie.clear();
        final AlertDialog dialog=builder.create();
        if(tablaVacia("series","serie")) {
            mensajes("Tabla vacia","");
        }
        else {
            consultarSeries();
        }

        etSeries.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(etSeries.getText().toString().equalsIgnoreCase("")) {
                    mensajes("Ingresar Serie","");
                }
                else {
                    if(existeSerie(etSeries.getText().toString())) {
                        //validar si ya existe serie
                        mensajes("Serie ya ha sido capturada.","");
                    }
                    else {
                        compararSerie(etSeries.getText().toString());
                        dialog.dismiss();
                    }
                }
                return false;
            }
        });
        ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,arraySerie);
        lv_series.setAdapter(adapter);
        dialog.show();
    }

    public boolean existeSerie(String serie) {
        boolean existeSerie = false;
        Database admin = new Database(this,null,1);
        SQLiteDatabase db = admin.getWritableDatabase();
        Cursor fila = db.rawQuery("SELECT * FROM series WHERE serie = '"+serie+"' AND codigo = '"+codigoexi+"' AND almacen = '"+ idalmacen +"' ",null);
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

    public void consultarSeries() {
        Database admin = new Database(this,null,1);
        SQLiteDatabase db = admin.getWritableDatabase();
        Cursor fila = db.rawQuery("SELECT * FROM series WHERE codigo = '"+codigoexi+"' AND almacen = '"+ idalmacen +"'",null);
        if(fila.moveToFirst()) {
            do{
                String id = fila.getString(0);
                String serie = fila.getString(1);
                String codigo = fila.getString(2);
                String almacen = fila.getString(3);
                arraySerie.add(serie);
                Log.i("CONSULTA SERIE","serie:"+serie+" codigo:"+codigo+" almacen:"+almacen);
            }while (fila.moveToNext());
        }
        else {
            //Toast.makeText(this, "Almacen sin conteo", Toast.LENGTH_SHORT).show();
        }
        db.close();



    }

    public void compararSerie(final String serie) {
        ServiceExiActual service=retrofit().create(ServiceExiActual.class);
        Call<RespExiActual> call=service.compSerie(serie,codigoexi);
        call.enqueue(new retrofit2.Callback<RespExiActual>() {
            @Override
            public void onResponse(Call<RespExiActual> call, Response<RespExiActual> response) {
                //String x="";
                if(response.isSuccessful()) {
                    //encontrarExiMacro = true;
                    RespExiActual r=response.body();
                    ArrayList<ExistenciaActual> l = r.getExistencia();
                    for (int i=0;i<l.size();i++) {
                        //encontro
                        ExistenciaActual e=l.get(i);
                        Log.i("almacen:","|"+idalmacen);
                        Log.i("existencia"," "+e.getFventa()+"|"+e.getAlmacen()+"|");
                        if(idalmacen.equalsIgnoreCase(e.getAlmacen())) {
                            //si almacen es igual
                            // Log.i("almacen:","mismo almacen");
                            if(e.getFventa().equalsIgnoreCase("")) {
                                //sin fecha de venta//disponible
                                // Log.i("serie:","no vendida");
                                insertarSerie(serie,"D");
                            }
                            else {
                                //vendida
                                //con fecha de venta. serie vendida
                                // Log.i("serie:"," vendida");
                                insertarSerie(serie,"V");
                            }
                        }
                        else {
                            //si almacen es diferente
                            //  Log.i("almacen:","almacen diferente");
                            if(e.getFventa().equalsIgnoreCase("")) {
                                insertarSerie(serie,"AO");
                                //sin fecha de venta//otro almacen
                                //  Log.i("serie:","no vendida");
                            }
                            else {
                                insertarSerie(serie,"VO");
                                //otro almacen vendida
                                //con fecha de venta. serie vendida
                                //  Log.i("serie:","vendida");
                            }
                        }

                    }
                    if(l.size()<1) {
                        //si no encuentra nada
                        //Log.i("serie:","no encontrada");
                        insertarSerie(serie,"NE");
                        //Toast.makeText(getApplicationContext(),"consulta vacia",Toast.LENGTH_SHORT).show();
                    }
                }
                else {
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

    public void insertarSerie(String serie,String estatus) {
        Database admin=new Database(this,null,1);
        SQLiteDatabase db = admin.getWritableDatabase();
        ContentValues registro = new ContentValues();
        //String descrip = getDescripcion(codigo);
        //registro.put("id","NULL");
        registro.put("serie",serie);
        registro.put("codigo",codigoexi);
        registro.put("almacen",idalmacen);
        registro.put("estatus",estatus);
        db.insert("series",null,registro);
        db.close();
        Log.i("INSERTAR_SERIE", ""+idalmacen+" - "+serie+" - "+codigoexi);
    }

    public void consultaInicial() {
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
                Inventario.add(0,new Inventario(false, codigo, descrip, conteo, existencia, serie,comentario));
            }while (fila.moveToNext());
        }
        else {
            //Toast.makeText(this, "Almacen sin conteo", Toast.LENGTH_SHORT).show();
        }
        db.close();
        inv_adapter = new InventarioListaConteoAdapter(this, Inventario);
        lv_conteo.setAdapter(inv_adapter);
        consultaPartidas();
    }
    public void consultaFiltro(String texto)
    {
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
        else {
            //Toast.makeText(this, "Almacen sin conteo", Toast.LENGTH_SHORT).show();
        }
        db.close();
        inv_adapter = new InventarioListaConteoAdapter(this, Inventario);
        lv_conteo.setAdapter(inv_adapter);
    }

    public void insertarConteo(String codigo, float conteo, float existencia, String descrip, String serie) {
        try {
            Database admin = new Database(this, null, 1);
            SQLiteDatabase db = admin.getWritableDatabase();
            ContentValues registro = new ContentValues();
            //String descrip = getDescripcion(codigo);
            //registro.put("id","NULL");
            registro.put("codigo", codigo);
            registro.put("descripcion", descrip);
            registro.put("conteo", conteo);
            registro.put("existencia", existencia);
            registro.put("idalmacen", idalmacen);
            registro.put("serie", serie);
            registro.put("estatus", "S");

            db.insert("conteo", null, registro);
            db.close();
            Log.i("INSERTAR_CONTEO", "" + codigo + " - " + descrip + " - " + conteo + " - " + existencia + " - " + serie+" - "+idalmacen);
            actualizarGrid();
        } catch (Exception e) {
            Log.e("error", e.getMessage());
        }
    }

    //BUSCA SI EL ARTICULO YA EXISTE EN EL CONTEO Y OBTIENE EL CONTEO
    public Boolean busqueda(String codigo) {
        Boolean encontro=false;
        String r;
        Database admin = new Database(this,null,1);
        SQLiteDatabase db = admin.getWritableDatabase();
        Cursor fila = db.rawQuery("SELECT * FROM conteo WHERE codigo = '"+codigo+"' AND idalmacen = '"+ idalmacen +"'",null);
        if(fila.moveToFirst()) {
            do{
                encontro=true;
                conteobusqueda=fila.getFloat(4);
                //Toast.makeText(this, "codigo: "+fila.getString(1)+" conteo: "+fila.getString(1)+" exi: "+fila.getString(2), Toast.LENGTH_SHORT).show();
            }while (fila.moveToNext());
        }
        else {
            encontro=false;
            //Toast.makeText(this, "Articulo sin registro", Toast.LENGTH_SHORT).show();
        }
        db.close();
        return encontro;
    }

    public Boolean tablaVacia(String nomTabla, String columna) {
        Boolean vacio = true;
        Database admin = new Database(this, null,1);
        SQLiteDatabase db = admin.getWritableDatabase();
        try {
            Cursor fila = db.rawQuery("SELECT " + columna + " FROM "+ nomTabla+" where idalmacen='"+idalmacen+"' ",null);
            if(fila.moveToFirst()) {
                vacio=false;
                //tv1.setText(vacio.toString());
            }
            else {
                //Toast.makeText(this, "No existe un artículo con dicho código", Toast.LENGTH_SHORT).show();
            }

        }catch (SQLiteException sql){
            vacio = true;
        }
        db.close();
        return vacio;
    }

    public void eliminarArticulo(){
        int size = Inventario.size();
        for ( int i = 0; i < size; i++) {
            Inventario model = Inventario.get(i);
            if (model.isSelected()){
                String cod = model.getCodigoArticulo();
                try {
                    Database admin = new Database(this,null,1);
                    SQLiteDatabase db = admin.getWritableDatabase();
                    db.execSQL("DELETE FROM conteo WHERE codigo ='"+cod+"' AND idalmacen = '"+ idalmacen +"'");
                    //Cursor fila = db.rawQuery("SELECT * FROM conteo WHERE codigo= '"+cod+"' AND idalmacen = '"+ idalmacen +"'",null);
                    //Log.i("eliminar0:",""+cod+" "+idalmacen);
                    db.close();
                    if (model.getSerie().equalsIgnoreCase("S")){
                        eliminarSeries(cod);
                    }
                } catch (SQLiteException e){
                    Toast.makeText(this,"Error al eliminar articulo" + e, Toast.LENGTH_LONG).show();
                }
                Toast.makeText(this,"Articulo eliminado", Toast.LENGTH_SHORT).show();
            }

        }
        actualizarGrid();

    }

    public void eliminarSeries(String cod){
        try {
            Database admin = new Database(this,null,1);
            SQLiteDatabase db = admin.getWritableDatabase();
            db.execSQL("DELETE FROM series WHERE  codigo ='"+cod+"' AND almacen = '"+ idalmacen +"'");

            db.close();
        } catch (SQLiteException e){
            Toast.makeText(this,"Error al eliminar series" + e, Toast.LENGTH_LONG).show();
        }
        Toast.makeText(this,"Series eliminadas", Toast.LENGTH_SHORT).show();
    }

    public Retrofit retrofit() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit;
        retrofit=new Retrofit.Builder()
                //.baseUrl("http://192.168.1.65:3001/")
                .baseUrl(URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
        return retrofit;
    }

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

    public void mensajes(String titulo, String mensaje) {
        Toast.makeText(getApplicationContext(),titulo +" "+mensaje,Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menu) {

        if (menu.getItemId() == R.id.done) {
            diferencias();
        }
        if (menu.getItemId() == R.id.cancel) {
            onBackPressed();
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

    public void diferencias(){
        if (Inventario.size() > 0){
            AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this);
            dialogo1.setTitle("Finalizar conteo");
            dialogo1.setMessage("\n¿Está seguro que desea continuar?\n");
            dialogo1.setCancelable(false);
            dialogo1.setPositiveButton("CONTINUAR", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogo1, int id) {
                    ValidarConteoSeries();
                    Intent diferencias = new Intent(getApplicationContext(),DiferenciasActivity.class);
                    diferencias.putExtra("almacen",tv_alm.getText());
                    diferencias.putExtra("idalmacen", idalmacen);
                    startActivity(diferencias);
                }
            });
            dialogo1.setNegativeButton("cancelar", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogo1, int id) {
                }
            });
            dialogo1.show();
        } else {
            Toast.makeText(this, "No hay artículos en el conteo", Toast.LENGTH_SHORT).show();
        }

    }

    public void ValidarConteoSeries(){
        //ESTE METODO ACTUALIZA EL CONTEO DE UN ARTÍCULO CON SERIES
        //A SÓLO LAS SERIES QUE SE ENCUENTREN DISPONIBLES
        app.arisoft_app.Modelo.Inventario model;
        Integer conteo = 0;
        for (int i = 0; i < Inventario.size(); i++){
            model = Inventario.get(i);
            if (model.getSerie().equalsIgnoreCase("S")){
                conteo = ContarSeriesDisponibles(model.getCodigoArticulo());
                ActualizarConteoSeries(model.getCodigoArticulo(), conteo);
            }
        }
    }

    public int ContarSeriesDisponibles(String codigo){
        Database admin = new Database(this,null,1);
        int conteo = 0;
        String respuesta = "";
        SQLiteDatabase db = admin.getWritableDatabase();
        Cursor fila = db.rawQuery("SELECT estatus FROM series WHERE codigo='"+ codigo +"' AND almacen = '" + idalmacen+"'",null);
        if(fila.moveToFirst()) {
            do{
                if (fila.getString(0).equalsIgnoreCase("D")){
                    conteo = conteo + 1;
                }
            }while (fila.moveToNext());
        }
        else {
            Toast.makeText(this, "Ninguna serie", Toast.LENGTH_SHORT).show();
        }
        db.close();
        return conteo;
    }

    public void ActualizarConteoSeries(String codigo, int conteo){
        Database admin = new Database(this,null,1);
        SQLiteDatabase db = admin.getWritableDatabase();
        try {
            ContentValues registro = new ContentValues();
            registro.put("conteo", conteo);
            db.update("conteo",registro,"codigo = '"+ codigo +"' AND idalmacen = '"+ idalmacen+"'",null);
        }catch (SQLiteException e){
            Toast.makeText(this, "Error. No se pudo contar diferencias de series", Toast.LENGTH_SHORT).show();
        }
        db.close();
    }


    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("¿Está seguro que desea salir?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Salir", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        InventarioActivity.super.onBackPressed();
                    }
                }).create().show();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        actualizarGrid();
    }
}
