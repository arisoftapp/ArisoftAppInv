package app.arisoft_app;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.content.Intent;

import java.util.Vector;

import app.arisoft_app.Tools.Database;

public class ArticulosExistenciaGeneralActivity extends AppCompatActivity {
    ListView lista_existencia;
    Vector<String> codigo = new Vector<String>();
    Vector<String> datos = new Vector<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_articulos_existencia);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        lista_existencia = (ListView) findViewById(R.id.lv_existencia);

        consultaArticulos();
        lista_existencia.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(),ArticuloExistenciaGnrlDetallesActivity.class);
                intent.putExtra("codigo",codigo.get(i).toString());
                Log.i("codigo:","" + codigo.get(i).toString());
                startActivity(intent);
            }
        });
    }

    public void consultaArticulos()
    {
        Database admin=new Database(this,null,1);
        String respuesta="";
        SQLiteDatabase db = admin.getWritableDatabase();
        Cursor fila=db.rawQuery("select * from articulos",null);
        if(fila.moveToFirst())
        {
            do{
                codigo.add(fila.getString(0));
                datos.add(fila.getString(0)+" - "+fila.getString(1));
            }while (fila.moveToNext());

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, datos);
            lista_existencia.setAdapter(adapter);
            Log.i("art",respuesta);
        }
        else
        {
            Toast.makeText(this, "No existe un artículo", Toast.LENGTH_SHORT).show();
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
