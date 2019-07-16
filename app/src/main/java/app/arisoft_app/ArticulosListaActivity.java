package app.arisoft_app;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Vector;

import app.arisoft_app.Tools.Database;

public class ArticulosListaActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private TextView tv1;
    private ListView lv1;
    Vector<String> codigo = new Vector<String>();
    Vector<String> datos = new Vector<String>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_articulos_lista);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        tv1=(TextView)findViewById(R.id.tv1);
        lv1 =(ListView)findViewById(R.id.lv1);

        consultaArticulos();
        lv1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView adapterView, View view, int i, long l) {
                //tv1.setText("Población de "+ lv1.getItemAtPosition(i) + " es "+ habitantes[i]);
                //Toast.makeText(getApplicationContext(), ""+lv1.getItemAtPosition(i)+"|"+codigo.get(i).toString(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(),ArticuloListaDetallesActivity.class);
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
             /*   respuesta+=fila.getString(0);
                respuesta+="|";*/
             codigo.add(fila.getString(0));
             //descripcion.add(fila.getString(1));
                datos.add(fila.getString(0)+" - "+fila.getString(1));
            }while (fila.moveToNext());
            //tv1.setText(respuesta);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, datos);
            lv1.setAdapter(adapter);
            Log.i("art",respuesta);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_listaArt) {

        } else if (id == R.id.nav_existencias) {

        } else if (id == R.id.nav_listaPrec) {

        } else if (id == R.id.nav_entrada) {

        } else if (id == R.id.nav_salida) {

        }else if (id == R.id.nav_entrega) {

        } else if (id == R.id.nav_recibido) {

        } else if (id == R.id.nav_invFisico) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    /*public void onBackPressed() {
        //Log.d("MainActivity","onBackPressed()");
        //Toast.makeText(this, "preciono back", Toast.LENGTH_SHORT).show();
        Intent MainActivity = new Intent(getApplicationContext(),MainActivity.class);
        startActivity(MainActivity);
        finish();
    }*/

}
