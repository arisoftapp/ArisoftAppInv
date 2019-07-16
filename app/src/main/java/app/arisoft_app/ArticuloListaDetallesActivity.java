package app.arisoft_app;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.text.NumberFormat;
import java.util.Locale;
import app.arisoft_app.Modelo.Articulo;
import app.arisoft_app.Tools.Database;

public class ArticuloListaDetallesActivity extends AppCompatActivity {
    private TextView tvcodigo;
    private Articulo articulo;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_art_lista_detalle);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        LinearLayout page = ((LinearLayout)findViewById(R.id.vertical_l));
        String obtenido = "";
        try{
            Bundle bundle = getIntent().getExtras();
            articulo = bundle.getParcelable("codigo");
            obtenido = bundle.getString("codigo");
            if(bundle!=null){
                ((TextView) page.findViewById(R.id.tv_codigo)).setText(obtenido);

            }
        }catch (Exception e){
            e.printStackTrace();
        }


        Database admin = new Database(this,null,1);
        String respuesta="";
        SQLiteDatabase db = admin.getWritableDatabase();
        Cursor fila = db.rawQuery("select * from articulos WHERE CodigoArticulo = ?", new String[] {"" + obtenido});
        if(fila.moveToFirst())
        {
            try{
                ((TextView) page.findViewById(R.id.tv_codigo)).setText(fila.getString(0));
                ((TextView) page.findViewById(R.id.tv_desc)).setText(fila.getString(1));
                NumberFormat format = NumberFormat.getCurrencyInstance(Locale.CANADA);
                double num = Double.parseDouble(fila.getString(2));
                String precio = format.format(num);
                ((TextView) page.findViewById(R.id.tv_precio)).setText(""+ precio);
                ((TextView) page.findViewById(R.id.tv_clas)).setText(fila.getString(5));
                ((TextView) page.findViewById(R.id.tv_venta)).setText(fila.getString(7));
                ((TextView) page.findViewById(R.id.tv_ultimaMod)).setText(fila.getString(11));
                ((TextView) page.findViewById(R.id.tv_exigral)).setText(consultaExiGral(obtenido));
            }catch (NumberFormatException e){

            }
        }
        else
        {
            Toast.makeText(this, "Error: Database", Toast.LENGTH_SHORT).show();
        }
        db.close();

        //tv1=(TextView)findViewById(R.id.tv_codigo);
        //tv1.setText(codigo);

    }
    public String consultaExiGral(String codigo)
    {
        int exi=0;
        Database admin = new Database(this,null,1);
        String respuesta="";
        SQLiteDatabase db = admin.getWritableDatabase();
        Cursor fila = db.rawQuery("select existenciaActual from existencia WHERE codProd='"+codigo+"'",null);
        if(fila.moveToFirst())
        {
            do{
                respuesta = fila.getString(0);
                exi+=Integer.parseInt(respuesta);

            }while (fila.moveToNext());
            respuesta=String.valueOf(exi);
        }
        db.close();
        return respuesta;
        //Toast.makeText(getApplicationContext(),"xd"+respuesta,Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

    /*public void onBackPressed() {
        //Log.d("MainActivity","onBackPressed()");
        //Toast.makeText(this, "preciono back", Toast.LENGTH_SHORT).show();
        Intent i = new Intent(getApplicationContext(),ArticulosListaActivity.class);
        startActivity(i);
        finish();
    }*/
}
