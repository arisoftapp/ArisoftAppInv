package app.arisoft_app;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Vector;

import app.arisoft_app.Modelo.Articulo;
import app.arisoft_app.Tools.Database;

public class ArticulosListaPreciosActivity extends AppCompatActivity {
    private ArrayList<Articulo> Articulos;
    private ListView lvArt;
    private ArticulosListaPreciosAdapter adapter;
    Vector<String> codigo = new Vector<String>();
    Vector<String> datos = new Vector<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_articulos_lista_precios);
        lvArt = (ListView) findViewById(R.id.lista_precios);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        getArticulos();

        lvArt.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView adapterView, View view, int i, long l) {
                //tv1.setText("Población de "+ lv1.getItemAtPosition(i) + " es "+ habitantes[i]);
                //Toast.makeText(getApplicationContext(), ""+lv1.getItemAtPosition(i)+"|"+codigo.get(i).toString(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(),ArticuloPrecioDetallesActivity.class);
                intent.putExtra("codigo",codigo.get(i).toString());
                Log.i("codigo:","" + codigo.get(i).toString());
                startActivity(intent);
            }
        });
    }

    public void getArticulos(){
        Articulos = new ArrayList<Articulo>();
        Database admin=new Database(this,null,1);
        String respuesta="";
        SQLiteDatabase db = admin.getWritableDatabase();
        Cursor fila=db.rawQuery("SELECT * FROM articulos",null);
        if(fila.moveToFirst())
        {
            do{
                codigo.add(fila.getString(0));
                String codigo = fila.getString(0);
                String descripcion = fila.getString(1);
                String p1 = fila.getString(2);
                String p2 = fila.getString(3);
                String p3 = fila.getString(4);
                Articulos.add(new Articulo(codigo, descripcion, p1, p2, p3));
            }while (fila.moveToNext());
            adapter = new ArticulosListaPreciosAdapter(this, Articulos);
            lvArt.setAdapter(adapter);
        }
        else
        {
            Toast.makeText(this, "No existen artículos", Toast.LENGTH_SHORT).show();
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
