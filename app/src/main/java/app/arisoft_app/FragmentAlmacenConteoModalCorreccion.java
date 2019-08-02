package app.arisoft_app;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Vector;

import app.arisoft_app.Modelo.Existencia;
import app.arisoft_app.Tools.Database;

public class FragmentAlmacenConteoModalCorreccion extends BottomSheetDialogFragment{
    private FragmentAlmacenModal.BottomSheetListener mListener;
    Existencia articulo;
    String obtenido;
    Spinner spinner;
    ArrayAdapter<String> comboAdapterSql;
    Vector<String> codigo = new Vector<String>();
    Vector<String> datos = new Vector<String>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottom_sheet_almacen_c, container, false);
        spinner = v.findViewById(R.id.spinner_alm2);
        spinner.setAdapter(getAlmacenes());

        Button btn_consultar = v.findViewById(R.id.btn_consultar);
        btn_consultar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int size = spinner.getAdapter().getCount();
                if (size>0){
                    Intent intent = new Intent(getContext(),InventarioActivity.class);
                    int pos = spinner.getSelectedItemPosition();
                    intent.putExtra("idalmacen", codigo.get(pos).toString());
                    intent.putExtra("almacen", datos.get(pos).toString());
                    startActivity(intent);
                    dismiss();
                }else Toast.makeText(getContext(), "No se seleccionó almacén", Toast.LENGTH_SHORT).show();



            }
        });
        return v;
    }


    public void onStart(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    public ArrayAdapter<String> getAlmacenes (){
        Database admin = new Database(getContext(),null,1);
        String respuesta="";
        SQLiteDatabase db = admin.getWritableDatabase();
        Cursor fila = db.rawQuery("SELECT * FROM almacenes",null);
        if(fila.moveToFirst())
        {
            do{
                codigo.add(fila.getString(0));
                datos.add(fila.getString(0)+" - "+fila.getString(1));
            }while (fila.moveToNext());
            comboAdapterSql = new ArrayAdapter<>(getContext(), R.layout.textspinner_alm, datos);
            spinner.setAdapter(comboAdapterSql);
        }
        else
        {
            Toast.makeText(getContext(), "No existen artículos", Toast.LENGTH_SHORT).show();
        }
        db.close();
        return comboAdapterSql;
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
