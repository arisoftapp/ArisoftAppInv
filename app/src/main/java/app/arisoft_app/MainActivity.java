package app.arisoft_app;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import android.app.*;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import app.arisoft_app.Tools.AuthResponse;
import app.arisoft_app.Tools.Database;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, FragmentAlmacenModal.BottomSheetListener , FragmentAlmacenConteoModalCorreccion.BottomSheetListener {
    private String URL;
    private int almsize;
    private String client_URL;
    private ProgressDialog progreso;
    Context contexto=this;
    LinearLayout ll_actalm,ll_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ll_actalm=(LinearLayout)findViewById(R.id.ll_actalm);
        ll_btn=(LinearLayout)findViewById(R.id.ll_btn);


        getDomain();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        View header = ((NavigationView)findViewById(R.id.nav_view)).getHeaderView(0);
        boolean reload = false;
        AuthResponse User = getUser();
        ((TextView) header.findViewById(R.id.txt_name)).setText(User.getUsername());
        ((TextView) header.findViewById(R.id.txt_empresa)).setText(User.getEmpresa());
        if (reload) {
            //new cargar().execute();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        cargarDatos();

        //new Tarea2().execute();

    }

    public AuthResponse getUser(){
        AuthResponse user = new AuthResponse();
        Database admin = new Database(this, null,1);
        SQLiteDatabase db = admin.getWritableDatabase();
        try {
            Cursor fila = db.rawQuery("SELECT empresa, username FROM authresponse",null);
            if(fila.moveToFirst())
            {
                user.setUsername(fila.getString(1));
                user.setEmpresa(fila.getString(0));
                //tv1.setText(vacio.toString());
            }
            else
            {
                //Toast.makeText(this, "No existe un artículo con dicho código", Toast.LENGTH_SHORT).show();
            }

        }catch (SQLiteException sql){
        }

        db.close();
        return user;
    }
    public void cargaAlmacenes(View view)
    {
        new ConsultaAlmacen().execute("almacenes");
    }
    public void inventarioFisico(View view)
    {
        FragmentAlmacenConteoModalCorreccion bottomSheet = new FragmentAlmacenConteoModalCorreccion();
        bottomSheet.show(getSupportFragmentManager(), "AlmacenBottomSheet");
    }
    public void existencia(View view)
    {
        Intent seleccionarExistecia = new Intent(getApplicationContext(),ArticuloExistenciaAlmDetallesActivity.class);
        startActivity(seleccionarExistecia);
    }
    public void controlSurtido(View view)
    {
        FragmentAlmacenRecibidoModal bottomSheet = new FragmentAlmacenRecibidoModal();
        bottomSheet.show(getSupportFragmentManager(), "AlmacenBottomSheet");
    }


    public void cargarDatos()
    {

        if(tablaVacia("almacenes","idalmacen"))
        {
                new ConsultaAlmacen().execute("almacenes");
        }
        else {
            eliminarTabla("almacenes");
            new ConsultaAlmacen().execute("almacenes");
        }
    }

    @Override
    public void onButtonClicked(String text) {

    }

    public void eliminarTabla(String tabla)
    {
        Database admin=new Database(this,null,1);
        SQLiteDatabase db = admin.getWritableDatabase();
        //db.execSQL("DROP TABLE IF EXISTS "+tabla);
        db.execSQL("DELETE FROM " + tabla);
        db.close();
    }

    class ConsultaAlmacen extends AsyncTask<String,Integer,String>
    {
        private ProgressDialog progreso;
        String validar;
        @Override
        protected void onPreExecute()
        {
            progreso = new ProgressDialog(contexto);
            progreso.setProgressStyle(ProgressDialog.
                    STYLE_HORIZONTAL);
            progreso.setMessage("Consultando Almacenes...");
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
                //HttpParams httpParameters = new BasicHttpParams();
                //HttpConnectionParams.setConnectionTimeout(httpParameters, 900000);
                //HttpConnectionParams.setSoTimeout(httpParameters, 900000);
                HttpGet htpoget = new HttpGet(URL+params[0]);
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
                JSONArray jArray = jObject.getJSONArray("almacenes"); //Obtenemos el array results
                progreso.setMax(jArray.length());
                Database admin=new Database(contexto,null,1);
                SQLiteDatabase db = admin.getWritableDatabase();
                ContentValues r = new ContentValues();


                for (int i=0; i < jArray.length(); i++) //Miramos en todos los objetos del array de objetos results
                {
                    publishProgress(i+1);
                    try {
                        JSONObject objeto = jArray.getJSONObject(i); //Obtenemos cada uno de los objetos del array results

                        r.put("idalmacen",objeto.getString("idalmacen"));
                        r.put("almacen",objeto.getString("almacen"));
                        db.insert("almacenes",null,r);
                        Log.i("Async",objeto.getString("idalmacen")+" "+objeto.getString("almacen"));
                    } catch (JSONException e) {
                        Log.e("error",e.getMessage());
                    }
                }
                db.close();
                bfr.close();
                validar="OK";

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
                Toast.makeText(getApplicationContext(), "Almacenes agregados con éxito",
                        Toast.LENGTH_LONG).show();
                ll_btn.setVisibility(View.VISIBLE);
                ll_actalm.setVisibility(View.GONE);
            }
            else
            {
                Toast.makeText(getApplicationContext(), "Error al cargar almacenes:"+s,
                        Toast.LENGTH_LONG).show();
                ll_btn.setVisibility(View.GONE);
                ll_actalm.setVisibility(View.VISIBLE);
            }
            progreso.dismiss();
            super.onPostExecute(s);

        }
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
    public Boolean tablaVacia(String nomTabla, String columna)
    {
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
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }else if(id == R.id.nav_listaArt){

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_listaArt) {
            Intent articulosLista = new Intent(getApplicationContext(),ArticulosListaActivity.class);
            startActivity(articulosLista);
        } else if (id == R.id.nav_existencias) {
            Intent seleccionarExistecia = new Intent(getApplicationContext(),ArticuloExistenciaAlmDetallesActivity.class);
            startActivity(seleccionarExistecia);
        } else if (id == R.id.nav_listaPrec) {
            Intent articulosListaPrecios = new Intent(getApplicationContext(),ArticulosListaPreciosActivity.class);
            startActivity(articulosListaPrecios);
        } else if (id == R.id.nav_entrada) {
            Intent entradas = new Intent(getApplicationContext(),entradas.class);
            startActivity(entradas);
        } else if (id == R.id.nav_salida) {

        }else if (id == R.id.nav_entrega) {

        } else if (id == R.id.nav_recibido) {
            //String xd="x";
            //Toast.makeText(this, "control de recibido", Toast.LENGTH_SHORT).show();
            FragmentAlmacenRecibidoModal bottomSheet = new FragmentAlmacenRecibidoModal();
            bottomSheet.show(getSupportFragmentManager(), "AlmacenBottomSheet");

        } else if (id == R.id.nav_invFisico) {
            FragmentAlmacenConteoModal bottomSheet = new FragmentAlmacenConteoModal();
            bottomSheet.show(getSupportFragmentManager(), "AlmacenBottomSheet");
        } else if (id == R.id.nav_logout){
            new AlertDialog.Builder(this)
                    .setMessage("¿Está seguro que desea cerrar sesión?")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Cerrar Sesión", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            Logout();
                        }
                    }).create().show();
        }else if (id==R.id.config){

            Intent config = new Intent(getApplicationContext(),configuracion.class);
            startActivity(config);
            //Toast.makeText(this,"configuracion",Toast.LENGTH_SHORT).show();
        }
        else if(id==R.id.nav_movimientos)
        {
            Intent mov = new Intent(getApplicationContext(),movimientosInventario.class);
            startActivity(mov);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void Logout(){
        this.deleteDatabase("arisoft_app");
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public void onBackPressed() {
        //Log.d("MainActivity","onBackPressed()");
        //Toast.makeText(this, "preciono back", Toast.LENGTH_SHORT).show();
        finish();
    }



}
