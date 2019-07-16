package app.arisoft_app;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
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

import app.arisoft_app.Modelo.Existencia;
import app.arisoft_app.Tools.Database;

public class ArticuloExistenciaAlmDetallesActivity extends AppCompatActivity {
    private exialmAdapter existenciaAdapter;
    ArrayList<ExiAlmancen> Existencia_list;
    String datos[];
    ListView lv_exi;
    EditText et_codigo;
    TextView tv_codigo,tv_desc,tv_uventa,tv_precio;
    private String URL,precio,uventa,descripcion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_art_existencia_almacen_detalle);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setElevation(0);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        lv_exi=(ListView)findViewById(R.id.lista_existencia);
        et_codigo=(EditText)findViewById(R.id.et_cod);
        tv_codigo=(TextView)findViewById(R.id.tv_codigo);
        tv_desc=(TextView)findViewById(R.id.tv_desc);
        tv_uventa=(TextView)findViewById(R.id.tv_uventa);
        tv_precio=(TextView)findViewById(R.id.tv_precio);
        /*
        Existencia_list = new ArrayList<ExiAlmancen>();
        Existencia_list.add(new ExiAlmancen("01-almacen general","15"));
        existenciaAdapter = new exialmAdapter( Existencia_list,this);
        lv_exi.setAdapter(existenciaAdapter);
*/

        getDomain();
        InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(et_codigo.getWindowToken(), 0);

        et_codigo.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String codigo=et_codigo.getText().toString().trim();
                new cargarArticuloWS().execute(codigo);
                return false;
            }
        });

    }

    class cargarArticuloWS extends AsyncTask<String,Integer,String>
    {
        String validar;
        private ProgressDialog progreso;

        @Override
        protected void onPreExecute()
        {

            //barra("Descargando Serie");
            progreso = new ProgressDialog(ArticuloExistenciaAlmDetallesActivity.this);
            progreso.setMessage("Descargando Articulo");
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
            Existencia_list = new ArrayList<ExiAlmancen>();
            try {

                //Log.i("Async",params[0]+" "+params[1]+" "+params[2]+" "+params[3]);
                HttpClient cliente = new DefaultHttpClient();
                /* Definimos la ruta al servidor. */
                String value="Fallo";
                HttpParams httpParameters = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParameters, 900000);
                HttpConnectionParams.setSoTimeout(httpParameters, 900000);
                HttpGet htpoget = new HttpGet(URL+"ExistenciaXAlm/"+codigo);
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
                JSONArray jArray = jObject.getJSONArray("Existencia"); //Obtenemos el array results
                if(jArray.length()==0)
                {
                    validar="Articulo Sin Existencia";
                }
                for (int i=0; i < jArray.length(); i++) //Miramos en todos los objetos del array de objetos results
                {
                    publishProgress(i+1);
                    try {
                        JSONObject objeto = jArray.getJSONObject(i); //Obtenemos cada uno de los objetos del array results
                        descripcion=objeto.getString("descripcion");
                        String idalm=objeto.getString("idalmacen");
                        uventa=objeto.getString("uventa");
                        precio=objeto.getString("precio");
                        String almacen=objeto.getString("almacen");
                        String existenciaActual=objeto.getString("existenciaActual");

                        //tv_codigo.setText(codigo);
                        Existencia_list.add(new ExiAlmancen(almacen,existenciaActual,idalm));
                        Log.i("cargararticulo",""+codigo+" "+descripcion+" "+idalm+""+almacen+" "+uventa+" "+precio+" "+existenciaActual);

                    } catch (JSONException e) {
                        Log.e("cargararticulo",e.getMessage());
                    }
                }
                bfr.close();


            }
            catch (Exception e)
            {
                validar=e.getMessage();
                Log.e("cargararticulo",""+e.getMessage());
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
                mensajes("","Exito");
                tv_desc.setText(descripcion);
                tv_codigo.setText(et_codigo.getText().toString());
                tv_precio.setText(precio);
                tv_uventa.setText(uventa);
                existenciaAdapter = new exialmAdapter( Existencia_list,getApplicationContext());
                lv_exi.setAdapter(existenciaAdapter);
                et_codigo.setText("");
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
    public void mensajes(String titulo, String mensaje) {
        Toast.makeText(getApplicationContext(),titulo +" "+mensaje,Toast.LENGTH_SHORT).show();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }
}
