package app.arisoft_app;

import android.app.DatePickerDialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
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
import java.util.Calendar;
import java.util.Date;

import app.arisoft_app.Tools.Database;

public class movimientosInventario extends AppCompatActivity implements DialogInterface.OnDismissListener {
    Button btn_mov;
    ImageButton btn_buscarcod;
    EditText et_mov,et_codigo;
    TextView tv_mov,tv_fecha;
    private String URL;
    String codigo,descripcion,contenido="";
    ProgressDialog progreso;
    private DatePickerDialog.OnDateSetListener datesl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movimientos_inventario);
        et_mov=(EditText)findViewById(R.id.et_mov);
        et_codigo=(EditText)findViewById(R.id.et_codigo);
        tv_mov=(TextView) findViewById(R.id.tv_mov);
        tv_fecha=(TextView) findViewById(R.id.tv_fecha);
        btn_buscarcod=(ImageButton)findViewById(R.id.btn_buscarcod);
        getDomain();
        fechaActual();

        et_mov.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String ruta=espaciosBlanco(s.toString().trim());
                //Toast.makeText(getApplicationContext(),""+ruta,Toast.LENGTH_SHORT).show();
                Log.i("busqueda2","|"+ruta+"|");
                new cargarArticulo().execute(ruta);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btn_buscarcod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"click",Toast.LENGTH_SHORT).show();


            }
        });

        datesl=new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month=month+1;
                String fecha="";
                //CharSequence s = DateFormat.format("dd-MM-yyyy",fecha);
                if(month<10)
                {
                    fecha=""+dayOfMonth+"-0"+month+"-"+year;
                }
                else
                {
                    fecha=""+dayOfMonth+"-"+month+"-"+year;
                }
                tv_fecha.setText(fecha);

            }
        };
    }
    public void buscar(View view)
    {
        contenido="";
        String ruta=espaciosBlanco(et_mov.getText().toString().trim());
        //Toast.makeText(getApplicationContext(),""+ruta,Toast.LENGTH_SHORT).show();
        new cargarArticulo().execute(ruta);
    }

    public void agregar(View view)
    {

    }
    public void comentario(View view)
    {

    }
    public void guardar(View view)
    {

    }
    public void fecha(View view)
    {
        Calendar cal= Calendar.getInstance();
        int año=cal.get(Calendar.YEAR);
        int mes=cal.get(Calendar.MONTH);
        int dia=cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog= new DatePickerDialog(
                this,
                android.R.style.Theme_Holo_Dialog_MinWidth,
                datesl,
                año,mes,dia
        );
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

    }
    public void fechaActual()
    {
        Date d = new Date();
        CharSequence s = DateFormat.format("dd-MM-yyyy", d.getTime());
        tv_fecha.setText(s);
    }
    public String espaciosBlanco(String cadena)
    {

        String x="";
            if(cadena.indexOf(" ")>-1)
            {
                //si encontro
                //Toast.makeText(getApplicationContext(),"si encontro espacios",Toast.LENGTH_SHORT).show();
                x=cadena.replaceAll(" ","%20");
            }
            else
            {
                x=cadena;
            }
        Log.i("busqueda","|"+x+"|");
        //Toast.makeText(getApplicationContext(),""+x,Toast.LENGTH_SHORT).show();

        return x;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {

    }

    //PETICION FACTURAS ASYNC TASK
    class cargarArticulo extends AsyncTask<String,Integer,String>
    {

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
            //progreso.show();
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
                JSONArray jArray = jObject.getJSONArray("busqueda"); //Obtenemos el array results
                progreso.setMax(jArray.length());
                //Log.i("busqueda",""+jArray.length());
                if(jArray.length()==0)
                {
                    validar="no encontrado";
                }
                for (int i=0; i < jArray.length(); i++) //Miramos en todos los objetos del array de objetos results
                {
                    publishProgress(i+1);
                    try {
                        JSONObject objeto = jArray.getJSONObject(i); //Obtenemos cada uno de los objetos del array results
                            codigo=objeto.getString("codigo");
                            descripcion=objeto.getString("descripcion");
                            contenido=contenido+codigo+" - "+descripcion+"\n";
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
                //Toast.makeText(getApplicationContext(), "éxito",
                  //      Toast.LENGTH_LONG).show();
                tv_mov.setText(contenido);
                contenido="";

            }
            else
            {
                //Toast.makeText(getApplicationContext(), ""+s,
                  //      Toast.LENGTH_LONG).show();
                contenido="";
                tv_mov.setText(contenido);
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
