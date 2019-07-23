package app.arisoft_app;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import app.arisoft_app.Tools.Database;

public class movimientosInventario extends AppCompatActivity {
    Button btn_mov;
    EditText et_mov;
    TextView tv_mov;
    private String URL;
    String codigo,descripcion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movimientos_inventario);
        et_mov=(EditText)findViewById(R.id.et_mov);
        tv_mov=(TextView) findViewById(R.id.tv_mov);
        getDomain();
    }
    public void buscar(View view)
    {
        Toast.makeText(getApplicationContext(),"click",Toast.LENGTH_SHORT).show();
    }

    //PETICION FACTURAS ASYNC TASK
    class cargarFacturaAT extends AsyncTask<String,Integer,String>
    {
        private ProgressDialog progreso;
        String validar;
        @Override
        protected void onPreExecute()
        {
            progreso = new ProgressDialog(getApplicationContext());
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
                HttpGet htpoget = new HttpGet(URL+"busquedaDesc/"+params[0]);
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
                            codigo=objeto.getString("vendedor");
                            descripcion=objeto.getString("fecha");
                            validar="Factura Surtida";
                    } catch (JSONException e) {
                        Log.e("error",e.getMessage());
                    }
                }
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
                Toast.makeText(getApplicationContext(), "Factura agregada con éxito",
                        Toast.LENGTH_LONG).show();

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


}
