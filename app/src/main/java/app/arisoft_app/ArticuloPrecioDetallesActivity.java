package app.arisoft_app;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.NumberFormat;
import java.util.Locale;

import app.arisoft_app.Modelo.Articulo;
import app.arisoft_app.Tools.Database;

public class ArticuloPrecioDetallesActivity extends AppCompatActivity {
    private TextView tvcodigo;
    private Articulo articulo;
    LinearLayout page;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_articulo_precio_detalles);

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        page = ((LinearLayout)findViewById(R.id.vertical_l_precio));
        String obtenido = "";
        try{
            Bundle bundle = getIntent().getExtras();
            articulo = bundle.getParcelable("codigo");
            obtenido = bundle.getString("codigo");
            if(bundle!=null){
                ((TextView) page.findViewById(R.id.tv_codigo)).setText(obtenido);
                getArticulo(obtenido);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void getArticulo (String obtenido) {
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
                double p1 = Double.parseDouble(fila.getString(2));
                String precio1 = format.format(p1);
                ((TextView) page.findViewById(R.id.tv_p1)).setText(""+ precio1);

                double p2 = Double.parseDouble(fila.getString(3));
                String precio2 = format.format(p2);
                ((TextView) page.findViewById(R.id.tv_p1)).setText(""+ precio2);

                double p3 = Double.parseDouble(fila.getString(4));
                String precio3 = format.format(p3);
                ((TextView) page.findViewById(R.id.tv_p1)).setText(""+ precio3);

                ((TextView) page.findViewById(R.id.tv_clas)).setText(fila.getString(5));
                ((TextView) page.findViewById(R.id.tv_ultimaMod)).setText(fila.getString(11));
            }catch (NumberFormatException e){

            }
        }
        else
        {
            Toast.makeText(this, "Error: Database", Toast.LENGTH_SHORT).show();
        }
        db.close();
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
