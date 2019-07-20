package app.arisoft_app;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Vector;

import app.arisoft_app.Modelo.Existencia;
import app.arisoft_app.Tools.Database;

public class FragmentAlmacenConteoModal extends BottomSheetDialogFragment {
    private FragmentAlmacenModal.BottomSheetListener mListener;
    Existencia articulo;
    String obtenido;
    Spinner spinner;
    CheckBox cb_text;
    EditText et_alm;
    AutoCompleteTextView actv_alm;
    ArrayAdapter<String> comboAdapterSql,adapterac;
    Vector<String> codigo = new Vector<String>();
    Vector<String> datos = new Vector<String>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottom_sheet_almacen_conteo, container, false);
        spinner = v.findViewById(R.id.spinner_alm2);
        spinner.setAdapter(getAlmacenes(false));
        spinner.requestFocus();
        cb_text=v.findViewById(R.id.cb_text);
        et_alm=v.findViewById(R.id.et_alm);
        //actv_alm=v.findViewById(R.id.actv_alm);
        //actv_alm.setAdapter(getAlmacenes(true));
        Button btn_consultar = v.findViewById(R.id.btn_consultar);
        if(revisarConfig()==true)
        {
            et_alm.setVisibility(View.VISIBLE);
        }
        else
        {
            et_alm.setVisibility(View.GONE);
        }

        btn_consultar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int size = spinner.getAdapter().getCount();
                if (size>0){
                    Intent intent = new Intent(getContext(),InventarioActivity.class);
                    int pos = spinner.getSelectedItemPosition();
                    Log.i("alm",""+codigo.get(pos).toString()+" "+datos.get(pos).toString());
                    intent.putExtra("idalmacen", codigo.get(pos).toString());
                    intent.putExtra("almacen", datos.get(pos).toString());
                    startActivity(intent);
                    dismiss();
                }else Toast.makeText(getContext(), "No se seleccionó almacén", Toast.LENGTH_SHORT).show();



            }
        });
        cb_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cb_text.isChecked())
                {
                    et_alm.setVisibility(View.VISIBLE);
                }
                else
                {
                    et_alm.setText("");
                    et_alm.setVisibility(View.GONE);
                }
            }
        });
        et_alm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


                if(s.toString().equalsIgnoreCase(""))
                {
                    comboAdapterSql.clear();
                    codigo.clear();
                    datos.clear();
                    spinner.setAdapter(getAlmacenes(false));
                }
                else
                {
                    spinner.setAdapter(getAlmacen(s.toString().trim()));
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        et_alm.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                int size = spinner.getAdapter().getCount();
                if (size>0){
                    Intent intent = new Intent(getContext(),InventarioActivity.class);
                    int pos = spinner.getSelectedItemPosition();
                    intent.putExtra("idalmacen", codigo.get(pos).toString());
                    intent.putExtra("almacen", datos.get(pos).toString());
                    Log.i("alm",""+codigo.get(pos).toString()+" "+datos.get(pos).toString()+" "+size);
                    startActivity(intent);
                    dismiss();
                }else Toast.makeText(getContext(), "No se seleccionó almacén", Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        return v;

    }



    public void onStart(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    public ArrayAdapter<String> getAlmacen (String texto){
        comboAdapterSql.clear();
        Database admin = new Database(getContext(),null,1);
        String respuesta="";
        SQLiteDatabase db = admin.getWritableDatabase();
        Cursor fila = db.rawQuery("SELECT * FROM almacenes where idalmacen='"+texto+"' ",null);
        if(fila.moveToFirst())
        {
            do{
                codigo.clear();
                datos.clear();
                codigo.add(fila.getString(0));
                datos.add(fila.getString(0)+" - "+fila.getString(1));
                Log.i("alm2",""+fila.getString(0)+" "+fila.getString(1));
            }while (fila.moveToNext());

            comboAdapterSql = new ArrayAdapter<>(getContext(), R.layout.textspinner_alm, datos);
            spinner.setAdapter(comboAdapterSql);
        }
        else
        {
            Toast.makeText(getContext(), "No existe almacen", Toast.LENGTH_SHORT).show();
            comboAdapterSql.clear();
            codigo.clear();
            datos.clear();
            spinner.setAdapter(getAlmacenes(false));
        }
        db.close();
        return comboAdapterSql;
    }

    public ArrayAdapter<String> getAlmacenes (boolean soloid){

        Database admin = new Database(getContext(),null,1);
        String respuesta="";
        SQLiteDatabase db = admin.getWritableDatabase();
        Cursor fila = db.rawQuery("SELECT * FROM almacenes",null);
        if(fila.moveToFirst())
        {
            do{

                    codigo.add(fila.getString(0));
                    codigo.add(fila.getString(0));
                    datos.add(fila.getString(0)+" - "+fila.getString(1));

            }while (fila.moveToNext());
            comboAdapterSql = new ArrayAdapter<>(getContext(), R.layout.textspinner_alm, datos);
            spinner.setAdapter(comboAdapterSql);
        }
        else
        {
            //Toast.makeText(getContext(), "No existe almacen", Toast.LENGTH_SHORT).show();
        }
        db.close();
        return comboAdapterSql;
    }
    public boolean revisarConfig()
    {
        boolean paramactivo=false;
        try {
            Database admin = new Database(getContext(), null,1);
            SQLiteDatabase db = admin.getWritableDatabase();
            Cursor fila = db.rawQuery("SELECT bus_alm_inv_fis FROM  configuracion where id=1",null);
            if(fila.moveToFirst())
            {
                if(fila.getString(0).equalsIgnoreCase("true"))
                {
                    paramactivo=true;
                }
                else
                {
                    paramactivo=false;
                }

                Log.i("config",""+fila.getString(0));

            }
            else
            {
                Log.i("config","no encontro nada");
            }
            db.close();
        }catch (SQLiteException sql){
            Toast.makeText(getContext(),"Error en consulta configuracion:"+sql.getMessage(),Toast.LENGTH_SHORT).show();
        }
        return paramactivo;
    }

    public interface BottomSheetListener {
        void onButtonClicked(String text);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (FragmentAlmacenModal.BottomSheetListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement BottomSheetListener");
        }
    }
}
