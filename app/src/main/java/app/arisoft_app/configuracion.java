package app.arisoft_app;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import app.arisoft_app.Tools.Database;

public class configuracion extends AppCompatActivity {
    Switch sw_busalm,sw_tipoalm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion);

        sw_busalm=(Switch)findViewById(R.id.sw_busalm);
        sw_tipoalm=(Switch)findViewById(R.id.sw_tipoalm);
        cargarConfig();

        sw_busalm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Database admin=new Database(getApplicationContext(),null,1);
                SQLiteDatabase db = admin.getWritableDatabase();
                ContentValues r = new ContentValues();
                if(isChecked==true)
                {
                    //Toast.makeText(getApplicationContext(),"on",Toast.LENGTH_SHORT).show();
                    try{
                        r.put("bus_alm_inv_fis","true");
                        db.update("configuracion",r,"id=1",null);
                        db.close();
                    }catch (Exception e)
                    {
                        Toast.makeText(getApplicationContext(),"error al insertar configuraciones:"+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    //Toast.makeText(getApplicationContext(),"of",Toast.LENGTH_SHORT).show();
                    try{

                        r.put("bus_alm_inv_fis","false");
                        db.update("configuracion",r,"id=1",null);
                        db.close();
                    }catch (Exception e)
                    {
                        Toast.makeText(getApplicationContext(),"error al insertar configuraciones:"+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        sw_tipoalm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Database admin=new Database(getApplicationContext(),null,1);
                SQLiteDatabase db = admin.getWritableDatabase();
                ContentValues r = new ContentValues();
                if(isChecked==true)
                {
                    //Toast.makeText(getApplicationContext(),"on",Toast.LENGTH_SHORT).show();
                    try{
                        r.put("dialog_tipo_inv","true");
                        db.update("configuracion",r,"id=1",null);
                        db.close();
                    }catch (Exception e)
                    {
                        Toast.makeText(getApplicationContext(),"error al insertar configuraciones:"+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    //Toast.makeText(getApplicationContext(),"of",Toast.LENGTH_SHORT).show();
                    try{

                        r.put("dialog_tipo_inv","false");
                        db.update("configuracion",r,"id=1",null);
                        db.close();
                    }catch (Exception e)
                    {
                        Toast.makeText(getApplicationContext(),"error al insertar configuraciones:"+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
    public void cargarConfig()
    {
        Log.i("config","cargar");


        try {
            Database admin = new Database(this, null,1);
            SQLiteDatabase db = admin.getWritableDatabase();
            Cursor fila = db.rawQuery("SELECT * FROM  configuracion where id=1",null);
            if(fila.moveToFirst())
            {
                Log.i("config",""+fila.getString(0)+" "+fila.getString(1)+" "+fila.getString(2));
                if(fila.getString(1).equalsIgnoreCase("false"))
                {
                    //si es falso - switch off
                    sw_busalm.setChecked(false);
                }
                else
                {
                    //switch on
                    sw_busalm.setChecked(true);
                }
                if(fila.getString(2).equalsIgnoreCase("false"))
                {
                    //si es falso - switch off
                    sw_tipoalm.setChecked(false);
                }
                else
                {
                    //switch on
                    sw_tipoalm.setChecked(true);
                }
            }
            else
            {
                Log.i("config","no encontro nada");
            }
            db.close();
        }catch (SQLiteException sql){
            Toast.makeText(this,"Error en consulta configuracion:"+sql.getMessage(),Toast.LENGTH_SHORT).show();
        }



    }
}
